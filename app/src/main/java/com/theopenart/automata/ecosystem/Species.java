package com.theopenart.automata.ecosystem;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>Species</code> has three basic attributes, which determine its behavior:
 * <p>
 * <table>
 * <tr><td>speed</td><td>Corresponding to <code>Jelly.FAST</code></td></tr>
 * <tr><td>longevity</td><td>Corresponding to <code>Jelly.HEALTHY</code></td></tr>
 * <tr><td>search radius</td><td>Corresponding to <code>Jelly.SMART</code></td></tr>
 * </table>
 * <p>
 * Each of these attributes is increased according to the number of each cell type the species has in its DNA.
 * <p>
 * <h3>Speed</h3>
 * Each cell will keep track of its progress toward its destination, if it is moving. When the ecosystem performs an
 * update cycle, this progress will be increased by the speed of that cell. The process begins anew when the cell
 * reaches its destination and selects a new one.
 * <p>
 * The speed of each cell is uniform across the organism, and determined by the number of fast cells in its DNA.
 * <h3>Longevity</h3>
 * Whenever the ecosystem performs an update cycle, it will increment each organisms age. The longevity of an organism
 * is the number of update cycles it will live before dying.
 * <h3>Search radius</h3>
 * When an organism selects a destination to move to, it will look for food it can consume, and head to the nearest one
 * it can find, or to some random spot if it can't find any. The search radius is the distance the organism can search
 * to find food before selecting a random destination.
 * <p>
 * 
 * @author daniel
 *
 */
public class Species {

  /** The maximum number of Jellies in a Species DNA. */
  public static final int MAX_JELLY_DNA = 9;

  private final List<Jelly> _dna;

  private final Ecosystem   _ecosystem;

  /** How many organisms of this species have ever lived */
  private int               _historicalPopulation;
  
  /** How many organisms of this species are alive now */
  private int               _extantPopulation;

  /** How long this organism can live, in update cycles. */
  private final int         _longevity;

  /** Search radius for food by this organism from the head cell. */
  private final int         _searchRadius;

  /** Speed of the organism. */
  private final int         _speed;

  public Species(Ecosystem ecosystem, List<Jelly> dna) {
    _ecosystem = ecosystem;
    _dna = new ArrayList<Jelly>(dna);

    // calculate the attributes using the constituent parts
    int speed = 2;
    int longevity = Ecosystem.TURN_LENGTH * 10;
    int searchRadius = 1;
    
    for(Jelly j : _dna){
      switch(j){
      case FAST:
        speed += 1;
        break;
      case HEALTHY:
        longevity += Ecosystem.TURN_LENGTH * 10;
        break;
      case SMART:
        searchRadius += 3;
        break;
      }
    }
    
    _speed = speed;
    _longevity = longevity;
    _searchRadius = searchRadius;

    _historicalPopulation = 0;
  }

  public List<Jelly> getDna() {
    return _dna;
  }

  void decExtantPopulation() {
    --_extantPopulation;
  }

  public int getExtantPopulation() {
    return _extantPopulation;
  }

  public int getHistoricalPopulation() {
    return _historicalPopulation;
  }

  public int getLongevity() {
    return _longevity;
  }

  Ecosystem getEcosystem() {
    return _ecosystem;
  }

  int getSearchRadius() {
    return _searchRadius;
  }

  float getSpeed() {
    return _speed;
  }

  void incExtantPopulation() {
    ++_extantPopulation;
  }

  void incHistoricalPopulation() {
    ++_historicalPopulation;
  }

}
