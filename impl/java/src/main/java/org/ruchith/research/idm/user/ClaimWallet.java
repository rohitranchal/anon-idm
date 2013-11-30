package org.ruchith.research.idm.user;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.ruchith.research.idm.IdentityClaim;

/**
 * This is the main store of the identity claim instances.
 * 
 * @author Ruchith Fernando
 * 
 */
public class ClaimWallet {

	private File walletDir;

	private ClaimWallet() throws Exception {
		// Look for the local wallet instance in current directory
		this.walletDir = new File("wallet");
		if (!this.walletDir.exists()) {
			this.walletDir.mkdir();
		}
		Collection<File> claimFileList = FileUtils.listFiles(this.walletDir, new String[] { "claim" }, false);
		for (Iterator<File> iterator = claimFileList.iterator(); iterator.hasNext();) {
			File claimFile = iterator.next();
			
			
		}
	}

	private static ClaimWallet instance;

	/**
	 * Map of claims stored in this wallet. Keys are claim definition hash values.
	 */
	private HashMap<String, IdentityClaim> claims = new HashMap<String, IdentityClaim>();

	public static ClaimWallet getInstance() throws Exception {
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
	public void storeClaim(IdentityClaim claim) throws Exception {
		// Remove directory seperator to avoid issue in creating the claim file
		String key = claim.getDefinition().getB64Hash().replace(File.separator, "").replace("\\", "").replace("=", "")
				.replace("+", "");
		this.claims.put(key, claim);

		// Store in wallet directory
		String claimFilePath = this.walletDir.getAbsolutePath() + File.separator + key + ".claim";
		FileUtils.writeStringToFile(new File(claimFilePath), claim.serializeJSON());
	}

}
