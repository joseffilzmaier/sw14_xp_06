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
    super.tearDown();
  }
  
  public void test1EnterChat() {
//	Account[] accounts = AccountManager.get(getInstrumentation().getContext())
//		      .getAccountsByType("com.google");
//    String email = accounts[0].name;
//	solo.clickOnView(getActivity().findViewById(com.sw.nam.R.id.action_add));
//	solo.enterText(0, email);;
//	solo.clickOnButton(1);
//	assertTrue("Could not add contact!", solo.waitForText("email"));
//	solo.clickOnText(email);
////	assertTrue("Could not enter chat!", solo.waitForActivity(ChatActivity.class));
//	solo.clickLongOnTextAndPress(email, 1);
  }
  
  
//  public void test5ContactDelete() {
//	 String name = "Bot";
//	 solo.clickLongOnTextAndPress(name, 1);
//	 assertFalse("User not deleted!", solo.searchText(name));
//  }
}