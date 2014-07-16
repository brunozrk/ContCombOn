/**
 * @author Bruno Zeraik 
 */

package com.client.exceptions;

public class ValidationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7776299629075741457L;

	public ValidationException() {
		super("Ocorreu um erro de validação");
	}
	
	public ValidationException(String message) {
		super(message);
	}
}
