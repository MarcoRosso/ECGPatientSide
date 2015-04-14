package com.marco.constant;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;

public class PatientUser extends BmobUser{
	private String roomnumber;
    private BmobRelation filename;
	public BmobRelation getFilename() {
		return filename;
	}

	public void setFilename(BmobRelation filename) {
		this.filename = filename;
	}

	public String getRoomnumber() {
		return roomnumber;
	}

	public void setRoomnumber(String roomnumber) {
		this.roomnumber = roomnumber;
	}

}
