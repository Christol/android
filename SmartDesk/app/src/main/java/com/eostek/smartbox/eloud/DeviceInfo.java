package com.eostek.smartbox.eloud;

public class DeviceInfo {
	//{"id":1, "name":"B1S-02-22B", "mac":"FC2325889900"}
	int id;
	String name;
	String mac;
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
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	
	public String toString(){
		return "id: " + id + ", name: " + name + ", mac: " + mac;
	}
	
}
