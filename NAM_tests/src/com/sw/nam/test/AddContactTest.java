package com.sw.nam.test;


import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;
import com.sw.nam.MainActivity;

public class AddContactTest extends ActivityInstrumentationTestCase2<MainActivity> {
  private Solo solo;

  public AddContactTest() {
    super(MainActivity.class);
  }

  protected void setUp() throws Exception {
    super.setUp();
    solo = new Solo(getInstrumentation(), getActivity());
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void test1InvalidEmail() {
	String email = "name" + "@example";
	solo.clickOnView(getActivity().findViewById(com.sw.nam.R.id.action_add));
	solo.enterText(0, email);;
	solo.clickOnButton(1);
	assertTrue("Valid email!", solo.waitForText("Invalid email!"));
  }
  
  public void test2NotRegistered() {
	 String email = "name" + "@example.com";
	 solo.clickOnView(getActivity().findViewById(com.sw.nam.R.id.action_add));
	 solo.enterText(0, email);;
	 solo.clickOnButton(1);
	 assertTrue("User is  registered!", solo.waitForText(email + " is not registered!"));
  }
  
  public void test3ContactAdded() {
	 String email = "Dummy" + "@gmail.com";
	 solo.clickOnView(getActivity().findViewById(com.sw.nam.R.id.action_add));
	 solo.enterText(0, email);;
	 solo.clickOnButton(1);
	 assertTrue("User not added!", solo.waitForText(email + " added!"));
	 assertTrue("User not added!", solo.searchText(email));
  }
  
  public void test4ContactRename() {
	 String email = "Dummy" + "@gmail.com";
	 String name = "Bot";
	 solo.clickLongOnTextAndPress(email, 0);
	 solo.clearEditText(0);
	 solo.enterText(0, name);
	 solo.clickOnButton(1);
	 assertTrue("Could not rename!", solo.searchText(name));
  }
  
  public void test5ContactDelete() {
	 String name = "Bot";
	 solo.clickLongOnTextAndPress(name, 1);
	 assertFalse("User not deleted!", solo.searchText(name));
  }
}