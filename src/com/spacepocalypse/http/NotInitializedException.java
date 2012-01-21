package com.spacepocalypse.http;

import com.spacepocalypse.R;

public class NotInitializedException extends Exception {
	private static final long serialVersionUID = 3339256749316952823L;
	
	@Override
	public String getMessage() {
		return "B4WebClient is not initialized. Initialize it with initialize(Context).";
	}		
}
