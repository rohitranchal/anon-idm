package org.ruchith.research.idm.idp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

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
		String claimName = br.readLine().trim();

		if (claimName == null || claimName.trim().length() == 0) {
			System.out.println("Claim name cannot be empty!");
		}

		System.out.println("Claim description (Optional):");
		String claimDesc = br.readLine().trim();

		System.out.println("Creating claim definition : " + claimName + " ...");

		IdentityManager manager = new IdentityManager();
		manager.generateNewClaimDefinition(claimName, claimDesc);

		System.out.println("Claim definition added successfully!");
		System.out.println("List of available claims:");
		List<String> claims = manager.getAllClaimNames();
		for (Iterator<String> iterator = claims.iterator(); iterator.hasNext();) {
			System.out.println((String) iterator.next());
		}
	}
}
