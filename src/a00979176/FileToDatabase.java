/**
 * Project: FileToDatabase
 * File: FileToDatabase.java
 * Date: May 31, 2017
 * Time: 2:31:16 AM
 */

package a00979176;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import a00979176.ApplicationException;
import a00979176.data.customer.Customer;
import a00979176.data.customer.CustomerDao;
import a00979176.data.customer.CustomerDaoTester;
import a00979176.data.Database;
import a00979176.io.CustomerReport;

/**
 * To demonstrate knowledge of JDBC
 * 
 * @author Rodrigo Silva, A00979176
 *
 */
public class FileToDatabase {
	private List<Customer> customers;
	private static boolean dropRequest;
	private static File customerDataFile;
	private static File configFile;
	private static FileInputStream fis;
	private static CustomerDao customerDao;
	private static Database db;
	private static final Instant startTime = Instant.now();
	private static final String DATA_FILE = "customers.txt";
	private static final String LOG4J_CONFIG_FILENAME = "log4j2.xml";
	private static final String DB_PROPERTIES_FILENAME = "dbConfig.properties";
	private static final String DROP = "-drop";


	static {
		configureLogging();
	}

	private static final Logger LOG = LogManager.getLogger();


	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws ApplicationException 
	 */
	public static void main(String[] args) throws SQLException, IOException, ApplicationException {
		Instant startTime = Instant.now();
		LOG.info(startTime);
		
		if (args.length == 1 && args[0].equals(DROP)) {
			dropRequest = true;
		}

		File file = new File(DATA_FILE);

		if (file.exists()) {
			new FileToDatabase(file).run();
			Instant endTime = Instant.now();
			LOG.info(endTime);
			LOG.info(String.format("Duration: %d ms", Duration.between(startTime, endTime).toMillis()));
		}
		else {
			System.out.format("Data file not found. Be sure that customer.txt exists.", DATA_FILE);
			System.exit(-1);
		}
	}
	
	/**
	 * FileToDatabase constructor.
	 */
	public FileToDatabase(File customerDataFile) {
		LOG.debug("FileToDatabase()");
		FileToDatabase.customerDataFile = customerDataFile;
	}
	
	private static void configureLogging() {
		ConfigurationSource source;
		try {
			source = new ConfigurationSource(new FileInputStream(LOG4J_CONFIG_FILENAME));
			Configurator.initialize(null, source);

		} catch (IOException e) {
			System.out.println(String.format("Can't find the log4j logging configuration file %s.", LOG4J_CONFIG_FILENAME));
		}
	}
	
	@Deprecated
	public static void printEndTimeAndDuration() {
		Instant endTime = Instant.now();
		System.out.println(endTime);

		// print the duration
		System.out.println(String.format("Duration: %d ms", Duration.between(startTime, endTime).toMillis()));
	}

	/**
	 * Populate the customers and print them out.
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void run() throws IOException, SQLException {
		LOG.debug("run()");
		try {
			loadCustomers();
			CustomerDaoTester tester = new CustomerDaoTester(customerDao);
			tester.test();
		} catch (ApplicationException e) {
			LOG.error(e.getMessage());
		}
	}
	
	private void loadCustomers() throws ApplicationException, IOException, SQLException {
		LOG.debug(String.format("Reading the Customers Report from '%s'", customerDataFile));
		customerDataFile = new File(DATA_FILE);
		configFile = new File(DB_PROPERTIES_FILENAME);
		fis = new FileInputStream(configFile);
		Properties propConfig = new Properties();
		propConfig.load(fis);
		db = new Database(propConfig);
		customerDao = new CustomerDao(db, customerDataFile);
		try{
			if (dropRequest == true || Database.tableExists(CustomerDao.TABLE)) {
				customerDao.drop();
			}
				customerDao.createTable(new File(DATA_FILE));
				LOG.debug("Customers inserted");	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void displayCustomers() {
		File customersFile = new File("customers_report.txt");
		LOG.debug(String.format("Writing the Customers Report to '%s'", customersFile));
		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream(customersFile));
			CustomerReport.write(customers, out);
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage());
		}
	}	
}
