/**
 * @author Bruno Zeraik 
 */

package com.client.exceptions;

public class InvalidCredentialsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7776299629075741457L;

	public InvalidCredentialsException() {
		super("Credenciais inválidas");
	}
	
	public InvalidCredentialsException(String message) {
		super(message);
	}
}
