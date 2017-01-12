package com.theopenart.automata.ecosystem;

public class Food {

  private Jelly _jelly;

  private int   _maturity;

  /**
   * 
   * @return
   */
  public Jelly getJelly() {
    return _jelly;
  }

  /**
   * @return The maturity of the food. By convention, a value of 100 means the Food is eligible to be eaten, while a
   *         value of 0 means there is no Jelly here at all. In the latter case, a call to <code>getJelly()</code>
   *         should return <code>null</code>.
   */
  public int getMaturity() {
    return _maturity;
  }

  /**
   * Increments the maturity of this Food. If the maturity level is already 100, or if the Food type (i.e. Jelly) is not
   * defined, calling this method has no effect.
   */
  public void incMaturity() {
    if (_maturity < 100 && _jelly != null) { // if there is anything to grow
      ++_maturity;
    }
  }

  public boolean isMature() {
    return _maturity == 100;
  }

  /**
   * 
   * @param jelly
   */
  public void setJelly(Jelly jelly) {
    _jelly = jelly;
  }

  public void setMaturity(int maturity) {
    if (maturity > 100) {
      _maturity = 100;
    }
    else if (maturity < 0) {
      _maturity = 0;
    }
    else {
      _maturity = maturity;
    }

  }

}
