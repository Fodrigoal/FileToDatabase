/**
 * Project: lab5
 * File: CustomerReader.java
 * Date: May 17, 2017
 * Time: 2:51:34 AM
 */


package a00979176.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import a00979176.data.customer.CustomerDao;
import a00979176.ApplicationException;
import a00979176.data.customer.Customer;
import a00979176.util.Validator;

/**
 * Read the customer input.
 * 
 * @author Rodrigo Silva, A00979176
 *
 */
public class CustomerReader {

	@Deprecated
	public static final String RECORD_DELIMITER = ":";
	public static final String FIELD_DELIMITER = "\\|";

	/**
	 * private constructor to prevent instantiation
	 */
	private CustomerReader() {
	}

	/**
	 * Read the customer input data.
	 * 
	 * @param data
	 *            The input data.
	 * @return A list of customers.
	 * @throws ApplicationException
	 */
	public static void read(File customerDataFile, CustomerDao dao) throws ApplicationException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(customerDataFile));

			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				Customer customer = readCustomerString(line);
				try {
					dao.create(customer);
				} catch (SQLException e) {
					throw new ApplicationException(e);
				}
			}
		} catch (IOException e) {
			throw new ApplicationException(e.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				throw new ApplicationException(e.getMessage());
			}
		}
	}

	/**
	 * Parse a Customer data string into a CUstomer object;
	 * 
	 * @param row
	 * @throws ApplicationException
	 */
	private static Customer readCustomerString(String data) throws ApplicationException {
		String[] elements = data.split(FIELD_DELIMITER);
		if (elements.length != Customer.ATTRIBUTE_COUNT) {
			throw new ApplicationException(
					String.format("Expected %d but got %d: %s", Customer.ATTRIBUTE_COUNT, elements.length, Arrays.toString(elements)));
		}

		int index = 0;
		long id = Integer.parseInt(elements[index++]);
		String firstName = elements[index++];
		String lastName = elements[index++];
		String street = elements[index++];
		String city = elements[index++];
		String postalCode = elements[index++];
		String phone = elements[index++];
		String emailAddress = elements[index++];
		
		if (!Validator.validateEmail(emailAddress)) {
			throw new ApplicationException(String.format("Invalid email: %s", emailAddress));
		}
		String yyyymmdd = elements[index];
		
		if (!Validator.validateJoinedDate(yyyymmdd)) {
			throw new ApplicationException(String.format("Invalid joined date: %s for customer %d", yyyymmdd, id));
		}
		
		int year = Integer.parseInt(yyyymmdd.substring(0, 4));
		int month = Integer.parseInt(yyyymmdd.substring(4, 6)) - 1;
		int day = Integer.parseInt(yyyymmdd.substring(6, 8));

		return new Customer.Builder(id, phone).setFirstName(firstName).setLastName(lastName).setStreet(street).setCity(city).setPostalCode(postalCode)
				.setEmailAddress(emailAddress).setJoinedDate(year, month, day).build();
	}

}
