/**
 * Project: FileToDatabase
 * File: CustomerDao.java
 * Date: May 31, 2017
 * Time: 4:22:16 AM
 */

package a00979176.data.customer;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import a00979176.ApplicationException;
import a00979176.data.customer.Customer;
import a00979176.data.Database;
import a00979176.io.CustomerReader;
import a00979176.dao.Dao;

/**
 * Creates a Customer Data Access Object.
 * 
 * @author Rodrigo Silva, A00979176
 *
 */
public class CustomerDao extends Dao {
	
	public static final String TABLE = "Customers";

	private static Logger LOG = LogManager.getLogger();

	public CustomerDao(Database database, File customersFile) throws ApplicationException, SQLException {
		super(database, TABLE);
	}
	
	public enum Fields {
		ID("id", 16), 
		FIRST_NAME("firstName", 25), 
		LAST_NAME("lastName", 25),
		STREET("street", 50), 
		CITY("city", 25), 
		POSTAL_CODE("postalCode", 8),
		PHONE("phone", 20), 
		EMAIL_ADDRESS("emailAddress", 50), 
		JOINED_DATE("joinedDate", 8); 

		String column;
		int length;

		private Fields(String column, int length) {
			this.column = column;
			this.length = length;
		}
		
		public String getColumn() {
			return column;
		}

	}
	
	@Override
	public void createTable(File customersDataFile) throws ApplicationException, SQLException {
		LOG.debug("Creating database table " + TABLE);

		try {
			if (Database.tableExists(TABLE)) {
				drop();
			} 
				LOG.debug("Inserting the customers");
				
				String sql = String.format(
						"CREATE TABLE %s(%s INTEGER, %s VARCHAR(%d), %s VARCHAR(%d), %s VARCHAR(%d), %s VARCHAR(%d), %s VARCHAR(%d), %s VARCHAR(%d), "
								+ "%s VARCHAR(%d), %s TIMESTAMP, PRIMARY KEY (%s))",
						TABLE, Fields.ID.column, 
						Fields.FIRST_NAME.column, Fields.FIRST_NAME.length, 
						Fields.LAST_NAME.column, Fields.LAST_NAME.length, 
						Fields.STREET.column, Fields.STREET.length, 
						Fields.CITY.column, Fields.CITY.length, 
						Fields.POSTAL_CODE.column, Fields.POSTAL_CODE.length, 
						Fields.PHONE.column, Fields.PHONE.length, 
						Fields.EMAIL_ADDRESS.column, Fields.EMAIL_ADDRESS.length, 
						Fields.JOINED_DATE.column, 
						Fields.ID.column);
				LOG.debug("Create statement: " + sql);
				super.create(sql);
			
			if (!customersDataFile.exists()) {
				throw new ApplicationException(String.format("Missing '%s'", customersDataFile));
			}
			CustomerReader.read(customersDataFile, this);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ApplicationException(e);
		}
	}
	
	public void create(Customer customer) throws SQLException {
		String sql = String.format("INSERT INTO %s values(?, ?, ?, ?, ?, ?, ?, ?, ?)", TABLE);
		doTheStatement(sql, 
				customer.getId(), 
				customer.getFirstName(), 
				customer.getLastName(), 
				customer.getStreet(), 
				customer.getCity(), 
				customer.getPostalCode(), 
				customer.getPhone(),
				customer.getEmailAddress(), 
				toTimestamp(customer.getJoinedDate()));
		LOG.debug(String.format("%s %s added to the database", customer.getFirstName(), customer.getLastName()));
	}
	
	public Customer read(Long customerId) throws SQLException, Exception {
		Connection connection;
		Statement statement = null;
		Customer customer = null;
		try {
			connection = Database.getConnection();
			statement = connection.createStatement();
			String sql = String.format("SELECT * FROM %s WHERE %s = %s", tableName, Fields.ID.getColumn(), customerId);
			LOG.debug(sql);
			ResultSet resultSet = statement.executeQuery(sql);

			int count = 0;
			while (resultSet.next()) {
				count++;
				if (count > 1) {
					throw new Exception(String.format("Expected one result, got %d", count));
				}
				
				Timestamp timestamp = resultSet.getTimestamp(Fields.JOINED_DATE.column);
				LocalDate date = timestamp.toLocalDateTime().toLocalDate();
				
				customer = new Customer.Builder(resultSet.getLong(Fields.ID.getColumn()), 
						resultSet.getString(Fields.PHONE.getColumn()))
						.setFirstName(resultSet.getString(Fields.FIRST_NAME.getColumn()))
						.setLastName(resultSet.getString(Fields.LAST_NAME.getColumn()))
						.setStreet(resultSet.getString(Fields.STREET.getColumn()))
						.setCity(resultSet.getString(Fields.CITY.getColumn()))
						.setPostalCode(resultSet.getString(Fields.POSTAL_CODE.getColumn()))
						.setEmailAddress(resultSet.getString(Fields.EMAIL_ADDRESS.getColumn()))
						.setJoinedDate(date).build();
				LOG.debug("Read statement: " + sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
		return customer;
	}
	
	public void update(Customer customer) throws SQLException {
		try {
			String sql = String.format("UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, WHERE %s=?",
				TABLE, 
				Fields.FIRST_NAME.column, 
				Fields.LAST_NAME.column, 
				Fields.STREET.column, 
				Fields.CITY.column, 
				Fields.POSTAL_CODE.column, 
				Fields.PHONE.column, 
				Fields.EMAIL_ADDRESS.column, 
				Fields.JOINED_DATE.column, 
				Fields.ID.column);
		LOG.debug("Update statement: " + sql);
		doTheStatement(sql, customer.getId(), sql, customer.getFirstName(),
				customer.getLastName(), customer.getStreet(), customer.getCity(), customer.getPostalCode(),
				customer.getPhone(), customer.getEmailAddress(), toTimestamp(customer.getJoinedDate()));
		LOG.debug(String.format("Updated %s", customer));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void delete(Customer customer) throws SQLException {
		Connection connection;
		Statement statement = null;
		try {
			connection = Database.getConnection();
			statement = connection.createStatement();
			String sql = String.format("DELETE FROM %s WHERE %s='%s'", TABLE, Fields.ID.column,
					customer.getId());
			LOG.debug("Delete statement: " + sql);
			int rowcount = statement.executeUpdate(sql);
			LOG.debug(String.format("Deleted %d rows", rowcount));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public List<Long> getIds() throws SQLException {
		List<Long> ids = new ArrayList<>();
		String sql = String.format("SELECT %s FROM %s", Fields.ID.column, TABLE);
		LOG.debug(sql);
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			Connection connection = Database.getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			
			while (resultSet.next()) {
				ids.add(resultSet.getLong(Fields.ID.column));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
		LOG.debug(String.format("Loaded %d customers IDs from the database", ids.size()));
		return ids;
	}
	
	public Customer getCustomer(Long id) throws SQLException, Exception {
		return read(id);
	}
	
	public int countAll() throws Exception {
		Statement statement = null;
		int count = 0;
		try {
			Connection connection = Database.getConnection();
			statement = connection.createStatement();
			String sqlString = String.format("SELECT COUNT(*) AS total FROM %s", TABLE);
			ResultSet resultSet = statement.executeQuery(sqlString);
			if (resultSet.next()) {
				count = resultSet.getInt("total");
			}
		} finally {
			close(statement);
		}
		return count;
	}

}

