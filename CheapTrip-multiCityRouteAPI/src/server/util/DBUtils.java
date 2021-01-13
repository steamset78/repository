package server.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBUtils {
    private static final String DB_NAME = "cheap_trip";

    public static final String FIXED_ROUTES_TABLE = "fixed_routes";
    public static final String MIXED_ROUTES_TABLE = "routes";
    public static final String FLYING_ROUTES_TABLE = "flying_routes";
    public static final String LOCATIONS_TABLE = "locations";
    public static final String TRAVEL_DATA_TABLE = "travel_data";


    public static Connection getConnection(){
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/" + DB_NAME);
            return ds.getConnection();
        } catch (NamingException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection(Connection conn){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
