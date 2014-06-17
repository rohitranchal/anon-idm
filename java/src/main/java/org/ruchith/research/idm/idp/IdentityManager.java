package org.ruchith.research.idm.idp;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.RootKeyGen;
import org.ruchith.research.idm.IdentityClaim;
import org.ruchith.research.idm.IdentityClaimDefinition;
import org.ruchith.research.idm.Util;
import org.ruchith.research.idm.idp.db.Database;

public class IdentityManager {

	/**
	 * RSA private key used for signing
	 */
	private PrivateKey privKey;

	/**
	 * Public key certificate of the identity provider.
	 */
	private Certificate cert;

	private Configuration config;

	private Database db;

	public IdentityManager(String configPath) throws Exception {
		this.config = Configuration.getInstance(configPath);

		// Read the keystore and get the private key
		KeyStore ks = KeyStore.getInstance(config.getKeystoreType());
		ks.load(new FileInputStream(config.getKeystoreFilePath()), config.getKeystorePassword().toCharArray());
		this.privKey = (PrivateKey) ks.getKey(config.getPrivKeyAlias(), config.getPrivKeyPassword().toCharArray());
		this.cert = (Certificate) ks.getCertificate(config.getPrivKeyAlias());

		this.db = Database.getInstance(this.config.getDbHost(), this.config.getDbUser(), this.config.getDbPassword());

	}

	/**
	 * Generate a new identity claim definition and give it a name.
	 * 
	 * @param name
	 *            Name of the claim.
	 * @param desc
	 *            Description of the claim.
	 * @return An instance of {@link IdentityClaimDefinition} which holds the public parameters and the master key.
	 */
	public IdentityClaimDefinition generateNewClaimDefinition(String name, String desc) throws Exception {
		 CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32).generate();
//		CurveParams curveParams = new CurveParams().load(new ByteArrayInputStream(this.config.getParamFileContents()));
		double t1 = new Date().getTime();
		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
//		ObjectMapper mapper = new ObjectMapper();
//		ObjectNode on = (ObjectNode)mapper.readTree(new String(this.config.getParamFileContents()));
//		AEParameters from = new AEParameters(on);
//		AEParameters params = paramGen.generateParameters(from);
		AEParameters params = paramGen.generateParameters();

//		if (this.config.isUseSameH1AndH2()) {
//			Field g1 = params.getPairing().getG1();
//			Element h1 = g1.newElement();
//			h1.setFromBytes(this.config.getH1());
//			params.setH1(h1.getImmutable());
//
//			Element h2 = g1.newElement();
//			h2.setFromBytes(this.config.getH2());
//			params.setH2(h2.getImmutable());
//		}

		Element mk = paramGen.getMasterKey();

		IdentityClaimDefinition claimDef = new IdentityClaimDefinition(name, params, mk);
		

		if (desc != null) {
			claimDef.setDescription(desc);
		}
		double t2 = new Date().getTime();
		
		byte[] contentBytes = claimDef.getDgstContet().getBytes();

		// Create digest
		MessageDigest dgst = MessageDigest.getInstance("SHA-512");
		dgst.update(contentBytes);
		byte[] sha512Dgst = dgst.digest();
		claimDef.setB64Hash(new String(Base64.encode(sha512Dgst)));

		double t3 = new Date().getTime();
		// Sign claim definition
		Signature sig = Signature.getInstance("SHA512withRSA");
		sig.initSign(this.privKey);
		sig.update(contentBytes);
		byte[] sigBytes = sig.sign();
		claimDef.setB64Sig(new String(Base64.encode(sigBytes)));

		// Set the pub key cert of the idp
		claimDef.setCert(this.cert);
		double t4 = new Date().getTime();

		this.db.storeClaimDefinition(claimDef);
		double t5 = new Date().getTime();
		
		System.out.println("Defn" + (t2 - t1));
		System.out.println("Dgst" + (t3 - t2));
		System.out.println("Sign" + (t4 - t3));
		System.out.println("Store" + (t5 - t4) + "\n");
		return claimDef;
	}

	/**
	 * Return the list of claim names.
	 * 
	 * @return The list of claim names available in the database.
	 * 
	 * @throws Exception
	 */
	public List<String> getAllClaimNames() throws Exception {
		ArrayList<String> claims = new ArrayList<String>();
		ResultSet rs = this.db.getAllClaimDefinitions();
		try {
			while (rs.next()) {
				String name = rs.getString("Name");
				claims.add(name);
			}
		} finally {
			rs.close();
		}
		return claims;
	}

	/**
	 * Issue a claim and serialize it to be returned to the user.
	 * 
	 * @param claimName
	 *            Name of the claim
	 * @param user
	 *            User name
	 * @param req
	 *            Serialized request element
	 * @return A Base64 encoded string of the claim.
	 */
	public String issueSerializedClaim(String claimName, String user, String req) {
		try {
			IdentityClaim claim = this.issueClaim(claimName, user, req);
			String claimStr = claim.getClaim().serializeJSON().toString();
			return new String(Base64.encode(claimStr.getBytes()));
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
			// return null;
		}
	}

	/**
	 * Issue an identity claim to the given user.
	 * 
	 * @param claimName
	 *            Name of the claim
	 * @param user
	 *            User name
	 * @param req
	 *            Serialized request element
	 * @return An {@link IdentityClaim} instance or null
	 * @throws Exception
	 */
	public IdentityClaim issueClaim(String claimName, String user, String req) throws Exception {
		// Check whether we know this user
		String b64Cert = this.db.getUserCertValue(user);
		if (b64Cert == null) { // User is not in DB
			return null;
		}
		// TODO: Verify sig

		// Fetch claim info
		IdentityClaimDefinition claimDef = this.db.getClaimDefinition(claimName);

		Element anonId = claimDef.getParams().getPairing().getG1().newElement();
		anonId.setFromBytes(Base64.decode(req));

		RootKeyGen rkg = new RootKeyGen();
		rkg.init(claimDef.getParams());
		Element r = claimDef.getParams().getPairing().getZr().newRandomElement();
		AEPrivateKey pk = rkg.genAnonKey(anonId, claimDef.getMasterKey(), r);

		// TODO : Apply policy

		IdentityClaim claim = new IdentityClaim();
		claim.setClaim(pk);

		// Store claim
		this.db.storeClaim(claimName, user, r, anonId);

		// TODO Encrypt

		return claim;
	}

	/**
	 * Add a user entry with the given information.
	 * 
	 * @param name
	 *            User name.
	 * @param cert
	 *            User certificate.
	 */
	public void addUser(String name, String b64Cert) throws Exception {

		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(Base64.decode(b64Cert));
		String fpr = Util.converToHexString(md.digest()).trim();

		// Create the user entry in the db
		this.db.addUserEntry(name, fpr, b64Cert);
	}

	/**
	 * Look up the certificate for the given certificate fingerprint and return.
	 * 
	 * @param certFpr
	 *            Certificate fingerprint.
	 * @return Certificate if exists, otherwise null.
	 */
	public Certificate getUserCertificate(String certFpr) throws Exception {
		String b64Cert = this.db.getUserCertValueByFpr(certFpr);
		ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(b64Cert));
		return CertificateFactory.getInstance("X.509").generateCertificate(bais);
	}

}
