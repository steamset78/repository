package server.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {
	// DB connection parameters

	private static final String URL = "jdbc:mysql://localhost:3306";
	private static final String DB = "cheap_trip";
	private static final String USER = "root";
	private static final String PASSWORD = "Njkcnjgepjd1";
	private static final boolean SSL = false;

	// -------------------------------------

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL+"/"+DB+(SSL ? "" : "?useSSL=false&serverTimezone = Israel"), USER, PASSWORD);
	}
}
