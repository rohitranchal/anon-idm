package org.ruchith.research.scenarios.healthcare.owner;

import org.ruchith.research.idm.idp.Configuration;
import org.ruchith.research.idm.idp.IdentityManager;
import org.ruchith.research.scenarios.healthcare.owner.db.Database;

public class Patient extends IdentityManager {
	// 01. As a identity manager, create claim definition
	// 02. After receiving (req) value from the data consumer, 
	// issue claim based on the claim definition
	// 
	
	// one claim definition?
	private Configuration config;
	private Database db;
	
	public Patient(String configPath) throws Exception {
		super(configPath); // Parent's constructor
		this.config = Configuration.getInstance(configPath);
		this.db = Database.getInstance(this.config.getDbHost(), this.config.getDbUser(), this.config.getDbPassword());
	}
	
	public void addRequestPermissionQueue(String recordId, String name, int type, String srcUrl) 
		throws Exception {
		db.addRequestPermission(recordId, name, type, srcUrl);
	}
	
	public void updateRegisteration(String recordId, String name, boolean registered)
		throws Exception {
		db.updateRequestRegisterationStatus(recordId, name, registered);
	}
	
	public void updateRecordPair(String recordId, String ownerName, String readName)
		throws Exception {
		db.groupRecordIdAndParams(recordId, ownerName, readName);
	}
}
