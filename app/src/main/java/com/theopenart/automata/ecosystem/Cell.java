package com.theopenart.automata.ecosystem;

public class Cell {

  private Ecosystem _ecosystem;

  /** Whether this Cell has eaten Jelly and is ready to divide. */
  private boolean   _fed;

  /** The Jelly this cell requires */
  private Jelly     _jelly;

  /** The x coordinate of this cell's destination. */
  private int       _xDest;

  /** The x coordinate of this cell. */
  private int       _xPos;

  /** The y coordinate of this cell's destination. . */
  private int       _yDest;

  /** The y coordinate of this cell. */
  private int       _yPos;

  Cell(Jelly jelly, Ecosystem ecosystem) {
    _ecosystem = ecosystem;
    _jelly = jelly;
  }

  /**
   * @return The Jelly this Cell is made of and consumes.
   */
  public Jelly getJelly() {
    return _jelly;
  }

  public int getxDest() {
    return _xDest;
  }

  public int getxPos() {
    return _xPos;
  }

  public int getyDest() {
    return _yDest;
  }

  public int getyPos() {
    return _yPos;
  }

  public boolean isFed() {
    return _fed;
  }

  /**
   * Returns <code>true</code> if the cell was able to consume this food.
   * 
   * @param food
   * @return
   */
  boolean consume(Food food) {
    if (food == null) {
      throw new Error("tried to consume a null food at " + _xPos + ", " + _yPos);
    }
    if (_fed == false && food.getJelly() == _jelly && food.getMaturity() >= 100) {
      food.setMaturity(0);
      food.setJelly(null);
      _fed = true;
    }
    return _fed;
  }

  void setFed(boolean fed) {
    _fed = fed;
  }

  void setJelly(Jelly jelly) {
    _jelly = jelly;
  }

  void setxDest(int xDest) {
    if (xDest < 0 || xDest >= _ecosystem.getWidth()) {
      throw new Error("tried to set cell x destination out of Ecosystem bounds: " + xDest);
    }
    _xDest = xDest;
  }

  void setxPos(int xPos) {
    if (xPos < 0 || xPos >= _ecosystem.getWidth()) {
      throw new Error("tried to set cell x position out of Ecosystem bounds: " + xPos);
    }
    _xPos = xPos;
  }

  void setyDest(int yDest) {
    if (yDest < 0 || yDest >= _ecosystem.getHeight()) {
      throw new Error("tried to set cell y destination out of Ecosystem bounds: " + yDest);
    }
    _yDest = yDest;
  }

  void setyPos(int yPos) {
    if (yPos < 0 || yPos >= _ecosystem.getHeight()) {
      throw new Error("tried to set cell y position out of Ecosystem bounds: " + yPos);
    }
    _yPos = yPos;
  }

}
