package org.ruchith.research.idm.idp;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.encoders.Base64;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
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

	private Configuration config;

	private Database db;

	public IdentityManager() throws Exception {
		this.config = Configuration.getInstance();

		// Read the keystore and get the private key
		KeyStore ks = KeyStore.getInstance(config.getKeystoreType());
		ks.load(new FileInputStream(config.getKeystoreFilePath()), config
				.getKeystorePassword().toCharArray());
		this.privKey = (PrivateKey) ks.getKey(config.getPrivKeyAlias(), config
				.getPrivKeyPassword().toCharArray());

		this.db = Database.getInstance(this.config.getDbHost(),
				this.config.getDbUser(), this.config.getDbPassword());

	}

	/**
	 * Generate a new identity claim definition and give it a name.
	 * 
	 * @param name
	 *            Name of the claim.
	 * @param desc
	 *            Description of the claim.
	 * @return An instance of {@link IdentityClaimDefinition} which holds the
	 *         public parameters and the master key.
	 */
	public IdentityClaimDefinition generateNewClaimDefinition(String name,
			String desc) throws Exception {
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();
		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		AEParameters params = paramGen.generateParameters();

		RootKeyGen rkg = new RootKeyGen();
		rkg.init(params);
		Element mk = paramGen.getMasterKey();

		IdentityClaimDefinition claimDef = new IdentityClaimDefinition(name,
				params, mk);

		if (desc != null) {
			claimDef.setDescription(desc);
		}

		byte[] contentBytes = claimDef.getDgstContet().getBytes();

		// Create digest
		MessageDigest dgst = MessageDigest.getInstance("SHA-512");
		dgst.update(contentBytes);
		byte[] sha512Dgst = dgst.digest();
		claimDef.setB64Hash(new String(Base64.encode(sha512Dgst)));

		// Sign claim definition
		Signature sig = Signature.getInstance("SHA512withRSA");
		sig.initSign(this.privKey);
		sig.update(contentBytes);
		byte[] sigBytes = sig.sign();
		claimDef.setB64Sig(new String(Base64.encode(sigBytes)));

		this.db.storeClaimDefinition(claimDef);

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
	 * Issue an identity claim to the given user.
	 * @param name
	 * @param user
	 * @param userInput
	 * @return
	 * @throws Exception
	 */
	public IdentityClaim issueClaim(String name, String user, Element userInput)
			throws Exception {
		// TODO
		return new IdentityClaim();
	}

	/**
	 * Add a user entry with the given information.
	 * 
	 * @param name User name.
	 * @param cert User certificate.
	 */
	public void addUser(String name, Certificate cert) throws Exception {
		// Create the user entry in the db
		byte[] certBytes = cert.getEncoded();
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(certBytes);
		String fpr = Util.converToHexString(md.digest()).trim();
		String certVal = new String(Base64.encode(certBytes));
		this.db.addUserEntry(name, fpr, certVal);
	}

	/**
	 * Look up the certificate for the given certificate fingerprint and return.
	 * 
	 * @param certFpr
	 *            Certificate fingerprint.
	 * @return Certificate if exists, otherwise null.
	 */
	public Certificate getUserCertificate(String certFpr) {
		throw new UnsupportedOperationException("TODO");
	}

}
