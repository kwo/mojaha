package mongrel2;

import java.util.Enumeration;

import junit.framework.Assert;

import org.junit.Test;

public class HttpRequestTest {

	@Test
	public void testAttributes() throws Exception {

		final HttpRequest req = new HttpRequest();

		req.setSenderAddr("addr1");
		req.setRequestId("10");
		req.setAttribute("a1", "a2");

		Assert.assertEquals("addr1", req.getSenderAddr());
		Assert.assertEquals("10", req.getRequestId());
		Assert.assertEquals("a2", req.getAttribute("a1"));

		int counter = 0;
		@SuppressWarnings("rawtypes")
		final Enumeration e = req.getAttributeNames();
		while (e.hasMoreElements()) {
			counter++;
			e.nextElement();
		}
		Assert.assertEquals(3, counter);

	}

}
