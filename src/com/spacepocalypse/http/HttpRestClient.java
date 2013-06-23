package com.spacepocalypse.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.util.Log;

import com.spacepocalypse.R;
import com.spacepocalypse.util.Conca;
import com.spacepocalypse.util.StrUtl;

/**
 * Provides an http client for POSTing to the webservice with optional HTTP with SSL.  
 * This class currently uses the following resources: R.string.sslHttp, R.integer.sslPort,
 * R.string.nonSslHttp, R.integer.nonSslPort, R.string.webServiceResource, R.string.web_service_address.
 * SSL is on by default, but can be turned off through the setUseSSL(..) method
 * 
 * @author dylan
 */

public class HttpRestClient {
	private static final String TAG = "RestClient";
	
	private Context context;
	private ArrayList<NameValuePair> params;
	private ArrayList<NameValuePair> headers;

	private String host;
	private int responseCode;
	private String message;
	private String response;
	private boolean useSSL;
	private String uri;
	
	public HttpRestClient(final Context context, final String serviceExtension) {
		setContext(context);
		setUseSSL(false);
		
		final String fullServiceUri = createFullServiceUri(serviceExtension);  // i.e. https://localhost:8443/beermap2/android/login
		
		setUri(fullServiceUri);
		
		params = new ArrayList<NameValuePair>();
		headers = new ArrayList<NameValuePair>();
	}
	
	public void setServiceExtension(final String serviceExtension) {
		setUri(createFullServiceUri(serviceExtension));
	}

	private String createFullServiceUri(final String serviceExtension) {
		int port = -1;
		String httpMethod = null;
		
		if (isUseSSL()) {
			httpMethod = getContext().getResources().getString(R.string.sslHttp);
			port = getContext().getResources().getInteger(R.integer.sslPort);
			
		} else {
			httpMethod =  getContext().getResources().getString(R.string.nonSslHttp);
			port = getContext().getResources().getInteger(R.integer.nonSslPort);
		}
		
		final String serviceAddress = getContext().getResources().getString(R.string.web_service_address);
		final String serviceResource = getContext().getResources().getString(R.string.webServiceResource);
		
		final String fullServiceUri = Conca.t(							// explanation			example
				httpMethod,                  					        // http(s)://			https://
				serviceAddress,											// hostname				localhost
				":", port,                              				// :port				:8443
				(StrUtl.startsWith(serviceResource, "/") ? "" : "/"),	// (/)					
				serviceResource,										// (/)resource(/)		/beermap2/android
				(StrUtl.endsWith(serviceResource, "/") ? "" : "/"),   	// (/)					/
				serviceExtension										// final resource		login
		);
		
		Log.i(TAG, Conca.t("full service uri set to [", fullServiceUri, "]"));
		
		return fullServiceUri;
	}
	
	public void clearParams() {
		params.clear();
	}

	/**
	 * @return the response obtained through the most recent
	 * call to execute(..), or null if there was a problem
	 */
	public String getResponse() {
		return response;
	}

	public String getErrorMessage() {
		return getMessage();
	}

	public int getResponseCode() {
		return responseCode;
	}


	public void addParam(String name, String value) {
		params.add(new BasicNameValuePair(name, value));
	}

	public void addHeader(String name, String value) {
		headers.add(new BasicNameValuePair(name, value));
	}

	public void execute(RequestMethod method) {
		if (method == RequestMethod.POST) {
			executePost();
		} else {
			executeGet();
		}

	}

	private void executeGet() {
		Log.e(TAG, "GET is currently not implemented!");
	}

	/**
	 * Executes a POST request to the web service, adding the parameters specified
	 * by the addParam(..) method.  After this method has returned, use getResponse()
	 * to obtain the String of the HTTP response.
	 */
	private void executePost() {
		InputStream instream = null;
		try {
			HttpPost request = createHttpRequest();

			request.addHeader("Host", getHost());
			request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

			BasicHttpParams httpParams = new BasicHttpParams();
			HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setUseExpectContinue(httpParams, false);

			HttpClient aClient = createHttpClient(httpParams);

			HttpResponse httpResponse = aClient.execute(request);
			
			setResponseCode(httpResponse.getStatusLine().getStatusCode());
			setMessage(httpResponse.getStatusLine().getReasonPhrase());

			StringBuilder logMessage = new StringBuilder();
			
			// TODO: truncate the 'message' to avoid logging too much text
			logMessage.append(Conca.t(
					"Response code: [",
					getResponseCode(),
					"] Message: [",
					getMessage(),
					"]")
			);
			
			HttpEntity entity = httpResponse.getEntity();

			if (entity != null) {
				instream = entity.getContent();
				
				setResponse(convertStreamToString(instream));
				
				logMessage.append(" Response_content: [");
				logMessage.append(getResponse());
				logMessage.append("]");
				Log.i(TAG, logMessage.toString());
			} else {
				logMessage.append(" Response_content (entity) was null!");
				Log.w(TAG, logMessage.toString());
			}
			
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			
		} finally {
			if (instream != null) {
				try {
					// Closing the input stream will trigger connection release
					instream.close();
				} catch (Exception e) {}
			}
		}
	}

	private HttpPost createHttpRequest() throws IllegalArgumentException {
		int port = -1;
		String httpMethod = null;
		
		if (isUseSSL()) {
			httpMethod = getContext().getResources().getString(R.string.sslHttp);
			port = getContext().getResources().getInteger(R.integer.sslPort);
			
		} else {
			httpMethod =  getContext().getResources().getString(R.string.nonSslHttp);
			port = getContext().getResources().getInteger(R.integer.nonSslPort);
		}
		
		HttpPost request 
			= new HttpPost(getUri());
		return request;
	}

	private HttpClient createHttpClient(BasicHttpParams httpParams) {
		com.spacepocalypse.http.HttpClient client = new com.spacepocalypse.http.HttpClient(getContext());
		ClientConnectionManager clientConnMgr = client.createClientConnectionManager();
		
		HttpClient aClient = new DefaultHttpClient(clientConnMgr, httpParams);
		return aClient;
	}

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	public void setHost(String url) {
		this.host = url;
	}

	public String getHost() {
		return host;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public boolean isUseSSL() {
		return useSSL;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public enum RequestMethod {
		GET,
		POST
	}
}
