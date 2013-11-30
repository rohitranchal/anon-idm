package org.ruchith.research.idm.user;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.ruchith.research.idm.IdentityClaim;

public class TestClaimWallet {

	@Test
	public void testNumClaims() throws Exception {
		String path = TestClaimWallet.class.getResource("/").getPath();
		ClaimWallet wallet = ClaimWallet.getInstance(path + File.separator + "wallet");

		Assert.assertEquals(2, wallet.getClaimCount());
	}

	public void testGetClaim() throws Exception {
		String path = TestClaimWallet.class.getResource("/").getPath();
		ClaimWallet wallet = ClaimWallet.getInstance(path + File.separator + "wallet");

		IdentityClaim claim = wallet
				.getClaim("gE6SboKAyvKAUw9QXbC6NuwcJZe9YjObnqkx618HAW5EaYgrTxcjQqbOw49LcoQgFa6WcHItQUEcFyizABgw");
		Assert.assertEquals("professor", claim.getDefinition().getName());
		
		claim = wallet
				.getClaim("UZCkEz93FEEZnRQg2wOfuWuJ02bUbJGS5vocsgK3aBeZqSC3bOywyuALFNtlEreSlmt4wWIqqPoYjLzWQoQIw");
		Assert.assertEquals("student", claim.getDefinition().getName());
	}
}
