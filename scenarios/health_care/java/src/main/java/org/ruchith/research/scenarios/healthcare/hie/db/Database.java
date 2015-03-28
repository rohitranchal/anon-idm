package org.ruchith.research.scenarios.healthcare.hie.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
	private static Database db;
	private static Connection con;
	private static final String DB_NAME = "hie";
	
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
	
	/*
	public void addRequestPermission(String recordId, String name, int type) throws Exception {
		String sql = "INSERT INTO RequestPermission(RecordId, Name, Type) VALUES" +
				"('" + recordId + "','" + name + "','" + type + "')";
		con.createStatement().execute(sql);
	}
	*/
	public void insertRecordPair(String g, String ownerParam, String readParam, String Record)
			throws Exception {
		String sql = "INSERT INTO HieRecord(GParam, ParamOwner, ParamRead, Record) VALUES" +
				"('" + g + "','" + ownerParam + "','" + readParam + "','" + Record + "')";
		con.createStatement().execute(sql);
	}

}
