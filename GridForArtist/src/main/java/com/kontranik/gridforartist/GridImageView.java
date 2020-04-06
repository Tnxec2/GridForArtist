package com.kontranik.gridforartist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import static com.kontranik.gridforartist.ImageViewHelper.getBitmapPositionInsideImageView;

public class GridImageView extends AppCompatImageView {

    Paint paint = new Paint();

    public GridImageView(Context context) {
        super(context);
    }

    public GridImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawGrid(canvas, getBitmapPositionInsideImageView(getImageMatrix(), this));
    }

    private void drawGrid(Canvas canvas, Params iparam) {

        Grid grid = MainActivity.grid;

        paint.setColor(grid.color);
        paint.setStrokeWidth(grid.stroke);
        paint.setStyle(Paint.Style.STROKE);

        float cellHeight = iparam.height / grid.rows;
        float cellWidth = iparam.width / grid.cols;

        Bitmap b = ((BitmapDrawable)getDrawable()).getBitmap();

        float offsetX = grid.offsetX / ( b.getWidth() / iparam.width);
        float offsetY = grid.offsetY / ( b.getHeight() / iparam.width);

        for (int row = 0; row < grid.rows; row++ ) {
            for(int col = 0; col < grid.cols; col++) {

                float startX = iparam.left + offsetX + col * cellWidth;
                float endX = startX + cellWidth;
                float startY = iparam.top + offsetY + row * cellHeight;
                float endY = startY + cellHeight;

                canvas.drawRect( startX, startY, endX, endY, paint);

                if ( grid.diagonal) {
                    canvas.drawLine(startX, startY, endX, endY, paint);
                    canvas.drawLine(startX, endY, endX, startY, paint);
                }
            }
        }
    }

    public Bitmap getBitmap() {
        BitmapDrawable drawable = (BitmapDrawable) this.getDrawable();
        Bitmap source = Bitmap.createBitmap( drawable.getBitmap() ).copy(Bitmap.Config.ARGB_8888, true);

        Bitmap bitmap = Bitmap.createBitmap(source);
        Canvas canvas = new Canvas(bitmap);

        drawGrid(canvas, new Params( 0, 0, bitmap.getWidth(), bitmap.getHeight(), 1f, 1f, 1f, 1f ));

        source.recycle();

        return bitmap;

    }
}
