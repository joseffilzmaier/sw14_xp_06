package com.sw.nam.test;

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
    han.finishOpenedActivities();
  }

  public void test1LoggedIn() {
    assertTrue("not logged in", han.searchText("Online"));
  }
}
