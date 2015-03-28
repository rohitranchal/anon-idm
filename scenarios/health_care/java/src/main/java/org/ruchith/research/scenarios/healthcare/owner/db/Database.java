package org.ruchith.research.scenarios.healthcare.owner.db;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 
 * @author Byungchan An
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

	public static Database getInstance(String host, String user, String password)
			throws Exception {
		if (db == null) {
			db = new Database(host, user, password);
		}
		return db;
	}
	
	public void addRequestPermission(String recordId, String name, int type) throws Exception {
		String sql = "INSERT INTO RequestPermission(RecordId, Name, Type) VALUES" +
				"('" + recordId + "','" + name + "','" + type + "')";
		con.createStatement().execute(sql);
	}
	
	public void updateRequestRegisterationStatus(String recordId, String name, boolean registered) throws Exception {
		String sql = "UPDATE RequestPermission SET Registered=" + registered + " WHERE RecordId='" + recordId +"'";
		con.createStatement().execute(sql);
	}
	
	public void groupRecordIdAndParams(String recordId, String ownerName, String readName) throws Exception {
		String sql = "INSERT INTO RecordPair(RecordId, OwnerName, ReadName) VALUES" +
				"('" + recordId + "','" + ownerName + "','" + readName + "')";
		con.createStatement().execute(sql);
	}
}