package com.spacepocalypse.activity;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.spacepocalypse.R;
import com.spacepocalypse.beermap2.domain.MappedBeer;
import com.spacepocalypse.beermap2.domain.MappedBeerRating;
import com.spacepocalypse.beermap2.domain.MappedUser;
import com.spacepocalypse.beermap2.domain.json.JSONArray;
import com.spacepocalypse.beermap2.domain.json.JSONException;
import com.spacepocalypse.beermap2.service.AndroidBeerQueryServlet;
import com.spacepocalypse.beermap2.service.BeerSearchEngine;
import com.spacepocalypse.http.HttpRestClient;
import com.spacepocalypse.http.HttpRestClient.RequestMethod;
import com.spacepocalypse.util.Conca;
import com.spacepocalypse.viewadapter.SearchResultsAdapter;

public class SearchResultsActivity extends Activity {
	public static final String TAG = "SearchResultsActivity";
	
	private ProgressDialog progressDialog;
	private  List<MappedBeer> searchResults;
	private MappedBeer lastSelected;
	private SearchResultsAdapter searchResultsAdapter;
	private boolean isInitialized = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_results);

		if (!isInitialized()) {
			showDialog();

			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					String response = null;
					if (getIntent().hasExtra(BeerMap4AndroidMain.RESULTS_KEY)) {
						response = getIntent().getStringExtra(BeerMap4AndroidMain.RESULTS_KEY);
					}

					if (response != null) {
						try {
							setSearchResults(MappedBeer.createListFromJSONArray(response));
						} catch (NumberFormatException e) {
							Log.e(
									TAG, 
									Conca.t("Error parsing number (probably abv). JSON=[", response, "]"), 
									e
							);
						} catch (JSONException e) {
							Log.e(
									TAG, 
									Conca.t("JSON error. JSON=[", response, "]"), 
									e
							);
						}
						
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								final ListView lv1 = resetSearchResultsListView();

								lv1.setOnItemClickListener(new OnItemClickListener() {
									@Override
									public void onItemClick(AdapterView<?> a, View v, int position, long id) {
										showDialog();

										Object o = lv1.getItemAtPosition(position);
										MappedBeer selectedBeer = (MappedBeer) o;

										setLastSelected(selectedBeer);
										Intent intent = new Intent(v.getContext(), DisplaySingleActivity.class);


										MappedUser user = (MappedUser) getIntent().getSerializableExtra(getResources().getString(R.string.user_key));
										HttpRestClient client = new HttpRestClient(SearchResultsActivity.this, getString(R.string.service_name_rating));
										client.addParam(AndroidBeerQueryServlet.KEY_QUERY, AndroidBeerQueryServlet.QUERY_TYPE_SEARCH);
										client.addParam(AndroidBeerQueryServlet.KEY_SEARCH_TYPE, AndroidBeerQueryServlet.SEARCH_TYPE_RATING);
										client.addParam(BeerSearchEngine.QUERY_KEY_BEER_ID, String.valueOf(selectedBeer.getId()));
										client.addParam(BeerSearchEngine.QUERY_KEY_USER_ID, String.valueOf(user.getId()));

										client.execute(RequestMethod.POST);

										String response = client.getResponse();
										if (response == null) {
											Log.w(
													TAG, 
													"Response was null when trying to find user rating. user id=[" 
													+ user.getId() + "] beer id=[" + selectedBeer.getId() + "]"
											);
										} else {
											try {
												JSONArray jsonArr = new JSONArray(response);
												if (jsonArr.length() > 0) {
													MappedBeerRating rating = MappedBeerRating.createMappedBeerRating(jsonArr.getJSONObject(0).toString());
													intent.putExtra(getResources().getString(R.string.displaySingle_rating_key), rating);
												}
											} catch (JSONException e) {
												Log.e(TAG, "JSON parsing problem. JSON string=[" + response + "]", e);
											}

										}


										String key = getResources().getString(R.string.displaySingleKey);
										int displaySingleRequestCode  = getResources().getInteger(R.integer.displaySingleRequestCode);
										intent.putExtra(key, selectedBeer);
										intent.putExtra(getResources().getString(R.string.user_key), user);
										startActivityForResult(intent, displaySingleRequestCode);
									}
								});
							}


						});
					}
					setInitialized(true);
					dismissDialog();
				}
			});
			t.start();
		}
	}

	private void showDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showDialog(0);
			}
		});
	}
	
	private ListView resetSearchResultsListView() {
		final ListView lv1 = (ListView) findViewById(R.id.searchResultsListView);
		setSearchResultsAdapter(new SearchResultsAdapter(this, getSearchResults()));
		lv1.setAdapter(getSearchResultsAdapter());
		return lv1;
	}	
	
	private void setListViewResult(int index, MappedBeer beer) {
		getSearchResultsAdapter().setItem(index, beer);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		int displaySingleRequestCode  = getResources().getInteger(R.integer.displaySingleRequestCode);
		if (requestCode == displaySingleRequestCode) {
			String resultKey = getResources().getString(R.string.displaySingle_result_key);
			if (data.hasExtra(resultKey)) {
				MappedBeer resultBeer = null;
				try {
					resultBeer = (MappedBeer) data.getExtras().getSerializable(resultKey);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
				
				if (resultBeer != null && getLastSelected() != null) {
					if (resultBeer.getId() == getLastSelected().getId()) {
						int index = getSearchResults().indexOf(getLastSelected());
						if (index > -1) {
							getSearchResults().set(index, resultBeer);
							setListViewResult(index, resultBeer);
						}
					}
				}
			}
			dismissDialog();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (getProgressDialog() == null) {
			setProgressDialog(new ProgressDialog(this));
			getProgressDialog().setIndeterminate(true);
			getProgressDialog().setMessage("Parsing search results...");
		}
		return getProgressDialog();
	}

	public void setProgressDialog(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

	public ProgressDialog getProgressDialog() {
		return progressDialog;
	}

	public void setSearchResults(List<MappedBeer> searchResults) {
		this.searchResults = searchResults;
	}

	public List<MappedBeer> getSearchResults() {
		return searchResults;
	}

	public void setLastSelected(MappedBeer lastSelected) {
		this.lastSelected = lastSelected;
	}

	public MappedBeer getLastSelected() {
		return lastSelected;
	}

	public void setSearchResultsAdapter(SearchResultsAdapter searchResultsAdapter) {
		this.searchResultsAdapter = searchResultsAdapter;
	}

	public SearchResultsAdapter getSearchResultsAdapter() {
		return searchResultsAdapter;
	}

	private void dismissDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getProgressDialog().isShowing()) {
					dismissDialog(0);
				}
			}
		});
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public boolean isInitialized() {
		return isInitialized;
	}
}
