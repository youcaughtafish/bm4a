package com.spacepocalypse.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.spacepocalypse.R;
import com.spacepocalypse.beermap2.domain.MappedBrewery;
import com.spacepocalypse.beermap2.domain.json.JSONArray;
import com.spacepocalypse.beermap2.service.Constants;
import com.spacepocalypse.http.HttpRestClient;
import com.spacepocalypse.http.HttpRestClient.RequestMethod;

public class FindBreweryActivity extends Activity {
    public static final String TAG = "FindBreweryActivity";
    
    private List<MappedBrewery> searchResults;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_brewery_layout);
        
        final EditText breweryNameEditText = (EditText)findViewById(R.id.find_brewery_layout_brewery_name_edit_text);
        final TextView resultsTextView = (TextView)findViewById(R.id.results_text_view);
        final Button searchBtn = (Button)findViewById(R.id.search_breweries_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchBtn.setEnabled(false);
                        searchBtn.setText(getString(R.string.searching_str));
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final HttpRestClient client 
                            = new HttpRestClient(FindBreweryActivity.this, getString(R.string.service_name_brewerysearch));
                        
                        client.addParam(Constants.KEY_QUERY, breweryNameEditText.getText().toString());
                        
                        client.execute(RequestMethod.POST);
                        
                        final String response = client.getResponse();
                        
                        if (response == null || response.isEmpty()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    resultsTextView.setText(getString(R.string.no_results_found_str));
                                }
                            });
                        
                        } else {
                            try {
                                final List<MappedBrewery> breweries = new ArrayList<MappedBrewery>();
                                final JSONArray arr = new JSONArray(response);
                                for (int i = 0; i < arr.length(); i++) {
                                    breweries.add(MappedBrewery.createMappedBrewery(arr.getJSONObject(i)));
                                }
                                
                                searchResults = breweries;
                                
                                runOnUiThread(createSearchResultListView());
                                
                            } catch (Exception e) {
                                Log.e(TAG, "Error occurred while attempting to parse brewery search results", e);
                            }
                        }
                        
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                searchBtn.setEnabled(true);
                                searchBtn.setText(getString(R.string.search_str));
                            }
                        });
                    }
                }).start();
            }
        });
    }
    
    private Runnable createSearchResultListView() {
        return new Runnable() {
            @Override
            public void run() {
                final ListView lv1 = resetSearchResultsListView();
                
                lv1.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                        // get the selected item
                        Object o = lv1.getItemAtPosition(position);
                        MappedBrewery selectedBeer = (MappedBrewery) o;
                        
                        getIntent().putExtra(getString(R.string.brewery_result_key), selectedBeer);
                        setResult(RESULT_OK, getIntent());
                        finish();
                    }
                });
            }
        };
    }

    private ListView resetSearchResultsListView() {
        final ListView lv1 = (ListView) findViewById(R.id.brewery_results_list_view);
        lv1.setAdapter(new BaseAdapter() {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = null;
                if (convertView == null) {
                    convertView = LayoutInflater.from(FindBreweryActivity.this).inflate(R.layout.string_list_item, parent, false);
                    textView = (TextView)convertView.findViewById(R.id.string_list_item_string);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, convertView.getResources().getInteger(R.integer.searchResultsNameFontSize));
                    textView.setTextColor(convertView.getResources().getColor(R.color.nameDisplayColor));
                    convertView.setTag(textView);
                    
                } else {
                    textView = (TextView)convertView.getTag();
                }

                textView.setText(searchResults.get(position).getName());
                
                return convertView;
            }
            
            @Override
            public long getItemId(int position) {
                return position;
            }
            
            @Override
            public Object getItem(int position) {
                return searchResults.get(position);
            }
            
            @Override
            public int getCount() {
                return searchResults.size();
            }
        });
        return lv1;
    }   
    
}
