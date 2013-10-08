package org.ruchith.research.idm.idp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class UserManager {
	
	private static final int NEW_USER = 1;
	private static final int DELETE_USER = 2;
	
	public static void main(String[] args) throws Exception {

		int mode = Integer.parseInt(args[0]);
		
		switch (mode) {
		case NEW_USER:
			newUser();
			break;
		case DELETE_USER:
			deleteUser();
			break;
		default:
			System.out.println("Invalid input!");
			System.exit(0);
		}
		
		

	}

	private static void deleteUser() {
		
	}

	private static void newUser() throws IOException, CertificateException,
			FileNotFoundException, Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("User name:");
		String userName = br.readLine().trim();

		if (userName == null || userName.trim().length() == 0) {
			System.out.println("User name cannot be empty!");
			System.exit(0);
		}
		
		System.out.println("User certificate file path:");
		String certPath = br.readLine().trim();
		
		if (certPath == null || certPath.trim().length() == 0) {
			System.out.println("Certificate path cannot be empty!");
			System.exit(0);
		}
		
		File certFile = new File(certPath);
		if(!certFile.exists()) {
			System.out.println(certPath + " does not exist!");
			System.exit(0);
		}
		
		//Load certificate
		CertificateFactory certFac = CertificateFactory.getInstance("X.509");
		Certificate cert = certFac.generateCertificate(new FileInputStream(certFile));
		
		IdentityManager idm = new IdentityManager();
		idm.addUser(userName, cert);
	}

}
