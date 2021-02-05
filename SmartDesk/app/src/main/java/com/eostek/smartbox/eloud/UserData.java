package com.eostek.smartbox.eloud;

import com.eostek.smartbox.utils.UrlConstants;

public class UserData {
	//"id":1, "name":"文奇兵", "depart":"产品中心", "职位":"产品总监", 
	//"e-mail":"bob@lenovo.com", "mobil":"1890000117"
	
	private int id;
	private String name = null;
	private String depart = null;
	private String position = null;
	private String email = null;
	private String mobil = null;
	private String photo;

	public String getPhoto() {
		return UrlConstants.getServiceUrlPhoto()+photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDepart() {
		return depart;
	}
	public void setDepart(String depart) {
		this.depart = depart;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobil() {
		return mobil;
	}
	public void setMobil(String mobil) {
		this.mobil = mobil;
	}
	
	public String toString(){
		return "id : "+id + ", name : "+ name+", depart : " + depart + ", position : " + position 
				+ ", e-mail : " + email+", mobil : "+mobil + ", photo : " + photo;
	}
}
