package com.theopenart.automata.ecosystem;

import java.util.ArrayList;
import java.util.List;

public class Organism {

  /** This Organism's age. */
  private int             _age;

  private ArrayList<Cell> _body;

  private boolean         _dead;

  /** Progress toward next square. */
  private int             _progress;

  /** The number of cells in this Organism. Not necessarily the same as _body.size(). */
  private int             _size;

  /** The Species of this Organism. */
  private Species         _species;

  private int             _waitCounter;

  /** The x coordinate this Organism is headed towards. */
  private int             _xDest;

  /** The y coordinate this Organism is headed towards. */
  private int             _yDest;

  Organism(int xPos, int yPos, Species species) {
    _species = species;

    _body = new ArrayList<Cell>(Species.MAX_JELLY_DNA);
    for (int i = 0; i < Species.MAX_JELLY_DNA; ++i) {
      Cell cell = new Cell(Jelly.FAST, getEcosystem());
      _body.add(cell);
    }

    int j = 0;
    for (Jelly jelly : _species.getDna()) {
      Cell cell = _body.get(j++);
      cell.setxPos(xPos);
      cell.setyPos(yPos);
      cell.setxDest(xPos);
      cell.setyDest(yPos);
      cell.setJelly(jelly);
    }

    _xDest = xPos;
    _yDest = yPos;

    _species.incHistoricalPopulation();
    _species.incExtantPopulation();

    _age = 0;
    _progress = 0;
    _dead = false;
    _size = species.getDna().size();
    // force the organism to wait a bit after breeding
    _waitCounter = (int)(Ecosystem.TURN_LENGTH * 0.07f);
  }

  Organism(Organism parent) {
    this(parent._body.get(0).getxPos(), parent._body.get(0).getyPos(), parent._species);
  }

  /**
   * WISE FWOM YOUR GWAVE
   * 
   * @param xPos
   * @param yPos
   * @param species
   * @return
   */
  Organism reincarnate(int xPos, int yPos, Species species, int progress) {
    _species = species;

    int j = 0;
    for (Jelly jelly : _species.getDna()) {
      Cell cell = _body.get(j++);
      cell.setxPos(xPos);
      cell.setyPos(yPos);
      cell.setxDest(xPos);
      cell.setyDest(yPos);
      cell.setJelly(jelly);
      cell.setFed(false);
    }

    _xDest = xPos;
    _yDest = yPos;

    _species.incHistoricalPopulation();
    _species.incExtantPopulation();

    _age = 0;
    _progress = progress;
    _dead = false;
    _size = species.getDna().size();
    // force the organism to wait a bit after breeding
    _waitCounter = (int)(Ecosystem.TURN_LENGTH * 0.11f);
    return this;
  }

  Organism reincarnate(Organism parent) {
    return reincarnate(parent._body.get(0).getxPos(), parent._body.get(0).getyPos(), parent._species, parent._progress);
  }

  public float getAge() {
    return _age;
  }

  public Cell getCell(int index) {
    return _body.get(index);
  }

  public int getProgress() {
    return _progress;
  }

  /**
   * The number of cells in this Organism.
   * @return
   */
  public int size() {
    return _size;
  }

  public Species getSpecies() {
    return _species;
  }

  public boolean isDead() {
    return _dead;
  }

  /**
   * Marks this creature as dead.
   * 
   * @param graveyard The list to be updated for keeping track of dead creatures.
   */
  public void kill(List<Organism> graveyard) {
    _species.decExtantPopulation();
    graveyard.add(this);
    _dead = true;
  }

  void clearFed() {
    for (Cell c : _body) {
      c.setFed(false);
    }
  }

  Ecosystem getEcosystem() {
    return _species.getEcosystem();
  }

  float getSpeed() {
    return _species.getSpeed();
  }

  boolean isFed() {
    // for (Cell c : _body) {
    for (int i = 0; i < _size; ++i) {
      Cell c = _body.get(i);
      if (!c.isFed())
        return false;
    }
    return true;
  }

