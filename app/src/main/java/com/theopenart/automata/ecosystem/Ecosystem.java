package com.theopenart.automata.ecosystem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;

public class Ecosystem {

  public interface EcosystemChangeListener {

    void onEcosystemChanged();
  }

  public interface EcosystemScoreChangeListener {

    void onEcosystemScoreChanged();
  }

  public interface EcosystemNewMapListener {

    void onEcosystemNewMap(int width, int height);
  }

  protected class UpdateTask implements Runnable {

    @Override
    public void run() {
      long start = System.nanoTime();
      if (_graveyard.size() != _organisms.size()) {
        // grow the food if it's time
        if (_updatesSinceGrowth >= _growPeriod) {
          for (int i = 0; i < _width; ++i) {
            for (int j = 0; j < _height; ++j) {
              Food f = getFoodAt(i, j);
              if (f == null) {
                throw new Error("got null food in Ecosystem");
              }
              if (f.getJelly() != null) {
                // grow what's already there
                f.incMaturity();
              }
              else {
                // try to grow a new one based on surrounding cells
                checkSurroundingAndPlantNew(i, j);
              }
            }
          }
          _updatesSinceGrowth = 1;
        }
        else {
          ++_updatesSinceGrowth;
        }

        // do organism stuff
        int extant = numExtantSpecies();
        for (int i = 0; i < _organisms.size(); ++i) {
          Organism o = _organisms.get(i);
          if (!o.isDead()) {
            o.update();

            // check if this organism NEEDS TO FUCK (itself (or a corpse))
            if (o.isFed()) {
              if (_graveyard.size() > 0) {
                Organism c = _graveyard.remove();
                c.reincarnate(o);
              }
              else {
                _organisms.add(new Organism(o));
              }
              _score += o.size() * extant;
              if (_score > _scoreHighWaterMark) {
                _scoreHighWaterMark = _score;
              }
              o.clearFed();
            }

            // check if this organism NEEDS TO DIE
            if (o.getAge() >= o.getSpecies().getLongevity()) {
              o.kill(_graveyard);
            }
          }
        }
        notifyScoreChangeListeners();
      }

      notifyListeners();

      long duration = (System.nanoTime() - start) / 1000000l;
      long delay = Ecosystem.MIN_UPDATE_TIME - duration;
      if (delay > 0) {
        // Log.v(getClass().getCanonicalName(), String.format("duration:%d | next update:%d", duration, delay));
        scheduleWithDelay(_updateTask, delay);
      }
      else {
        // Log.v(getClass().getCanonicalName(), String.format("duration:%d | next update:immediate", duration));
        scheduleForImmediateExecution(_updateTask);
      }
    }
  }

  /**
   * The number of update cycles that consitututes a 'turn'.
   */
  public static final int                          TURN_LENGTH           = 20;

  /**
   * Used to determine what the grow period should be based on the size of this Ecosystem.
   */
  private static final float                       GROW_PERIOD_SIZE_MULT = 0.00667f;

  /**
   * Used to determine what the grow period should be based on turn length.
   */
  private static final float                       GROW_PERIOD_TURN_MULT = 0.025f;

  /**
   * Period of each update cycle, in milliseconds. The Ecosystem will try to perform the update within this period,
   * however it could take longer.
   */
  protected static final int                       MIN_UPDATE_TIME       = 50;

  protected UpdateTask                             _updateTask           = new UpdateTask();

  protected ArrayList<Organism>                    _organisms;

  protected LinkedList<Organism>                   _graveyard;

  /**
   * A grow event for Food will occur every GROW_PERIOD update cycles.
   */
  private int                                      _growPeriod;

  private LinkedList<EcosystemChangeListener>      _ecosystemChangeListeners;

  private LinkedList<EcosystemNewMapListener>      _ecosystemNewMapListeners;

  private LinkedList<EcosystemScoreChangeListener> _ecosystemScoreChangeListeners;

  private ScheduledExecutorService                 _executor;

  private Food[][]                                 _foods;

  private int                                      _height;

  private boolean                                  _paused;

  private int                                      _score                = 0;

  private int                                      _scoreHighWaterMark   = 0;

  private List<Species>                            _species;

  private int                                      _updatesSinceGrowth   = 1;

