package com.sw.nam.test;

import com.sw.nam.MainMenu;
import com.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

public class MainMenuButtonTest extends ActivityInstrumentationTestCase2<MainMenu> {

	private Solo mySolo;
	public MainMenuButtonTest() {
		super(MainMenu.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mySolo = new Solo(getInstrumentation(), getActivity());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testButtons(){
		mySolo.clickOnButton("View Contacts");
		mySolo.clickOnButton("Latest Messages");
		mySolo.clickOnButton("Add new Contact");
		mySolo.clickOnButton("Logout");
	}
	
}
