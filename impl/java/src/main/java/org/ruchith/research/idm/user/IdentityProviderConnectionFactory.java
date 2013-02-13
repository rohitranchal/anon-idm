package org.ruchith.research.idm.user;
/**
 * 
 * @author Ruchith Fernando
 *
 */
public class IdentityProviderConnectionFactory {

	/**
	 * First(default?) implementation of the identity provider.
	 * 
	 */
	public final static int IDP_CONN_TYPE_HTTP_JSON = 1;

	public static IdentityProviderConnection getConnection(int type) {
		switch (type) {
		case IDP_CONN_TYPE_HTTP_JSON:
			return new BasicIdentityProviderConnection();
		default:
			throw new RuntimeException("Invalid conection type");
		}
	}
}
