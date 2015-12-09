package com.org.coop.society.data.admin.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the user_credential_otp database table.
 * 
 */
@Entity
@Table(name="user_credential_otp")
@NamedQuery(name="UserCredentialOtp.findAll", query="SELECT u FROM UserCredentialOtp u")
public class UserCredentialOtp implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="user_id")
	private int userId;

	@Column(name="end_date")
	private Timestamp endDate;

	private String otp;

	@Column(name="start_date")
	private Timestamp startDate;

	//bi-directional one-to-one association to User
	@OneToOne
	@JoinColumn(name="user_id")
	private User user;

	public UserCredentialOtp() {
	}

	public int getUserId() {
		return this.userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Timestamp getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public String getOtp() {
		return this.otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public Timestamp getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}