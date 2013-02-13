package org.ruchith.research.idm.user;

import org.ruchith.research.idm.IdentityClaim;

/**
 * This is the main store of the identity claim instances.
 * 
 * @author Ruchith Fernando
 * 
 */
public class ClaimWallet {

	private ClaimWallet() {
	}

	private static ClaimWallet instance;

	public static ClaimWallet getInstance() {
		if (instance == null) {
			instance = new ClaimWallet();
		}

		return instance;
	}

	/**
	 * Store an identity claim instance.
	 * 
	 * @param claim
	 *            The claim instance to be stored.
	 */
	public void storeClaim(IdentityClaim claim) {
		// TODO
	}

}
