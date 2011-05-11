package mongrel2;

import java.io.IOException;

/**
 * 
 * HandlerListeners extend the handler.
 * 
 * @see HttpHandler#addHandlerListener(HandlerListener)
 * @see HttpHandler#removeHandlerListener(HandlerListener)
 * 
 * @author Karl Ostendorf
 * 
 */
public interface HandlerListener {

	/**
	 * Called by the handler before sending a response.
	 * 
	 * @param rsp
	 *            the response to be sent
	 * 
	 * @throws IOException
	 * 
	 */
	void beforeSendResponse(Response rsp) throws IOException;

}
