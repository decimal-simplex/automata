package com.theopenart.automata.ecosystem;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.theopenart.automata.R;
import com.theopenart.automata.TitleActivity;

public class TutorialEco extends AbstractTutorialEcosystem {

  protected class BeginnerUpdateTask extends Ecosystem.UpdateTask {

    @Override
    public void run() {
      int redCount, blueGreenCount, redGreenBlueCount, blueGreenRedX2Count;
      switch (_stage) {
      case BEG_WELCOME:
        // Still in the welcome dialogue, so ignore this.
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case BEG_TEACH_RED_DLG:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case BEG_PLACE_RED:
        super.run();
        // check that a red creature was added
        if (_organisms.size() > 0) {
          Organism first = null; // the first ALIVE organism
          for (Organism o : _organisms) {
            if (!o.isDead()) {
              first = o;
              break;
            }
          }

          if (first != null) {
            if (first.size() == 1 && first.getCell(0).getJelly() == Jelly.FAST) {
              // correctly placed
              _stage = Stage.BEG_WAIT_GROW_RED;
            }
            else {
              // incorrectly placed
              for (Organism o : _organisms) {
                if (!o.isDead()) {
                  o.kill(_graveyard);
                }
              }
              showDialog(_dlgBeg3FailRed);
              _stage = Stage.BEG_FAIL_RED;
            }
          }
        }
        break;
      case BEG_FAIL_RED:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case BEG_WAIT_GROW_RED:
        super.run();
        redCount = 0;
        for (Organism o : _organisms) {
          if (!o.isDead() && o.size() == 1 && o.getCell(0).getJelly() == Jelly.FAST) {
            ++redCount;
          }
        }
        if (redCount > 12) {
          showDialog(_dlgBeg5TeachBlueGreen1);
          _stage = Stage.BEG_TEACH_BLUE_GREEN_DLG;
        }
        else if (redCount == 0) {
          // they all died
          showDialog(_dlgBeg4FailGrowRed);
          _stage = Stage.BEG_PLACE_RED;
        }
        break;
      case BEG_FAIL_GROW_RED:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case BEG_TEACH_BLUE_GREEN_DLG:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case BEG_PLACE_BLUE_GREEN:
        super.run();
        // check that a green-blue creature was added
        if (_organisms.size() > 0) {
          Organism first = null; // the first ALIVE multicelled organism
          for (Organism o : _organisms) {
            if (!o.isDead() && o.size() > 1) {
              first = o;
              break;
            }
            else if (!o.isDead() && o.size() == 1 && o.getCell(0).getJelly() != Jelly.FAST) {
              // this user is special: let it fail below
              first = o;
              break;
            }
          }

          if (first != null) {
            if (first.size() == 2 && first.getCell(0).getJelly() == Jelly.SMART
                && first.getCell(1).getJelly() == Jelly.HEALTHY) {
              // correctly placed
              _stage = Stage.BEG_WAIT_GROW_BLUE_GREEN;
            }
            else if (first.size() == 2 && first.getCell(0).getJelly() == Jelly.HEALTHY
                     && first.getCell(1).getJelly() == Jelly.SMART) {
              // correctly placed
              _stage = Stage.BEG_WAIT_GROW_BLUE_GREEN;
            }
            else {
              // incorrectly placed
              for (Organism o : _organisms) {
                if (!o.isDead() && (o.size() > 1 || o.getCell(0).getJelly() != Jelly.FAST)) {
                  o.kill(_graveyard);
                }
              }
              showDialog(_dlgBeg6FailBlueGreen);
              _stage = Stage.BEG_FAIL_BLUE_GREEN;
            }
          }
        }
        break;
      case BEG_FAIL_BLUE_GREEN:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case BEG_WAIT_GROW_BLUE_GREEN:
        super.run();
        blueGreenCount = 0;
        for (Organism o : _organisms) {
          if (!o.isDead() && o.size() == 2 && o.getCell(0).getJelly() == Jelly.HEALTHY
              && o.getCell(1).getJelly() == Jelly.SMART) {
            ++blueGreenCount;
          }
          else if (!o.isDead() && o.size() == 2 && o.getCell(0).getJelly() == Jelly.SMART
                   && o.getCell(1).getJelly() == Jelly.HEALTHY) {
            ++blueGreenCount;
          }
        }
        if (blueGreenCount > 8) {
          showDialog(_dlgBeg8Done);
          _stage = Stage.BEG_FINISH;
        }
        else if (blueGreenCount == 0) {
          // they all died
          showDialog(_dlgBeg7FailGrowBlueGreen);
          _stage = Stage.BEG_FAIL_GROW_BLUE_GREEN;
        }
        break;
      case BEG_FAIL_GROW_BLUE_GREEN:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case BEG_FINISH:
        super.run();
        break;
      case ADV_WELCOME:
        // Still in the welcome dialogue, so ignore this.
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case ADV_TEACH_RED_DLG:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case ADV_PLACE_RED_GREEN_BLUE:
        super.run();
        // check that a red-green-blue creature was added
        if (_organisms.size() > 0) {
          Organism first = null; // the first ALIVE organism
          for (Organism o : _organisms) {
            if (!o.isDead()) {
              first = o;
              break;
            }
          }

          if (first != null) {
            List<Jelly> dna = first.getSpecies().getDna();
            if (first.size() == 3 && dna.contains(Jelly.FAST) && dna.contains(Jelly.HEALTHY)
                && dna.contains(Jelly.SMART)) {
              // correctly placed
              _stage = Stage.ADV_WAIT_GROW_RED_GREEN_BLUE;
            }
            else {
              // incorrectly placed
              for (Organism o : _organisms) {
                if (!o.isDead()) {
                  o.kill(_graveyard);
                }
              }
              showDialog(_dlgAdv3FailRedGreenBlue);
              _stage = Stage.ADV_FAIL_RED_GREEN_BLUE;
            }
          }
        }
        break;
      case ADV_FAIL_RED_GREEN_BLUE:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case ADV_WAIT_GROW_RED_GREEN_BLUE:
        super.run();
        redGreenBlueCount = 0;
        for (Organism o : _organisms) {
          if (!o.isDead() && o.size() == 3) {
            ++redGreenBlueCount;
          }
        }
        if (redGreenBlueCount > 6) {
          showDialog(_dlgAdv5TeachBlueGreenRedX2);
          _stage = Stage.ADV_TEACH_BLUE_GREEN_DLG;
        }
        else if (redGreenBlueCount == 0) {
          // they all died
          showDialog(_dlgAdv4FailGrowRedGreenBlue);
          _stage = Stage.ADV_PLACE_RED_GREEN_BLUE;
        }
        break;
      case ADV_FAIL_GROW_RED_GREEN_BLUE:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case ADV_TEACH_BLUE_GREEN_DLG:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case ADV_PLACE_BLUE_GREEN_REDX2:
        super.run();
        // check that a green-blue-redx2 creature was added
        if (_organisms.size() > 0) {
          Organism first = null; // the first ALIVE four-celled organism
          for (Organism o : _organisms) {
            if (!o.isDead() && o.size() > 3) {
              first = o;
              break;
            }
          }

          if (first != null) {
            redCount = 0;
            List<Jelly> dna = first.getSpecies().getDna();
            for (Jelly j : dna) {
              if (j == Jelly.FAST) {
                ++redCount;
              }
            }
            if (first.size() == 4 && redCount == 2 && dna.contains(Jelly.HEALTHY) && dna.contains(Jelly.SMART)) {
              // correctly placed
              _stage = Stage.ADV_WAIT_GROW_BLUE_GREEN_REDX2;
            }
            else {
              // incorrectly placed
              for (Organism o : _organisms) {
                if (!o.isDead() && (o.size() > 3)) {
                  o.kill(_graveyard);
                }
              }
              showDialog(_dlgAdv6FailBlueGreenRedX2);
              _stage = Stage.ADV_FAIL_BLUE_GREEN_REDX2;
            }
          }
        }
        break;
      case ADV_FAIL_BLUE_GREEN_REDX2:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case ADV_WAIT_GROW_BLUE_GREEN_REDX2:
        super.run();
        blueGreenRedX2Count = 0;
        for (Organism o : _organisms) {
          redCount = 0;
          List<Jelly> dna = o.getSpecies().getDna();
          for (Jelly j : dna) {
            if (j == Jelly.FAST) {
              ++redCount;
            }
          }
          if (!o.isDead() && o.size() == 4 && redCount == 2 && dna.contains(Jelly.HEALTHY) && dna.contains(Jelly.SMART)) {
            ++blueGreenRedX2Count;
          }
        }
        if (blueGreenRedX2Count > 3) {
          showDialog(_dlgAdv8Done1);
          _stage = Stage.ADV_FINISH;
        }
        else if (blueGreenRedX2Count == 0) {
          // they all died
          showDialog(_dlgAdv7FailGrowBlueGreenRedX2);
          _stage = Stage.ADV_FAIL_GROW_BLUE_GREEN_REDX2;
        }
        break;
      case ADV_FAIL_GROW_BLUE_GREEN_REDX2:
        scheduleWithDelay(_updateTask, MIN_UPDATE_TIME);
        break;
      case ADV_FINISH:
        super.run();
        break;
      default:
        break;
      }
    }
  }

