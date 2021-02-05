package com.eostek.smartbox.eloud;

public class UserRight {
	//{    "id":1,    "mag-lock":1,    "usb-1":1,    "usb-2":1,    "usb-3":0,    "usb-4":1 }
	int userID;
	int maglockRight;
	int usb1Right;
	int usb2Right;
	int usb3Right;
	int usb4Right;
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public int getMaglockRight() {
		return maglockRight;
	}
	public void setMaglockRight(int maglockRight) {
		this.maglockRight = maglockRight;
	}
	public int getUsb1Right() {
		return usb1Right;
	}
	public void setUsb1Right(int usb1Right) {
		this.usb1Right = usb1Right;
	}
	public int getUsb2Right() {
		return usb2Right;
	}
	public void setUsb2Right(int usb2Right) {
		this.usb2Right = usb2Right;
	}
	public int getUsb3Right() {
		return usb3Right;
	}
	public void setUsb3Right(int usb3Right) {
		this.usb3Right = usb3Right;
	}
	public int getUsb4Right() {
		return usb4Right;
	}
	public void setUsb4Right(int usb4Right) {
		this.usb4Right = usb4Right;
	}
	
	public String toString() {
		return "userID: " + userID + ", maglock: " + maglockRight + ", USB-1: " + usb1Right
				+ ", USB-2: " + usb2Right + ", USB-3: " + usb3Right + ", USB-4: " + usb4Right;
	}
	
}
