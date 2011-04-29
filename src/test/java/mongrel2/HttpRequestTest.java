package mongrel2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

	@Test
	public void testParse() throws Exception {

		final byte[] mongrel2RequestMessage = getResourceAsByteArray("/test-request.txt");
		final HttpRequest req = HttpRequest.parse(mongrel2RequestMessage);

		// ----- ATTRIBUTES -----

		// sender addr
		Assert.assertEquals("54c6755b-9628-40a4-9a2d-cc82a816345e", req.getAttribute(HttpRequest.ATTR_SENDER_ADDR));
		Assert.assertEquals(req.getAttribute(HttpRequest.ATTR_SENDER_ADDR), req.getSenderAddr());

		// request id
		Assert.assertEquals("0", req.getAttribute(HttpRequest.ATTR_REQUEST_ID));
		Assert.assertEquals(req.getAttribute(HttpRequest.ATTR_REQUEST_ID), req.getRequestId());

		// ----- HEADERS -----

		// TODO: request url has no direct match
		// Assert.assertEquals("http://localhost:6767//search/for/a/string",
		// req.getRequestURL());

		// URI has no direct match
		Assert.assertEquals("/search/for/a/string?p1=33&p1=22&p2=hello", req.getHeader("URI"));

		// PATH == requestURI
		Assert.assertEquals("/search/for/a/string", req.getHeader("PATH"));
		Assert.assertEquals(req.getHeader("PATH"), req.getRequestURI());

		// PATTERN == servlet path
		Assert.assertEquals("/search/", req.getHeader("PATTERN"));
		Assert.assertEquals(req.getHeader("PATTERN"), req.getServletPath());

		// path info, no direct match, is requestURI without servletPath
		Assert.assertEquals("for/a/string", req.getPathInfo());
		Assert.assertEquals(req.getPathInfo(), req.getRequestURI().substring(req.getServletPath().length()));

		// VERSION == protocol
		Assert.assertEquals("HTTP/1.1", req.getHeader("VERSION"));
		Assert.assertEquals(req.getHeader("VERSION"), req.getProtocol());

		// METHOD == method
		Assert.assertEquals("GET", req.getHeader("METHOD"));
		Assert.assertEquals(req.getHeader("METHOD"), req.getMethod());

		// HOST has no direct match
		Assert.assertEquals("localhost:6767", req.getHeader("HOST"));
		Assert.assertEquals("localhost", req.getHost());
		Assert.assertEquals(6767, req.getPort());

		// content-length
		Assert.assertEquals(0, req.getIntHeader("Content-Length"));
		Assert.assertEquals(req.getIntHeader("Content-Length"), req.getContentLength());

		// content-type
		Assert.assertNull(req.getHeader("Content-Type"));
		Assert.assertEquals(req.getHeader("Content-Type"), req.getContentType());

		// content
		Assert.assertNotNull(req.getContent());
		Assert.assertEquals(0, req.getContent().length);

		// QUERY == queryString
		Assert.assertEquals("p1=33&p1=22&p2=hello", req.getHeader("QUERY"));
		Assert.assertEquals(req.getHeader("QUERY"), req.getQueryString());

		Assert.assertTrue(req.containsParameter("p1"));
		Assert.assertEquals(2, req.getParameterValues("p1").length);
		Assert.assertEquals("33", req.getParameter("p1"));
		Assert.assertEquals("33", req.getParameterValues("p1")[0]);
		Assert.assertEquals("22", req.getParameterValues("p1")[1]);

		Assert.assertTrue(req.containsParameter("p2"));
		Assert.assertEquals(1, req.getParameterValues("p2").length);
		Assert.assertEquals("hello", req.getParameter("p2"));
		Assert.assertEquals("hello", req.getParameterValues("p2")[0]);

		// reverse proof for contains header
		Assert.assertFalse(req.containsHeader("bogus-header-name"));
		Assert.assertFalse(req.containsParameter("bogus-param-name"));

		// TODO: case-insensetive header names
		// Assert.assertEquals(req.getHeader("PATTERN"),
		// req.getHeader("pattern"));

	}

	private byte[] getResourceAsByteArray(final String resource) throws IOException {

		int len = 0;
		final byte[] buf = new byte[1024];
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final InputStream in = this.getClass().getResourceAsStream(resource);
		while ((len = in.read(buf)) > -1)
			out.write(buf, 0, len);
		in.close();
		out.close();

		return out.toByteArray();

	}

}
