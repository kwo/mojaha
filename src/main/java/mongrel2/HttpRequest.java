package mongrel2;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest extends BaseHttpObject {

	public static final String ATTR_REQUEST_ID = "request-id";
	public static final String ATTR_SENDER_ADDR = "sender-addr";

	private final Map<String, Object> attributes;
	private String servletPath = null;

	public HttpRequest() {
		this.attributes = new HashMap<String, Object>();
	}

	public Object getAttribute(final String name) {
		return this.attributes.get(name);
	}

	public Iterable<String> getAttributeNames() {
		return this.attributes.keySet();
	}

	public String getRequestId() {
		return (String) getAttribute(ATTR_REQUEST_ID);
	}

	public String getSenderAddr() {
		return (String) getAttribute(ATTR_SENDER_ADDR);
	}

	public String getServletPath() {
		return this.servletPath;
	}

	public void removeAttribute(final String name) {
		this.attributes.remove(name);
	}

	public void setAttribute(final String name, final Object value) {
		this.attributes.put(name, value);
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
