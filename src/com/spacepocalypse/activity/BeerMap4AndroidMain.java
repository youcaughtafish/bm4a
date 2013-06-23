package com.spacepocalypse.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.spacepocalypse.R;
import com.spacepocalypse.app.BeerMap4AndroidApp;
import com.spacepocalypse.beermap2.service.Constants;
import com.spacepocalypse.http.HttpRestClient;
import com.spacepocalypse.http.HttpRestClient.RequestMethod;

public class BeerMap4AndroidMain extends Activity  {
	private static final int LOGON_REQUEST_CODE = 0;
	private static final int SEARCH_REQUEST_CODE = 1;
	private static final int INSERT_REQUEST_CODE = 2;
	private static final int MY_BEERS_REQUEST_CODE = 3;
	public static final String TAG = "BeerMap4AndroidMain";
	public static final String RESULTS_KEY = "results";
	
	private ProgressDialog progressDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
        
        initGui();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        final BeerMap4AndroidApp appInstance = BeerMap4AndroidApp.getInstance();
        if (useLogonCacheing()) {
            appInstance.getStoredCredentials();
            
        } else {
            appInstance.deleteStoredCredentials();
        }
        
        checkAuth();
    }

	private void initGui() {
	    runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Button searchBtn = (Button)findViewById(R.id.searchBtn);
                searchBtn.setOnClickListener(createSearchBtnOnClickListener());
                
                final Button insertBtn = (Button)findViewById(R.id.insertBtn);
                insertBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), InsertBeerActivity.class);
                        startActivityForResult(intent, INSERT_REQUEST_CODE);
                    }
                });
                
                final Button myBeersBtn = (Button)findViewById(R.id.main_my_beers_btn);
                myBeersBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleMyBeersBtnClick();
                    }
                });
            }
        });
	}

	private OnClickListener createSearchBtnOnClickListener() {
		return new View.OnClickListener() {
			
			@Override
			public void onClick(final View v) {
				TextView textInput = (TextView)findViewById(R.id.textInputTop);
				final String searchQuery = textInput.getText().toString();
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showDialog(0);
					}
				});
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						final HttpRestClient client = new HttpRestClient(BeerMap4AndroidMain.this, getString(R.string.service_name_beersearch));
						
						client.addParam(Constants.KEY_QUERY, searchQuery);
						client.addParam(Constants.KEY_GET_RESULTS_AS_IDS, Constants.VALUE_TRUE);
						
						try {
							client.execute(RequestMethod.POST);
							
						} catch (Exception e) {
							Log.e(TAG, "Error occurred while searching for beers.", e);
						}

						String response = client.getResponse();
						
						if (response != null) {
							Log.i(TAG, response);
							
						} else {
							Log.w(TAG, "response is null.");
							finish();
							return;
						}
						
						if (getProgressDialog().isShowing()) {
							Intent intent = new Intent(v.getContext(), SearchResultsActivity.class);
							intent.putExtra(RESULTS_KEY, response);
							startActivityForResult(intent, SEARCH_REQUEST_CODE);
						}
					}
				}).start();
				
			}
        };
	}
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	if (progressDialog == null) {
    		setProgressDialog(new ProgressDialog(this));
    		getProgressDialog().setIndeterminate(true);
    		getProgressDialog().setMessage("Querying...");
    	}
    	
    	return getProgressDialog();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
        return false;//true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.logout_menu_item:
//                BeerMap4AndroidApp.getInstance().logout(this);
//                break;
//                
//            default:
                return super.onOptionsItemSelected(item);
//        }
//        
//        return true;
    }
    
	private boolean useLogonCacheing() {
		return getResources().getBoolean(R.bool.useLogonCacheing);
	}
    
    private void checkAuth() {
    	if (System.currentTimeMillis() > BeerMap4AndroidApp.getInstance().getTimeoutTimeAbsMs()) {
    		Log.i(TAG, "Timeout occurred. Prompting user to login.");
    		
    		Intent intent = new Intent(this, LogonActivity.class);
    		startActivityForResult(intent, LOGON_REQUEST_CODE);
			
    	} else {
    		Log.i(TAG, "Timeout has not occurred.  User will auto-login.");
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == LOGON_REQUEST_CODE) {
    		if (resultCode != RESULT_OK) {
    			setResult(RESULT_CANCELED);
    			finish();
    		} 
    		
    	} else if (requestCode == SEARCH_REQUEST_CODE) {
    		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (getProgressDialog().isShowing()) {
						dismissDialog(0);
					}
				}
			});
    		
    	} else {
    		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (getProgressDialog().isShowing()) {
						dismissDialog(0);
					}
				}
			});
    	}
    }
    
	public void setProgressDialog(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

	public ProgressDialog getProgressDialog() {
		if (progressDialog == null) {
			onCreateDialog(0);
		}
		return progressDialog;
	}

    private void handleMyBeersBtnClick() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final HttpRestClient client 
                    = new HttpRestClient(BeerMap4AndroidMain.this, getString(R.string.service_name_beersforuserid));
                
                client.addParam(Constants.KEY_USER_ID, String.valueOf(BeerMap4AndroidApp.getInstance().getUser().getId()));
                
                client.execute(RequestMethod.POST);
                
                final String response = client.getResponse();
                
                if (response != null && !response.isEmpty()) {
                    Intent intent = new Intent(BeerMap4AndroidMain.this, SearchResultsActivity.class);
                    intent.putExtra(RESULTS_KEY, response);
                    startActivityForResult(intent, MY_BEERS_REQUEST_CODE);
                }
            }
        }).start();
    }
	
}