  private enum Stage {
    BEG_WELCOME,
    BEG_TEACH_RED_DLG,
    BEG_PLACE_RED,
    BEG_FAIL_RED,
    BEG_WAIT_GROW_RED,
    BEG_FAIL_GROW_RED,
    BEG_TEACH_BLUE_GREEN_DLG,
    BEG_PLACE_BLUE_GREEN,
    BEG_FAIL_BLUE_GREEN,
    BEG_WAIT_GROW_BLUE_GREEN,
    BEG_FAIL_GROW_BLUE_GREEN,
    BEG_FINISH,
    ADV_WELCOME,
    ADV_TEACH_RED_DLG,
    ADV_PLACE_RED_GREEN_BLUE,
    ADV_FAIL_RED_GREEN_BLUE,
    ADV_WAIT_GROW_RED_GREEN_BLUE,
    ADV_FAIL_GROW_RED_GREEN_BLUE,
    ADV_TEACH_BLUE_GREEN_DLG,
    ADV_PLACE_BLUE_GREEN_REDX2,
    ADV_FAIL_BLUE_GREEN_REDX2,
    ADV_WAIT_GROW_BLUE_GREEN_REDX2,
    ADV_FAIL_GROW_BLUE_GREEN_REDX2,
    ADV_FINISH, ;
  }

