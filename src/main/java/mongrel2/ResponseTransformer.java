package mongrel2;

import java.io.IOException;

/**
 * 
 * ResponseTransformers can manipulate responses before a response is sent by
 * the handler. They are registered at the handler.
 * 
 * @see HttpHandler#addTransformer(ResponseTransformer)
 * @see HttpHandler#removeTransformer(ResponseTransformer)
 * 
 * @author Karl Ostendorf
 * 
 */
public interface ResponseTransformer {

	/**
	 * Called by the handler before sending a response.
	 * 
	 * @param rsp
	 *            the response to be transformed
	 * 
	 * @throws IOException
	 * 
	 */
	void transform(Response rsp) throws IOException;

}
