package com.spacepocalypse.beermap2.domain;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.spacepocalypse.beermap2.domain.json.JSONException;
import com.spacepocalypse.beermap2.domain.json.JSONObject;

public class MappedBeerRating implements Serializable {
	private static final long serialVersionUID = -2444895024305314779L;
	private MappedUser user;
	private MappedBeer beer;
	private int ratingValue;
	private String comment;
	private int id;
	
	public MappedBeerRating() {
		setUser(new MappedUser());
		setBeer(new MappedBeer());
		setRatingValue(1);
		setComment("");
		setId(-1);
	}
	
	
	public void setUser(MappedUser user) {
		this.user = user;
	}
	public MappedUser getUser() {
		return user;
	}
	public void setBeer(MappedBeer beer) {
		this.beer = beer;
	}
	public MappedBeer getBeer() {
		return beer;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getId() {
		return id;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}


	public String getComment() {
		return comment;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append(":{id=[");
		sb.append(getId());
		sb.append("] comment=[");
		sb.append(getComment());
		sb.append("] rating=[");
		sb.append(getRatingValue());
		sb.append("] user=[");
		sb.append(user.getUsername());
		sb.append("] beer=[");
		sb.append(getBeer().toString());
		sb.append("]}");
		return sb.toString();
	}
	
	public static MappedBeerRating createMappedBeerRating(String jsonString) throws JSONException {
		MappedBeerRating rating = new MappedBeerRating();

		JSONObject jsonObj = new JSONObject(jsonString);

		if (jsonObj.has("id")) {
			rating.setId(jsonObj.getInt("id"));
		}

		if (jsonObj.has("user")) {
			JSONObject userJSONObj = jsonObj.getJSONObject("user");
			rating.setUser(MappedUser.createMappedUser(userJSONObj));
		}

		if (jsonObj.has("beer")) {
			JSONObject beerJSONObj = jsonObj.getJSONObject("beer");
			rating.setBeer(MappedBeer.createMappedBeer(beerJSONObj.toString()));
		}

		if (jsonObj.has("comment")) {
			rating.setComment(jsonObj.getString("comment"));
		}
		
		if (jsonObj.has("ratingValue")) {
			rating.setRatingValue(jsonObj.getInt("ratingValue"));
		}

		return rating;
	}
	
	
	public int getRatingValue() {
		return ratingValue;
	}


	public void setRatingValue(int ratingValue) {
		this.ratingValue = ratingValue;
	}

}
