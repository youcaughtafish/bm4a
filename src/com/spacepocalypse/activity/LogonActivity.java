package com.spacepocalypse.activity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.spacepocalypse.R;
import com.spacepocalypse.beermap2.domain.MappedUser;
import com.spacepocalypse.beermap2.domain.json.JSONException;
import com.spacepocalypse.beermap2.domain.json.JSONObject;
import com.spacepocalypse.beermap2.service.AndroidBeerQueryServlet;
import com.spacepocalypse.http.HttpRestClient;
import com.spacepocalypse.http.HttpRestClient.RequestMethod;
import com.spacepocalypse.utility.security.AeSimpleSHA1;

public class LogonActivity extends Activity {
	private static final String TAG = "LogonActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.login_layout);
        
        if (getIntent().hasExtra(getResources().getString(R.string.user_key))) {
        	MappedUser user = (MappedUser) getIntent().getExtras().getSerializable(getString(R.string.username));
        	
        	String userName = "";
        	
        	if (user != null) {
        		userName = user.getUsername();
        	}
			setUsernameEditText(userName);
        }
        
        setResult(RESULT_CANCELED);
	}


	private void setUsernameEditText(String userName) {
		((EditText)findViewById(R.id.username_edit)).setText(userName);
	}
	

    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication.
     * 
     * @param view The Submit button for which this method is invoked
     */
    public void handleLogin(View view) {
    	// create the rest client
    	HttpRestClient client = new HttpRestClient(this, getString(R.string.service_name_login));
    	
    	// add the username parameter (from the username_edit edittext field)
    	final String username = ((EditText)findViewById(R.id.username_edit)).getText().toString();
		client.addParam(AndroidBeerQueryServlet.KEY_USERNAME, username);
    	
		// get the password parameter (from the password_edit edittext field)
		final String password = ((EditText)findViewById(R.id.password_edit)).getText().toString();
		
		// SHA1 hash the password
    	String hashPass = null;
    	try {
			hashPass = AeSimpleSHA1.SHA1(password);

    	} catch (Exception e) {
			Log.e(TAG, "Error while attempting to hash password", e);
		}
		
		// add hashed password to params
		client.addParam(AndroidBeerQueryServlet.KEY_PASSWORD, hashPass);
		
		// execute request and get response
    	client.execute(RequestMethod.POST);
    	String response = client.getResponse();
    	
    	// check for successful login
    	boolean success = false;
    	try {
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.has("success")) {
				success = jsonResponse.getBoolean("success");
			}
			MappedUser user = null;
			long userTimeoutAbsMs = -1;
			if (success) {
				if (jsonResponse.has("timeoutAbsMs")) {
					userTimeoutAbsMs = jsonResponse.getLong("timeoutAbsMs");
				}
				
				if (jsonResponse.has("user")) {
					user = MappedUser.createMappedUser(jsonResponse.getJSONObject("user"));
				}
				
				if (user != null && userTimeoutAbsMs > System.currentTimeMillis()) {
					storeCredentialsToFile(jsonResponse.toString(), getResources().getString(R.string.credentialsFilename));
				}					
				
			}
		} catch (Exception e) {
			Log.e(TAG, "Error while handling login response.", e);
		}
		
		// login success
    	if (success) {
    		setResult(RESULT_OK, new Intent(this, BeerMap4AndroidMain.class));
    		finish();
    	} 
    	// login fail
    	else {
    		Toast.makeText(this, "Wrong Username or Password!", Toast.LENGTH_LONG).show();
    	}
    }


	private void storeCredentialsToFile(String stringToWrite, String fileName)
			throws JSONException {
		FileOutputStream fos = null;
		try {
			fos = openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write(stringToWrite.getBytes());
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
	}

}
