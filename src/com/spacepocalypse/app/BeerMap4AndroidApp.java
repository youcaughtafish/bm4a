package com.spacepocalypse.app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.spacepocalypse.R;
import com.spacepocalypse.beermap2.domain.MappedUser;
import com.spacepocalypse.beermap2.domain.json.JSONException;
import com.spacepocalypse.beermap2.domain.json.JSONObject;
import com.spacepocalypse.beermap2.service.Constants;

public class BeerMap4AndroidApp extends Application {
    private static BeerMap4AndroidApp instance;
    private static final int DEFAULT_SEARCH_PAGE_SIZE = 15;
    private static String TAG = BeerMap4AndroidApp.class.getSimpleName();
    
    private MappedUser user;
    private long timeoutTimeAbsMs;
    private int searchResultsPageSize;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        instance = this;
    }
    
    public BeerMap4AndroidApp() {
        super();
        
        setUser(new MappedUser());
        setTimeoutTimeAbsMs(0);
        setSearchResultsPageSize(DEFAULT_SEARCH_PAGE_SIZE);
    }
    
    public static BeerMap4AndroidApp getInstance() {
        synchronized (BeerMap4AndroidApp.class) {
            if (instance == null) {
                instance = new BeerMap4AndroidApp();
            }
            
            return instance;
        }
    }
    
    public void logout(final Activity context) {
        setTimeoutTimeAbsMs(System.currentTimeMillis());
        
        deleteStoredCredentials();
        
        if (context != null) {
            context.finish();
        }
        
    }
    
    public void storeCredentialsToFile(String stringToWrite)
            throws JSONException {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(getResources().getString(R.string.credentialsFilename), Context.MODE_PRIVATE);
            fos.write(stringToWrite.getBytes());
            
        } catch (Exception e) {
            Log.e(TAG, "Error occurred while attempting to write credentials file", e);
            
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error occurred while attempting to write credentials file", e);
                }
            }
        }
    }
    
    public void getStoredCredentials() {
        String userCredentialsFilename = getResources().getString(R.string.credentialsFilename);
        boolean fileDoesNotExist = true;
        String[] filenameList = fileList();
        for (String ea : filenameList) {
            if (ea.toLowerCase().equals(userCredentialsFilename)) {
                fileDoesNotExist = false;
                break;
            }
        }
        
        if (fileDoesNotExist) {
            return;
        }
        
        JSONObject credentialsObj = null;
        FileInputStream fin = null; 
        
        try {
            fin = openFileInput(userCredentialsFilename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            
            StringBuilder sb = new StringBuilder();
            String line = null;
            
            while ((line = reader.readLine()) != null){
                sb.append(line);
            }
            
            credentialsObj = new JSONObject(sb.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "Error occurred while reading cached credentials.", e);
            
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        
        if (credentialsObj.has(Constants.KEY_USER)) {
            try {
                setUser(MappedUser.createMappedUser(credentialsObj.getJSONObject(Constants.KEY_USER)));
            } catch (Exception e) {
                Log.e(TAG, "JSON exception while retrieving user from cache file", e);
            }
        }
        
        if (credentialsObj.has(Constants.KEY_TIMEOUT_MS)) {
            try {
                setTimeoutTimeAbsMs(credentialsObj.getLong(Constants.KEY_TIMEOUT_MS));
                
            } catch (Exception e) {
                Log.e(TAG, "JSON exception while retrieving timeout from cache file", e);
            }
        }
    }
    
    public void deleteStoredCredentials() {
        String userCredentialsFilename = getResources().getString(R.string.credentialsFilename);
        String[] filenameList = fileList();
        for (String ea : filenameList) {
            if (ea.equalsIgnoreCase(userCredentialsFilename)) {
                deleteFile(ea);
                return;
            }
        }
    }

    public MappedUser getUser() {
        return user;
    }

    public void setUser(MappedUser user) {
        this.user = user;
    }

    public long getTimeoutTimeAbsMs() {
        return timeoutTimeAbsMs;
    }

    public void setTimeoutTimeAbsMs(long timeoutTimeAbsMs) {
        this.timeoutTimeAbsMs = timeoutTimeAbsMs;
    }

    public int getSearchResultsPageSize() {
        return searchResultsPageSize;
    }

    public void setSearchResultsPageSize(int searchResultsPageSize) {
        this.searchResultsPageSize = searchResultsPageSize;
    }
}
