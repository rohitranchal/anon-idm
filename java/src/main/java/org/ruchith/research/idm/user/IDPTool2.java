package org.ruchith.research.idm.user;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.ruchith.research.idm.IdentityClaim;
import org.ruchith.research.idm.IdentityClaimDefinition;

/**
 * IDPTool v2 which provides static public functions
 * <ul>
 * <li>List available claim definitions</li>
 * <li>Request a claim</li>
 * </ul>
 * 
 * @author Byungchan An
 * 
 */
public class IDPTool2 {

	private static ClaimWallet claimWallet;

	/**
	 * Request a claim from the IDP and process response. This will connect with the given IDP and attempt to obtain a
	 * claim instance.
	 * 
	 * @param conn
	 *            IDP connection
	 * @param claimName
	 *            Name of the claim
	 * @param user
	 *            Username to be used. This user should be already known to the IDP for the claim issuance to be
	 *            successful.
	 * @param certPath
	 *            Path to the certificate file.
	 */
	public static void reqClaim(String idpUrl, String claimName, String user, String storePath,
			String storePass, String alias, String keyPass) throws Exception {
		
		claimWallet = ClaimWallet.getInstance();
		IdentityProviderConnection conn = IdentityProviderConnectionFactory
				.getConnection(IdentityProviderConnectionFactory.IDP_CONN_TYPE_HTTP_JSON);
		boolean ready_to_send = prepare_to_send(conn, idpUrl);
		if(!ready_to_send) {
			throw new Exception("Error in connection in reqClaim");
		}
		
		Map<String, IdentityClaimDefinition> claimDefs = conn.getAllClaimDefinitions();

		// Check for the given claim
		if (claimDefs.keySet().contains(claimName)) {

			try {
				// Get user's private key
				KeyStore ks = KeyStore.getInstance("JKS");
				FileInputStream is = new FileInputStream(storePath);
				ks.load(is, storePass.toCharArray());
				Key key = ks.getKey(alias, keyPass.toCharArray());
				
				long t1 = new Date().getTime();
				IdentityClaimDefinition claimDef = claimDefs.get(claimName);
				IdentityClaim issuedClaim = conn.requestClaim(claimDef, (PrivateKey) key, user, null);

				claimWallet.storeClaim(issuedClaim);
				long t2 = new Date().getTime();
				System.out.println(t2 - t1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Invalid claim : " + claimName);
		}

	}

	public static void listClaims(String idpUrl) throws Exception{
		IdentityProviderConnection conn = IdentityProviderConnectionFactory
				.getConnection(IdentityProviderConnectionFactory.IDP_CONN_TYPE_HTTP_JSON);
		boolean ready_to_send = prepare_to_send(conn, idpUrl);
		if(!ready_to_send) {
			throw new Exception("Error in connection in listClaims");
		}
		
		Map<String, IdentityClaimDefinition> defs = conn.getAllClaimDefinitions();
		for (Iterator<String> iterator = defs.keySet().iterator(); iterator.hasNext();) {
			System.out.println(iterator.next());
		}
	}

	private static boolean prepare_to_send(IdentityProviderConnection conn, String idpUrl) {
		Properties configuration = new Properties();
		configuration.put("claims_url", idpUrl + "/claims/");
		configuration.put("cert_url", idpUrl + "/cert/");
		configuration.put("claim_service_url", idpUrl + "/claim_service/");
		boolean connResult = conn.connect(configuration);
		
		return connResult;
	}
}
