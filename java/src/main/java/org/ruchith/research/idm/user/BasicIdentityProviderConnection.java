package org.ruchith.research.idm.user;

import it.unisa.dia.gas.jpbc.Element;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.research.idm.IdentityClaim;
import org.ruchith.research.idm.IdentityClaimDefinition;

/**
 * This connection assumes the default implementation of the IDP. This implementation is available at :
 * https://anon-idm.googlecode.com/svn/trunk/impl/js/idp
 * 
 * @author Ruchith Fernando
 * 
 */
public class BasicIdentityProviderConnection implements IdentityProviderConnection {

	private Certificate idpCert;
	private HashMap<String, IdentityClaimDefinition> claims = new HashMap<String, IdentityClaimDefinition>();
	private String claimServiceUrl;

	public boolean connect(Properties configuration) {

		String claimsUrl = configuration.getProperty("claims_url");
		String certUrl = configuration.getProperty("cert_url");
		this.claimServiceUrl = configuration.getProperty("claim_service_url");

		try {

			String cert = readUrlContent(certUrl);
			CertificateFactory factory2 = CertificateFactory.getInstance("X.509");
			this.idpCert = factory2.generateCertificate(new ByteArrayInputStream(cert.getBytes()));

			String content = readUrlContent(claimsUrl);

			MessageDigest dgst = MessageDigest.getInstance("SHA-512");
			Signature sig = Signature.getInstance("SHA512withRSA");

			ObjectMapper mapper = new ObjectMapper();
			ArrayNode an = (ArrayNode) mapper.readTree(content.toString());

			Iterator<JsonNode> elements = an.getElements();
			while (elements.hasNext()) {
				ObjectNode node = (ObjectNode) elements.next();
				String name = node.get("name").asText();
				String params = node.get("params").asText();
				String b64Dgst = node.get("dgst").asText();
				String b64Sig = node.get("sig").asText();
				String b64Cert = node.get("cert").asText();
				// String createDate = node.get("DateCreated").asText();

				ObjectNode on = (ObjectNode) mapper.readTree(Base64.decode(params));
				AEParameters aeParams = new AEParameters(on);

				IdentityClaimDefinition tmpDef = new IdentityClaimDefinition(name, aeParams);

				// Verify dgst
				byte[] contentBytes = tmpDef.getDgstContet().getBytes();

				dgst.reset();
				dgst.update(contentBytes);
				String newDgstVal = new String(Base64.encode(dgst.digest()));

				if (!newDgstVal.equals(b64Dgst)) {
					// TODO : Log error
					return false;
				}

				// Verify signature
				sig.initVerify(this.idpCert);
				sig.update(contentBytes);
				if (!sig.verify(Base64.decode(b64Sig))) {
					// TODO : Log error
					return false;
				}

				tmpDef.setB64Hash(b64Dgst);
				tmpDef.setB64Sig(b64Sig);

				ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(b64Cert));
				tmpDef.setCert(CertificateFactory.getInstance("X.509").generateCertificate(bais));

				this.claims.put(name, tmpDef);
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

	private String readUrlContent(String claimsUrl) throws IOException, MalformedURLException {
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

	public Map<String, IdentityClaimDefinition> getAllClaimDefinitions() {
		return this.claims;
	}

	/**
	 * Request a new key with the given master key.
	 */
	public IdentityClaim requestClaim(IdentityClaimDefinition claim, PrivateKey privKey, Element masterKey, String user)
			throws IDPConnectionException {

		HashMap<String, String> values = new HashMap<String, String>();

		// {h_1}^{I_1}
		Element req = claim.getParams().getH1().powZn(masterKey);

		// TODO:Sign and Encrypt

		// Request parameters
		values.put("claim", claim.getName());
		values.put("user", user);
		values.put("anonId", new String(Base64.encode(req.toBytes())));

		try {
			String resp = this.postRequest(values, this.claimServiceUrl);
			// B64 decode
			String issuedClaimStr = new String(Base64.decode(resp));
			// JSON decode
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode tmpOn = (ObjectNode) mapper.readTree(issuedClaimStr);
			// Create an instanc of AEPrivateKey which is the issued claim
			AEPrivateKey pk = new AEPrivateKey(tmpOn, claim.getParams().getPairing());

			// Create identity claim instance
			IdentityClaim issuedClaim = new IdentityClaim();
			issuedClaim.init(claim);
			issuedClaim.setClaim(pk);

			return issuedClaim;
		} catch (Exception e) {
			throw new IDPConnectionException(e);
		}

	}

	/**
	 * Request a new claim with a randomly generated master key. Master key here is the identity I1 of the system.
	 */
	public IdentityClaim requestClaim(IdentityClaimDefinition claim, PrivateKey privKey, String user, String i1Val)
			throws IDPConnectionException {
		AEParameters params = claim.getParams();
		Element i1 = null;
		if (i1Val != null) {
			i1 = params.getPairing().getZr().newElement();
			i1.set(new BigInteger(i1Val));
		} else {
			i1 = params.getPairing().getZr().newRandomElement();
		}

		IdentityClaim issuedClaim = this.requestClaim(claim, privKey, i1, user);
		issuedClaim.setClaimKey(i1); // Add the claim key value

		return issuedClaim;
	}

	/**
	 * Generate an HTTP POST request to the given URL.
	 * 
	 * @param values
	 *            Key value pairs to be sent as post params.
	 * @param to
	 *            URL to make the POST to.
	 * @return Value return from the POST.
	 * @throws Exception
	 */
	private String postRequest(Map<String, String> values, String to) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(to);

		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		Iterator<String> keys = values.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			params.add(new BasicNameValuePair(key, values.get(key)));
		}
		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		// Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();

		String ret = null;
		if (entity != null) {
			InputStream instream = entity.getContent();
			try {
				byte[] data = new byte[1024];
				int len = -1;
				while ((len = instream.read(data)) > 0) {
					if (ret == null) {
						ret = new String(data, 0, len);
					} else {
						ret += new String(data, 0, len);
					}
				}
			} finally {
				instream.close();
			}
		}
		return ret;
	}

}
