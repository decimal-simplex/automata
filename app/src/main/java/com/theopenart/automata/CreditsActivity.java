package com.theopenart.automata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class CreditsActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.credits);

    WebView tutorialWebView = (WebView)findViewById(R.id.tutorialWebView);
    StringBuilder content = new StringBuilder();
    InputStream tis = null;
    InputStreamReader isr = null;
    BufferedReader br = null;

    try {
      tis = getAssets().open("tutorial.html");
      isr = new InputStreamReader(tis);
      br = new BufferedReader(isr);
      String buffer;
      while ((buffer = br.readLine()) != null) {
        content.append(buffer);
      }
    }
    catch (IOException ioe) {
      Log.e(getClass().getCanonicalName(), "error loading tutorial.html", ioe);
      content.append("couldn't load tutorial.html!");
    }
    finally {
      try {
        if (tis != null) {
          tis.close();
        }
        if (isr != null) {
          isr.close();
        }
        if (br != null) {
          br.close();
        }
      }
      catch (IOException ioe) {
        Log.e(getClass().getCanonicalName(), "error closing stream", ioe);
      }
    }

    tutorialWebView.loadDataWithBaseURL("file:///android_asset/", content.toString(), "text/html", "utf-8", null);
  }

}
