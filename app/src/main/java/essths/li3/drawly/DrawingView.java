package essths.li3.drawly;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private static class Stroke {
        Path path;
        Paint paint;

        Stroke(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }

    private int currentColor = 0xFFFF0000;
    private float currentBrushSize = 10f;
    private List<Stroke> strokes = new ArrayList<>();
    private List<Stroke> undoneStrokes = new ArrayList<>();

    private Path currentPath;
    private Paint currentPaint;



    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        startNewStroke();
    }




    private void startNewStroke() {
        // Cloner le paint pour ce nouveau trait
        currentPaint = new Paint();
        currentPaint.setColor(currentColor);
        currentPaint.setAntiAlias(true);
        currentPaint.setStrokeWidth(currentBrushSize);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);

        currentPath = new Path();
        strokes.add(new Stroke(currentPath, currentPaint));
    }

    public void setColor(int color) {
        currentColor = color;
        startNewStroke();
        invalidate();
    }

    public void setBrushSize(float size) {
        currentBrushSize = size;
        startNewStroke();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Stroke stroke : strokes) {
            canvas.drawPath(stroke.path, stroke.paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath.moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                // Chaque fois que tu lèves le doigt, on commence un nouveau stroke
                startNewStroke();
                break;
        }

        invalidate();
        return true;
    }


    public void undo() {
        if (!strokes.isEmpty()) {
            // On enlève le dernier stroke et on le met dans undoneStrokes
            Stroke removed = strokes.remove(strokes.size() - 1);
            undoneStrokes.add(removed);
            invalidate();
        }
    }

    public void redo() {
        if (!undoneStrokes.isEmpty()) {
            // On reprend le dernier stroke annulé et on le remet dans strokes
            Stroke restored = undoneStrokes.remove(undoneStrokes.size() - 1);
            strokes.add(restored);
            invalidate();
        }
    }

    public void clear() {
        strokes.clear();
        undoneStrokes.clear(); // aussi vider redo stack
        currentPath = null;
        startNewStroke(); // repartir proprement
        invalidate();
    }





}
