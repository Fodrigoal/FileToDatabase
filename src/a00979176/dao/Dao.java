/**
 * Project: FileToDatabase
 * File: Dao.java
 * Date: May 31, 2017
 * Time: 5:21:36 AM
 */

package a00979176.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import a00979176.ApplicationException;
import a00979176.data.Database;

/**
 * Creates a Data Access Object.
 * 
 * @author Rodrigo Silva, A00979176
 *
 */
public abstract class Dao {
	private static Logger LOG = LogManager.getLogger();
	protected final Database database;
	protected String tableName;

	protected Dao(Database database, String tableName) {
		this.database = database;
		this.tableName = tableName;
	}

	public abstract void createTable(File customerDataFile) throws ApplicationException, SQLException;
	
	protected void create(String createStatement) throws SQLException {
		LOG.debug(createStatement);
		Statement statement = null;
		try {
			Connection connection = Database.getConnection();
			statement = connection.createStatement();
			statement.executeUpdate(createStatement);
		} finally {
			close(statement);
		}
	}

	protected boolean doTheStatement(String statement, Object... args) throws SQLException {
		LOG.debug(statement);
		boolean result;
		PreparedStatement preparedStatement = null;
		try {
			Connection connection = Database.getConnection();
			preparedStatement = connection.prepareStatement(statement);
			int i = 1;
			for (Object obj : args) {
				if (obj instanceof String) {
					preparedStatement.setString(i, obj.toString());
				} else if (obj instanceof Integer) {
					preparedStatement.setInt(i, (Integer) obj);
				} else if (obj instanceof Timestamp) {
					preparedStatement.setTimestamp(i, (Timestamp) obj);
				} else if (obj instanceof LocalDateTime) {
					preparedStatement.setTimestamp(i, Timestamp.valueOf((LocalDateTime) obj));
				} else {
					preparedStatement.setString(i, obj.toString());
				}
				i++;
			}
			result = preparedStatement.execute();
		} finally {
			close(preparedStatement);
		}

		return result;
	}
	
	public void drop() throws SQLException {
		Statement statement = null;
		try {
			Connection connection = Database.getConnection();
			statement = connection.createStatement();
			if (Database.tableExists(tableName)) {
				LOG.debug("Table " + tableName + " was droped");
				statement.executeUpdate("Drop table " + tableName);
			}
		} finally {
			close(statement);
		}
	}

	public void shutdown() {
		database.shutdown();
		LOG.debug("Database shutdown");
	}

	protected static void close(Statement statement) {
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (SQLException e) {
			LOG.error("Failed to close statement" + e);
		}
	}

	public static Timestamp toTimestamp(LocalDate date) {
		return Timestamp.valueOf(LocalDateTime.of(date, LocalTime.now()));
	}

}

