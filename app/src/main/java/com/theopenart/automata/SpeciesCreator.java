package com.theopenart.automata;

import java.util.ArrayList;
import java.util.List;

import com.theopenart.automata.ecosystem.Jelly;
import com.theopenart.automata.ecosystem.Species;

public class SpeciesCreator {

  public interface SpeciesCreatorChangeListener {

    public void onSpeciesCreatorChange();
  }

  /** A list of Jellies to hand off when the user decides to create a new species */
  private final ArrayList<Jelly>       _dna;

  private SpeciesCreatorChangeListener _speciesCreatorChangeListener;

  public SpeciesCreator() {
    _dna = new ArrayList<Jelly>(Species.MAX_JELLY_DNA);
  }

  /**
   * Adds a Jelly at the given position, moving subsequent Jellies down the list by one. Adds the Jelly to the end of
   * the list if <code>index >= size()</code>, and to the beginning if <code>index <= 0</code>;
   * 
   * @param index
   * @param jelly
   */
  public void add(int index, Jelly jelly) {
    if (index <= 0) {
      _dna.add(0, jelly);
    }
    else if (index >= _dna.size()) {
      _dna.add(_dna.size(), jelly);
    }
    else {
      _dna.add(index, jelly);
    }
    notifyChangeListener();
  }

  public void addLast(Jelly jelly) {
    _dna.add(_dna.size(), jelly);
    notifyChangeListener();
  }

  /**
   * Removes the DNA information from this SpeciesCreator.
   */
  public void clear() {
    _dna.clear();
    notifyChangeListener();
  }

  /**
   * 
   * @param index
   * @return
   */
  public Jelly get(int index) {
    if (_dna.size() == 0 || index >= _dna.size() || index < 0) {
      return null;
    }
    else {
      return _dna.get(index);
    }
  }

  /**
   * Changes to the returned List will not be reflected in this SpeciesCreator.
   * 
   * @return
   */
  public List<Jelly> getDna() {
    return new ArrayList<Jelly>(_dna);
  }

  /**
   * Removes the Jelly at <code>index</code>. Removes the first Jelly if <code>index <= 0</code>, and the last if
   * <code>index >= size</code>.
   * 
   * @param index
   * @return
   */
  public Jelly remove(int index) {
    Jelly result = null;
    if (_dna.size() > 0) {
      if (index <= 0) {
        result = _dna.remove(0);
      }
      else if (index >= _dna.size()) {
        result = _dna.remove(_dna.size() - 1);
      }
      else {
        result = _dna.remove(index);
      }
    }
    notifyChangeListener();
    return result;
  }

  /**
   * Replaces the Jelly at <code>index</code> with the given Jelly.
   * 
   * @param index
   * @param jelly
   * @return
   */
  public Jelly replace(int index, Jelly jelly) {
    Jelly result = remove(index);
    add(index, jelly);
    notifyChangeListener();
    return result;
  }

  public void setSpeciesCreatorChangeListener(SpeciesCreatorChangeListener listener) {
    _speciesCreatorChangeListener = listener;
  }

  /**
   * 
   * @return
   */
  public int size() {
    return _dna.size();
  }

  private void notifyChangeListener() {
    if (_speciesCreatorChangeListener != null) {
      _speciesCreatorChangeListener.onSpeciesCreatorChange();
    }
  }
}
