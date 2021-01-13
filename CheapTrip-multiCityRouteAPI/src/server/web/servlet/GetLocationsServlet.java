package server.web.servlet;

import com.google.gson.Gson;
import server.model.Location;
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

public class GetLocationsServlet extends HttpServlet{

    private static final Logger LOGGER = Logger.getLogger(GetLocationsServlet.class.getName());

    private static final String TYPE_KEY = "type";
    private static final String SEARCH_NAME_KEY = "search_name";
    private static final String LIMIT_KEY = "limit";

    private static final int LIMIT_DEFAULT = 10;

    enum LocationsType {
        TYPE_ALL, TYPE_FROM, TYPE_TO;

        public static LocationsType getLocationType(Integer type){
            if(type == null)
                return TYPE_ALL;
            switch (type){
                case 1:
                    return TYPE_FROM;
                case 2:
                    return TYPE_TO;
                default:
                    return TYPE_ALL;
            }
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        ResponseFormat responseFormat = ResponseFormat.JSON;

        LocationsType type = LocationsType.TYPE_ALL;
        try{
            String typeParam = request.getParameter(TYPE_KEY);
            if(typeParam != null)
                type = LocationsType.getLocationType(Integer.valueOf(request.getParameter(TYPE_KEY)));
        }catch (NumberFormatException e){
            ResponseUtil.respondWithError(
                    response,
                    new RequestError(HttpServletResponse.SC_BAD_REQUEST, "type must be 0 for all, 1 for from, 2 for to"),
                    responseFormat
            );
            return;
        }

        String searchName = request.getParameter(SEARCH_NAME_KEY);

        int limit = LIMIT_DEFAULT;
        try {
            String limitParam = request.getParameter(LIMIT_KEY);
            if(limitParam != null)
                limit = Integer.parseInt(limitParam);
        }catch (NumberFormatException e){
            ResponseUtil.respondWithError(
                    response,
                    new RequestError(HttpServletResponse.SC_BAD_REQUEST, "Limit parameter must be a number"),
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


        List<Location> locations = getLocations(type, searchName, limit, conn);
        if(locations == null){
            ResponseUtil.respondWithError(
                    response,
                    new RequestError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
                    responseFormat
            );
        }
        else{
            PrintWriter out = ResponseUtil.getSuccessfulResponseWriter(response, responseFormat);

            if(out != null) {
                Gson gson = ResponseUtil.getGson();
                out.write(gson.toJson(locations));
            }
        }

        DBUtils.closeConnection(conn);
    }


    private List<Location> getLocations(LocationsType type, String searchName, int limit, Connection conn){
        try {
            String query = "SELECT * FROM " + DBUtils.LOCATIONS_TABLE + " WHERE 1 ";

            switch (type){
                case TYPE_TO:
                    query += " AND id IN (SELECT `to` FROM " + DBUtils.MIXED_ROUTES_TABLE + ")";
                    break;
                case TYPE_FROM:
                    query += " AND id IN (SELECT `from` FROM " + DBUtils.MIXED_ROUTES_TABLE + ")";
                    break;
            }

            if(searchName != null)
                query += " AND `name` LIKE '%" + searchName + "%' ORDER BY CASE WHEN `name` LIKE '" + searchName + "%' THEN 0 ELSE 1 END ";

            query += " LIMIT " + limit;

            PreparedStatement statement = conn.prepareStatement(query);
            statement.execute();
            ResultSet locationsResultSet = statement.getResultSet();
            List<Location> locations = new ArrayList<>();
            while (locationsResultSet.next()) {
                int id = locationsResultSet.getInt("id");
                String name = locationsResultSet.getString("name");
                locations.add(new Location(id, name));
            }

            return locations;
        }catch (SQLException e){
            e.printStackTrace();
        }

        return null;
    }
}