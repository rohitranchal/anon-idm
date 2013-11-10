package org.ruchith.research.idm;

import it.unisa.dia.gas.jpbc.Element;

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
	 * This can either be the master key of the user or a randomly created key.
	 * In the case where the key was randomly created, claimKey will have a
	 * value other than null
	 */
	private Element claimKey;

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

//	public String serializeJSON() {
//		ObjectMapper mapper = new ObjectMapper();
//		JsonNode rootNode = mapper.createObjectNode();
//		ObjectNode on = (ObjectNode) rootNode;
//		
//		return null;
//	}
	
}
