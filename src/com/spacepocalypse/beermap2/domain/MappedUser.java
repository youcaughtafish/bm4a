package com.spacepocalypse.beermap2.domain;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.spacepocalypse.beermap2.domain.json.JSONException;
import com.spacepocalypse.beermap2.domain.json.JSONObject;

public class MappedUser implements Serializable {
	private static final long serialVersionUID = -2121990998905286418L;
	private int id;
	private String username;
	private boolean active;
	private List<String> roles;
	
	public MappedUser() {
		setUsername("");
		setActive(false);
		setId(-1);
		roles = new ArrayList<String>();
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	/**
	 * This method will map a user given a {@link ResultSet}.
	 * Note that rs.next should be called exactly once prior
	 * to being handed to this method.
	 */
	public static MappedUser createMappedUser(ResultSet rs) throws SQLException {
	    final MappedUser user = new MappedUser();

		user.setId(rs.getInt("id"));
		user.setUsername(rs.getString("username"));
		user.setActive(rs.getInt("active") == 1);
		
		final List<String> roles = new ArrayList<String>();
		
		do {
    		if (rs.getInt("role_active") == 1) {
    		    roles.add(rs.getString("role_name"));
    		}

		} while (rs.next());
		
		user.setRoles(roles);
		
		return user;
	}

	public static MappedUser createMappedUser(JSONObject jsonObj) throws JSONException {
		MappedUser user = new MappedUser();

		if (jsonObj.has("id")) {
			user.setId(jsonObj.getInt("id"));
		}

		if (jsonObj.has("username")) {
			user.setUsername(jsonObj.getString("username"));
		}

		if (jsonObj.has("active")) {
			user.setActive(jsonObj.getBoolean("active"));
		}
		return user;
	}
	
	public static void main(String[] args) {
		String jsonObject = new JSONObject(new MappedUser()).toString();
		System.out.println(jsonObject);
	}

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
