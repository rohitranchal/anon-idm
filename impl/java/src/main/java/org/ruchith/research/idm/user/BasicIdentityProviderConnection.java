package org.ruchith.research.idm.user;

import it.unisa.dia.gas.jpbc.Element;

import java.security.PrivateKey;
import java.util.Collection;
import java.util.Properties;

import org.ruchith.research.idm.IdentityClaim;
import org.ruchith.research.idm.IdentityClaimDefinition;
/**
 * 
 * @author Ruchith Fernando	
 *
 */
public class BasicIdentityProviderConnection implements
		IdentityProviderConnection {

	@Override
	public boolean connect(Properties configuration) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<IdentityClaimDefinition> getAllClaimDefinitions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IdentityClaim requestClaim(IdentityClaimDefinition claim,
			PrivateKey privKey, Element masterKey) {
		// TODO Auto-generated method stub
		return null;
	}

}
