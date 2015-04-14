package com.marco.constant;

import cn.bmob.v3.BmobObject;

public class Filename extends BmobObject{
    private String filename;
    private String realname;
    public String getRealname() {
		return realname;
	}
	public void setRealname(String realname) {
		this.realname = realname;
	}
	private PatientUser user;
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public PatientUser getUser() {
		return user;
	}
	public void setUser(PatientUser user) {
		this.user = user;
	}
}
