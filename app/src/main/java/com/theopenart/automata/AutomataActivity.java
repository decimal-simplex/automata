package com.theopenart.automata;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.theopenart.automata.SpeciesCreator.SpeciesCreatorChangeListener;
import com.theopenart.automata.ecosystem.Ecosystem;
import com.theopenart.automata.ecosystem.Ecosystem.EcosystemScoreChangeListener;
import com.theopenart.automata.ecosystem.Jelly;
import com.theopenart.automata.ecosystem.Species;

import java.util.Timer;
import java.util.TimerTask;

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

  private Ecosystem           _ecosystem;

  private TextView            _scoreView;

  private ScoreChangeListener _scoreChangeListener;

  private SpeciesCreator      _speciesCreator = new SpeciesCreator();

  private Timer               _timer          = new Timer();

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

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
          }
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
  }
}