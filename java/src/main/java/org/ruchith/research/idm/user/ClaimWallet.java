package org.ruchith.research.idm.user;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.research.idm.IdentityClaim;

/**
 * This is the main store of the identity claim instances.
 * 
 * @author Ruchith Fernando
 * 
 */
public class ClaimWallet {

	private static ClaimWallet instance;

	private File walletDir;

	/**
	 * Map of claims stored in this wallet. Keys are claim definition hash values.
	 */
	private HashMap<String, IdentityClaim> claims = new HashMap<String, IdentityClaim>();

	private ClaimWallet(String path) throws Exception {
		// Look for the local wallet instance in current directory
		this.walletDir = new File(path);
		if (!this.walletDir.exists()) {
			this.walletDir.mkdir();
		}
		Collection<File> claimFileList = FileUtils.listFiles(this.walletDir, null, false);
		for (Iterator<File> iterator = claimFileList.iterator(); iterator.hasNext();) {
			File claimFile = iterator.next();
			String content = FileUtils.readFileToString(claimFile);
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode on = (ObjectNode) mapper.readTree(content.toString());
			IdentityClaim claim = new IdentityClaim(on);
			this.claims.put(claim.getDefinition().getName(), claim);
		}
	}


	public static ClaimWallet getInstance() throws Exception {
		if (instance == null) {
			instance = new ClaimWallet("wallet");
		}

		return instance;
	}
	
	public static ClaimWallet getInstance(String path) throws Exception {
		if (instance == null) {
			instance = new ClaimWallet(path);
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
		// claim name is the "key"
		this.claims.put(claim.getDefinition().getName(), claim);
		
		// Remove directory seperator to avoid issue in creating the claim file
		String filename = claim.getDefinition().getB64Hash().replace(File.separator, "").replace("\\", "").replace("=", "")
					.replace("+", "");
		// Store in wallet directory
		String claimFilePath = this.walletDir.getAbsolutePath() + File.separator + filename;		

		/* 
		 * This stores filename as a key in the data structure which doesn't match
		String key = claim.getDefinition().getB64Hash().replace(File.separator, "").replace("\\", "").replace("=", "")
				.replace("+", "");
		System.out.println(key);
		this.claims.put(key, claim);

		String claimFilePath = this.walletDir.getAbsolutePath() + File.separator + key;
		*/
		FileUtils.writeStringToFile(new File(claimFilePath), claim.serializeJSON());
	}

	/**
	 * Return the number of claims in the wallet
	 * @return
	 */
	public int getClaimCount() {
		return this.claims.size();
	}
	
	/**
	 * Lookup a claim by the given id.
	 * 
	 * @param id
	 * @return
	 */
	public IdentityClaim getClaim(String id) {
		return this.claims.get(id);
	}

	/**
	 * Set of claimdefs currently existing in wallet
	 * @return
	 */
	public Set<String> getClaimdefNameSet() {
		return claims.keySet();
	}
}