  private int                                      _width;

  /**
   * Creates an Ecosystem from <code>map</code>. The food is placed according to the color of each pixel in the bitmap,
   * the maturity of the food being determined by the color saturation of that pixel.
   * 
   * @param map
   */
  public Ecosystem(Bitmap map) {
    loadMap(map);
    _ecosystemChangeListeners = new LinkedList<EcosystemChangeListener>();
    _ecosystemNewMapListeners = new LinkedList<EcosystemNewMapListener>();
    _ecosystemScoreChangeListeners = new LinkedList<EcosystemScoreChangeListener>();
  }

  /**
   * Creates an ecosystem of the given size, with randomized, fully-grown food.
   * 
   * @param width
   * @param height
   */
  public Ecosystem(int width, int height) {
    _width = width;
    _height = height;

    _foods = new Food[_width][_height];

    _organisms = new ArrayList<Organism>(_width * _height);

    _graveyard = new LinkedList<Organism>();

    _species = new LinkedList<Species>();

    _ecosystemChangeListeners = new LinkedList<EcosystemChangeListener>();
    _ecosystemNewMapListeners = new LinkedList<EcosystemNewMapListener>();
    _ecosystemScoreChangeListeners = new LinkedList<EcosystemScoreChangeListener>();

    _growPeriod = (int)((_width * _height * GROW_PERIOD_SIZE_MULT) * (TURN_LENGTH * GROW_PERIOD_TURN_MULT));

    _paused = true;

    for (int i = 0; i < _width; ++i) {
      for (int j = 0; j < _height; ++j) {
        Food f = new Food();
        double c = Math.random();

        if (c < 0.333) {
          f.setJelly(Jelly.FAST);
        }
        else if (c < 0.666) {
          f.setJelly(Jelly.HEALTHY);
        }
        else {
          f.setJelly(Jelly.SMART);
        }

        // f.setJelly(Jelly.HEALTHY);

        // f.setMaturity((int)(Math.random() * 100) + 1);
        f.setMaturity(100);
        _foods[i][j] = f;
      }
    }
  }

  public void addEcosystemChangeListener(EcosystemChangeListener listener) {
    synchronized (_ecosystemChangeListeners) {
      _ecosystemChangeListeners.add(listener);
    }
  }

  public void removeEcosystemChangeListener(EcosystemChangeListener listener) {
    synchronized (_ecosystemChangeListeners) {
      while (_ecosystemChangeListeners.remove(listener))
        ;
    }
  }

  public void addEcosystemNewMapListener(EcosystemNewMapListener listener) {
    synchronized (_ecosystemNewMapListeners) {
      _ecosystemNewMapListeners.add(listener);
    }
  }

  public void removeEcosystemNewMapListener(EcosystemNewMapListener listener) {
    synchronized (_ecosystemNewMapListeners) {
      while (_ecosystemNewMapListeners.remove(listener))
        ;
    }
  }

  public void addEcosystemScoreChangeListener(EcosystemScoreChangeListener listener) {
    synchronized (_ecosystemScoreChangeListeners) {
      _ecosystemScoreChangeListeners.add(listener);
    }
  }

  public void removeEcosystemScoreChangeListener(EcosystemScoreChangeListener listener) {
    synchronized (_ecosystemScoreChangeListeners) {
      while (_ecosystemScoreChangeListeners.remove(listener))
        ;
    }
  }

  public void createSpeciesAt(int x, int y, List<Jelly> dna) {
    // Log.v(getClass().getCanonicalName(), "trying to create a new species...");
    if (_score > _scoreHighWaterMark) {
      _scoreHighWaterMark = _score;
    }
    _score *= 0.8f;
    Species s = new Species(this, dna);
    _species.add(s);
    _organisms.add(new Organism(x, y, s));
  }

  public Food getFoodAt(int x, int y) {
    if (x < _width && x >= 0 && y < _height && y >= 0) {
      return _foods[x][y];
    }
    else {
      // Log.i(getClass().getCanonicalName(), "tried to get a Food out of Ecosystem bounds");
      return null;
    }
  }

  public int getHeight() {
    return _height;
  }

  public int getHighestScore() {
    return _scoreHighWaterMark;
  }

