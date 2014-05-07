package com.sw.nam.test;

import com.sw.nam.MainMenu;

import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class RegIdTest extends ActivityInstrumentationTestCase2<MainMenu> {

	public RegIdTest(String name) {
		super(MainMenu.class);
	}
	
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	//tests
	public void testRegId(){
		SharedPreferences sp = getInstrumentation().getTargetContext().getSharedPreferences("nam_prefs", 0);
	    boolean is_registered = sp.getBoolean("registered", false);
    	String registered = null;
    	registered = sp.getString("registration_id", null);
	    if (is_registered){
	    	Log.d("RegIdTest", "RegId correctly available: " + registered);
	    	assertNotNull(registered);
	    }
	    else
	    {
	    	assertNull(registered);
	    	Log.d("RegIdTest", "RegId correctly NULL");
	    }
	}

}
