package org.ruchith.research.idm;

import java.io.InputStream;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

public class TestIdentityClaim {

	@Test
	public void testDeserialization() throws Exception {
		InputStream in = getClass().getResourceAsStream("/wallet/gE6SboKAyvKAUw9QXbC6NuwcJZe9YjObnqkx618HAW5EaYgrTxcjQqbOw49LcoQgFa6WcHItQUEcFyizABgw");
		
		String content = "";
		byte[] data = new byte[1024];
		int count = 0;
		while((count = in.read(data)) > 0) {
			content += new String(data, 0, count);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode on = (ObjectNode) mapper.readTree(content.toString());
		
		IdentityClaim claim = new IdentityClaim(on);
		Assert.assertEquals("professor", claim.getDefinition().getName());
	}

}
