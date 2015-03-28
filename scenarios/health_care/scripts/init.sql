# Initialization for patient databases
CREATE DATABASE IF NOT EXISTS idp;

USE idp;

DROP TABLE IF EXISTS Claim_Definition;
CREATE TABLE Claim_Definition (
	Name VARCHAR(512) NOT NULL PRIMARY KEY,
	Description VARCHAR(2048),
	PrivateKey TEXT NOT NULL,
	PublicParams TEXT NOT NULL,
	Digest TEXT NOT NULL,
	Sig TEXT NOT NULL,
	Cert TEXT NOT NULL,
	DateCreated TIMESTAMP NOT NULL	
);

DROP TABLE IF EXISTS Claim_Definition_Re_Key;
CREATE TABLE Claim_Definition_Re_Key (
	ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	Name VARCHAR(512) NOT NULL,
	ReKeyInfo TEXT NOT NULL
);

DROP TABLE IF EXISTS User;
CREATE TABLE User (
	Name VARCHAR(256) NOT NULL PRIMARY KEY,
	PubKeyCertificateFpr VARCHAR(256) NOT NULL, 
	PubKeyCertificate TEXT NOT NULL,
	DateCreated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


DROP TABLE IF EXISTS Claim;
CREATE TABLE Claim (
	ClaimName VARCHAR(512) NOT NULL,
	UserName VARCHAR(256) NOT NULL,
	UserRandom TEXT NOT NULL,
	UserAnonId TEXT NOT NULL,
	IssueDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(ClaimName, UserName)
);

DROP TABLE IF EXISTS RequestPermission;
CREATE TABLE RequestPermission (
    RecordId VARCHAR(256) NOT NULL,
    Name VARCHAR(256) NOT NULL,
    Type TINYINT UNSIGNED NOT NULL,
    Registered BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY(RecordId, Name)
);

DROP TABLE IF EXISTS RecordPair;
CREATE TABLE RecordPair (
    RecordId VARCHAR(256) NOT NULL,
    OwnerName VARCHAR(256) NOT NULL,
    ReadName VARCHAR(256) NOT NULL,
    PRIMARY KEY(RecordId, ReadName)
);

# Initialization for lab databases
CREATE DATABASE IF NOT EXISTS lab;

USE lab;

DROP TABLE IF EXISTS LabRecord;
CREATE TABLE LabRecord (
    id MEDIUMINT NOT NULL PRIMARY KEY AUTO_INCREMENT, # id in the lab
    ParamOwner TEXT NOT NULL,       # ParamOwner
    ParamRead TEXT NOT NULL,        # ParamRead
    Record TEXT NOT NULL            # Medical Result
);

# Initialization for hie databases
CREATE DATABASE IF NOT EXISTS hie;

USE hie;

DROP TABLE IF EXISTS HieRecord;
CREATE TABLE HieRecord (
    GParam VARCHAR(512) NOT NULL PRIMARY KEY,   # hash g in the parameter
    ParamOwner TEXT NOT NULL,           # ParamOwner
    ParamRead TEXT NOT NULL,            # ParamRead
    Record TEXT NOT NULL                # Medical Result
);

