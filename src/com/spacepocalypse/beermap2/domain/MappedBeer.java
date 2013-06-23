package com.spacepocalypse.beermap2.domain;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.spacepocalypse.beermap2.domain.json.JSONArray;
import com.spacepocalypse.beermap2.domain.json.JSONException;
import com.spacepocalypse.beermap2.domain.json.JSONObject;
import com.spacepocalypse.beermap2.service.Constants;

public class MappedBeer implements Serializable {
	private static final long serialVersionUID = 2563267495110624938L;

	private int id;
	private String name;
	private float abv;
	private String descript;
	private MappedBrewery brewery;
	
	public MappedBeer() {
		id = Constants.INVALID_ID;
		setName("");
		setAbv(Constants.INVALID_ID);
		setDescript("");
		setBrewery(new MappedBrewery());
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setAbv(float abv) {
		this.abv = abv;
	}
	public float getAbv() {
		return abv;
	}
	public void setDescript(String descript) {
		this.descript = descript;
	}
	public String getDescript() {
		return descript;
	}
	
	public static List<MappedBeer> createListFromJSONArray(String response) throws JSONException, NumberFormatException {
		JSONArray resultsArr = null;
		try {
			resultsArr = new JSONArray(response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		List<MappedBeer> searchResults = new ArrayList<MappedBeer>();

		for (int i = 0; i < resultsArr.length(); i++) {
			JSONObject ea = resultsArr.getJSONObject(i);
			MappedBeer eaMappedValue = new MappedBeer();
			if (ea.has("id")) {
				eaMappedValue.setId(ea.getInt("id"));
			}

			if (ea.has("name")) {
				eaMappedValue.setName(ea.getString("name"));
			}

			if (ea.has("abv")) {
				eaMappedValue.setAbv(Double.valueOf(ea.getDouble("abv")).floatValue());
			}

			if (ea.has("descript")) {
				eaMappedValue.setDescript(ea.getString("descript"));
			}
			
			if (ea.has("brewery")) {
			    eaMappedValue.setBrewery(MappedBrewery.createMappedBrewery(ea.getJSONObject("brewery")));
			}

			searchResults.add(eaMappedValue);
		}
		return searchResults;
	}

	public static MappedBeer createMappedBeer(String json) throws JSONException, NumberFormatException {
		final JSONObject ea = new JSONObject(json);
		MappedBeer eaMappedValue = new MappedBeer();
		if (ea.has("id")) {
			eaMappedValue.setId(ea.getInt("id"));
		}

		if (ea.has("name")) {
			eaMappedValue.setName(ea.getString("name"));
		}

		if (ea.has("abv")) {
			eaMappedValue.setAbv(Double.valueOf(ea.getDouble("abv")).floatValue());
		}

		if (ea.has("descript")) {
			eaMappedValue.setDescript(ea.getString("descript"));
		}
		
		if (ea.has("brewery")) {
		    eaMappedValue.setBrewery(MappedBrewery.createMappedBrewery(ea.getJSONObject("brewery")));
		}

		return eaMappedValue;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public MappedBrewery getBrewery() {
		return brewery;
	}

	public void setBrewery(MappedBrewery brewery) {
		this.brewery = brewery;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MappedBeer [id=");
        builder.append(id);
        builder.append(", name=");
        builder.append(name);
        builder.append(", abv=");
        builder.append(abv);
        builder.append(", descript=");
        builder.append(descript);
        builder.append(", brewery=");
        builder.append(brewery);
        builder.append("]");
        return builder.toString();
    }
    
    public static MappedBeer createMappedBeer(ResultSet rs) throws SQLException {
        final MappedBeer beer = new MappedBeer();
        beer.setId(rs.getInt("beer_id"));
        beer.setName(rs.getString("beer_name"));
        beer.setAbv(rs.getFloat("beer_abv"));
        beer.setDescript(rs.getString("beer_descript"));
        
        beer.getBrewery().setName(rs.getString("brewery_name"));
        beer.getBrewery().setCountry(rs.getString("brewery_country"));
        beer.getBrewery().setDescript(rs.getString("brewery_descript"));
        beer.getBrewery().setId(rs.getInt("brewery_id"));
        
        return beer;
    }
}
