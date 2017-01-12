package com.theopenart.automata;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

//import com.facebook.android.DialogError;
//import com.facebook.android.Facebook;
//import com.facebook.android.Facebook.DialogListener;
//import com.facebook.android.FacebookError;
import com.theopenart.automata.SpeciesCreator.SpeciesCreatorChangeListener;
import com.theopenart.automata.ecosystem.Ecosystem;
import com.theopenart.automata.ecosystem.Ecosystem.EcosystemScoreChangeListener;
import com.theopenart.automata.ecosystem.Jelly;
import com.theopenart.automata.ecosystem.Species;

public class AutomataActivity extends Activity {

  private class ScoreChangeListener implements EcosystemScoreChangeListener {

    @Override
    public void onEcosystemScoreChanged() {
      AutomataActivity.this.runOnUiThread(new Runnable() {

        @Override
        public void run() {
          _scoreView.setText(String.format(SCORE_FORMAT, _ecosystem.numOrganisms(), _ecosystem.numExtantSpecies(),
                                           _ecosystem.getScore(), _ecosystem.getHighestScore()));
        }
      });
    }
  }

  private static final long   BUZZ_WAIT       = 300L;

  private static final long   BUZZ_INTERVAL   = 100L;

  private static final String SCORE_FORMAT    = "CREATURES:%4d  SPECIES:%2d\n" + "SCORE:%,8d  HIGH SCORE:%,8d";

//  private Facebook            _facebook;

  private SharedPreferences   _mPrefs;

  private Ecosystem           _ecosystem;

  private TextView            _scoreView;

  private ScoreChangeListener _scoreChangeListener;

  private SpeciesCreator      _speciesCreator = new SpeciesCreator();

  private Timer               _timer          = new Timer();

//  private Button              _shareButton;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

//    _facebook = new Facebook(getResources().getString(R.string.facebook_app_id));

//    _shareButton = new Button(this);

    if (EcosystemManager.INSTANCE.getEcosystem() == null) {
      EcosystemManager.INSTANCE.setParentActivity(this);
      EcosystemManager.INSTANCE.buildEcosystem(this);
    }
    else {
      EcosystemManager.INSTANCE.setParentActivity(this);
    }

    _ecosystem = EcosystemManager.INSTANCE.getEcosystem();

    LinearLayout gameplayLayout = (LinearLayout)findViewById(R.id.gameplayLayout);

    LinearLayout scoreLayout = new LinearLayout(this);
    scoreLayout.setOrientation(LinearLayout.HORIZONTAL);

    _scoreView = new TextView(this);
    final SpeciesCreatorView speciesCreatorView = new SpeciesCreatorView(this, _speciesCreator);
    final EcosystemView ecosystemView = new EcosystemView(this, _speciesCreator, _ecosystem);

    // Main view of the game field.
    LayoutParams evParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 9.25f);
    gameplayLayout.addView(ecosystemView, evParams);

    // The score text.
    _scoreView.setBackgroundColor(Color.BLACK);
    _scoreView.setTextColor(Color.rgb(0x80, 0x60, 0xFF));
    _scoreView.setMaxLines(2);
    _scoreView.setTextSize(10);
    _scoreView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
    _scoreView.setGravity(Gravity.CENTER);
    _scoreView.setText("\nPlace a creature to begin...");
    LayoutParams scrParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1);
    scoreLayout.addView(_scoreView, scrParams);

    // The share button.
//    _shareButton.setBackgroundResource(R.drawable.share_button_bg);
//    LayoutParams shrParams = new LinearLayout.LayoutParams(80, 40, 0);
//    scoreLayout.addView(_shareButton, shrParams);

    // The score layout.
    LayoutParams scrLyoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 80, 0);
    gameplayLayout.addView(scoreLayout, scrLyoutParams);

    // The species creator.
    LayoutParams scvParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 0.75f);
    gameplayLayout.addView(speciesCreatorView, scvParams);

    _speciesCreator.setSpeciesCreatorChangeListener(new SpeciesCreatorChangeListener() {

      @Override
      public void onSpeciesCreatorChange() {
        AutomataActivity.this.runOnUiThread(new Runnable() {

          public void run() {
            speciesCreatorView.invalidate();
          };
        });
      }
    });

    speciesCreatorView.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          _timer.schedule(new TimerTask() {

            @Override
            public void run() {
              ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(BUZZ_INTERVAL);
            }
          }, BUZZ_WAIT);
          break;
        case MotionEvent.ACTION_UP:
          _timer.cancel();
          _timer = new Timer();
          long downInterval = event.getEventTime() - event.getDownTime();
          int position = (int)(event.getX() / speciesCreatorView.getWidth() * Species.MAX_JELLY_DNA);

          if (downInterval < BUZZ_WAIT) {
            Jelly j = _speciesCreator.get(position);

            if (j != null) {
              switch (j) {
              case FAST:
                _speciesCreator.replace(position, Jelly.HEALTHY);
                break;
              case HEALTHY:
                _speciesCreator.replace(position, Jelly.SMART);
                break;
              case SMART:
                _speciesCreator.replace(position, Jelly.FAST);
                break;
              }
            }
            else {
              _speciesCreator.addLast(Jelly.FAST);
            }
          }
          else {
            _speciesCreator.remove(position);
          }
          break;
        default:
          break;
        }
        return true;
      }
    });