  private Stage  _stage = Stage.BEG_WELCOME;

  private Dialog _dlgBeg1Welcome;

  private Dialog _dlgBeg2TeachRed1;

  // private Dialog _dlgBeg2TeachRed2;

  private Dialog _dlgBeg3FailRed;

  private Dialog _dlgBeg4FailGrowRed;

  private Dialog _dlgBeg5TeachBlueGreen1;

  private Dialog _dlgBeg5TeachBlueGreen2;

  private Dialog _dlgBeg6FailBlueGreen;

  private Dialog _dlgBeg7FailGrowBlueGreen;

  private Dialog _dlgBeg8Done;

  private Dialog _dlgAdv1Welcome;

  private Dialog _dlgAdv2TeachRedGreenBlue1;

  private Dialog _dlgAdv2TeachRedGreenBlue2;

  private Dialog _dlgAdv3FailRedGreenBlue;

  private Dialog _dlgAdv4FailGrowRedGreenBlue;

  private Dialog _dlgAdv5TeachBlueGreenRedX2;

  private Dialog _dlgAdv6FailBlueGreenRedX2;

  private Dialog _dlgAdv7FailGrowBlueGreenRedX2;

  private Dialog _dlgAdv8Done1;

  private Dialog _dlgAdv8Done2;

  private Bitmap _secondMap;

