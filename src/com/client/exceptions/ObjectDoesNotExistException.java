/**
 * @author Bruno Zeraik 
 */

package com.client.exceptions;

public class ObjectDoesNotExistException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1359253055630513156L;

	public ObjectDoesNotExistException() {
		super("Objeto não existe");
	}
	
	public ObjectDoesNotExistException(String message) {
		super(message);
	}
}
