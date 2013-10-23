package org.ruchith.research.idm.user;

public class IDPConnectionException extends Exception {

	private static final long serialVersionUID = -6103697198324963023L;

	public IDPConnectionException(String msg) {
		super(msg);
	}

	public IDPConnectionException(Throwable e) {
		super(e);
	}

	public IDPConnectionException(String msg, Throwable e) {
		super(msg, e);
	}

}
