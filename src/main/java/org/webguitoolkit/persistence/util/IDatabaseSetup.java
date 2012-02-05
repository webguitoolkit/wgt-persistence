package org.webguitoolkit.persistence.util;

/**
 * The lifecycle of the database setup is as follows:
 * <ol>
 *   <li>connectToDB (provided by a IDatabaseConnector)</li>
 *   <li>createDB</li>
 *   <li>insertInitialData</li>
 *   <li>insertTestData</li>
 *   <li>disconnectFromDB (provided by a IDatabaseConnector)</li>
 * </ol>
 * 
 * A calling application may choose to omit calling a specific method (e.g. if no test data should be generated)
 * or a implementing class may provide a void operation (e.g. if no special handling for connecting to the
 * DB is required).
 * 
 * In general, the database setup should be database independant. Establishing a connection to a database
 * may be database-specific and should be provided by a IDatabaseConnector.
 * 
 * @author Wolfram Kaiser
 */
public interface IDatabaseSetup {
	
	public void createDB() throws DatabaseSetupException;
	public void insertInitialData() throws DatabaseSetupException;
	public void insertTestData() throws DatabaseSetupException;

}
