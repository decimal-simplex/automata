package com.theopenart.automata;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.theopenart.automata.ecosystem.Jelly;
import com.theopenart.automata.ecosystem.Species;

public class SpeciesCreatorView extends View {

  private final Bitmap   _blueCell;

  private final Bitmap   _cellBox;

  private final Bitmap   _greenCell;

  private final Bitmap   _redCell;

  private SpeciesCreator _speciesCreator;

  public SpeciesCreatorView(Context context, SpeciesCreator speciesCreator) {
    super(context);
    _speciesCreator = speciesCreator;
    _redCell = BitmapFactory.decodeResource(getResources(), R.drawable.red_cell_64);
    _greenCell = BitmapFactory.decodeResource(getResources(), R.drawable.green_cell_64);
    _blueCell = BitmapFactory.decodeResource(getResources(), R.drawable.blue_cell_64);
    _cellBox = BitmapFactory.decodeResource(getResources(), R.drawable.cell_box_64);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.drawColor(Color.BLACK);

    float blockWidth = (float)getWidth() / Species.MAX_JELLY_DNA;

    Paint paint = new Paint();
    for (int i = 0; i < Species.MAX_JELLY_DNA; ++i) {
      RectF bounds = new RectF(i * blockWidth, 0, (i + 1) * blockWidth, getHeight());
      Jelly j = _speciesCreator.get(i);
      

      canvas.drawBitmap(_cellBox, null, bounds, paint);
      
      if (j != null) {
        Bitmap cellBitmap;
        switch (j) {
        case FAST:
          cellBitmap = _redCell;
          break;
        case HEALTHY:
          cellBitmap = _greenCell;
          break;
        case SMART:
          cellBitmap = _blueCell;
          break;
        default: // WTF
          cellBitmap = _redCell;
          break;
        }
        canvas.drawBitmap(cellBitmap, null, bounds, paint);
      }
    }
  }

}
