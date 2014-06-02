package org.ruchith.research.idm;

import java.util.Date;

import org.junit.Test;
import org.ruchith.research.idm.idp.IdentityManager;

public class TestIdentityManager {
	
	@Test
	public void testCreateIdentityClaim() throws Exception {
		IdentityManager idm = new IdentityManager("/Users/ruchith/Documents/research/anon_idm/source/java/config");
		
		long start = new Date().getTime();
		for(int i = 0; i < 1000; i++) {
			IdentityClaimDefinition cd = idm.generateNewClaimDefinition("Test" + i, "Test Desc");
		}
		long end = new Date().getTime();
		System.out.println("Total time: " +  (end - start));
	}

}
