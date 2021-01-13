package server.web.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import server.model.DirectRoute;
import server.model.MultiRoutePath;
import server.util.DBUtils;
import server.web.model.RequestError;
import server.web.model.ResponseFormat;
import server.web.util.ResponseUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GetRouteServlet extends HttpServlet{

    private static final Logger LOGGER = Logger.getLogger(GetRouteServlet.class.getName());

    private static final String FROM_KEY = "from";
    private static final String TO_KEY = "to";

    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        String from = request.getParameter(FROM_KEY);
        String to = request.getParameter(TO_KEY);
        ResponseFormat responseFormat = ResponseFormat.getFormat(request.getParameter("format"));

        int fromID = 0, toID = 0;

        String missingKey = null;
        if(from == null)
            missingKey = FROM_KEY;
        else if(to == null)
            missingKey = TO_KEY;

        if(missingKey != null){
            ResponseUtil.respondWithError(
                    response,
                    new RequestError(HttpServletResponse.SC_BAD_REQUEST, "Missing \"" + missingKey + "\" parameter"),
                    responseFormat
            );
            return;
        }

        String wrongKey = null;
        try {
            fromID = Integer.parseInt(from);
        }catch (NumberFormatException e) {
            wrongKey = FROM_KEY;
        }

        try {
            toID = Integer.parseInt(to);
        }catch (NumberFormatException e) {
            wrongKey = TO_KEY;
        }

        if(wrongKey != null){
            ResponseUtil.respondWithError(
                    response,
                    new RequestError(HttpServletResponse.SC_BAD_REQUEST, "Parameter \"" + wrongKey + "\" must be a location ID"),
                    responseFormat
            );
            return;
        }

        Connection conn = DBUtils.getConnection();
        if(conn == null){
            ResponseUtil.respondWithError(
                    response,
                    new RequestError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
                    responseFormat
            );
            return;
        }


        MultiRoutePath fixedRoutesPath = getRoutes(fromID, toID, DBUtils.FIXED_ROUTES_TABLE, conn);
        MultiRoutePath routesPath = getRoutes(fromID, toID, DBUtils.MIXED_ROUTES_TABLE, conn);
        MultiRoutePath flyingRoutesPath = getRoutes(fromID, toID, DBUtils.FLYING_ROUTES_TABLE, conn);

        DBUtils.closeConnection(conn);

        PrintWriter out = ResponseUtil.getSuccessfulResponseWriter(response, responseFormat);

        if(out != null) {
            switch (responseFormat) {
                case HTML:
                    out.write("<html><body>");
                    out.write(getMultiRoutePathHTML(routesPath, "Mixed Routes"));
                    out.write(getMultiRoutePathHTML(flyingRoutesPath, "Flying Routes"));
                    out.write(getMultiRoutePathHTML(fixedRoutesPath, "Ground Routes"));
                    out.write("</body></html>");
                    break;
                case JSON:
                    Gson gson = ResponseUtil.getGson();
                    JsonObject JSONResponse = new JsonObject();
                    if (routesPath != null)
                        JSONResponse.add("mixed_routes", new Gson().toJsonTree(routesPath));
                    if (routesPath != null)
                        JSONResponse.add("flying_routes", new Gson().toJsonTree(flyingRoutesPath));
                    if (routesPath != null)
                        JSONResponse.add("ground_routes", new Gson().toJsonTree(fixedRoutesPath));
                    out.write(gson.toJson(JSONResponse));
                    break;
            }
        }
    }

    private String getMultiRoutePathHTML(MultiRoutePath path, String name){
        if(path != null) {
            return ("<h1>" + name + "</h1><br><div>Price: " +
                    path.getTotalEuroPrice() +
                    "€<br>Duration: " +
                    getDurationString(path.getTotalTimeInMinutes()) +
                    "<br>" +
                    getDirectRoutesHTML(path.getDirectRoutes()) +
                    "</div>");
        }
        else
            return ("Failed to get " + name);
    }

    private String getDirectRoutesHTML(List<DirectRoute> directRoutes){
        StringBuilder builder = new StringBuilder();
        for (DirectRoute directRoute : directRoutes) {
            builder.append("From: ")
                    .append(directRoute.getFromName())
                    .append(", To: ")
                    .append(directRoute.getToName())
                    .append(", Price: ").append(directRoute.getEuroPrice())
                    .append("€ , Duration: ")
                    .append(getDurationString(directRoute.getTimeInMinutes()))
                    .append(", Transport Type: ")
                    .append(directRoute.getTransportationTypeName())
                    .append("<br>");
        }
        return builder.toString();
    }


    private MultiRoutePath getRoutes(int from, int to, String fromTable, Connection conn){
        try {
            PreparedStatement statement = conn.prepareStatement("select * from " + fromTable + " where `from` = " + from + " and `to` = " + to);
            statement.execute();
            ResultSet routesResultSet = statement.getResultSet();
            //noinspection LoopStatementThatDoesntLoop
            while (routesResultSet.next()) {
                String travelData = routesResultSet.getString("travel_data");
                float totalEuroPrice = routesResultSet.getFloat("euro_price");

                String query = "select td.id, line, time_in_minutes ,euro_price, transportation_type, " +
                        "       `from` as fromID ," +
                        "       (select(name) from locations l where l.id = td.from) as 'from'," +
                        "       `to` as toID," +
                        "       (select(name) from locations l where l.id = td.to) as 'to'," +
                        "       (select(name) from transportation_types tt where tt.id = td.transportation_type) as transportation_type_name from travel_data td" +
                        "       where td.id in (" + travelData + ") order by field(id, " + travelData + ");";

                statement = conn.prepareStatement(query);
                statement.execute();
                ResultSet directRoutesResultSet = statement.getResultSet();

                ArrayList<DirectRoute> directRoutes = new ArrayList<>();
                int totalTimeInMinutes = 0;
                while (directRoutesResultSet.next()) {
                    int id = directRoutesResultSet.getInt("id");
                    int fromID = directRoutesResultSet.getInt("fromID");
                    int toID = directRoutesResultSet.getInt("toID");
                    String fromName = directRoutesResultSet.getString("from");
                    String toName = directRoutesResultSet.getString("to");
                    float euroPrice = directRoutesResultSet.getFloat("euro_price");
                    int timeInMinutes = directRoutesResultSet.getInt("time_in_minutes");
                    int transportationType = directRoutesResultSet.getInt("transportation_type");
                    String transportationTypeName = directRoutesResultSet.getString("transportation_type_name");

                    DirectRoute directRoute = new DirectRoute(id, fromID, toID, euroPrice);
                    directRoute.setFromName(fromName);
                    directRoute.setToName(toName);
                    directRoute.setTimeInMinutes(timeInMinutes);
                    directRoute.setTransportationType(transportationType);
                    directRoute.setTransportationTypeName(transportationTypeName);
                    directRoutes.add(directRoute);
                    totalTimeInMinutes += timeInMinutes;
                }

                return new MultiRoutePath(directRoutes, totalEuroPrice, totalTimeInMinutes);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        return null;
    }

    private String getDurationString(int durationMinutes){
        int minutes = durationMinutes % 60;
        int hours = (durationMinutes / 60) % 24;
        int days = (durationMinutes / (60*24));


        if(days > 0) {
            if(hours > 0) {
                if(minutes > 0)
                    return String.format("%dd %dh %dm", days, hours, minutes);
                else
                    return String.format("%dd %dh", days, hours);
            }
            else if(minutes > 0)
                return String.format("%dd %dm", days, minutes);
            else
                return String.format("%dd", days);
        }
        else if(hours > 0) {
            if(minutes > 0)
                return String.format("%dh %dm", hours, minutes);
            else
                return String.format("%dh", hours);
        }
        else
            return String.format("%dm", minutes);

    }
}