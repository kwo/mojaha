package mongrel2;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

	public static final String ATTR_REQUEST_ID = "request-id";
	public static final String ATTR_SENDER_ADDR = "sender-addr";

	private final Map<String, Object> attributes;

	public HttpRequest() {
		this.attributes = new HashMap<String, Object>();
	}

	public Object getAttribute(final String name) {
		return this.attributes.get(name);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		return new IteratorEnumeration(this.attributes.keySet().iterator());
	}

	public String getRequestId() {
		return (String) getAttribute(ATTR_REQUEST_ID);
	}

	public String getSenderAddr() {
		return (String) getAttribute(ATTR_SENDER_ADDR);
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

}
