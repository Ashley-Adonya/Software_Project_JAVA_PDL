package dao;

import java.sql.Connection;

/**
 * Classe d'acces a la base de donnees
 * 
 * @author ESIGELEC - TIC Department
 * @version 2.0
 * */
public class ConnectionDAO {
	/**
	 * Parametres de connexion a la base de donnees oracle
	 * URL, LOGIN et PASS sont des constantes
	 */
	// � utiliser si vous �tes sur une machine personnelle :
	final static String URL   = "jdbc:oracle:thin:@oracle.esigelec.fr:1521:orcl";
	
	// � utiliser si vous �tes sur une machine de l'�cole :
	// final static String URL   = "jdbc:oracle:thin:@//srvoracledb.intranet.int:1521/orcl.intranet.int";

	final static String LOGIN = "C##BDD7_16";   // remplacer les ********. Exemple C##BDD1_1
	final static String PASS  = "BDD716";   // remplacer les ********. Exemple BDD11
	
	private static Connection instanceConn = null;
	/**
	 * Constructor
	 * 
	 */
	public ConnectionDAO() {
		// chargement du pilote de bases de donnees
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.err.println("Impossible de charger le pilote de BDD, ne pas oublier d'importer le fichier .jar dans le projet");
		}
	}

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