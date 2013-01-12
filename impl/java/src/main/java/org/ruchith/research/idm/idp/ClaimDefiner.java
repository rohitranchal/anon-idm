package org.ruchith.research.idm.idp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.PrivateKey;

import org.ruchith.research.idm.IdentityClaimDefinition;
import org.ruchith.research.idm.idp.db.Database;

/**
 * Tool to define identity claims.
 * 
 * @author Ruchith Fernando
 * 
 */
public class ClaimDefiner {

	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Claim name:");
		String claimName = br.readLine();

		System.out.println("Claim description (Optional):");
		String claimDesc = br.readLine();

		Configuration config = Configuration.getInstance();

		// Read the keystore and get the private key
		KeyStore ks = KeyStore.getInstance(config.getKeystoreType());
		ks.load(new FileInputStream(config.getKeystoreFilePath()), 
				config.getKeystorePassword().toCharArray());
		PrivateKey pk = (PrivateKey) ks.getKey(config.getPrivKeyAlias(), 
				config.getPrivKeyPassword().toCharArray());

		IdentityManager manager = new IdentityManager(pk);
		IdentityClaimDefinition def = manager
				.generateNewClaimDefinition(claimName);
		if (claimDesc != null && claimDesc.trim().length() != 0) {
			def.setDescription(claimDesc);
		}

		Database db = Database.getInstance(config.getDbHost(),
				config.getDbUser(), config.getDbPassword());
		db.storeClaimDefinition(def);

	}
}
