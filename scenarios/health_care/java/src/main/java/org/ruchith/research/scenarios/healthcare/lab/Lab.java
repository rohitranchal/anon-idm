package org.ruchith.research.scenarios.healthcare.lab;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Iterator;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.research.idm.IdentityClaimDefinition;
import org.ruchith.research.idm.idp.Configuration;
import org.ruchith.research.scenarios.healthcare.lab.db.Database;

/**
 * 
 * @author Byungchan An
 *
 */
public class Lab {
	
	private Configuration config;
	private Database db;
	
	public Lab(String configPath) throws Exception {
		this.config = Configuration.getInstance(configPath);
		this.db = Database.getInstance(this.config.getDbHost(), this.config.getDbUser(), this.config.getDbPassword());		
	}
	
	/**
	 * Extract public parameter in claim definition
	 * 
	 * @param	claimdef	Claim definition in JSON format
	 * @return	String		String encoded in Base64 for public parameter
	 * @throws	Exception
	 */
	public String extractPublicParams(String claimdef) throws Exception {
		System.out.println(claimdef);
		String params = null;
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode rootNode = mapper.readTree(claimdef);

			ObjectNode node = (ObjectNode) rootNode;
			params = node.get("params").asText();
			System.out.println("Encoded public params: " + params);
			
			/*			
			ObjectNode on = (ObjectNode) mapper.readTree(Base64.decode(params));
			
			System.out.println("Comparison String 1:" + new String(Base64.decode(params)));
			System.out.println("Comparison String 2: " + on.toString());
			String test = new String(Base64.decode(params));
			if(test.equals(on.toString()))
				System.out.println("Equal!");
			*/
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return params;
	}
	
	public boolean verifyClaimdef(String fetchedCert, String claimdef) {
		try {
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			Certificate idpCert = factory.generateCertificate(new ByteArrayInputStream(fetchedCert.getBytes()));

			MessageDigest dgst = MessageDigest.getInstance("SHA-512");
			Signature sig = Signature.getInstance("SHA512withRSA");

			System.out.println(claimdef);
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode rootOn = (ObjectNode) mapper.readTree(claimdef);

			String name = rootOn.get("name").asText();
			String params = rootOn.get("params").asText();
			String b64Dgst = rootOn.get("dgst").asText();
			String b64Sig = rootOn.get("sig").asText();
			String b64Cert = rootOn.get("cert").asText();
			// String createDate = node.get("DateCreated").asText();

			ObjectNode on = (ObjectNode) mapper.readTree(Base64.decode(params));
			AEParameters aeParams = new AEParameters(on);

			IdentityClaimDefinition tmpDef = new IdentityClaimDefinition(name, aeParams);

			// Verify dgst
			byte[] contentBytes = tmpDef.getDgstContet().getBytes();

			dgst.reset();
			dgst.update(contentBytes);
			String newDgstVal = new String(Base64.encode(dgst.digest()));

			if (!newDgstVal.equals(b64Dgst)) {
				// TODO : Log error
				return false;
			}

			// Verify signature
			sig.initVerify(idpCert);
			sig.update(contentBytes);
			if (!sig.verify(Base64.decode(b64Sig))) {
				// TODO : Log error
				return false;
			}

			tmpDef.setB64Hash(b64Dgst);
			tmpDef.setB64Sig(b64Sig);

			ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(b64Cert));
			tmpDef.setCert(CertificateFactory.getInstance("X.509").generateCertificate(bais));
	
		} catch (Exception e) {
			// Swallow for now
			// TODO
			e.printStackTrace();
			return false;
		}
			
		// Everything is good
		return true;
	}
	
	/**
	 * 
	 * @param ownerParam
	 * @param readParam
	 * @throws Exception
	 */
	public void initRecord(String ownerParam, String readParam) 
			throws Exception {
		db.storeIdAndParams(ownerParam, readParam);
	}
	
	public void updateRecord(String id, String Record) 
			throws Exception {
		db.updateRecord(id, Record);
	}

}
