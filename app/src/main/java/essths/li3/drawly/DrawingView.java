package essths.li3.drawly;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
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

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);  // Dessine le contenu de ton view dans le bitmap
        return bitmap;
    }
    public void saveToGallery(Context context, String fileName) {
        Bitmap bitmap = getBitmap();
        OutputStream fos;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyDrawings");

                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    fos = context.getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    if (fos != null) fos.close();
                }
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyDrawings");
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, fileName + ".png");
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
            Toast.makeText(context, "Image saved to gallery!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving image!", Toast.LENGTH_SHORT).show();
        }
    }






}
