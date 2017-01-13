package com.theopenart.automata;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.theopenart.automata.ecosystem.Cell;
import com.theopenart.automata.ecosystem.Ecosystem;
import com.theopenart.automata.ecosystem.Ecosystem.EcosystemChangeListener;
import com.theopenart.automata.ecosystem.Ecosystem.EcosystemNewMapListener;
import com.theopenart.automata.ecosystem.Food;
import com.theopenart.automata.ecosystem.Jelly;
import com.theopenart.automata.ecosystem.Organism;

/**
 * @author daniel
 * 
 */
public class EcosystemView extends SurfaceView implements SurfaceHolder.Callback, EcosystemChangeListener,
    EcosystemNewMapListener {

  private static final int      NUM_ALPHAS = 64;         // needs to be a power of two less than 256

  private float                 _blockHeight;

  private float                 _blockWidth;

  private final Bitmap[]        _blueCell;

  private final Bitmap[]        _blueCellFilled;

//  private final Bitmap[]        _blueFood;

  private final RectF           _bounds    = new RectF();

  private final Ecosystem       _ecosystem;

  private final Bitmap[]        _greenCell;

  private final Bitmap[]        _greenCellFilled;

//  private final Bitmap[]        _greenFood;

  private final Paint           _paint     = new Paint();

  private final Bitmap[]        _redCell;

  private final Bitmap[]        _redCellFilled;

//  private final Bitmap[]        _redFood;

  private final Bitmap          _foodAlphaMap;

  private Bitmap                _foodAlphaPattern;

  private final SurfaceHolder   _holder;

  private PowerManager.WakeLock _wakeLock;

  public static Bitmap loadBitmap(Drawable sprite, Bitmap.Config config) {
    int w = sprite.getIntrinsicWidth(), h = sprite.getIntrinsicHeight();
    Bitmap b = Bitmap.createBitmap(w, h, config);
    Canvas c = new Canvas(b);
    sprite.setBounds(0, 0, w, h);
    sprite.draw(c);
    return b;
  }

  public static Bitmap[] loadAlphaBitmaps(Drawable sprite, Bitmap.Config config) {
    int w = sprite.getIntrinsicWidth(), h = sprite.getIntrinsicHeight();
    Bitmap[] b = new Bitmap[NUM_ALPHAS];
    int gap = 256 / NUM_ALPHAS;
    sprite.setBounds(0, 0, w, h);

    b[0] = Bitmap.createBitmap(w, h, config);
    Canvas c = new Canvas(b[0]);
    sprite.setAlpha(0);
    sprite.draw(c);

    for (int i = 1; i < NUM_ALPHAS; ++i) {
      b[i] = Bitmap.createBitmap(w, h, config);
      c = new Canvas(b[i]);
      sprite.setAlpha((i + 1) * gap - 1);
      sprite.draw(c);
    }

    return b;
  }

  EcosystemView(Context context, final SpeciesCreator speciesCreator, Ecosystem ecosystem) {
    super(context);

    PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
    _wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getCanonicalName());

    _ecosystem = ecosystem;

    _redCell = loadAlphaBitmaps(getResources().getDrawable(R.drawable.red_cell_32), Bitmap.Config.ARGB_4444);
    _greenCell = loadAlphaBitmaps(getResources().getDrawable(R.drawable.green_cell_32), Bitmap.Config.ARGB_4444);
    _blueCell = loadAlphaBitmaps(getResources().getDrawable(R.drawable.blue_cell_32), Bitmap.Config.ARGB_4444);
    _redCellFilled = loadAlphaBitmaps(getResources().getDrawable(R.drawable.red_cell_filled_32),
                                      Bitmap.Config.ARGB_4444);
    _greenCellFilled = loadAlphaBitmaps(getResources().getDrawable(R.drawable.green_cell_filled_32),
                                        Bitmap.Config.ARGB_4444);
    _blueCellFilled = loadAlphaBitmaps(getResources().getDrawable(R.drawable.blue_cell_filled_32),
                                       Bitmap.Config.ARGB_4444);
//    _redFood = loadAlphaBitmaps(getResources().getDrawable(R.drawable.food_red_16), Bitmap.Config.RGB_565);
//    _greenFood = loadAlphaBitmaps(getResources().getDrawable(R.drawable.food_green_16), Bitmap.Config.RGB_565);
//    _blueFood = loadAlphaBitmaps(getResources().getDrawable(R.drawable.food_blue_16), Bitmap.Config.RGB_565);

    _foodAlphaMap = loadBitmap(getResources().getDrawable(R.drawable.food_alpha_32), Bitmap.Config.ALPHA_8);

    _holder = getHolder();
    _holder.addCallback(this);

    setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          if (speciesCreator.getDna().size() > 0) {
            int ecosystemX = (int)(event.getX() / getWidth() * _ecosystem.getWidth());
            int ecosystemY = (int)(event.getY() / getHeight() * _ecosystem.getHeight());
            _ecosystem.createSpeciesAt(ecosystemX, ecosystemY, speciesCreator.getDna());
            speciesCreator.clear();
          }
          else if (_ecosystem.isPaused()) {
            _ecosystem.resume();
            if (!_wakeLock.isHeld()) {
              _wakeLock.acquire();
            }
          }
          else {
            _ecosystem.pause();
            if (_wakeLock.isHeld()) {
              _wakeLock.release();
            }
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
  protected void onDraw(Canvas canvas) {
    // render the food
    for (int i = 0; i < _ecosystem.getWidth(); ++i) {
      for (int j = 0; j < _ecosystem.getHeight(); ++j) {
        Food f = _ecosystem.getFoodAt(i, j);

        _bounds.set(i * _blockWidth, j * _blockHeight, (i + 1) * _blockWidth, (j + 1) * _blockHeight);

        Jelly jelly = f.getJelly();

        // using a switch here would be slower due to the need to check if jelly is null
        if (jelly == Jelly.FAST) {
          // canvas.drawBitmap(_redFood[(NUM_ALPHAS - 1) * f.getMaturity() / 100], null, _bounds, _paint);
          _paint.setARGB(255, 255 * f.getMaturity() / 100, 0, 0);
          canvas.drawRect(_bounds, _paint);
        }
        else if (jelly == Jelly.HEALTHY) {
          // canvas.drawBitmap(_greenFood[(NUM_ALPHAS - 1) * f.getMaturity() / 100], null, _bounds, _paint);
          _paint.setARGB(255, 0, 255 * f.getMaturity() / 100, 0);
          canvas.drawRect(_bounds, _paint);
        }
        else if (jelly == Jelly.SMART) {
          // canvas.drawBitmap(_blueFood[(NUM_ALPHAS - 1) * f.getMaturity() / 100], null, _bounds, _paint);
          _paint.setARGB(255, 0, 0, 255 * f.getMaturity() / 100);
          canvas.drawRect(_bounds, _paint);
        }
        else {
          // canvas.drawBitmap(_redFood[0], null, _bounds, _paint);
          _paint.setARGB(255, 255 * f.getMaturity() / 100, 0, 0);
          canvas.drawRect(_bounds, _paint);
        }
      }
    }

    // render the food alpha pattern
    _bounds.set(0, 0, getWidth(), getHeight());
    _paint.setColor(Color.BLACK);
    canvas.drawBitmap(getFoodAlphaPattern(), null, _bounds, _paint);

    // render each organism
    ArrayList<Organism> organisms = _ecosystem.getOrganisms();
    if (organisms.size() > 0) {
      for (int i = 0; i < organisms.size(); ++i) {
        if (organisms.get(i).isDead()) {
          continue;
        }
        Organism o = organisms.get(i);

        // For fading out the organism as it approaches death.
        int alpha = NUM_ALPHAS - 1;
        if (o.getAge() > o.getSpecies().getLongevity() - Ecosystem.TURN_LENGTH * 2) {
          alpha *= (o.getSpecies().getLongevity() - o.getAge()) / (Ecosystem.TURN_LENGTH * 2);
          alpha = alpha < 0 ? 0 : alpha;
        }

        for (int j = o.size() - 1; j >= 0; --j) {
          Cell c = o.getCell(j);

          // The offsets for drawing based on how far along the cell is to the next block.
          float xOffset = (c.getxDest() - c.getxPos()) * o.getProgress() * _blockWidth / Ecosystem.TURN_LENGTH;
          float yOffset = (c.getyDest() - c.getyPos()) * o.getProgress() * _blockHeight / Ecosystem.TURN_LENGTH;

          _bounds.set(c.getxPos() * _blockWidth + xOffset, c.getyPos() * _blockHeight + yOffset, c.getxPos()
                                                                                                 * _blockWidth
                                                                                                 + xOffset
                                                                                                 + _blockWidth,
                      c.getyPos() * _blockHeight + yOffset + _blockHeight);

          switch (c.getJelly()) {
          case FAST:
            if (c.isFed()) {
              canvas.drawBitmap(_redCellFilled[alpha], null, _bounds, _paint);
            }
            else {
              canvas.drawBitmap(_redCell[alpha], null, _bounds, _paint);
            }
            break;
          case HEALTHY:
            if (c.isFed()) {
              canvas.drawBitmap(_greenCellFilled[alpha], null, _bounds, _paint);
            }
            else {
              canvas.drawBitmap(_greenCell[alpha], null, _bounds, _paint);
            }
            break;
          case SMART:
            if (c.isFed()) {
              canvas.drawBitmap(_blueCellFilled[alpha], null, _bounds, _paint);
            }
            else {
              canvas.drawBitmap(_blueCell[alpha], null, _bounds, _paint);
            }
            break;
          default: // WTF
            canvas.drawBitmap(_redCell[alpha], null, _bounds, _paint);
            break;
          }
        }
      }
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    _blockWidth = (float)w / _ecosystem.getWidth();
    _blockHeight = (float)h / _ecosystem.getHeight();
    _foodAlphaPattern = null;
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    _ecosystem.addEcosystemChangeListener(this);
    _ecosystem.addEcosystemNewMapListener(this);
    _ecosystem.resume();
  }

  @SuppressLint("WrongCall")
  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    _blockWidth = (float)width / _ecosystem.getWidth();
    _blockHeight = (float)height / _ecosystem.getHeight();
    _foodAlphaPattern = null;
    Canvas canvas = null;
    try {
      canvas = _holder.lockCanvas();
      synchronized (_holder) {
        onDraw(canvas);
      }
    }
    catch (Throwable t) {
      Log.e(getClass().getCanonicalName(), "error in onDraw()", t);
      throw(new Error(t));
    }
    finally {
      if (canvas != null) {
        _holder.unlockCanvasAndPost(canvas);
      }
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    _ecosystem.pause();
    _ecosystem.removeEcosystemChangeListener(this);
    _ecosystem.removeEcosystemNewMapListener(this);
  }

  @SuppressLint("WrongCall")
  @Override
  public void onEcosystemChanged() {
    Canvas canvas = null;
    try {
      canvas = _holder.lockCanvas();
      synchronized (_holder) {
        onDraw(canvas);
      }
    }
    catch (Throwable t) {
      Log.e(getClass().getCanonicalName(), "error in onDraw()", t);
      throw(new Error(t));
    }
    finally {
      if (canvas != null) {
        _holder.unlockCanvasAndPost(canvas);
      }
    }
  }

  @Override
  public void onEcosystemNewMap(int width, int height) {
    _blockWidth = (float)getWidth() / width;
    _blockHeight = (float)getHeight() / height;
    _foodAlphaPattern = null;
  }

  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus) {
    super.onWindowFocusChanged(hasWindowFocus);
    if (hasWindowFocus) {
      _ecosystem.resume();
      if (!_wakeLock.isHeld()) {
        _wakeLock.acquire();
      }
    }
    else {
      _ecosystem.pause();
      if (_wakeLock.isHeld()) {
        _wakeLock.release();
      }
    }
  }

  private Bitmap getFoodAlphaPattern() {
    if (_foodAlphaPattern == null) {
      _foodAlphaPattern = Bitmap.createBitmap((int)(_ecosystem.getWidth() * _blockWidth),
                                              (int)(_ecosystem.getHeight() * _blockHeight), Bitmap.Config.ALPHA_8);
      
      Canvas canvas = new Canvas(_foodAlphaPattern);
      RectF bounds = new RectF();

      for (int i = 0; i < _ecosystem.getWidth(); ++i) {
        for (int j = 0; j < _ecosystem.getHeight(); ++j) {
          bounds.set(i * _blockWidth, j * _blockHeight, (i + 1) * _blockWidth, (j + 1) * _blockHeight);
          canvas.drawBitmap(_foodAlphaMap, null, bounds, _paint);
        }
      }
    }
    return _foodAlphaPattern;
  }
}
