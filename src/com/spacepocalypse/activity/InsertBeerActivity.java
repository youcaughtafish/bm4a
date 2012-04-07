package com.spacepocalypse.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.spacepocalypse.R;
import com.spacepocalypse.app.BeerMap4AndroidApp;
import com.spacepocalypse.beermap2.domain.MappedBeer;
import com.spacepocalypse.beermap2.domain.MappedBrewery;
import com.spacepocalypse.beermap2.domain.json.JSONObject;
import com.spacepocalypse.beermap2.service.Constants;
import com.spacepocalypse.http.HttpRestClient;
import com.spacepocalypse.http.HttpRestClient.RequestMethod;
import com.spacepocalypse.util.Conca;

public class InsertBeerActivity extends Activity {
	private static String TAG = "InsertBeerActivity";
	
	private MappedBrewery selectedBrewery;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.insert_layout);
		
		final EditText breweryEditText = (EditText)findViewById(R.id.brewery_edit_text);
		breweryEditText.setKeyListener(null);  // make it uneditable
		breweryEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEditBrewery();
            }
        });

		final Button breweryEditBtn = (Button)findViewById(R.id.edit_brewery_btn);
		breweryEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEditBrewery();
            }
        });
		
		final EditText abvEdit = (EditText)findViewById(R.id.insertLayout_abv_edit);
		final String abvText = abvEdit.getText().toString();
		if (abvText.isEmpty()) {
		    abvEdit.setText("0");
		}
		
		Button saveBtn = (Button)findViewById(R.id.insertLayout_saveButton);
		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MappedBeer toInsert = new MappedBeer();

				try {
					toInsert.setAbv(Double.valueOf(abvEdit.getText().toString()).floatValue());
				} catch (NumberFormatException e) {
					runOnUiThread(
						new Runnable() {
							@Override
							public void run() {
								Toast.makeText(InsertBeerActivity.this,
										"Abv is not a decimal number!",
										Toast.LENGTH_LONG).show();
							}
						}
					);
					return;
				}
				
				EditText nameEdit = (EditText)findViewById(R.id.insertLayout_name_edit);
				toInsert.setName(nameEdit.getText().toString());

				EditText descriptEdit = (EditText)findViewById(R.id.insertLayout_descript_edit);
				toInsert.setDescript(descriptEdit.getText().toString());
				
				toInsert.setBrewery(selectedBrewery);
				
				final HttpRestClient httpRestClient = new HttpRestClient(InsertBeerActivity.this, getString(R.string.service_name_beerinsert));

				httpRestClient.addParam(Constants.KEY_MAPPED_BEER, new JSONObject(toInsert).toString());
				httpRestClient.addParam(Constants.KEY_USER_ID, String.valueOf(BeerMap4AndroidApp.getInstance().getUser().getId()));

				try {
					httpRestClient.execute(RequestMethod.POST);
					
					final String response = httpRestClient.getResponse();
					JSONObject result = new JSONObject(response);
					
					boolean success = false;
					if (result.has(Constants.KEY_BM4A_JSON_RESULT)) {
						success = result.getBoolean(Constants.KEY_BM4A_JSON_RESULT);
					}
					
					if (!success) {
						runOnUiThread(
							new Runnable() {
								@Override
								public void run() {
									Toast.makeText(InsertBeerActivity.this,
											"An error occurred while attempting to insert.",
											Toast.LENGTH_LONG).show();
								}
							}
						);
						return;
					}
					
					setResult(RESULT_OK);
					finish();
					
				} catch (Exception e) {
					Log.e(TAG, Conca.t("Error occurred while attempting to insert beer [", toInsert.toString(), "]"), e);
				}
			}
		});
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == getResources().getInteger(R.integer.brewerySearchRequestCode)) {
	        final String resultKey = getString(R.string.brewery_result_key);
            
	        if (resultCode == RESULT_OK && data.hasExtra(resultKey)) {
	            selectedBrewery = (MappedBrewery)data.getSerializableExtra(resultKey);
	            
	            runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (selectedBrewery != null) {
                            ((EditText)findViewById(R.id.brewery_edit_text)).setText(selectedBrewery.getName());
                        }
                    }
                });
	        }
	    }
	}

    private void handleEditBrewery() {
        final Intent intent = new Intent(this, FindBreweryActivity.class);
        startActivityForResult(intent, getResources().getInteger(R.integer.brewerySearchRequestCode));
    }
}
