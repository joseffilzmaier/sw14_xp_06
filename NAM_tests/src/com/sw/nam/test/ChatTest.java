package com.sw.nam.test;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.test.ActivityInstrumentationTestCase2;
import com.robotium.solo.Solo;
import com.sw.nam.ChatActivity;
import com.sw.nam.MainActivity;

public class ChatTest extends ActivityInstrumentationTestCase2<MainActivity> {
  private Solo solo;

  public ChatTest() {
    super(MainActivity.class);
  }

  protected void setUp() throws Exception {
    super.setUp();
    solo = new Solo(getInstrumentation(), getActivity());
  }

  protected void tearDown() throws Exception {
    solo.finishOpenedActivities();
  }
  
  public void testChat() {
	Account[] accounts = AccountManager.get(getInstrumentation().getContext())
	      .getAccountsByType("com.google");
    String email = accounts[0].name;
    String message = "Test message!";
    
	solo.clickOnView(getActivity().findViewById(com.sw.nam.R.id.action_add));
	solo.enterText(0, email);
	solo.clickOnButton(1);
	assertTrue("Could not add contact!", solo.waitForText(email + " added!"));
	solo.clickInList(0);
	assertTrue("Could not enter chat!", solo.waitForActivity(ChatActivity.class));
	solo.enterText(0, message);
	solo.clickOnButton("Send");
	assertTrue("Could not send message!", solo.waitForText(message));
	assertTrue("Could not reveice message!", solo.waitForText(message, 2, 1000));
	solo.clickOnActionBarHomeButton();
	assertTrue("Could not leave chat!", solo.waitForActivity(MainActivity.class));
	solo.clickLongOnTextAndPress(email, 1);
	assertTrue("Could not delete contact!", solo.waitForText("Contact deleted"));
  }
}