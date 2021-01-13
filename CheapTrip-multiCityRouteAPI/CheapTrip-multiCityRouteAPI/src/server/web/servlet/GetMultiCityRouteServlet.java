package server.web.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
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
import java.util.stream.Collectors;

public class GetMultiCityRouteServlet extends HttpServlet{

    private static final Logger LOGGER = Logger.getLogger(GetMultiCityRouteServlet.class.getName());

    private static final String FROM_KEY = "from";
    private static final String TO_KEY = "to";
    private static final String CITIES_KEY = "cities";

    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        String from = request.getParameter(FROM_KEY);
        String to = request.getParameter(TO_KEY);
        String cities = request.getParameter(CITIES_KEY);

        ResponseFormat responseFormat = ResponseFormat.getFormat(request.getParameter("format"));

        Integer fromID = null, toID = null;

        String wrongKey = null;
        if(from != null) {
            try {
                fromID = Integer.parseInt(from);
            }catch (NumberFormatException e) {
                wrongKey = FROM_KEY;
            }
        }

        if(to != null) {
            try {
                toID = Integer.parseInt(to);
            }catch (NumberFormatException e) {
                wrongKey = TO_KEY;
            }
        }

        if(wrongKey != null){
            ResponseUtil.respondWithError(
                    response,
                    new RequestError(HttpServletResponse.SC_BAD_REQUEST, "Parameter \"" + wrongKey + "\" must be a location ID"),
                    responseFormat
            );
            return;
        }



        if(cities == null){
            ResponseUtil.respondWithError(
                    response,
                    new RequestError(HttpServletResponse.SC_BAD_REQUEST, "Missing \"cities\" parameter"),
                    responseFormat
            );
            return;
        }

        String[] citiesStrings = cities.split(",");
        List<Integer> citiesIds = new ArrayList<>();
        try {
            for (String city : citiesStrings) {
                citiesIds.add(Integer.parseInt(city));
            }
        }catch (NumberFormatException e){
            ResponseUtil.respondWithError(
                    response,
                    new RequestError(HttpServletResponse.SC_BAD_REQUEST, "cities parameter should only contain comma separated location IDs"),
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


        MultiRoutePath fixedRoutesPath = getRoutes(fromID, toID, citiesIds, DBUtils.FIXED_ROUTES_TABLE, conn);
        MultiRoutePath routesPath = getRoutes(fromID, toID, citiesIds, DBUtils.MIXED_ROUTES_TABLE, conn);
        MultiRoutePath flyingRoutesPath = getRoutes(fromID, toID, citiesIds, DBUtils.FLYING_ROUTES_TABLE, conn);

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


    private MultiRoutePath getRoutes(Integer from, Integer to, List<Integer> cities, String fromTable, Connection conn){
        String citiesList =cities.stream().map(Object::toString).collect(Collectors.joining(","));

        try {
            PreparedStatement statement = conn.prepareStatement("select * from " + fromTable + " where `from` in (" + citiesList + ") and `to` in (" + citiesList + ")");
            statement.execute();
            ResultSet routesResultSet = statement.getResultSet();

            SimpleDirectedWeightedGraph<Integer, DefaultEdge> routeGraph = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);

            for(Integer city : cities){
                routeGraph.addVertex(city);
            }

            while (routesResultSet.next()) {
                int fromID = routesResultSet.getInt("from");
                int toID = routesResultSet.getInt("to");
                float totalEuroPrice = routesResultSet.getFloat("euro_price");
                DefaultEdge e = routeGraph.addEdge(fromID, toID);
                routeGraph.setEdgeWeight(e, totalEuroPrice);
            }

            //TODO
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