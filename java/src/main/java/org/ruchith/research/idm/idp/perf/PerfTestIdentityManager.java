package org.ruchith.research.idm.idp.perf;

import java.util.Date;

import org.ruchith.research.idm.idp.IdentityManager;

public class PerfTestIdentityManager {

	public static void main(String[] args) throws Exception {

		IdentityManager idm = new IdentityManager(args[0]);

		for (int i = 1; i < 100; i++) {
			long start = new Date().getTime();
			idm.generateNewClaimDefinition("Test:" + i + ":", "Test Desc");
			long end = new Date().getTime();
			System.out.println(i + ":" + (end - start));

		}
	}

}
