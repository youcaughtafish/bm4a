package com.spacepocalypse.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spacepocalypse.R;
import com.spacepocalypse.app.BeerMap4AndroidApp;
import com.spacepocalypse.beermap2.domain.MappedBeer;
import com.spacepocalypse.beermap2.domain.MappedBeerRating;
import com.spacepocalypse.beermap2.domain.MappedUser;
import com.spacepocalypse.beermap2.domain.MappedValue;
import com.spacepocalypse.beermap2.domain.json.JSONArray;
import com.spacepocalypse.beermap2.domain.json.JSONException;
import com.spacepocalypse.beermap2.domain.json.JSONObject;
import com.spacepocalypse.beermap2.service.Constants;
import com.spacepocalypse.http.B4AWebClient;
import com.spacepocalypse.http.HttpClient;
import com.spacepocalypse.http.HttpRestClient;
import com.spacepocalypse.http.HttpRestClient.RequestMethod;
import com.spacepocalypse.http.NotInitializedException;
import com.spacepocalypse.util.Conca;

public class DisplaySingleActivity extends Activity {
	private static final String TAG = "DisplaySingleActivity";
//	private static List<MappedValue> cachedValueTypes;
	private boolean editingCurrentBeer;
	private boolean editingComment;
	private MappedBeer thisBeer;
	private MappedBeerRating thisRating;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		if (cachedValueTypes == null) {
//			HttpRestClient client = new HttpRestClient(this, getString(R.string.service_name_all_rating_types));
//			
//			client.execute(RequestMethod.POST);
//			String response = client.getResponse();
//			JSONArray jsonArr = null;
//			
//			try {
//				jsonArr = new JSONArray(response);
//				
//				cachedValueTypes = new ArrayList<MappedValue>();
//				
//				for (int i = 0; i < jsonArr.length(); i++) {
//					cachedValueTypes.add(MappedValue.createMappedValue(jsonArr.getJSONObject(i)));
//				}
//				
//			} catch (Exception e) {
//				Log.w(TAG, Conca.t("Error parsing JSON for rating value types. response=[", response, "]"), e);
//			}
//		}
		
        initGui();
        
        String key = getResources().getString(R.string.displaySingleKey);
        Serializable extra = getIntent().getSerializableExtra(key);
        if (extra == null) {
        	setResult(RESULT_CANCELED);
        	finish();
        }
        
        setThisBeer((MappedBeer)extra);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(getResources().getString(R.string.displaySingle_result_key), getThisBeer());
        setResult(RESULT_OK, resultIntent);
        populateGui();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.display_single_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_edit:
				Button saveBtn = (Button)findViewById(R.id.displaySingle_saveButton);
				if (isEditingCurrentBeer()) {
					saveBtn.setVisibility(View.GONE);
				} else {
					saveBtn.setVisibility(View.VISIBLE);
					copyTextFromTextToEdit();
				}
				toggleVisibility();
				setEditingCurrentBeer(!isEditingCurrentBeer());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void initGui() {
	    runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	            setContentView(R.layout.display_single_layout);
	            final TextView nameTextView = (TextView)findViewById(R.id.displaySingle_beerName);
	            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getInteger(R.integer.displaySingleNameFontSize));
	            nameTextView.setTextColor(getResources().getColor(R.color.nameDisplayColor));

	            final EditText nameEdit = (EditText)findViewById(R.id.displaySingle_name_edit);
	            nameEdit.setVisibility(View.GONE);

	            final EditText abvEdit = (EditText)findViewById(R.id.displaySingle_abv_edit);
	            abvEdit.setVisibility(View.GONE);

	            setEditingCurrentBeer(false);
	            setEditingComment(false);

	            final Button saveBtn = (Button)findViewById(R.id.displaySingle_saveButton);
	            saveBtn.setVisibility(View.GONE);
	            saveBtn.setOnClickListener(new View.OnClickListener() {
	                @Override
	                public void onClick(View v) {
	                    EditText abvEdit = (EditText)findViewById(R.id.displaySingle_abv_edit);
	                    try {
	                        getThisBeer().setAbv(Double.valueOf(abvEdit.getText().toString()).floatValue());

	                    } catch (NumberFormatException e) {
	                        Toast.makeText(DisplaySingleActivity.this,
	                                "Abv is not a decimal number!",
	                                Toast.LENGTH_LONG).show();
	                        return;
	                    }

	                    EditText nameEdit = (EditText)findViewById(R.id.displaySingle_name_edit);
	                    getThisBeer().setName(nameEdit.getText().toString());

	                    EditText descriptEdit = (EditText)findViewById(R.id.displaySingle_descript_edit);
	                    getThisBeer().setDescript(descriptEdit.getText().toString());

	                    JSONObject obj = new JSONObject(getThisBeer());

	                    HttpRestClient client = new HttpRestClient(DisplaySingleActivity.this, getString(R.string.service_name_beerupdate));
	                    client.addParam(Constants.KEY_MAPPED_BEER, obj.toString());

	                    client.execute(RequestMethod.POST);

	                    JSONObject result = null;
	                    try {
	                        result = new JSONObject(client.getResponse());

	                    } catch (Exception e1) {
	                        Log.e(TAG, "Error constructing json result object for beer update", e1);
	                    }

	                    if (result.has(Constants.KEY_BM4A_JSON_RESULT)) {
	                        try {
	                            if (result.getBoolean(Constants.KEY_BM4A_JSON_RESULT)) {

	                                if (isEditingCurrentBeer()) {
	                                    saveBtn.setVisibility(View.GONE);

	                                } else {
	                                    saveBtn.setVisibility(View.VISIBLE);
	                                }

	                                toggleVisibility();
	                                setEditingCurrentBeer(!isEditingCurrentBeer());
	                                copyTextFromEditToText();

	                            } else {
	                                Toast.makeText(DisplaySingleActivity.this,
	                                        "There was a problem storing the changes!",
	                                        Toast.LENGTH_LONG).show();
	                            }

	                        } catch (Exception e) {
	                            Log.e(TAG, "Error retrieving beer update result from json response.", e);
	                        }
	                    }
	                }
	            });

	            final Button editCommentButton = (Button)findViewById(R.id.displaySingle_editCommentButton);
	            editCommentButton.setText(getResources().getString(R.string.displaySingle_editCommentBtn_notedit));
	            editCommentButton.setOnClickListener(new View.OnClickListener() {
	                @Override
	                public void onClick(View v) {

	                    if (isEditingComment()) {
	                        if (getThisRating() == null) {
	                            insertRating();
	                        } else {
	                            updateRating();
	                        }
	                    } else {
	                        TextView textComment = (TextView)findViewById(R.id.displaySingle_rating_comment);
	                        EditText editComment = (EditText)findViewById(R.id.displaySingle_rating_comment_edit);
	                        Button editCommentButton = (Button)findViewById(R.id.displaySingle_editCommentButton);

	                        editCommentButton.setText(getResources().getString(R.string.displaySingle_editCommentBtn_edit));
	                        editComment.setText(textComment.getText());

	                        textComment.setVisibility(View.GONE);
	                        editComment.setVisibility(View.VISIBLE);

	                        RatingBar ratingBar = (RatingBar)findViewById(R.id.displaySingle_ratingBar);
	                        ratingBar.setIsIndicator(false);

	                        setEditingComment(true);
	                    }
	                }


	            });


	            ((RatingBar)findViewById(R.id.displaySingle_ratingBar)).setRating(3f);
	        }
	    });
	}
	
	private void updateRating() {
		Button editCommentButton = (Button)findViewById(R.id.displaySingle_editCommentButton);
		TextView textComment = (TextView)findViewById(R.id.displaySingle_rating_comment);
		EditText editComment = (EditText)findViewById(R.id.displaySingle_rating_comment_edit);
		RatingBar ratingBar = (RatingBar)findViewById(R.id.displaySingle_ratingBar);
		
		MappedBeerRating rating = getThisRating();
		
		rating.setUser(BeerMap4AndroidApp.getInstance().getUser());
		rating.setBeer(getThisBeer());
		
		int ratingBarValue = (int)ratingBar.getRating();
//		for (MappedValue val : cachedValueTypes) {
//			if (val.getValue() == ratingBarValue) {
//				rating.setRatingValue(val);
//				break;
//			}
//		}
		
		rating.setRatingValue(ratingBarValue);
		
		rating.setComment(editComment.getText().toString());
		
		HttpRestClient httpClient = new HttpRestClient(this, getString(R.string.service_name_ratingupdate));
		httpClient.addParam(Constants.KEY_MAPPED_RATING, new JSONObject(rating).toString());
		
		try {
			httpClient.execute(RequestMethod.POST);
			
		} catch (Exception e1) {
			Log.e(TAG, Conca.t("Error when attempting to update rating:[", rating.toString(), "]"), e1);
		}
		
		boolean updateResult = false;
		JSONObject resultObj = null;
		try {
			resultObj = new JSONObject(httpClient.getResponse());

			if (resultObj.has(Constants.KEY_BM4A_JSON_RESULT)) {
				updateResult = resultObj.getBoolean(Constants.KEY_BM4A_JSON_RESULT);
			}
			
		} catch (Exception e) {
			Log.e(TAG, Conca.t("Error occurred when attempting to update rating:[", rating.toString(), "]"), e);
		}
		
		
		if (updateResult) {
			editCommentButton.setText(getResources().getString(R.string.displaySingle_editCommentBtn_notedit));
			textComment.setText(editComment.getText());

			textComment.setVisibility(View.VISIBLE);
			editComment.setVisibility(View.GONE);

			ratingBar.setIsIndicator(true);

			setEditingComment(false);
			
		} else {
			Toast.makeText(DisplaySingleActivity.this, "Error storing rating!", Toast.LENGTH_LONG);
		}
	}

	private void insertRating() {
		Button editCommentButton = (Button)findViewById(R.id.displaySingle_editCommentButton);
		TextView textComment = (TextView)findViewById(R.id.displaySingle_rating_comment);
		EditText editComment = (EditText)findViewById(R.id.displaySingle_rating_comment_edit);
		RatingBar ratingBar = (RatingBar)findViewById(R.id.displaySingle_ratingBar);
		MappedBeerRating rating = new MappedBeerRating();
		rating.setUser(BeerMap4AndroidApp.getInstance().getUser());
		rating.setBeer(getThisBeer());
		
		int ratingBarValue = (int)ratingBar.getRating();
//		for (MappedValue val : cachedValueTypes) {
//			if (val.getValue() == ratingBarValue) {
//				rating.setRating(val);
//				break;
//			}
//		}
		rating.setRatingValue(ratingBarValue);
		
		rating.setComment(editComment.getText().toString());
		
		HttpRestClient httpClient = new HttpRestClient(this, getString(R.string.service_name_ratinginsert));
		httpClient.addParam(Constants.KEY_MAPPED_RATING, new JSONObject(rating).toString());
		
		boolean insertResult = false;
		
		JSONObject resultObj = null;
		try {
			httpClient.execute(RequestMethod.POST);
			resultObj = new JSONObject(httpClient.getResponse());

			if (resultObj.has(Constants.KEY_BM4A_JSON_RESULT)) {
				insertResult = resultObj.getBoolean(Constants.KEY_BM4A_JSON_RESULT);
			}
			
		} catch (Exception e) {
			Log.e(TAG, Conca.t("Error occurred when attempting to insert rating:[", rating.toString(), "]"), e);
		}
		
		if (insertResult){
			editCommentButton.setText(getResources().getString(R.string.displaySingle_editCommentBtn_notedit));
			textComment.setText(editComment.getText());

			textComment.setVisibility(View.VISIBLE);
			editComment.setVisibility(View.GONE);

			ratingBar.setIsIndicator(true);

			setEditingComment(false);
		} else {
			Toast.makeText(DisplaySingleActivity.this, "Error storing rating!", Toast.LENGTH_LONG);
		}
	}
	
	private void toggleVisibility() {
		TextView nameTextView = (TextView)findViewById(R.id.displaySingle_beerName);
		
		int textViewVisibility = -1;
		int editViewVisibility = -1;
		
		if (nameTextView.getVisibility() == View.GONE) {
			textViewVisibility = View.VISIBLE;
			editViewVisibility = View.GONE;	
		} else {
			textViewVisibility = View.GONE;
			editViewVisibility = View.VISIBLE;
		}
		
		setViewVisibilities(textViewVisibility, editViewVisibility);
		
	}
	
	private void copyTextFromEditToText() {
		TextView nameTextView = (TextView)findViewById(R.id.displaySingle_beerName);
		EditText nameEdit = (EditText)findViewById(R.id.displaySingle_name_edit);
		nameTextView.setText(nameEdit.getText());
		
		TextView abvTextView = (TextView)findViewById(R.id.displaySingle_abv);
		EditText abvEdit = (EditText)findViewById(R.id.displaySingle_abv_edit);
		abvTextView.setText(abvEdit.getText());
		
		TextView descriptTextView = (TextView)findViewById(R.id.displaySingle_descript);
		EditText descEdit = (EditText)findViewById(R.id.displaySingle_descript_edit);
		descriptTextView.setText(descEdit.getText());
	}
	
	private void copyTextFromTextToEdit() {
		TextView nameTextView = (TextView)findViewById(R.id.displaySingle_beerName);
		EditText nameEdit = (EditText)findViewById(R.id.displaySingle_name_edit);
		nameEdit.setText(nameTextView.getText());
		
		TextView abvTextView = (TextView)findViewById(R.id.displaySingle_abv);
		EditText abvEdit = (EditText)findViewById(R.id.displaySingle_abv_edit);
		abvEdit.setText(abvTextView.getText());
		
		TextView descriptTextView = (TextView)findViewById(R.id.displaySingle_descript);
		EditText descEdit = (EditText)findViewById(R.id.displaySingle_descript_edit);
		descEdit.setText(descriptTextView.getText());
	}
	
	private void setViewVisibilities(int textViewVisibility, int editViewVisibility) {
		TextView nameTextView = (TextView)findViewById(R.id.displaySingle_beerName);
		EditText nameEdit = (EditText)findViewById(R.id.displaySingle_name_edit);
		
		TextView abvTextView = (TextView)findViewById(R.id.displaySingle_abv);
		EditText abvEdit = (EditText)findViewById(R.id.displaySingle_abv_edit);
		
		TextView descriptTextView = (TextView)findViewById(R.id.displaySingle_descript);
		EditText descEdit = (EditText)findViewById(R.id.displaySingle_descript_edit);
		
		nameTextView.setVisibility(textViewVisibility);
		abvTextView.setVisibility(textViewVisibility);
		descriptTextView.setVisibility(textViewVisibility);
		
		nameEdit.setVisibility(editViewVisibility);
		abvEdit.setVisibility(editViewVisibility);
		descEdit.setVisibility(editViewVisibility);
	}
	
	private void populateGui() {
		MappedBeer beer = getThisBeer();
		TextView nameTextView = (TextView)findViewById(R.id.displaySingle_beerName);
		String name = beer.getName();
		if (name != null) {
			nameTextView.setText(name);
		}
		
		final TextView breweryTextView = (TextView)findViewById(R.id.displaySingle_brewery);
		if (beer.getBrewery() != null && beer.getBrewery().getId() != Constants.INVALID_ID) {
		    breweryTextView.setText(beer.getBrewery().getName());
		
		} else {
		    breweryTextView.setText("");
		}

		TextView abvTextView = (TextView)findViewById(R.id.displaySingle_abv);
		float abv = beer.getAbv();
		abvTextView.setText(String.valueOf(abv));
		
		TextView descriptTextView = (TextView)findViewById(R.id.displaySingle_descript);
		String descript = beer.getDescript();
		if (descript != null) {
			descriptTextView.setText(descript);
		}
		
		if (getIntent().hasExtra(getResources().getString(R.string.displaySingle_rating_key))) {
			MappedBeerRating rating = (MappedBeerRating)getIntent().getSerializableExtra(getResources().getString(R.string.displaySingle_rating_key));
			setThisRating(rating);
			
			RatingBar ratingBar = (RatingBar)findViewById(R.id.displaySingle_ratingBar);
			ratingBar.setRating(rating.getRatingValue());

			TextView comment = (TextView)findViewById(R.id.displaySingle_rating_comment);
			comment.setText(rating.getComment());

		} else {
		    TextView comment = (TextView)findViewById(R.id.displaySingle_rating_comment);
            comment.setText("");
		}
	}

	public void setEditingCurrentBeer(boolean isCancel) {
		this.editingCurrentBeer = isCancel;
	}

	public boolean isEditingCurrentBeer() {
		return editingCurrentBeer;
	}

	public void setThisBeer(MappedBeer thisBeer) {
		this.thisBeer = thisBeer;
	}

	public MappedBeer getThisBeer() {
		return thisBeer;
	}

	public void setEditingComment(boolean editingComment) {
		this.editingComment = editingComment;
	}

	public boolean isEditingComment() {
		return editingComment;
	}

	public void setThisRating(MappedBeerRating thisRating) {
		this.thisRating = thisRating;
	}

	public MappedBeerRating getThisRating() {
		return thisRating;
	}
	
	
	
	
	
	
	
	
	
	
}
