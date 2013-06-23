package com.spacepocalypse.beermap2.domain;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.spacepocalypse.beermap2.domain.json.JSONException;
import com.spacepocalypse.beermap2.domain.json.JSONObject;

public class MappedValue implements Comparable<MappedValue>, Serializable {
	private static final long serialVersionUID = 3819666251370179702L;
	private int id;
	private String desc;
	private int value;
	
	public MappedValue() {
		setDesc("");
		setValue(-1);
		setId(-1);
	}
	


	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName());
		sb.append(": {id=[");
		sb.append(getId());
		sb.append("] value=[");
		sb.append(getValue());
		sb.append("] desc=[");
		sb.append(getDesc());
		sb.append("]}");
		
		return sb.toString();
	}
	
	public static MappedValue createMappedValue(JSONObject jsonObj) throws JSONException {
		MappedValue val = new MappedValue();
		
		if (jsonObj.has("id")) {
			val.setId(jsonObj.getInt("id"));
		}
		
		if (jsonObj.has("value")) {
			val.setValue(jsonObj.getInt("value"));
		}

		if (jsonObj.has("desc")) {
			val.setDesc(jsonObj.getString("desc"));
		}
		
		return val;
	}
	
	
	public static MappedValue createMappedValue(ResultSet rs) throws SQLException {
		int col = 1;
		MappedValue val = new MappedValue();
		val.setId(rs.getInt(col++));
		val.setDesc(rs.getString(col++));
		val.setValue(rs.getInt(col++));

		return val;
	}

	public int compareTo(MappedValue o) {
		if (o == null || getValue() < o.getValue()) {
			return -1;
		}
		if (getValue() > o.getValue()) {
			return 1;
		}
		return 0;
	}

}
