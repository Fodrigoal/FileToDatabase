package a00979176.data.customer;

/**
 * Project: FileToDatabase
 * File: CustomerDaoTester.java
 * Date: May 31, 2017
 * Time: 4:34:46 AM
 */

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import a00979176.data.customer.Customer;
import a00979176.data.customer.CustomerDao;

/**
 * Creates a Customer Data Access Object tester.
 * 
 * @author Rodrigo Silva, A00979176
 *
 */
public class CustomerDaoTester {
	private static Logger LOG = LogManager.getLogger();
	private CustomerDao customerDao;

	public CustomerDaoTester(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	public void test() {
		try {
			LOG.info("Getting the IDs");
			List<Long> ids = customerDao.getIds();
			LOG.info("Customer IDs: " + Arrays.toString(ids.toArray()));
			for (Long id : ids) {
				LOG.info(id);
				Customer customer = customerDao.getCustomer(id);
				LOG.info(customer);
			}
			long count = customerDao.countAll();
			LOG.info(count);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

	}
}
