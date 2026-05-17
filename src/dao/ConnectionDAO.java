package dao;

import java.sql.Connection;

/**
 * Data Access Object providing a shared database connection to an Oracle
 * database.
 * <p>
 * This class manages a single static {@code Connection} instance used
 * throughout the application. It loads the Oracle JDBC driver and provides
 * a connection to the database specified by the configured URL, LOGIN, and
 * PASS constants.
 * </p>
 *
 * <p>The connection is reused across DAO calls to avoid repeatedly opening
 * and closing database connections. All other DAO classes in this package
 * obtain their connections through {@link #getConnection()}.</p>
 *
 * @author ESIGELEC - TIC Department
 * @version 2.0
 */
public class ConnectionDAO {
	/**
	 * Oracle database connection parameters.
	 * <p>
	 * {@code URL} specifies the JDBC connection string to the Oracle database.
	 * {@code LOGIN} and {@code PASS} are the database credentials.
	 * These values are constants configured at build time.
	 * </p>
	 *
	 * <p>Two URL variants are provided:
	 * <ul>
	 *   <li>The first is for personal machines connecting to
	 *       {@code oracle.esigelec.fr:1521:orcl}.</li>
	 *   <li>The second (commented out) is for the school network using
	 *       the internal Oracle server via TNS alias.</li>
	 * </ul>
	 * </p>
	 */
	// � utiliser si vous �tes sur une machine personnelle :
	final static String URL   = "jdbc:oracle:thin:@oracle.esigelec.fr:1521:orcl";
	
	// � utiliser si vous �tes sur une machine de l'�cole :
	// final static String URL   = "jdbc:oracle:thin:@//srvoracledb.intranet.int:1521/orcl.intranet.int";

	final static String LOGIN = "C##BDD7_16";   // remplacer les ********. Exemple C##BDD1_1
	final static String PASS  = "BDD716";   // remplacer les ********. Exemple BDD11
	
	private static Connection instanceConn = null;
	/**
	 * Constructs a new ConnectionDAO and loads the Oracle JDBC driver.
	 * <p>
	 * The driver class ({@code oracle.jdbc.OracleDriver}) is loaded via
	 * {@link Class#forName(String)}. If the driver is not found on the
	 * classpath, an error message is printed to stderr.
	 * </p>
	 */
	public ConnectionDAO() {
		// chargement du pilote de bases de donnees
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.err.println("Impossible de charger le pilote de BDD, ne pas oublier d'importer le fichier .jar dans le projet");
		}
	}

	/**
	 * Returns the singleton database connection.
	 * <p>
	 * If a connection already exists and is still open, it is returned
	 * directly. Otherwise a new connection is created using the configured
	 * URL, LOGIN, and PASS constants. The connection is then stored as a
	 * static reference for subsequent calls.
	 * </p>
	 *
	 * @return a {@code Connection} to the Oracle database, or {@code null}
	 *         if the driver could not be loaded or the connection attempt
	 *         failed with an {@code SQLException}
	 */
	public static Connection getConnection() {
		try {
			if (instanceConn != null && !instanceConn.isClosed()) {
				return instanceConn;
			}
			if (instanceConn == null) {
				try {
					Class.forName("oracle.jdbc.OracleDriver");
				} catch (ClassNotFoundException e) {
				}
			}
			instanceConn = java.sql.DriverManager.getConnection(URL, LOGIN, PASS);
		} catch (java.sql.SQLException e) {
			System.err.println("Erreur de connexion a la base de donnees : " + e.getMessage());
		}
		return instanceConn;
	}
}