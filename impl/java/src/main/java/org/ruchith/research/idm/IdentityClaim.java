package org.ruchith.research.idm;

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
	
	public void init(IdentityClaimDefinition def) {
		this.definition = def;
	}

	public IdentityClaimDefinition getDefinition() {
		return definition;
	}
	
}
