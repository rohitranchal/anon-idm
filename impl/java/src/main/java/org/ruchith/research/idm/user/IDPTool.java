package org.ruchith.research.idm.user;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.ruchith.research.idm.IdentityClaimDefinition;

/**
 * This allows the user to interact with the identity provider.
 * <ul>
 * <li>List available claim definitions</li>
 * <li>Request a claim</li>
 * </ul>
 * 
 * @author Ruchith Fernando
 * 
 */
public class IDPTool {

	public static void main(String[] args) {
		if (args.length == 0) {
			// Print usage and exit
			printUsage();
			System.exit(0);
		}

		// Get idp url
		String idpUrl = null;
		String claimName = null;
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals("-url")) {
				idpUrl = args[i + 1];
			} else if (args[i].equals("-claim")) {
				claimName = args[i + 1];
			}
		}
		
		if(idpUrl == null) {
			System.out.println("-url Missing!");
			printUsage();
			System.exit(0);
		}

		IdentityProviderConnection conn = IdentityProviderConnectionFactory
				.getConnection(IdentityProviderConnectionFactory.IDP_CONN_TYPE_HTTP_JSON);

		Properties configuration = new Properties();
		configuration.put("claims_url", idpUrl + "/claims/");
		configuration.put("cert_url", idpUrl + "/cert/");
		boolean connResult = conn.connect(configuration);
		if (connResult) {
			String action = args[0];
			if (action.equals("-list")) {
				Collection<IdentityClaimDefinition> defs = conn.getAllClaimDefinitions();
				for (Iterator iterator = defs.iterator(); iterator.hasNext();) {
					IdentityClaimDefinition identityClaimDefinition = (IdentityClaimDefinition) iterator.next();
					System.out.println(identityClaimDefinition.getName());
				}
			}

		} else {
			System.out.println("Error connecting to IDP : " + idpUrl);
		}

	}

	public static void printUsage() {
		System.out.println("Usage:");
		System.out.println("idptool -list -url <idp url>");
		System.out.println("idptool -req_claim -url <idp url> -claim <claim name>");
	}

}
