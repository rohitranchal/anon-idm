package org.ruchith.research.idm;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEPrivateKey;

/**
 * 
 * @author Ruchith Fernando
 * 
 */
public class IdentityClaim {

	/**
	 * Definition related to this identity claim instance.
	 */
	private IdentityClaimDefinition definition;

	/**
	 * The actual claim instance
	 */
	private AEPrivateKey claim;

	/**
	 * This can either be the master key of the user or a randomly created key. In the case where the key was randomly
	 * created, claimKey will have a value other than null
	 */
	private Element claimKey;

	public IdentityClaim() {
	}

	public IdentityClaim(ObjectNode on) {
		this.definition = new IdentityClaimDefinition((ObjectNode) on.get("claimDef"));

		Pairing pairing = this.definition.getParams().getPairing();
		this.claim = new AEPrivateKey((ObjectNode) on.get("claim"), pairing);

		Field group1 = pairing.getZr();
		this.claimKey = group1.newElement();
		String claimKeyValueB64 = on.get("claimKey").getTextValue();
		this.claimKey.setFromBytes(Base64.decode(claimKeyValueB64));
	}

	public void init(IdentityClaimDefinition def) {
		this.definition = def;
	}

	public IdentityClaimDefinition getDefinition() {
		return definition;
	}

	public AEPrivateKey getClaim() {
		return claim;
	}

	public void setClaim(AEPrivateKey claim) {
		this.claim = claim;
	}

	public Element getClaimKey() {
		return claimKey;
	}

	public void setClaimKey(Element claimKey) {
		this.claimKey = claimKey;
	}

	public String serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;

		on.put("claimDef", this.definition.serializeJSON());
		on.put("claim", this.claim.serializeJSON());
		on.put("claimKey", new String(Base64.encode(this.claimKey.toBytes())));

		return on.toString();
	}

}
