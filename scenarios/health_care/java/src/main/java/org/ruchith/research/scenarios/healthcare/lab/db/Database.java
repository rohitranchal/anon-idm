package org.ruchith.research.scenarios.healthcare.lab.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
	private static Database db;
	private static Connection con;
	private static final String DB_NAME = "lab";
	
	private Database(String host, String user, String password) 
			throws Exception {
		String url = "jdbc:mysql://" + host + ":3306/" + DB_NAME;
		con = DriverManager.getConnection(url, user, password);
	}

	public static Database getInstance(String host, String user, String password)
			throws Exception {
		if (db == null) {
			db = new Database(host, user, password);
		}
		return db;
	}
	

	public void storeIdAndParams(String ownerParam, String readParam) 
			throws Exception {
		String sql = "INSERT INTO LabRecord(ParamOwner, ParamRead, Record) VALUES" +
					"('" + ownerParam + "','" + readParam + "','')";
		con.createStatement().execute(sql);
	}
	
	/*
	 * public void updateRecord
	 */
}