  void update() {
    // check if this organism was supposed to skip a turn (after breeding)
    if (_waitCounter > 0) {
      --_waitCounter;
      return;
    }

    _progress += getSpeed();
    ++_age;

    if (_progress >= Ecosystem.TURN_LENGTH) {
      // update the head cell and check organism destination
      Cell head = _body.get(0);
      head.setxPos(head.getxDest());
      head.setyPos(head.getyDest());

      boolean found = false;

      // search for nearby food
      int x = head.getxPos(), y = head.getyPos();
      double getDirection = Math.random();
      if (getDirection >= 0.75) {
        found = searchRight(x, y);
      }
      else if (getDirection >= 0.5) {
        found = searchLeft(x, y);
      }
      else if ((getDirection >= 0.25)) {
        found = searchUp(x, y);
      }
      else {
        found = searchDown(x, y);
      }

      if (!found && head.getxPos() == _xDest && head.getyPos() == _yDest) {
        // the organism has reached its destination, so choose a new one
        // no food nearby, so go to random location
        _xDest = getEcosystem().getRandomX(_xDest, 10);
        _yDest = getEcosystem().getRandomY(_yDest, 10);
        // Log.v(getClass().getCanonicalName(), "setting destination to (" + _xDest + ", " + _yDest + ")");
      }

      // calculate the head's next destination to reach the target
      // assumes the destination is not equal to the current position
      if (Math.abs(_yDest - head.getyPos()) >= Math.abs(_xDest - head.getxPos())) {
        head.setxDest(head.getxPos());
        head.setyDest(_yDest > head.getyPos() ? head.getyPos() + 1 : head.getyPos() - 1);
      }
      else {
        head.setxDest(_xDest > head.getxPos() ? head.getxPos() + 1 : head.getxPos() - 1);
        head.setyDest(head.getyPos());
      }
      head.consume(getEcosystem().getFoodAt(head.getxPos(), head.getyPos()));

      // update position and new destination for each cell in the tail
      for (int i = 1; i < _size; ++i) {
        Cell curCell = _body.get(i);
        Cell nextCell = _body.get(i - 1);
        curCell.setxPos(curCell.getxDest());
        curCell.setyPos(curCell.getyDest());
        curCell.setxDest(nextCell.getxPos());
        curCell.setyDest(nextCell.getyPos());
        curCell.consume(getEcosystem().getFoodAt(curCell.getxPos(), curCell.getyPos()));
      }

      _progress = _progress % Ecosystem.TURN_LENGTH;
    }
  }

  private boolean needsFood(Jelly j) {
    // for (Cell c : _body) {
    for (int i = 0; i < _size; ++i) {
      Cell c = _body.get(i);
      if (!c.isFed() && c.getJelly() == j) {
        return true;
      }
    }
    return false;
  }

  private boolean searchDown(int x, int y) {
    // Log.v(getClass().getCanonicalName(), "searching from below for food centered on (" + x + ", " + y
    // + ") with search radius == " + getSpecies().getSearchRadius());
    for (int radius = 1; radius <= getSpecies().getSearchRadius(); ++radius) {
      int dx, dy;
      Food f;

      dx = 0;
      dy = radius;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      dy = -dy;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      for (dy = 1; dy < radius; ++dy) {
        dx = dy - radius;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y + dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y + dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y + dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y + dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + -dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y - dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y - dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + -dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y - dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y - dy;
          return true;
        }
      }

      dx = -radius;
      dy = 0;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      dx = -dx;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }
    }

    return false;
  }

  private boolean searchLeft(int x, int y) {
    // Log.v(getClass().getCanonicalName(), "searching from the left for food centered on (" + x + ", " + y
    // + ") with search radius == " + getSpecies().getSearchRadius());
    for (int radius = 1; radius <= getSpecies().getSearchRadius(); ++radius) {
      int dx, dy;
      Food f;

      dx = -radius;
      dy = 0;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      dx = -dx;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      for (dy = 1; dy < radius; ++dy) {
        dx = dy - radius;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y + dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y + dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y + dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y + dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + -dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y - dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y - dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + -dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y - dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y - dy;
          return true;
        }
      }

      dx = 0;
      dy = radius;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      dy = -dy;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }
    }

    return false;
  }

  private boolean searchRight(int x, int y) {
    // Log.v(getClass().getCanonicalName(), "searching from the right for food centered on (" + x + ", " + y
    // + ") with search radius == " + getSpecies().getSearchRadius());
    for (int radius = 1; radius <= getSpecies().getSearchRadius(); ++radius) {
      int dx, dy;
      Food f;

      dx = radius;
      dy = 0;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      dx = -dx;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      for (dy = 1; dy < radius; ++dy) {
        dx = radius - dy;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y + dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y + dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y + dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y + dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + -dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y - dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y - dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + -dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y - dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y - dy;
          return true;
        }
      }

      dx = 0;
      dy = radius;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      dy = -dy;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }
    }

    return false;
  }

  private boolean searchUp(int x, int y) {
    // Log.v(getClass().getCanonicalName(), "searching from above for food centered on (" + x + ", " + y
    // + ") with search radius == " + getSpecies().getSearchRadius());
    for (int radius = 1; radius <= getSpecies().getSearchRadius(); ++radius) {
      int dx, dy;
      Food f;

      dx = 0;
      dy = -radius;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      dy = -dy;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      for (dy = 1; dy < radius; ++dy) {
        dx = dy - radius;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y + dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y + dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y + dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y + dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + -dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y - dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y - dy;
          return true;
        }

        dx = -dx;
        // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + -dy + ")");
        f = getEcosystem().getFoodAt(x + dx, y - dy);
        if (f != null && f.isMature() && needsFood(f.getJelly())) {
          // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
          _xDest = x + dx;
          _yDest = y - dy;
          return true;
        }
      }

      dx = radius;
      dy = 0;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }

      dx = -dx;
      // Log.v(getClass().getCanonicalName(), "\twith (dx, dy) == (" + dx + ", " + dy + ")");
      f = getEcosystem().getFoodAt(x + dx, y + dy);
      if (f != null && f.isMature() && needsFood(f.getJelly())) {
        // Log.v(getClass().getCanonicalName(), "\t\tFOUND - breaking off search...");
        _xDest = x + dx;
        _yDest = y + dy;
        return true;
      }
    }

    return false;
  }
}
