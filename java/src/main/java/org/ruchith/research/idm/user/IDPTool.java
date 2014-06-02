package org.ruchith.research.idm.user;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.ruchith.research.idm.IdentityClaim;
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

	private static ClaimWallet claimWallet;
	
	public static void main(String[] args) throws Exception {
		
		claimWallet = ClaimWallet.getInstance();
		
		if (args.length == 0) {
			// Print usage and exit
			printUsage();
			System.exit(0);
		}

		// Get idp url
		String idpUrl = null;
		String claimName = null;
		String action = null;
		String user = null;
		String keystorePath = null;
		String storePass = null;
		String alias = null;
		String keyPass = null;

		for (int i = 0; i < args.length - 1; i++) {
			String curr = args[i];
			String next = args[i + 1];
			if (curr.equals("-url")) {
				idpUrl = next;
			} else if (curr.equals("-claim")) {
				claimName = next;
			} else if (curr.equals("-action")) {
				action = next;
			} else if (curr.equals("-user")) {
				user = next;
			} else if (curr.equals("-keystore")) {
				keystorePath = next;
			} else if (curr.equals("-storepass")) {
				storePass = next;
			} else if (curr.equals("-alias")) {
				alias = next;
			} else if (curr.equals("-keypass")) {
				keyPass = next;
			}
		}

		if (idpUrl == null) {
			System.out.println("-url Missing!");
			printUsage();
			System.exit(0);
		} else if(action == null) {
			System.out.println("-action Missing!");
			printUsage();
			System.exit(0);
		}

		IdentityProviderConnection conn = IdentityProviderConnectionFactory
				.getConnection(IdentityProviderConnectionFactory.IDP_CONN_TYPE_HTTP_JSON);

		Properties configuration = new Properties();
		configuration.put("claims_url", idpUrl + "/claims/");
		configuration.put("cert_url", idpUrl + "/cert/");
		configuration.put("claim_service_url", idpUrl + "/claim_service/");
		boolean connResult = conn.connect(configuration);
		if (connResult) {
			if (action.equals("list")) {
				listClaims(conn);
			} else if (action.equals("req_claim")) {
				reqClaim(conn, claimName, user, keystorePath, storePass, alias, keyPass);
			} else {
				System.out.println("Invalid action : " + action);
			}
		} else {
			System.out.println("Error connecting to IDP : " + idpUrl);
		}

	}

	/**
	 * Request a claim from the IDP and process response. This will connect with
	 * the given IDP and attempt to obtain a claim instance.
	 * 
	 * @param conn
	 *            IDP connection
	 * @param claimName
	 *            Name of the claim
	 * @param user
	 *            Username to be used. This user should be already known to the
	 *            IDP for the claim issuance to be successful.
	 * @param certPath
	 *            Path to the certificate file.
	 */
	private static void reqClaim(IdentityProviderConnection conn, String claimName, String user, String storePath,
			String storePass, String alias, String keyPass) {
		Map<String, IdentityClaimDefinition> claimDefs = conn.getAllClaimDefinitions();

		// Check for the given claim
		if (claimDefs.keySet().contains(claimName)) {

			try {
				// Get user's private key
				KeyStore ks = KeyStore.getInstance("JKS");
				FileInputStream is = new FileInputStream(storePath);
				ks.load(is, storePass.toCharArray());
				Key key = ks.getKey(alias, keyPass.toCharArray());
				
				IdentityClaimDefinition claimDef = claimDefs.get(claimName);				
				IdentityClaim issuedClaim = conn.requestClaim(claimDef, (PrivateKey) key, user);

				System.out.println(issuedClaim.serializeJSON());
				
				claimWallet.storeClaim(issuedClaim);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Invalid claim : " + claimName);
		}

	}

	private static void listClaims(IdentityProviderConnection conn) {
		Map<String, IdentityClaimDefinition> defs = conn.getAllClaimDefinitions();
		for (Iterator<String> iterator = defs.keySet().iterator(); iterator.hasNext();) {
			System.out.println(iterator.next());
		}
	}

	public static void printUsage() {
		System.out.println("Usage:");
		System.out.println("idptool -action list -url <idp url>");
		System.out.println("idptool -action req_claim -url <idp url> -claim <claim name> \n"
				+ "-user <user name> -keystore <keystore file path> \n"
				+ "-storepass <key store password> -alias <alias of private key> \n"
				+ "-keypass <private key password>\n");
	}
}
