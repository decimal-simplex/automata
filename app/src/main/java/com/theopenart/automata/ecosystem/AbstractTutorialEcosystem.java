package com.theopenart.automata.ecosystem;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.PowerManager;

public abstract class AbstractTutorialEcosystem extends Ecosystem {

  protected Activity              _ctx;

  protected PowerManager.WakeLock _wakeLock;

  public AbstractTutorialEcosystem(Bitmap map, Activity ctx) {
    super(map);
    _ctx = ctx;
    PowerManager pm = (PowerManager)ctx.getSystemService(Context.POWER_SERVICE);
    _wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getCanonicalName());
  }

  public final void updateParentActivity(Activity ctx) {
    _ctx = ctx;
    buildDialogs(ctx);
  }

  protected final void showDialog(final Dialog dlg) {
    _ctx.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        if (!_wakeLock.isHeld()) {
          _wakeLock.acquire();
        }
        dlg.show();
      }
    });
  }
  
  protected abstract void buildDialogs(Activity ctx);

}
