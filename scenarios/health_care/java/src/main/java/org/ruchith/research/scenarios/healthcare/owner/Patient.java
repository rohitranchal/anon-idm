package org.ruchith.research.scenarios.healthcare.owner;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.research.idm.idp.Configuration;
import org.ruchith.research.idm.idp.IdentityManager;
import org.ruchith.research.scenarios.healthcare.owner.db.Database;
import org.ruchith.ae.base.ReKey;
import org.ruchith.ae.base.AEParameters;
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
		
		this.config = Configuration.getInstance(configPath);
		
		KeyStore ks = KeyStore.getInstance(config.getKeystoreType());
		ks.load(new FileInputStream(config.getKeystoreFilePath()), config.getKeystorePassword().toCharArray());
		this.privKey = (PrivateKey) ks.getKey(config.getPrivKeyAlias(), config.getPrivKeyPassword().toCharArray());
		this.cert = (Certificate) ks.getCertificate(config.getPrivKeyAlias());
		
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
	
	public String processRevocation(String claimdefname, String username, String params) throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(Base64.decode(params));
		ObjectNode node = (ObjectNode) rootNode;
		
		System.out.println("Reading claimdef:" + claimdefname);
		System.out.println("Reading params:" + node.toString());
		
		AEParameters prevParam = new AEParameters(node);
		//System.out.println("Prev: " + prevParams.serializeJSON());
		
		ReKey newKeyUpdater = new ReKey(prevParam);
		// picking new alpha and updating g1 is processed in update
		// new master key is immutable key
		Element newMk = newKeyUpdater.update();
		// retrieve updated params
		AEParameters newParam = newKeyUpdater.getParams();
		// pick a new beta value
		Element beta = newParam.getPairing().getZr().newRandomElement().getImmutable();

		// create updatedClaimdefinition
		IdentityClaimDefinition updatedClaimdef = new IdentityClaimDefinition(claimdefname, newParam, newMk);

		byte[] contentBytes = updatedClaimdef.getDgstContet().getBytes();

		// Create digest
		MessageDigest dgst = MessageDigest.getInstance("SHA-512");
		dgst.update(contentBytes);
		byte[] sha512Dgst = dgst.digest();
		updatedClaimdef.setB64Hash(new String(Base64.encode(sha512Dgst)));		
		
		// Sign claim definition
		Signature sig = Signature.getInstance("SHA512withRSA");
		sig.initSign(this.privKey);
		sig.update(contentBytes);
		byte[] sigBytes = sig.sign();
		updatedClaimdef.setB64Sig(new String(Base64.encode(sigBytes)));
		// Set the pub key cert of the idp
		updatedClaimdef.setCert(this.cert);

		// Remove user from the claim, RequestClaim
		db.removeClaim(claimdefname, username);
		
		// RecordId_username format will be split for updating request infos
		String[] splits = claimdefname.split("_");
		db.revocateRequestRegisteration(splits[0], username);
		
		// Read all claim for db
		HashMap<String, String> updateVals = new HashMap<String, String>();
		ResultSet rs = this.db.getClaims(claimdefname);
		try {
			while (rs.next()) {
				String userRandom = rs.getString("UserRandom");
				String userAnonId = rs.getString("UserAnonId");
				System.out.println("userRandom:" + userRandom);
				System.out.println("userAnonId:" + userAnonId);
				
				// calculate req^beta
				Element anonId = newParam.getPairing().getG1().newElement();
				anonId.setFromBytes(Base64.decode(userAnonId));
				System.out.println("Test anonId:" + new String(Base64.encode(anonId.toBytes())));
				
				Element tmpAnonId = anonId.getImmutable();
				Element newAnonId = tmpAnonId.powZn(beta).getImmutable();
				System.out.println("Test newAnonId:" + new String(Base64.encode(newAnonId.toBytes())));
				System.out.println("Test anonId:" + new String(Base64.encode(anonId.toBytes())));
				
				// caculate c0 value
				Element g3 = newParam.getG3();
				System.out.println("Test newAnonId:" + new String(Base64.encode(newAnonId.toBytes())));
				System.out.println("Test anonId:" + new String(Base64.encode(anonId.toBytes())));
				Element tmp1 = anonId.mul(g3);
				System.out.println("Test newAnonId:" + new String(Base64.encode(newAnonId.toBytes())));
				System.out.println("Test anonId:" + new String(Base64.encode(anonId.toBytes())));
				
				Element r = newParam.getPairing().getZr().newElement();
				r.setFromBytes(Base64.decode(userRandom));
				System.out.println("Test r:" + new String(Base64.encode(r.toBytes())));
				System.out.println("Test newAnonId:" + new String(Base64.encode(newAnonId.toBytes())));
				System.out.println("Test anonId:" + new String(Base64.encode(anonId.toBytes())));
				
				Element tmp2 = tmp1.powZn(r);
				System.out.println("Test newAnonId:" + new String(Base64.encode(newAnonId.toBytes())));
				System.out.println("Test anonId:" + new String(Base64.encode(anonId.toBytes())));
				// This is c0 value
				Element tmp3 = updatedClaimdef.getMasterKey().mul(tmp2).getImmutable();
				System.out.println("Test newAnonId:" + new String(Base64.encode(newAnonId.toBytes())));
				System.out.println("Test tmp3:" + new String(Base64.encode(tmp3.toBytes())));
				
				// mappings
				String key = new String(Base64.encode(newAnonId.toBytes()));
				System.out.println("key:" + key);
				String value = new String(Base64.encode(tmp3.toBytes()));
				System.out.println("value:" + value);
				updateVals.put(key, value);
				
				// TODO : remove
				System.out.println("DEBUG:" + new String(Base64.encode(tmp3.toBytes())));
				System.out.println();
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
		
		JsonNode newNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) newNode;
				
		on.put("params", updatedClaimdef.getParams().serializeJSON());
		on.put("dgst", new String(Base64.encode(sha512Dgst)));
		on.put("sig", new String(Base64.encode(sigBytes)));
		on.put("cert", new String(Base64.encode(this.cert.getEncoded())));
		on.put("beta", new String(Base64.encode(beta.toBytes())));
		
		ArrayNode updateArray = (ArrayNode) mapper.createArrayNode();
		
		for (Map.Entry<String, String> entry : updateVals.entrySet())
		{
		    System.out.println(entry.getKey() + "	" + entry.getValue());
		    
		    ObjectNode tmp = mapper.createObjectNode();
		    tmp.put("req", entry.getKey());
			tmp.put("c0", entry.getValue());
			
			updateArray.add(tmp);
		}

		on.put("updateArray", updateArray);
		
		// Update claim defintion
		db.updateClaimDefinition(updatedClaimdef);

		System.out.println("TESTING: " + on.toString());
		
		
		// TODO return in Encoded format
		return new String(Base64.encode(on.toString().getBytes()));
	}
}