  /**
   * @return The Organisms present in this Ecosystem when this method was called. It would be wise not to molest this
   *         List.
   */
  public ArrayList<Organism> getOrganisms() {
    return _organisms;
  }

  public int getScore() {
    return _score;
  }

  public int getWidth() {
    return _width;
  }

  public boolean isPaused() {
    return _paused;
  }

  /**
   * @return The number of extant Species in this Ecosystem.
   */
  public int numExtantSpecies() {
    int extant = 0;
    for (Species s : _species) {
      if (s.getExtantPopulation() > 0) {
        ++extant;
      }
    }
    return extant;
  }

  /**
   * @return The number of live Organisms in this Ecosystem.
   */
  public int numOrganisms() {
    return _organisms.size() - _graveyard.size();
  }

  /**
   * @return The number of Species in this Ecosystem, extinct or not.
   */
  public int numSpecies() {
    return _species.size();
  }

  public void pause() {
    if (!_paused) {
      _executor.shutdown();
      try {
        _executor.awaitTermination(5, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        // Log.e(getClass().getCanonicalName(), "updater thread interrupted while shutting down for some reason");
      }
      finally {
        _paused = true;
      }
    }
  }

  public void resume() {
    if (_paused) {
      _executor = Executors.newSingleThreadScheduledExecutor();
      scheduleForImmediateExecution(_updateTask);
      _paused = false;
    }
  }

  protected void loadMap(Bitmap map) {
    _width = map.getWidth();
    _height = map.getHeight();

    _foods = new Food[_width][_height];

    _organisms = new ArrayList<Organism>(_width * _height);

    _graveyard = new LinkedList<Organism>();

    _species = new LinkedList<Species>();

    _growPeriod = (int)((_width * _height * GROW_PERIOD_SIZE_MULT) * (TURN_LENGTH * GROW_PERIOD_TURN_MULT));

    _paused = true;

    for (int i = 0; i < _width; ++i) {
      for (int j = 0; j < _height; ++j) {
        Food f = new Food();

        int color = map.getPixel(i, j);
        color &= 0x00FFFFFF;
        int red = (color & 0x00FF0000) >>> 16;
        int green = (color & 0x0000FF00) >>> 8;
        int blue = color & 0x000000FF;

        if (red > green && red > blue) {
          f.setJelly(Jelly.FAST);
          f.setMaturity((int)(red * (100f / 255f)));
        }
        else if (green > red && green > blue) {
          f.setJelly(Jelly.HEALTHY);
          f.setMaturity((int)(green * (100f / 255f)));
        }
        else if (blue > red && blue > green) {
          f.setJelly(Jelly.SMART);
          f.setMaturity((int)(blue * (100f / 255f)));
        }

        _foods[i][j] = f;
      }
    }

    notifyScoreChangeListeners();
    notifyNewMapListeners();
    notifyListeners();

  }

  protected void scheduleForImmediateExecution(UpdateTask task) {
    _executor.execute(task);
  }

  protected void scheduleWithDelay(UpdateTask task, long delay) {
    _executor.schedule(task, delay, TimeUnit.MILLISECONDS);
  }

  /**
   * Returns a random square in the Ecosystem at most <code>radius</code> squares from <code>center</code>.
   * 
   * @param center
   * @param radius
   * @return
   */
  int getRandomX(int center, int radius) {
    int delta;
    if (center >= radius && center < _width - radius) {
      // in the middle somewhere
      delta = (int)(Math.random() * radius) + 1;
      if (Math.random() < 0.5) {
        delta *= -1;
      }
    }
    else if (center < _width - radius) {
      // close to the left edge
      int rightDelta = (int)(Math.random() * radius) + 1;
      int leftDelta = center != 0 ? (int)(Math.random() * center) + 1 : 0;
      if (Math.random() < leftDelta / (double)(rightDelta + leftDelta)) {
        delta = -leftDelta;
      }
      else {
        delta = rightDelta;
      }
    }
    else if (center >= radius) {
      // close to the right edge
      int leftDelta = (int)(Math.random() * radius) + 1;
      int rightDelta = center != _width - 1 ? (int)(Math.random() * (_width - 1 - center)) + 1 : 0;
      if (Math.random() < rightDelta / (double)(rightDelta + leftDelta)) {
        delta = rightDelta;
      }
      else {
        delta = -leftDelta;
      }
    }
    else {
      // this ecosystem is too small, fuck it
      return 0;
    }

    int result = center + delta;
    if (result < 0 || result >= _width) {
      throw new Error("your math was wrong");
    }
    return result;
    // return center + delta;
  }

  /**
   * Returns a random square in the Ecosystem at most <code>radius</code> squares from <code>center</code>.
   * 
   * @param center
   * @param radius
   * @return
   */
  int getRandomY(int center, int radius) {
    int delta;
    if (center >= radius && center < _height - radius) {
      // in the middle somewhere
      delta = (int)(Math.random() * radius) + 1;
      if (Math.random() < 0.5) {
        delta *= -1;
      }
    }
    else if (center < _height - radius) {
      // close to the left edge
      int rightDelta = (int)(Math.random() * radius) + 1;
      int leftDelta = center != 0 ? (int)(Math.random() * center) + 1 : 0;
      if (Math.random() < leftDelta / (double)(rightDelta + leftDelta)) {
        delta = -leftDelta;
      }
      else {
        delta = rightDelta;
      }
    }
    else if (center >= radius) {
      // close to the right edge
      int leftDelta = (int)(Math.random() * radius) + 1;
      int rightDelta = center != _height - 1 ? (int)(Math.random() * (_height - 1 - center)) + 1 : 0;
      if (Math.random() < rightDelta / (double)(rightDelta + leftDelta)) {
        delta = rightDelta;
      }
      else {
        delta = -leftDelta;
      }
    }
    else {
      // this ecosystem is too small, fuck it
      return 0;
    }

    int result = center + delta;
    if (result < 0 || result >= _height) {
      throw new Error("your math was wrong");
    }
    return result;
    // return center + delta;
  }

  /**
   * Helper method to check food surrounding at the given point and try to grow a new food there.
   * 
   * @param x
   * @param y
   */
  private void checkSurroundingAndPlantNew(int x, int y) {
    short[] xds = { -1, 0, 1, 1, 1, 0, -1, -1 };
    short[] yds = { -1, -1, -1, 0, 1, 1, 1, 0 };
    int f = 0, h = 0, s = 0;

    for (int i = 0; i < 8; ++i) {
      Food nearFood;
      nearFood = getFoodAt(x + xds[i], y + yds[i]);
      if (nearFood != null && nearFood.getJelly() != null) {
        switch (nearFood.getJelly()) {
        case FAST:
          f += nearFood.getMaturity();
          break;
        case HEALTHY:
          h += nearFood.getMaturity();
          break;
        case SMART:
          s += nearFood.getMaturity();
          break;
        }
      }
    }

    Jelly j = null;
    double total = f + h + s;
    if (total > 150) {
      double rand = Math.random();
      if (rand < f / total) {
        j = Jelly.FAST;
      }
      else if (rand < (f + h) / total) {
        j = Jelly.HEALTHY;
      }
      else {
        j = Jelly.SMART;
      }
    }

    Food food = getFoodAt(x, y);
    food.setJelly(j);
    food.setMaturity(j != null ? 1 : 0);
  }

  private void notifyListeners() {
//    long startTime = System.nanoTime();
    if (_ecosystemChangeListeners != null) {
      synchronized (_ecosystemChangeListeners) {
        for (EcosystemChangeListener l : _ecosystemChangeListeners) {
          l.onEcosystemChanged();
        }
      }
    }
//    long duration = (System.nanoTime() - startTime) / 1000000l;
//    Log.v(getClass().getCanonicalName(), String.format("drawing took %d ms", duration));
  }

  private void notifyNewMapListeners() {
    if (_ecosystemNewMapListeners != null) {
      synchronized (_ecosystemNewMapListeners) {
        for (EcosystemNewMapListener l : _ecosystemNewMapListeners) {
          l.onEcosystemNewMap(_width, _height);
        }
      }
    }
  }

  private void notifyScoreChangeListeners() {
    if (_ecosystemScoreChangeListeners != null) {
      synchronized (_ecosystemScoreChangeListeners) {
        for (EcosystemScoreChangeListener l : _ecosystemScoreChangeListeners) {
          l.onEcosystemScoreChanged();
        }
      }
    }
  }
}
