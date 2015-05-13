package com.marco.constant;

import cn.bmob.v3.BmobObject;

public class PatientIn extends BmobObject{
    private String PatientPassword;
    private String PatientName;
    private int Online;
	public int getOnline() {
		return Online;
	}
	public void setOnline(int online) {
		Online = online;
	}
	public String getPatientPassword() {
		return PatientPassword;
	}
	public void setPatientPassword(String patientPassword) {
		PatientPassword = patientPassword;
	}
	public String getPatientName() {
		return PatientName;
	}
	public void setPatientName(String patientName) {
		PatientName = patientName;
	}

}
