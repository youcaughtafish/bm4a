package com.spacepocalypse.activity;

import java.io.Serializable;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.spacepocalypse.R;
import com.spacepocalypse.app.BeerMap4AndroidApp;
import com.spacepocalypse.beermap2.domain.MappedUser;
import com.spacepocalypse.beermap2.domain.json.JSONObject;
import com.spacepocalypse.beermap2.service.Constants;
import com.spacepocalypse.http.HttpRestClient;
import com.spacepocalypse.http.HttpRestClient.RequestMethod;

public class LogonActivity extends Activity {
	private static final String TAG = "LogonActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.login_layout);
        
        if (getIntent().getExtras() != null) {
	        Serializable userObj = getIntent().getExtras().getSerializable(getString(R.string.user_key));
	        
	        if (userObj != null && userObj instanceof MappedUser) {
	        	MappedUser user = (MappedUser) userObj;
	
	        	String userName = "";
	
	        	if (user != null) {
	        		userName = user.getUsername();
	        	}
	        	
	        	setUsernameEditText(userName);
	        }
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
		client.addParam(Constants.KEY_USERNAME, username);
    	
		// get the password parameter (from the password_edit edittext field)
		final String password = ((EditText)findViewById(R.id.password_edit)).getText().toString();
		
		// add hashed password to params
		client.addParam(Constants.KEY_PASSWORD, password);
		
		// execute request and get response
    	client.execute(RequestMethod.POST);
    	String response = client.getResponse();
    	
    	// check for successful login
    	boolean success = false;
    	try {
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.has(Constants.RESULT_SUCCESS)) {
				success = jsonResponse.getBoolean(Constants.RESULT_SUCCESS);
			}
			MappedUser user = null;
			long userTimeoutAbsMs = -1;
			if (success) {
				if (jsonResponse.has(Constants.KEY_TIMEOUT_MS)) {
					userTimeoutAbsMs = jsonResponse.getLong(Constants.KEY_TIMEOUT_MS);
				}
				
				if (jsonResponse.has(Constants.KEY_USER)) {
					user = MappedUser.createMappedUser(jsonResponse.getJSONObject(Constants.KEY_USER));
				}
				
				if (user != null && userTimeoutAbsMs > System.currentTimeMillis()) {
					BeerMap4AndroidApp.getInstance().storeCredentialsToFile(jsonResponse.toString());
					
					final BeerMap4AndroidApp app = BeerMap4AndroidApp.getInstance();
					app.setUser(user);
					app.setTimeoutTimeAbsMs(userTimeoutAbsMs);
				}					
				
			}
		} catch (Exception e) {
			Log.e(TAG, "Error while handling login response.", e);
		}
		
		// login success
    	if (success) {
    		setResult(RESULT_OK, getIntent());
    		finish();
    	} 
    	// login fail
    	else {
    		Toast.makeText(this, "Wrong Username or Password!", Toast.LENGTH_LONG).show();
    	}
    }
}
