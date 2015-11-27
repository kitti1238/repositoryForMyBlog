package com.org.coop.society.data.customer.entities;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the user_address database table.
 * 
 */
@Embeddable
public class UserAddressPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="user_name")
	private String userName;

	@Column(name="address_id")
	private int addressId;

	public UserAddressPK() {
	}
	public String getUserName() {
		return this.userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getAddressId() {
		return this.addressId;
	}
	public void setAddressId(int addressId) {
		this.addressId = addressId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UserAddressPK)) {
			return false;
		}
		UserAddressPK castOther = (UserAddressPK)other;
		return 
			this.userName.equals(castOther.userName)
			&& (this.addressId == castOther.addressId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.userName.hashCode();
		hash = hash * prime + this.addressId;
		
		return hash;
	}
}