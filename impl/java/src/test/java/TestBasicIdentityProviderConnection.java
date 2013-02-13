import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.ruchith.research.idm.IdentityClaimDefinition;
import org.ruchith.research.idm.user.IdentityProviderConnection;
import org.ruchith.research.idm.user.IdentityProviderConnectionFactory;


public class TestBasicIdentityProviderConnection {

	public static void main(String[] args) {
		IdentityProviderConnection conn = IdentityProviderConnectionFactory.getConnection(IdentityProviderConnectionFactory.IDP_CONN_TYPE_HTTP_JSON);
		Properties configuration = new Properties();
		configuration.put("claims_url", "http://localhost:3000/claims/");
		configuration.put("cert_url", "http://localhost:3000/cert/");
		System.err.println(conn.connect(configuration));
		Collection<IdentityClaimDefinition> defs = conn.getAllClaimDefinitions();
		for (Iterator iterator = defs.iterator(); iterator.hasNext();) {
			IdentityClaimDefinition identityClaimDefinition = (IdentityClaimDefinition) iterator
					.next();
			System.out.println(identityClaimDefinition.getName());
		}
		
	}
}
