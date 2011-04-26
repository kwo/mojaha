package mongrel2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class HttpRequest {

	@SuppressWarnings("rawtypes")
	class ArrayEnumeration implements Enumeration {

		private final String[] array;
		private int i = 0;

		public ArrayEnumeration(final String[] array) {
			this.array = array;
		}

		@Override
		public boolean hasMoreElements() {
			return this.i < this.array.length - 1;
		}

		@Override
		public Object nextElement() {
			return this.array[this.i++];
		}

	}

	@SuppressWarnings("rawtypes")
	class IteratorEnumeration implements Enumeration {

		private final Iterator i;

		public IteratorEnumeration(final Iterator i) {
			this.i = i;
		}

		@Override
		public boolean hasMoreElements() {
			return this.i.hasNext();
		}

		@Override
		public Object nextElement() {
			return this.i.next();
		}

	}

	public static final String ATTR_REQUEST_ID = "request-id";
	public static final String ATTR_SENDER_ADDR = "sender-addr";

	private final Map<String, Object> attributes;
	private byte[] content = null;
	private final Map<String, String[]> headers;

	private String servletPath = null;

	public HttpRequest() {
		this.attributes = new HashMap<String, Object>();
		this.headers = new HashMap<String, String[]>();
	}

	public void addHeader(final String name, final String value) {
		if (!containsHeader(name)) {
			setHeader(name, value);
		} else {
			final String[] values = this.headers.get(name);
			final String[] newValues = new String[values.length + 1];
			for (int i = 0; i < values.length; i++)
				newValues[i] = values[i];
			newValues[newValues.length - 1] = value;
			this.headers.put(name, newValues);
		}
	}

	public boolean containsHeader(final String name) {
		return this.headers.containsKey(name);
	}

	public Object getAttribute(final String name) {
		return this.attributes.get(name);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		return new IteratorEnumeration(this.attributes.keySet().iterator());
	}

	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getDateHeader(final String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getHeader(final String name) {
		if (containsHeader(name))
			return getHeaderValues(name)[0];
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getHeaderNames() {
		return new IteratorEnumeration(this.headers.keySet().iterator());
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getHeaders(final String name) {
		if (!containsHeader(name))
			return null;
		return new ArrayEnumeration(this.headers.get(name));
	}

	public String[] getHeaderValues(final String name) {
		return this.headers.get(name);
	}

	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(this.content);
	}

	public int getIntHeader(final String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getParameter(final String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Map getParameterMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getParameterValues(final String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQueryString() {
		// TODO Auto-generated method stub
		return null;
	}

	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRealPath(final String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRequestId() {
		return (String) getAttribute(ATTR_REQUEST_ID);
	}

	public String getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSenderAddr() {
		return (String) getAttribute(ATTR_SENDER_ADDR);
	}

	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getServletPath() {
		return this.servletPath;
	}

	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeAttribute(final String name) {
		this.attributes.remove(name);
	}

	public void setAttribute(final String name, final Object value) {
		this.attributes.put(name, value);
	}

	public void setCharacterEncoding(final String env) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

	}

	public void setContent(final byte[] content) {
		this.content = content;
	}

	public void setHeader(final String name, final String value) {
		this.headers.put(name, new String[] { value });
	}

	public void setRequestId(final String requestId) {
		setAttribute(ATTR_REQUEST_ID, requestId);
	}

	public void setSenderAddr(final String senderAddr) {
		setAttribute(ATTR_SENDER_ADDR, senderAddr);
	}

	public void setServletPath(final String servletPath) {
		this.servletPath = servletPath;
	}

}
