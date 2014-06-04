package com.sw.nam.test;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.test.ActivityInstrumentationTestCase2;
import com.robotium.solo.Solo;
import com.sw.nam.MainActivity;

public class LoginTest extends ActivityInstrumentationTestCase2<MainActivity> {
  private Solo han;

  public LoginTest() {
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
}
