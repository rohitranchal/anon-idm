package org.ruchith.research.idm.idp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import org.ruchith.research.idm.IdentityClaimDefinition;

/**
 * Database connection and access helper methods.
 * 
 * @author Ruchith Fernando
 *
 */
public class Database {

	private static Database db;
	private static Connection con;
	private static final String DB_NAME = "idp";

	private Database(String host, String user, String password)
			throws Exception {
		String url = "jdbc:mysql://" + host + ":3306/" + DB_NAME;
		con = DriverManager.getConnection(url, user, password);
	}

	/**
	 * Return the singleton instance of the database.
	 * 
	 * @param host DB Host
	 * @param user DB Username
	 * @param password DB Password
	 * @return A {@link Database} instance
	 * @throws Exception
	 */
	public static Database getInstance(String host, String user, String password)
			throws Exception {
		if (db == null) {
			db = new Database(host, user, password);
		}
		return db;
	}

	/**
	 * Insert the given {@link IdentityClaimDefinition} instance into the 
	 * Claim_Definition table.
	 * @param claimDef {@link IdentityClaimDefinition} instance to insert.
	 * @throws Exception
	 */
	public void storeClaimDefinition(IdentityClaimDefinition claimDef)
			throws Exception {
		String sql = "INSERT INTO Claim_Definition VALUES('"
				+ claimDef.getName() + "','" + claimDef.getDescription()
				+ "','" + claimDef.getMasterKey().toString() + "','"
				+ claimDef.getParams().serializeJSON() + "','"
				+ claimDef.getB64Hash() + "','" + claimDef.getB64Sig() + "',"
				+ "NOW())";
		
		con.createStatement().execute(sql);
	}

	/**
	 * Return all claim definitions.
	 * 
	 * @return
	 */
	public ResultSet getAllClaimDefinitions() throws Exception {
		String sql = "SELECT * FROM Claim_Definition";
		return con.createStatement().executeQuery(sql);
	}
}
