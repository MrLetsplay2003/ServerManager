package me.mrletsplay.servermanager.util;

import me.mrletsplay.mrcore.misc.FriendlyException;

public class SetupException extends FriendlyException {
	
	private static final long serialVersionUID = 8609674409625919206L;

	public SetupException(Throwable cause) {
		super(cause);
	}
	
	public SetupException(String reason) {
		super(reason);
	}
	
	public SetupException(String reason, Throwable cause) {
		super(reason, cause);
	}

}
