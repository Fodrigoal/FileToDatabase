/**
 * Project: lab5
 * File: CompareByJoinedDate.java
 * Date: May 17, 2017
 * Time: 3:01:12 AM
 */


package a00979176.util;

import java.util.Comparator;

import a00979176.data.customer.Customer;

/**
 * @author Rodrigo Silva, A00979176
 *
 */
public class CompareByJoinedDate implements Comparator<Customer> {
	@Override
	public int compare(Customer customer1, Customer customer2) {
		return customer1.getJoinedDate().compareTo(customer2.getJoinedDate());
	}
}
