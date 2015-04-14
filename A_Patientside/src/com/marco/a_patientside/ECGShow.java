package com.marco.a_patientside;

import android.app.Activity;
import android.os.Bundle;

public class ECGShow extends Activity{
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecgshow_layout);
        getActionBar().hide();
        
	}
}
