/**
 * Project: lab5
 * File: CustomerReport.java
 * Date: May 17, 2017
 * Time: 2:56:32 AM
 */

package a00979176.io;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

import a00979176.data.customer.Customer;
import a00979176.util.Common;

/**
 * Prints a formated Customers report.
 * 
 * @author Rodrigo Silva, A00979176
 *
 */
public class CustomerReport {

	public static final String HORIZONTAL_LINE = "----------------------------------------------------------------------------------------------------------------------------------------------";
	public static final String HEADER_FORMAT = "%3s. %-6s %-12s %-12s %-25s %-12s %-12s %-15s %-25s%s";
	public static final String CUSTOMER_FORMAT = "%3d. %06d %-12s %-12s %-25s %-12s %-12s %-15s %-25s%s";

	/**
	 * private constructor to prevent instantiation
	 */
	private CustomerReport() {
	}

	/**
	 * Print the report.
	 * 
	 * @param customers
	 */
	public static void write(List<Customer> customers, PrintStream out) {
		out.println("Customers Report");
		out.println(HORIZONTAL_LINE);
		String text = String.format(HEADER_FORMAT, "#", "ID", "First name", "Last name", "Street", "City", "Postal Code", "Phone", "Email", "Join Date");
		out.println(text);
		out.println(HORIZONTAL_LINE);

		int i = 0;
		
		for (Customer customer : customers) {
			LocalDate date = customer.getJoinedDate();
			text = String.format(CUSTOMER_FORMAT, ++i, customer.getId(), customer.getFirstName(), customer.getLastName(), customer.getStreet(),
					customer.getCity(), customer.getPostalCode(), customer.getPhone(), customer.getEmailAddress(), Common.DATE_FORMAT.format(date));
			out.println(text);
		}
	}

}