package org.ruchith.research.idm.user;

import java.util.HashMap;

import it.unisa.dia.gas.jpbc.Element;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AECipherTextBlock;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.ContactKeyGen;
import org.ruchith.ae.base.Decrypt;
import org.ruchith.research.idm.IdentityClaim;
import org.ruchith.research.idm.IdentityClaimDefinition;

public class Client {

	private ClaimWallet wallet;

	private HashMap<String, AEPrivateKey> privKeys = new HashMap<String, AEPrivateKey>();

	public Client(String walletDir) throws Exception {
		this.wallet = ClaimWallet.getInstance(walletDir);
	}

	public String generateRequest(String claimName) throws Exception {
		IdentityClaim claim = wallet.getClaim(claimName);

		IdentityClaimDefinition icd = claim.getDefinition();

		// Create request
		AEParameters params = icd.getParams();

		ContactKeyGen conKeyGen = new ContactKeyGen();
		conKeyGen.init(claim.getClaimKey(), claim.getClaim(), params);
		Element r = conKeyGen.genRandomID();
		AEPrivateKey tmpPriv = conKeyGen.getTmpPrivKey(r);

		this.privKeys.put(claimName, tmpPriv);

		Element val = conKeyGen.getTmpPubKey(r);

		return new String(Base64.encode(val.toBytes()));
	}

	public String extractSessionKey(String claimName, String spResponse) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode ctOn = (ObjectNode) mapper.readTree(spResponse);

		IdentityClaim claim = this.wallet.getClaim(claimName);

		AEPrivateKey tmpPriv = this.privKeys.get(claimName);

		AEParameters params = claim.getDefinition().getParams();
		AECipherTextBlock ct = new AECipherTextBlock(ctOn, params.getPairing());

		Decrypt decrypt = new Decrypt();
		decrypt.init(params);
		Element result = decrypt.doDecrypt(ct, tmpPriv);

		return new String(Base64.encode(result.toBytes()));
	}
	
	public String extractSessionKeyDouble(String claimName1, String claimName2, String ch1, String ch2) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode ct1On = (ObjectNode) mapper.readTree(ch1);
		ObjectNode ct2On = (ObjectNode) mapper.readTree(ch2);

		IdentityClaim claim1 = this.wallet.getClaim(claimName1);
		AEPrivateKey tmpPriv1 = this.privKeys.get(claimName1);

		AEParameters params1 = claim1.getDefinition().getParams();
		AECipherTextBlock ct1 = new AECipherTextBlock(ct1On, params1.getPairing());

		Decrypt decrypt = new Decrypt();
		decrypt.init(params1);
		Element result1 = decrypt.doDecrypt(ct1, tmpPriv1);
		
		IdentityClaim claim2 = this.wallet.getClaim(claimName2);
		AEPrivateKey tmpPriv2 = this.privKeys.get(claimName2);
		
		AEParameters params2 = claim2.getDefinition().getParams();
		AECipherTextBlock ct2 = new AECipherTextBlock(ct2On, params2.getPairing());

		decrypt.init(params2);
		Element result2 = decrypt.doDecrypt(ct2, tmpPriv2);

		Element result = result1.add(result2);
		
		return new String(Base64.encode(result.toBytes()));
	}
	
}
