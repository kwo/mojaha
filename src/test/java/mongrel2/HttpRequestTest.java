package mongrel2;

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
		for (@SuppressWarnings("unused")
		final String name : req.getAttributeNames()) {
			counter++;
		}
		Assert.assertEquals(3, counter);

	}

	@Test
	public void testHeaders() throws Exception {

		final HttpRequest req = new HttpRequest();

		// test setting a header
		req.setHeader("h1", "v1");
		Assert.assertEquals("v1", req.getHeader("h1"));
		Assert.assertEquals(1, req.getHeaderValues("h1").length);
		Assert.assertEquals("v1", req.getHeaderValues("h1")[0]);

		// test contains
		Assert.assertTrue(req.containsHeader("h1"));
		Assert.assertFalse(req.containsHeader("h0"));

		// non-existent headers are null
		Assert.assertNull(req.getHeader("h2"));

		// test setting a 2d header
		req.setHeader("h2", "v2");
		Assert.assertEquals("v2", req.getHeader("h2"));
		Assert.assertEquals(1, req.getHeaderValues("h2").length);
		Assert.assertEquals("v2", req.getHeaderValues("h2")[0]);
		Assert.assertTrue(req.containsHeader("h2"));

		// test adding to a header
		req.addHeader("h2", "v2-2");
		Assert.assertEquals("v2", req.getHeader("h2"));
		Assert.assertEquals(2, req.getHeaderValues("h2").length);
		Assert.assertEquals("v2", req.getHeaderValues("h2")[0]);
		Assert.assertEquals("v2-2", req.getHeaderValues("h2")[1]);

		// test overwriting a header
		req.setHeader("h2", "v3");
		Assert.assertEquals("v3", req.getHeader("h2"));
		Assert.assertEquals(1, req.getHeaderValues("h2").length);
		Assert.assertEquals("v3", req.getHeaderValues("h2")[0]);

		// test initializing a header with add
		req.addHeader("h3", "v4");
		Assert.assertEquals("v4", req.getHeader("h3"));
		Assert.assertEquals(1, req.getHeaderValues("h3").length);
		Assert.assertEquals("v4", req.getHeaderValues("h3")[0]);
		Assert.assertTrue(req.containsHeader("h3"));

		int counter = 0;
		for (@SuppressWarnings("unused")
		final String name : req.getHeaderNames()) {
			counter++;
		}
		Assert.assertEquals(3, counter);

	}

}
