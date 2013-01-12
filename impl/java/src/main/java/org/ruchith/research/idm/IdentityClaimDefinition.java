package org.ruchith.research.idm;

import it.unisa.dia.gas.jpbc.Element;

import org.ruchith.ae.base.AEParameters;
/**
 * 
 * @author Ruchith Fernando
 *
 */
public class IdentityClaimDefinition {

	/**
	 * Name of the claim
	 */
	private String name;
	
	/**
	 * Name of the claim
	 */
	private String description;
	
	/**
	 * Public Parameters of the claim
	 */
	private AEParameters params;
	
	/**
	 * Master key associated with this claim.
	 * This is used in issuing new claim instances.
	 */
	private Element masterKey;
	
	/**
	 * SHA 512 digest of the claim definition.
	 * SHA512(name||params)
	 */
	private String b64Hash;
	
	/**
	 * Identity provider's signature on the SHA512 digest.
	 * Sig(SHA512(name||params))
	 */
	private String b64Sig;
	
	
	public IdentityClaimDefinition(String name, AEParameters params) {
		this.name = name;
		this.params = params;
	}
	
	public IdentityClaimDefinition(String name, AEParameters params, Element mk) {
		this.name = name;
		this.params = params;
		this.masterKey = mk;
	}

	public String getName() {
		return name;
	}

	public AEParameters getParams() {
		return params;
	}

	public Element getMasterKey() {
		return masterKey;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getB64Hash() {
		return b64Hash;
	}

	public String getB64Sig() {
		return b64Sig;
	}

	public void setB64Hash(String b64Hash) {
		this.b64Hash = b64Hash;
	}

	public void setB64Sig(String b64Sig) {
		this.b64Sig = b64Sig;
	}

	
	
}