//    _shareButton.setOnClickListener(new OnClickListener() {
//
//      @Override
//      public void onClick(View v) {
//        EcosystemManager.INSTANCE.getEcosystem().pause();
//
//        int score = EcosystemManager.INSTANCE.getEcosystem().getHighestScore();
//
//        showScorePublishConfirmDlg(score);
//
//        EcosystemManager.INSTANCE.getEcosystem().resume();
//      }
//    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
//    _facebook.authorizeCallback(requestCode, resultCode, data);
  }

  @Override
  protected void onResume() {
    super.onResume();
//    _facebook.extendAccessTokenIfNeeded(this, null);
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      _scoreChangeListener = new ScoreChangeListener();
      _ecosystem.addEcosystemScoreChangeListener(_scoreChangeListener);
    }
    else if (_scoreChangeListener != null) {
      _ecosystem.removeEcosystemScoreChangeListener(_scoreChangeListener);
    }
  }

  private void showScorePublishConfirmDlg(final int score) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.fb_dialog_title));
    builder.setMessage(String.format(getString(R.string.fb_dialog_text), score));
    builder.setCancelable(false);
    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        publishScoreToFacebook(score);
      }
    });

    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    });

    Dialog confirmDlg = builder.create();
    confirmDlg.show();
  }

  private void publishScoreToFacebook(int score) {
    final Bundle params = new Bundle();
    params.putString("picture", getString(R.string.fb_post_picture_url));
    params.putString("name", String.format(getString(R.string.fb_post_name), score));
    params.putString("link", getString(R.string.fb_post_link));

    // Get existing access_token if any.
//    _mPrefs = getPreferences(MODE_PRIVATE);
//    String access_token = _mPrefs.getString("access_token", null);
//    long expires = _mPrefs.getLong("access_expires", 0);
//    if (access_token != null) {
//      _facebook.setAccessToken(access_token);
//    }
//    if (expires != 0) {
//      _facebook.setAccessExpires(expires);
//    }
//
//    // Only call authorize if the access_token has expired.
//    if (!_facebook.isSessionValid()) {
//      authAndSendFacebookRequest(params);
//    }
//    else {
//      sendFacebookRequest(params, true);
//    }
  }

//  private void authAndSendFacebookRequest(final Bundle params) {
//    _facebook.authorize(AutomataActivity.this, new String[] { "publish_stream" }, new DialogListener() {
//
//      @Override
//      public void onComplete(Bundle values) {
//        SharedPreferences.Editor editor = _mPrefs.edit();
//        editor.putString("access_token", _facebook.getAccessToken());
//        editor.putLong("access_expires", _facebook.getAccessExpires());
//        editor.commit();
////        Log.d(getClass().getCanonicalName(), "Facebook auth'd successfully");
//        sendFacebookRequest(params, false);
//      }
//
//      @Override
//      public void onFacebookError(FacebookError error) {
////        Log.e(getClass().getCanonicalName(), "Facebook error authorizing Facebook");
//      }
//
//      @Override
//      public void onError(DialogError e) {
////        Log.e(getClass().getCanonicalName(), "internal error authorizing Facebook");
//      }
//
//      @Override
//      public void onCancel() {
////        Log.d(getClass().getCanonicalName(), "Facebook auth canceled");
//      }
//    });
//  }

//  private void sendFacebookRequest(final Bundle params, final boolean retry) {
//    new Thread(new Runnable() {
//
//      public void run() {
//
//        try {
//          String responseString = _facebook.request("me/feed", params, "POST");
////          Log.d(getClass().getCanonicalName(), responseString);
//
//          JSONObject responseJson = new JSONObject(responseString);
//          if (responseJson.has("error") && retry) {
//            JSONObject error = responseJson.getJSONObject("error");
//            if (error.has("type") && error.getString("type").equals("OAuthException")) {
//              // give the user a chance to authorize the app
//              authAndSendFacebookRequest(params);
//            }
//          }
//        }
//        catch (IOException ioe) {
////          Log.e(getClass().getCanonicalName(), "IOException POSTing to Facebook, giving up...", ioe);
//        }
//        catch (JSONException jsone) {
////          Log.e(getClass().getCanonicalName(), "Couldn't parse the Facebook response, giving up", jsone);
//        }
//      };
//    }).start();
//  }
}