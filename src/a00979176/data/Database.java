/**
 * Project: FileToDatabase
 * File: Database.java
 * Date: May 31, 2017
 * Time: 3:33:36 AM
 */

package a00979176.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Connect and manage database.
 * 
 * @author Rodrigo Silva, A00979176
 *
 */
public class Database {
	
	public static final String DB_DRIVER_KEY = "db.driver";
	public static final String DB_URL_KEY = "db.url";
	public static final String DB_USER_KEY = "db.user";
	public static final String DB_PASSWORD_KEY = "db.password";
	
	private static final Logger LOG = LogManager.getLogger();

	private static Properties properties;
	private static Connection connection;

	public Database(Properties properties) throws FileNotFoundException, IOException {
		LOG.debug("Loading database properties from dbConfig.properties");
		Database.properties = properties;
	}
	
	public static Connection getConnection() throws SQLException {
		if (connection != null) {
			return connection;
		} try {
			connect();
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
		return connection;
	}
	
	private static void connect() throws ClassNotFoundException, SQLException {
		String dbDriver = properties.getProperty(DB_DRIVER_KEY);
		LOG.debug(dbDriver);
		Class.forName(dbDriver);
		System.out.println("Loaded the driver.");
		connection = DriverManager.getConnection(properties.getProperty(DB_URL_KEY),
				properties.getProperty(DB_USER_KEY), properties.getProperty(DB_PASSWORD_KEY));
		LOG.debug("Database connected");
	}
	
	public static boolean tableExists(String targetTableName) throws SQLException {
		DatabaseMetaData databaseMetaData = getConnection().getMetaData();
		ResultSet resultSet = null;
		String table = null;

		try {
			resultSet = databaseMetaData.getTables(connection.getCatalog(), "%", "%", null);
			while (resultSet.next()) {
				table = resultSet.getString("TABLE_NAME");
				if (table.equalsIgnoreCase(targetTableName)) {
					LOG.debug("Found the target table named: " + targetTableName);
					return true;
				}
			}
		} finally {
			resultSet.close();
		}
		return false;
	}
	
	public void shutdown() {
		LOG.info("Shutting down");
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				LOG.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
