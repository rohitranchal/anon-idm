package org.ruchith.research.scenarios.healthcare.owner;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.sql.ResultSet;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.research.idm.idp.Configuration;
import org.ruchith.research.idm.idp.IdentityManager;
import org.ruchith.research.scenarios.healthcare.owner.db.Database;
import org.ruchith.ae.base.ReKey;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.research.idm.IdentityClaimDefinition;

import it.unisa.dia.gas.jpbc.Element;

public class Patient extends IdentityManager {
	// 01. As a identity manager, create claim definition
	// 02. After receiving (req) value from the data consumer, 
	// issue claim based on the claim definition
	// 
	
	// one claim definition?
	private Configuration config;
	private Database db;
	private PrivateKey privKey;
	private Certificate cert;
	
	public Patient(String configPath) throws Exception {
		super(configPath); // Parent's constructor
		
		KeyStore ks = KeyStore.getInstance(config.getKeystoreType());
		ks.load(new FileInputStream(config.getKeystoreFilePath()), config.getKeystorePassword().toCharArray());
		this.privKey = (PrivateKey) ks.getKey(config.getPrivKeyAlias(), config.getPrivKeyPassword().toCharArray());
		this.cert = (Certificate) ks.getCertificate(config.getPrivKeyAlias());
		
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
	
	public void retrieveRevocateInfo(String name, String params) throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(Base64.decode(params));
		ObjectNode node = (ObjectNode) rootNode;
		
		System.out.println("Reading claimdef:" + name);
		System.out.println("Reading params:" + node.toString());
		
		AEParameters prevParams = new AEParameters(node);
		System.out.println("Prev: " + prevParams.serializeJSON());
		
		ReKey newKeyUpdater = new ReKey(prevParams);
		// picking new alpha and updating g1 is processed in update
		Element newMk = newKeyUpdater.update();
		// retrieve updated params
		AEParameters newParam = newKeyUpdater.getParams();
		// pick a new beta value
		Element beta = newParam.getPairing().getZr().newRandomElement().getImmutable();
		
		IdentityClaimDefinition updatedClaim = new IdentityClaimDefinition(name, newParam, newMk);
				
		byte[] contentBytes = updatedClaim.getDgstContet().getBytes();

		// Create digest
		MessageDigest dgst = MessageDigest.getInstance("SHA-512");
		dgst.update(contentBytes);
		byte[] sha512Dgst = dgst.digest();
		updatedClaim.setB64Hash(new String(Base64.encode(sha512Dgst)));		
		
		// Sign claim definition
		Signature sig = Signature.getInstance("SHA512withRSA");
		sig.initSign(this.privKey);
		sig.update(contentBytes);
		byte[] sigBytes = sig.sign();
		updatedClaim.setB64Sig(new String(Base64.encode(sigBytes)));

		// Set the pub key cert of the idp
		updatedClaim.setCert(this.cert);		

		JsonNode newNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) newNode;
				
		on.put("claimdef", updatedClaim.serializeJSON());
		on.put("beta", Base64.encode(beta.toBytes()));
		
		// Read all claim for db
		// TODO store UserAnonId in separate list : later augmented with power of beta
		ResultSet rs = this.db.getClaims(name);
		try {
			while (rs.next()) {
				String UserRandom = rs.getString("UserRandom");
				String UserAnonId = rs.getString("UserAnonId");
				System.out.println("userRandom:" + UserRandom);
				System.out.println("userAnonId:" + UserAnonId);
				
				// req^beta, C0 pair
				
				// TODO simply put Random vals
				// TODO calculate new C0s and put that in to the list
			}
		} finally {
			rs.close();
		}
		
		
		// 1 Pread
		// beta
		// pairs = req^b and new computed c0
		// Read update its value
		// first get c0s. Only after finishing calculation of C0s, then update req
		// Then Pead, C0s and beta can be exported

		System.out.println("TESTING: " + on.toString());
	}
}
