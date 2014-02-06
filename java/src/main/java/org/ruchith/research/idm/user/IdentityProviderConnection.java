package org.ruchith.research.idm.user;

import it.unisa.dia.gas.jpbc.Element;

import java.security.PrivateKey;
import java.util.Map;
import java.util.Properties;

import org.ruchith.research.idm.IdentityClaim;
import org.ruchith.research.idm.IdentityClaimDefinition;

/**
 * Interface to connect to an identity provider.
 * 
 * @author Ruchith Fernando	
 * 
 */
public interface IdentityProviderConnection {

	/**
	 * Establish the connection and carry out tasks such as obtaining identity
	 * provider policy and public key certificate.
	 * 
	 * @return true on success.
	 */
	public boolean connect(Properties configuration);

	/**
	 * Get the list of all supported claims.
	 * 
	 * @return A collection of supported claims.
	 */
	public Map<String, IdentityClaimDefinition> getAllClaimDefinitions();

	/**
	 * Request claim issuance
	 * 
	 * @param claim
	 *            Requested claim type.
	 * @param privKey
	 *            Private key of the user to sign the request.
	 * @param masterKey
	 *            Master key of the user to create the request.
	 * @return The issued claim instance.
	 */
	public IdentityClaim requestClaim(IdentityClaimDefinition claim,
			PrivateKey privKey, Element masterKey, String user) throws IDPConnectionException;

	/**
	 * Request claim issuance. 
	 * A random id will be created instead of the master key.
	 * 
	 * @param claim
	 *            Requested claim type.
	 * @param privKey
	 *            Private key of the user to sign the request.
	 *            
	 * @return The issued claim instance.
	 */
	public IdentityClaim requestClaim(IdentityClaimDefinition claim,
			PrivateKey privKey, String user) throws IDPConnectionException;
	
}
