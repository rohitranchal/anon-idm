package org.ruchith.research.idm.idp;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;

import org.bouncycastle.util.encoders.Base64;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.RootKeyGen;
import org.ruchith.research.idm.IdentityClaim;
import org.ruchith.research.idm.IdentityClaimDefinition;

public class IdentityManager {

	/**
	 * RSA private key used for signing
	 */
	private PrivateKey privKey;
	
	public IdentityManager(PrivateKey privKey) {
		this.privKey = privKey;
	}
	
	/**
	 * Generate a new identity claim definition and give it a name.
	 * 
	 * @param name
	 *            Name of the claim.
	 * @return An instance of {@link IdentityClaimDefinition} which holds the
	 *         public parameters and the master key.
	 */
	public IdentityClaimDefinition generateNewClaimDefinition(String name) throws Exception {
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();
		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);
		AEParameters params = paramGen.generateParameters();

		RootKeyGen rkg = new RootKeyGen();
		rkg.init(params);
		Element mk = paramGen.getMasterKey();

		IdentityClaimDefinition claimDef = new IdentityClaimDefinition(name, params, mk);
		
		//Create digest
		MessageDigest dgst = MessageDigest.getInstance("SHA-512");
		String claimDefToDgst = claimDef.getName() + claimDef.getParams().serializeJSON();
		dgst.update(claimDefToDgst.getBytes());
		byte[] sha512Dgst = dgst.digest();
		claimDef.setB64Hash(new String(Base64.encode(sha512Dgst)));
		
		//Sign claim definition
		Signature sig = Signature.getInstance("SHA512withRSA");
		sig.initSign(this.privKey);
		sig.update(claimDefToDgst.getBytes());
		byte[] sigBytes = sig.sign();
		claimDef.setB64Sig(new String(Base64.encode(sigBytes)));
		
		return claimDef;
	}

	public IdentityClaim issueClaim(String name, String user, Element userInput) {
		// TODO
		return new IdentityClaim();
	}

	
	
}
