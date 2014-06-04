package org.ruchith.research.idm.idp.perf;

import it.unisa.dia.gas.jpbc.Element;

import java.util.Date;

import org.bouncycastle.util.encoders.Base64;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.research.idm.IdentityClaimDefinition;
import org.ruchith.research.idm.idp.IdentityManager;

public class PerfTestIdentityManager {

	public static void main(String[] args) throws Exception {

		if (args[1].equals("claimdef")) {

			IdentityManager idm = new IdentityManager(args[0]);

			int total = 0;
			for (int i = 0; i < 1001; i++) {
				long start = new Date().getTime();
				idm.generateNewClaimDefinition("Test:" + i + ":", "Test Desc");
				long end = new Date().getTime();
				// System.out.println(i + ":" + (end - start));
				if (i > 0) {
					total += end - start;
				}
			}

			System.out.println("Average:" + (total / 1000));

		} else if (args[1].equals("issueclaim")) {

			IdentityManager idm = new IdentityManager(args[0]);
			IdentityClaimDefinition cd = idm.generateNewClaimDefinition("Test", "Test Desc");
			AEParameters params = cd.getParams();
			double totalReqTime = 0;
			double totalIssueTime = 0;
			for (int i = 1; i < 1001; i++) {
				long t1 = new Date().getTime();

				// User create a new master key
				Element i1 = params.getPairing().getZr().newRandomElement();
				Element req = params.getH1().powZn(i1);

				long t2 = new Date().getTime();
				if (i > 0) {
					totalReqTime += t2 - t1;
				}

				long t3 = new Date().getTime();
				idm.issueClaim("Test", "alice" + i, new String(Base64.encode(req.toBytes())));
				long t4 = new Date().getTime();
				if (i > 0) {
					totalIssueTime += t4 - t3;
				}
			}

			System.out.println("Total request generation time: " + totalReqTime);
			System.out.println("Average request generation time: " + (totalReqTime / 1000));
			System.out.println("Total claim issue time: " + totalIssueTime);
			System.out.println("Average claim issue time: " + (totalIssueTime / 1000));
		}
	}

}
