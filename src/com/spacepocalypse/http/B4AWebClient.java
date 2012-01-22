package com.spacepocalypse.http;

import android.content.Context;

import com.spacepocalypse.R;
import com.spacepocalypse.beermap2.domain.MappedBeer;
import com.spacepocalypse.beermap2.domain.MappedBeerRating;
import com.spacepocalypse.beermap2.domain.json.JSONException;
import com.spacepocalypse.beermap2.domain.json.JSONObject;
import com.spacepocalypse.beermap2.service.Constants;
import com.spacepocalypse.http.HttpRestClient.RequestMethod;

public class B4AWebClient {
	private static B4AWebClient instance;
	private HttpRestClient httpClient;
	private Context context;
		
	
	private B4AWebClient() {}

	public static boolean isInitialized() {
		return instance != null && instance.httpClient != null;
	}

	private void setContext(Context context) {
		this.context = context;
	}

	private Context getContext() {
		return context;
	}
	
	public static void initialize(Context context) {
		if (context == null) {
			return;
		}
		if (instance == null) {
			instance = new B4AWebClient();
		}
		instance.setContext(context);
		instance.httpClient = new HttpRestClient(context, "");
	}
	
//	public static synchronized boolean insertMappedBeerRating(MappedBeerRating rating) throws NotInitializedException, JSONException {
//		checkInit();
//		
//		HttpRestClient instanceHttpClient = instance.httpClient;
//		instanceHttpClient.clearParams();
//		instanceHttpClient.setServiceExtension(instance.getContext().getString(R.string.service_name_rating));
//		
//		instanceHttpClient.addParam(Constants.KEY_QUERY, Constants.QUERY_TYPE_INSERT);
//		instanceHttpClient.addParam(Constants.KEY_MAPPED_RATING, new JSONObject(rating).toString());
//		
//		instanceHttpClient.execute(RequestMethod.POST);
//		String response = instanceHttpClient.getResponse();
//		
//		return processBooleanResponse(response);
//	}
	
//	public static synchronized boolean insertMappedBeer(MappedBeer beer) throws NotInitializedException, JSONException {
//		HttpRestClient instanceHttpClient = instance.httpClient;
//		instanceHttpClient.clearParams();
//		instanceHttpClient.setServiceExtension(instance.getContext().getString(R.string.service_name_beerinsert));
//		instanceHttpClient.addParam(Constants.KEY_QUERY, Constants.QUERY_TYPE_INSERT);
//		instanceHttpClient.addParam(Constants.KEY_MAPPED_BEER, new JSONObject(beer).toString());
//		
//		instanceHttpClient.execute(RequestMethod.POST);
//		String response = instanceHttpClient.getResponse();
//		
//		return processBooleanResponse(response);
//	}
	
//	public static synchronized boolean updateMappedBeerRating(MappedBeerRating rating) throws NotInitializedException, JSONException {
//		checkInit();
//		
//		HttpRestClient instanceHttpClient = instance.httpClient;
//		instanceHttpClient.clearParams();
//		instanceHttpClient.setServiceExtension(instance.getContext().getString(R.string.service_name_rating));
//		
//		instanceHttpClient.addParam(Constants.KEY_QUERY, Constants.QUERY_TYPE_UPDATE);
//		instanceHttpClient.addParam(Constants.KEY_MAPPED_RATING, new JSONObject(rating).toString());
//		
//		instanceHttpClient.execute(RequestMethod.POST);
//		
//		String response = instanceHttpClient.getResponse();
//		
//		return processBooleanResponse(response);
//	}

//	private static boolean processBooleanResponse(String response)
//			throws JSONException {
//		JSONObject jsonObj = new JSONObject(response);
//		if (jsonObj.has(Constants.RESULT_SUCCESS)) {
//			return jsonObj.getBoolean(Constants.RESULT_SUCCESS);
//		}
//		
//		return false;
//	}
//
// 	private static void checkInit() throws NotInitializedException {
//		if (!isInitialized()) {
//			throw new NotInitializedException();
//		}
//	}
	
}
