package com.sw.nam.test;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.test.ActivityInstrumentationTestCase2;
import com.robotium.solo.Solo;
import com.sw.nam.MainActivity;

public class Login extends ActivityInstrumentationTestCase2<MainActivity> {
  private Solo han;

  public Login() {
    super(MainActivity.class);
  }

  protected void setUp() throws Exception {
    super.setUp();
    han = new Solo(getInstrumentation(), getActivity());
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void test1LoggedIn() {
    Account[] accounts = AccountManager.get(getInstrumentation().getContext())
        .getAccountsByType("com.google");
    assertTrue("not logged in", han.searchText(accounts[0].name));
  }

  String name = "email";
  public void test2UIAddContact() {
    String email = name + "@example.com";
    han.clickOnView(getActivity().findViewById(com.sw.nam.R.id.action_add));
    han.enterText(0, email);
    han.clickOnButton(1);
    assertTrue("contact not added", han.waitForText(name));
  }
  
  public void test3EditName() {
    String newName = "new" + name;
    han.clickOnText(name);
    assertTrue("name not found", han.waitForText(name));
    han.clickOnText("Edit");
    assertTrue("name different", han.waitForText(name));
    han.clearEditText(0);
    han.enterText(0, newName);
    han.clickOnButton(1);
    assertTrue("name not changed", han.searchText(newName));
    han.clickOnText("Edit");
    han.clearEditText(0);
    han.enterText(0, name);
    han.clickOnButton(1);
    assertTrue("name not changed", han.searchText(name));
    han.goBack();
  }
  
  public void test4MessageContact() {
    String message = "message to send";
    han.clickOnText(name);
    han.enterText(0, message);
    han.clickOnButton("Send");
    assertTrue("name different", han.waitForText(message));
  }
}
