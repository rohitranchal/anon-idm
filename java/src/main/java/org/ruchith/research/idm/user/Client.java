package org.ruchith.research.idm.user;

import it.unisa.dia.gas.jpbc.Element;

import org.bouncycastle.util.encoders.Base64;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.research.idm.IdentityClaim;
import org.ruchith.research.idm.IdentityClaimDefinition;

public class Client {

	private ClaimWallet wallet;
	
	public Client(String walletDir) throws Exception{
		this.wallet = ClaimWallet.getInstance(walletDir);
	}
	 
	public String generateRequest(String claimName) throws Exception {
		System.out.println(claimName);
		IdentityClaim claim = wallet.getClaim(claimName);
		
		IdentityClaimDefinition icd = claim.getDefinition();
		
		
		//Create request
		AEParameters params = icd.getParams();
		
		Element claimKey = claim.getClaimKey();
		Element r = params.getPairing().getZr().newRandomElement();
		
		Element e1 = params.getH1().powZn(claimKey);
		Element e2 = params.getH2().powZn(r);
		
		Element val = e1.mul(e2);

		return new String(Base64.encode(val.toBytes()));
	}
}
