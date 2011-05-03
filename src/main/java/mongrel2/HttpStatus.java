package mongrel2;

/**
 * HTTP status codes with accompanying reason phrases.
 * 
 * @author Karl Ostendorf
 *
 */
public enum HttpStatus {

	Continue(100, "Continue"),
	SwitchingProtocols(101, "Switching Protocols"),

	OK(200, "OK"),
	Created(201, "Created"),
	Accepted(202, "Accepted"),
	NonAuthoritativeInformation(203, "Non-Authoritative Information"),
	NoContent(204, "No Content"),
	ResetContent(205, "Reset Content"),
	PartialContent(206, "Partial Content"),

	MultipleChoices(300, "Multiple Choices"),
	MovedPermanently(301, "Moved Permanently"),
	Found(302, "Found"),
	SeeOther(303, "See Other"),
	NotModified(304, "Not Modified"),
	UseProxy(305, "Use Proxy"),
	TemporaryRedirect(307, "Temporary Redirect"),

	BadRequest(400, "Bad Request"),
	Unauthorized(401, "Unauthorized"),
	PaymentRequired(402, "Payment Required"),
	Forbidden(403, "Forbidden"),
	NotFound(404, "Not Found"),
	MethodNotAllowed(405, "Method Not Allowed"),
	NotAcceptable(406, "Not Acceptable"),
	ProxyAuthenticationRequired(407, "Proxy Authentication Required"),
	RequestTimeout(408, "Request Time-out"),
	Conflict(409, "Conflict"),
	Gone(410, "Gone"),
	LengthRequired(411, "Length Required"),
	PreconditionFailed(412, "Precondition Failed"),
	RequestEntityTooLarge(413, "Request Entity Too Large"),
	RequestURITooLarge(414, "Request-URI Too Large"),
	UnsupportedMediaType(415, "Unsupported Media Type"),
	RequestedRangeNotSatisfiable(416, "Requested range not satisfiable"),
	ExpectationFailed(417, "Expectation Failed"),

	InternalServerError(500, "Internal Server Error"),
	NotImplemented(501, "Not Implemented"),
	BadGateway(502, "Bad Gateway"),
	ServiceUnavailable(503, "Service Unavailable"),
	GatewayTimeout(504, "Gateway Time-out"),
	HttpVersionNotSupported(505, "HTTP Version not supported");

	/**
	 * Returns the HttpStatus instance for a given HTTP status code.
	 * 
	 * @param code
	 *            HTTP status code
	 * @return HttpStatus instance for the given code, or null if not found.
	 */
	public static HttpStatus findByCode(final int code) {
		for (final HttpStatus status : values())
			if (code == status.code)
				return status;
		return null;
	}

	public final int code;
	public final String msg;

	HttpStatus(final int code, final String msg) {
		this.code = code;
		this.msg = msg;
	}

}
