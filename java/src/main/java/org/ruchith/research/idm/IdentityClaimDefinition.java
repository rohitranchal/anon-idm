package org.ruchith.research.idm;

import it.unisa.dia.gas.jpbc.Element;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameters;

/**
 * 
 * @author Ruchith Fernando
 * 
 */
public class IdentityClaimDefinition {

	/**
	 * Name of the claim
	 */
	private String name;

	/**
	 * Name of the claim
	 */
	private String description;

	/**
	 * Public Parameters of the claim
	 */
	private AEParameters params;

	/**
	 * Master key associated with this claim. This is used in issuing new claim instances.
	 */
	private Element masterKey;

	/**
	 * SHA 512 digest of the claim definition. SHA512(name||params)
	 */
	private String b64Hash;

	/**
	 * Identity provider's signature on the SHA512 digest. Sig(SHA512(name||params))
	 */
	private String b64Sig;

	/**
	 * PUblic key certificate of issuer.
	 */
	private Certificate cert;

	public IdentityClaimDefinition(ObjectNode on) {
		this.name = on.get("name").getTextValue();
		this.description = on.get("description").getTextValue();
		this.b64Hash = on.get("dgst").getTextValue();
		this.b64Sig = on.get("sig").getTextValue();
		this.params = new AEParameters((ObjectNode)on.get("params"));
	}
	
	public IdentityClaimDefinition(String name, AEParameters params) {
		this.name = name;
		this.params = params;
	}

	public IdentityClaimDefinition(String name, AEParameters params, Element mk) {
		this.name = name;
		this.params = params;
		this.masterKey = mk;
	}

	public String getName() {
		return name;
	}

	public AEParameters getParams() {
		return params;
	}

	public Element getMasterKey() {
		return masterKey;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getB64Hash() {
		return b64Hash;
	}

	public String getB64Sig() {
		return b64Sig;
	}

	public void setB64Hash(String b64Hash) {
		this.b64Hash = b64Hash;
	}

	public void setB64Sig(String b64Sig) {
		this.b64Sig = b64Sig;
	}

	public String getDgstContet() {
		return this.getName() + this.getParams().serializeJSON();
	}

	public Certificate getCert() {
		return cert;
	}

	public void setCert(Certificate cert) {
		this.cert = cert;
	}

	public ObjectNode serializeJSON() {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();
		ObjectNode on = (ObjectNode) rootNode;

		on.put("name", this.name);
		on.put("params", this.params.serializeJSON());
		on.put("description", this.description);
		on.put("dgst", this.b64Hash);
		on.put("sig", this.b64Sig);
		try {
			on.put("cert", new String(Base64.encode(this.cert.getEncoded())));
		} catch (CertificateEncodingException e) {
			throw new RuntimeException(e);
		}

		return on;
	}
}
