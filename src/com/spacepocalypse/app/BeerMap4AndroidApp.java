package com.spacepocalypse.app;

import com.spacepocalypse.beermap2.domain.MappedUser;

import android.app.Application;

public class BeerMap4AndroidApp extends Application {
    private static BeerMap4AndroidApp instance;
    private static final int DEFAULT_SEARCH_PAGE_SIZE = 15;
    
    private MappedUser user;
    private long timeoutTimeAbsMs;
    private int searchResultsPageSize;
    
    @Override
    public void onCreate() {
        super.onCreate();
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
