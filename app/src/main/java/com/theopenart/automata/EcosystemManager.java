package com.theopenart.automata;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.theopenart.automata.ecosystem.Ecosystem;
import com.theopenart.automata.ecosystem.TutorialEco;

enum EcosystemManager {
  INSTANCE, ;

  private Ecosystem _ecosystem;

  private String    _mapPath;
  
  private String    _secondMapPath;

  private Activity  _parentActivity;

  private int       _width  = 45;

  private int       _height = 60;

  Ecosystem buildEcosystem(Context ctx) {
    try {
      Bitmap map = null;
      if (_mapPath != null) {
        map = BitmapFactory.decodeStream(ctx.getAssets().open(_mapPath));
      }
      if (map != null) {
        String mapsDirectoryPath = ctx.getResources().getString(R.string.level_path);
        String tutBegLevelPath = mapsDirectoryPath + ctx.getResources().getString(R.string.level_tut_beg_fname);
        if (_parentActivity != null && _mapPath.equals(tutBegLevelPath)) {
          Bitmap secondMap = BitmapFactory.decodeStream(ctx.getAssets().open(_secondMapPath));
          _ecosystem = new TutorialEco(map, secondMap, _parentActivity);
        }
        else {
          _ecosystem = new Ecosystem(map);
        }
      }
      else {
        _ecosystem = new Ecosystem(_width, _height);
      }
    }
    catch (IOException ioe) {
      _ecosystem = new Ecosystem(45, 60);
    }
    return _ecosystem;
  }

  /**
   * 
   * @return
   */
  boolean clear() {
    boolean result = _ecosystem != null;
    _ecosystem = null;
    _mapPath = null;
    _parentActivity = null;
    _width = 45;
    _height = 60;
    return result;
  }

  /**
   * Returns the current Ecosystem owned by this manager.
   * 
   * @return
   */
  Ecosystem getEcosystem() {
    return _ecosystem;
  }

  /**
   * Only necessary for the tutorial levels. Other level types don't need to set this, but it doesn't hurt any.
   * 
   * @param ctx
   * @return
   */
  EcosystemManager setParentActivity(Activity ctx) {
    _parentActivity = ctx;
    if (_ecosystem != null && _ecosystem instanceof TutorialEco) {
      ((TutorialEco)_ecosystem).updateParentActivity(ctx);
    }
    return this;
  }

  /**
   * For all levels other than the tutorial this is the only map that needs to be set.
   * @param path
   * @return
   */
  EcosystemManager setMapPath(String path) {
    _mapPath = path;
    return this;
  }

  /**
   * This is necessary for the tutorial which will load a new map halfway through.
   * 
   * @param path
   * @return
   */
  EcosystemManager setSecondMapPath(String path) {
    _secondMapPath = path;
    return this;
  }

  EcosystemManager setDimension(int width, int height) {
    _width = width;
    _height = height;
    return this;
  }
}
