package Exceptions;

import java.io.Serializable;

public class WebException extends RuntimeException implements Serializable {

	private static final long serialVersionUID = 5758522610083267292L;

	public WebException(Exception e) {
		super(e);
	}

	public WebException(String string) {
		super(string);
	}
}
