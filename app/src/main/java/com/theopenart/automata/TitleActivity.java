package com.theopenart.automata;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class TitleActivity extends Activity {

  public static final String AUTOMATA_MAP_TITLE     = "Automata Map";

  public static final String RANDOM_HUGE_MAP_TITLE  = "Random Map (Huge)";

  public static final String RANDOM_LARGE_MAP_TITLE = "Random Map (Large)";

  public static final String RANDOM_SMALL_MAP_TITLE = "Random Map (Small)";

  public static final String RANDOM_TINY_MAP_TITLE  = "Random Map (Tiny)";

  public static final String TUT_MAP_TITLE      = "Tutorial";

  public static final String PAID                   = "PAID";

  private Button             _resumeButton;

  private CharSequence[]     _levels                = { AUTOMATA_MAP_TITLE };

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.title);

    _resumeButton = (Button)findViewById(R.id.resumeButton);

    Button newGameButton = (Button)findViewById(R.id.newGameButton);

    newGameButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        // Ask for the level
        AlertDialog.Builder builder = new AlertDialog.Builder(TitleActivity.this);
        builder.setTitle(R.string.level_chooser_title);
        builder.setItems(_levels, new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            // if none of these get hit, the Automata Activity will still initialize a default level
            EcosystemManager.INSTANCE.clear();
            if (which < _levels.length) {
              String levelPath = getResources().getString(R.string.level_path);
              if (_levels[which].equals(AUTOMATA_MAP_TITLE)) {
                String levelFName = getResources().getString(R.string.level_automata_fname);
                EcosystemManager.INSTANCE.setMapPath(levelPath + levelFName);
              }
              else if (_levels[which].equals(RANDOM_HUGE_MAP_TITLE)) {
                EcosystemManager.INSTANCE.setDimension(60, 80);
              }
              else if (_levels[which].equals(RANDOM_LARGE_MAP_TITLE)) {
                EcosystemManager.INSTANCE.setDimension(45, 60);
              }
              else if (_levels[which].equals(RANDOM_SMALL_MAP_TITLE)) {
                EcosystemManager.INSTANCE.setDimension(30, 40);
              }
              else if (_levels[which].equals(RANDOM_TINY_MAP_TITLE)) {
                EcosystemManager.INSTANCE.setDimension(15, 20);
              }
              else if (_levels[which].equals(TUT_MAP_TITLE)) {
                String level1FName = getResources().getString(R.string.level_tut_beg_fname);
                String level2FName = getResources().getString(R.string.level_tut_adv_fname);
                EcosystemManager.INSTANCE.setMapPath(levelPath + level1FName);
                EcosystemManager.INSTANCE.setSecondMapPath(levelPath + level2FName);
              }
            }
            Intent i = new Intent(TitleActivity.this, AutomataActivity.class);
            startActivity(i);
          }
        });

        builder.show();
      }
    });

    _resumeButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent i = new Intent(TitleActivity.this, AutomataActivity.class);
        startActivity(i);
      }
    });
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);

    if (getResources().getString(R.string.version).equals(PAID)) {
      _levels = new CharSequence[] { AUTOMATA_MAP_TITLE, RANDOM_HUGE_MAP_TITLE, RANDOM_LARGE_MAP_TITLE,
          RANDOM_SMALL_MAP_TITLE, RANDOM_TINY_MAP_TITLE, TUT_MAP_TITLE };
    }
    else {
      _levels = new CharSequence[] { TUT_MAP_TITLE };
    }

    if (EcosystemManager.INSTANCE.getEcosystem() == null) {
      _resumeButton.setVisibility(View.GONE);
    }
    else {
      _resumeButton.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, 1, Menu.NONE, R.string.about_menu_item);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case 1:
      Intent i = new Intent(TitleActivity.this, CreditsActivity.class);
      startActivity(i);
      break;
    }
    return false;
  }

}