  public TutorialEco(Bitmap firstMap, Bitmap secondMap, Activity ctx) {
    super(firstMap, ctx);
    _secondMap = secondMap;
    _updateTask = new BeginnerUpdateTask();

    buildDialogs(ctx);

    showDialog(_dlgBeg1Welcome);
  }

  @Override
  protected void buildDialogs(Activity ctx) {
    /* START EASY PORTION OF THE TUTORIAL **************************************************************************** */

    /* Create Welcome dialog. */
    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_beginner_dialog_title);
    builder.setMessage(R.string.tut_beginner_welcome_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.BEG_TEACH_RED_DLG;
        showDialog(_dlgBeg2TeachRed1);
      }
    });

    _dlgBeg1Welcome = builder.create();

    /* Create dialog for teaching to place a red creature. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_beginner_dialog_title);
    builder.setMessage(R.string.tut_beginner_teach_red_text1);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        // showDialog(_dlgBeg2TeachRed2);
        _stage = Stage.BEG_PLACE_RED;
      }
    });

    _dlgBeg2TeachRed1 = builder.create();

    /* Create dialog for teaching about removing cells from the species creator. */
    // builder = new AlertDialog.Builder(ctx);
    // builder.setTitle(R.string.tut_beginner_dialog_title);
    // builder.setMessage(R.string.tut_beginner_teach_red_text2);
    // builder.setCancelable(false);
    // builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // if (_wakeLock.isHeld()) {
    // _wakeLock.release();
    // }
    // _stage = Stage.BEG_PLACE_RED;
    // }
    // });
    //
    // _dlgBeg2TeachRed2 = builder.create();

    /* Create dialog for failed to properly create a red creature. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_beginner_dialog_title);
    builder.setMessage(R.string.tut_beginner_fail_red_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.BEG_PLACE_RED;
      }
    });

    _dlgBeg3FailRed = builder.create();

    /* Create dialog for all the red creatures died. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_beginner_dialog_title);
    builder.setMessage(R.string.tut_beginner_fail_grow_red_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.BEG_PLACE_RED;
      }
    });

    _dlgBeg4FailGrowRed = builder.create();

    /* Create dialog for teaching to place a green-blue creature. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_beginner_dialog_title);
    builder.setMessage(R.string.tut_beginner_teach_green_blue_text1);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        showDialog(_dlgBeg5TeachBlueGreen2);
      }
    });

    _dlgBeg5TeachBlueGreen1 = builder.create();

    /* Create dialog for teaching to place a green-blue creature. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_beginner_dialog_title);
    builder.setMessage(R.string.tut_beginner_teach_green_blue_text2);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.BEG_PLACE_BLUE_GREEN;
      }
    });

    _dlgBeg5TeachBlueGreen2 = builder.create();

    /* Create dialog for failed to properly create a green-blue creature. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_beginner_dialog_title);
    builder.setMessage(R.string.tut_beginner_fail_green_blue_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.BEG_PLACE_BLUE_GREEN;
      }
    });

    _dlgBeg6FailBlueGreen = builder.create();

    /* Create dialog for all the blue and green creatures died. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_beginner_dialog_title);
    builder.setMessage(R.string.tut_beginner_fail_grow_green_blue_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.BEG_PLACE_BLUE_GREEN;
      }
    });

    _dlgBeg7FailGrowBlueGreen = builder.create();

    /* Create dialog for tutorial finished. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_beginner_dialog_title);
    builder.setMessage(R.string.tut_beginner_done_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        // load the new map
        loadMap(_secondMap);
        resume();
        _stage = Stage.ADV_WELCOME;
        showDialog(_dlgAdv1Welcome);
      }
    });

    _dlgBeg8Done = builder.create();

    /* NEW MAP - START ADVANCED PORTION OF THE TUTORIAL ************************************************************** */

    /* Create Welcome dialog. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_advanced_dialog_title);
    builder.setMessage(R.string.tut_advanced_welcome_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.ADV_TEACH_RED_DLG;
        showDialog(_dlgAdv2TeachRedGreenBlue1);
      }
    });

    _dlgAdv1Welcome = builder.create();

    /* Create dialog for teaching to place a red-green-blue creature. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_advanced_dialog_title);
    builder.setMessage(R.string.tut_advanced_teach_red_green_blue_text1);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        showDialog(_dlgAdv2TeachRedGreenBlue2);
      }
    });

    _dlgAdv2TeachRedGreenBlue1 = builder.create();

    /* Create dialog for teaching to place a red-green-blue creature. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_advanced_dialog_title);
    builder.setMessage(R.string.tut_advanced_teach_red_green_blue_text2);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.ADV_PLACE_RED_GREEN_BLUE;
      }
    });

    _dlgAdv2TeachRedGreenBlue2 = builder.create();

    /* Create dialog for failed to properly create a red-green-blue creature. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_advanced_dialog_title);
    builder.setMessage(R.string.tut_advanced_fail_red_green_blue_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.ADV_PLACE_RED_GREEN_BLUE;
      }
    });

    _dlgAdv3FailRedGreenBlue = builder.create();

    /* Create dialog for all the red-green-blue creatures died. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_advanced_dialog_title);
    builder.setMessage(R.string.tut_advanced_fail_grow_red_green_blue_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.ADV_PLACE_RED_GREEN_BLUE;
      }
    });

    _dlgAdv4FailGrowRedGreenBlue = builder.create();

    /* Create dialog for teaching to place a green-blue-red-red creature. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_advanced_dialog_title);
    builder.setMessage(R.string.tut_advanced_teach_green_blue_redx2_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.ADV_PLACE_BLUE_GREEN_REDX2;
      }
    });

    _dlgAdv5TeachBlueGreenRedX2 = builder.create();

    /* Create dialog for failed to properly create a green-blue-red-red creature. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_advanced_dialog_title);
    builder.setMessage(R.string.tut_advanced_fail_green_blue_redX2_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.ADV_PLACE_BLUE_GREEN_REDX2;
      }
    });

    _dlgAdv6FailBlueGreenRedX2 = builder.create();

    /* Create dialog for all the blue and green and red-red creatures died. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_advanced_dialog_title);
    builder.setMessage(R.string.tut_advanced_fail_grow_green_blue_redx2_text);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        _stage = Stage.ADV_PLACE_BLUE_GREEN_REDX2;
      }
    });

    _dlgAdv7FailGrowBlueGreenRedX2 = builder.create();

    /* Create dialog for tutorial finished. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_advanced_dialog_title);
    builder.setMessage(R.string.tut_advanced_done_text1);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
        showDialog(_dlgAdv8Done2);
        ((TextView)_dlgAdv8Done2.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
      }
    });

    _dlgAdv8Done1 = builder.create();

    /* Create dialog for tutorial finished. */
    builder = new AlertDialog.Builder(ctx);
    builder.setTitle(R.string.tut_advanced_dialog_title);
    if (ctx.getResources().getString(R.string.version).equals(TitleActivity.PAID)) {
      builder.setMessage(R.string.tut_advanced_done_text2);
    }
    else {
      builder.setMessage(R.string.tut_advanced_done_text2_free);
    }
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (_wakeLock.isHeld()) {
          _wakeLock.release();
        }
      }
    });

    _dlgAdv8Done2 = builder.create();
  }
}
