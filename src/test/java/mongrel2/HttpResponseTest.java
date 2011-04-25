package mongrel2;

import org.junit.Assert;
import org.junit.Test;

public class HttpResponseTest {

	@Test
	public void testHeaders() throws Exception {

		final HttpResponse rsp = new HttpResponse();

		// test setting a header
		rsp.setHeader("h1", "v1");
		Assert.assertEquals("v1", rsp.getHeader("h1"));
		Assert.assertEquals(1, rsp.getHeaderValues("h1").length);
		Assert.assertEquals("v1", rsp.getHeaderValues("h1")[0]);

		Assert.assertTrue(rsp.containsHeader("h1"));
		Assert.assertTrue(!rsp.containsHeader("h0"));

		// test setting a 2d header
		rsp.setHeader("h2", "v2");
		Assert.assertEquals("v2", rsp.getHeader("h2"));
		Assert.assertEquals(1, rsp.getHeaderValues("h2").length);
		Assert.assertEquals("v2", rsp.getHeaderValues("h2")[0]);
		Assert.assertTrue(rsp.containsHeader("h2"));

		// test adding to a header
		rsp.addHeader("h2", "v2-2");
		Assert.assertEquals("v2", rsp.getHeader("h2"));
		Assert.assertEquals(2, rsp.getHeaderValues("h2").length);
		Assert.assertEquals("v2", rsp.getHeaderValues("h2")[0]);
		Assert.assertEquals("v2-2", rsp.getHeaderValues("h2")[1]);

		// test overwriting a header
		rsp.setHeader("h2", "v3");
		Assert.assertEquals("v3", rsp.getHeader("h2"));
		Assert.assertEquals(1, rsp.getHeaderValues("h2").length);
		Assert.assertEquals("v3", rsp.getHeaderValues("h2")[0]);

		// test initializing a header with add
		rsp.addHeader("h3", "v4");
		Assert.assertEquals("v4", rsp.getHeader("h3"));
		Assert.assertEquals(1, rsp.getHeaderValues("h3").length);
		Assert.assertEquals("v4", rsp.getHeaderValues("h3")[0]);
		Assert.assertTrue(rsp.containsHeader("h3"));

		int counter = 0;
		for (@SuppressWarnings("unused")
		final String name : rsp.getHeaderNames()) {
			counter++;
		}
		Assert.assertEquals(3, counter);

	}

}
