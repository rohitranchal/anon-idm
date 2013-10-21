package org.ruchith.research.idm.user;

import it.unisa.dia.gas.jpbc.Element;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.research.idm.IdentityClaim;
import org.ruchith.research.idm.IdentityClaimDefinition;


/**
 * 
 * @author Ruchith Fernando
 * 
 */
public class BasicIdentityProviderConnection implements
		IdentityProviderConnection {

	private Certificate idpCert;
	private ArrayList<IdentityClaimDefinition> claims = new ArrayList<IdentityClaimDefinition>();

	public boolean connect(Properties configuration) {

		String claimsUrl = configuration.getProperty("claims_url");
		String certUrl = configuration.getProperty("cert_url");

		try {

			String cert = readUrlContent(certUrl);
			CertificateFactory factory2 = CertificateFactory
					.getInstance("X.509");
			this.idpCert = factory2
					.generateCertificate(new ByteArrayInputStream(cert
							.getBytes()));

			String content = readUrlContent(claimsUrl);

			MessageDigest dgst = MessageDigest.getInstance("SHA-512");
			Signature sig = Signature.getInstance("SHA512withRSA");
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayNode an = (ArrayNode) mapper.readTree(content.toString());

			Iterator<JsonNode> elements = an.getElements();
			while (elements.hasNext()) {
				ObjectNode node = (ObjectNode) elements.next();
				String name = node.get("Name").asText();
				String params = node.get("PublicParams").asText();
				String b64Dgst = node.get("Digest").asText();
				String b64Sig = node.get("Sig").asText();
				// String createDate = node.get("DateCreated").asText();

				ObjectNode on = (ObjectNode) mapper.readTree(Base64
						.decode(params));
				AEParameters aeParams = new AEParameters(on);

				IdentityClaimDefinition tmpDef = new IdentityClaimDefinition(
						name, aeParams);

				//Verify dgst 
				byte[] contentBytes = tmpDef.getDgstContet().getBytes();
				
				dgst.reset();
				dgst.update(contentBytes);
				String newDgstVal = new String(Base64.encode(dgst.digest()));
				
				if(!newDgstVal.equals(b64Dgst)) {
					//TODO : Log error
					return false;
				}
				
				//Verify signature
				sig.initVerify(this.idpCert);
				sig.update(contentBytes);
				if(!sig.verify(Base64.decode(b64Sig))) {
					//TODO : Log error
					return false;
				}
				
				tmpDef.setB64Hash(b64Dgst);
				tmpDef.setB64Sig(b64Sig);

				
				this.claims.add(tmpDef);
			}

			// Everything went well!
			return true;
		} catch (Exception e) {
			// Swallow for now
			// TODO
			e.printStackTrace();
			return false;
		}
	}

	private String readUrlContent(String claimsUrl) throws IOException,
			MalformedURLException {
		InputStream is = new URL(claimsUrl).openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		StringBuilder sb = new StringBuilder();
		int len;
		char[] buf = new char[1024];
		while ((len = reader.read(buf, 0, 1024)) != -1) {
			sb.append(buf, 0, len);
		}
		return sb.toString();
	}

	public Collection<IdentityClaimDefinition> getAllClaimDefinitions() {
		return this.claims;
	}

	public IdentityClaim requestClaim(IdentityClaimDefinition claim, PrivateKey privKey, Element masterKey) {
		// TODO Auto-generated method stub
		return null;
	}



}
