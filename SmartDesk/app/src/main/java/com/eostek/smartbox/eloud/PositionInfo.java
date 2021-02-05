package com.eostek.smartbox.eloud;

public class PositionInfo {
//{"val1-h":21212,"val1-l":-212121,"val2-h":5555,"val2-l":11111,"val3-h":-111154,"val3-l":222110}
	long id1_h;
	long id1_l;
	long id2_h;
	long id2_l;
	long id3_h;
	long id3_l;
	public long getId1_h() {
		return id1_h;
	}
	public void setId1_h(long id1_h) {
		this.id1_h = id1_h;
	}
	public long getId1_l() {
		return id1_l;
	}
	public void setId1_l(long id1_l) {
		this.id1_l = id1_l;
	}
	public long getId2_h() {
		return id2_h;
	}
	public void setId2_h(long id2_h) {
		this.id2_h = id2_h;
	}
	public long getId2_l() {
		return id2_l;
	}
	public void setId2_l(long id2_l) {
		this.id2_l = id2_l;
	}
	public long getId3_h() {
		return id3_h;
	}
	public void setId3_h(long id3_h) {
		this.id3_h = id3_h;
	}
	public long getId3_l() {
		return id3_l;
	}
	public void setId3_l(long id3_l) {
		this.id3_l = id3_l;
	}

	public String toString(){
		return "id1_h : "+id1_h + ", id1_l : "+ id1_l+", id2_h : " + id2_h + ", id2_l : " + id2_l
				+ ", id3_h : " + id3_h+", id3_l : "+id3_l ;
	}
}
