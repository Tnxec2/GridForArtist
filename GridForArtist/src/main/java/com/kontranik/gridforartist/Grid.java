package com.kontranik.gridforartist;

import android.graphics.Color;
import android.widget.ImageView;

import androidx.core.math.MathUtils;

public class Grid {
    int offsetX, offsetY;
    float cellWidth, cellHeight;
    int rows, cols;
    boolean square;
    boolean diagonal;
    boolean locked;
    int color;
    int stroke;

    public Grid(ImageView iv) {
        rows = 8;
        cols = 6;
        cellWidth = iv.getDrawable().getIntrinsicWidth() / cols;
        cellHeight = iv.getDrawable().getIntrinsicHeight() / rows;
        offsetX = 0;
        offsetY = 0;
        square = true;
        diagonal = false;
        locked = true;
        stroke =1;
        color = Color.argb(100, 0, 0, 0);

        if ( square ) {
            cellHeight = cellWidth;
            recalculateGrid(iv);
        }
    }

    void recalculateGrid(ImageView iv) {
        Params iparams = ImageViewHelper.getBitmapPositionInsideImageView(iv.getMatrix(), iv);

        rows = (int) (iparams.height / cellHeight);
        cols = (int) (iparams.width / cellWidth);

        if ( rows < 2 || rows > 20 || cols < 2 || cols > 20) {
            rows = MathUtils.clamp(rows, 2, 20);
            cols = MathUtils.clamp(cols, 2, 20);
            cellHeight = iparams.height / rows;
            cellWidth = iparams.width / cols;
        }
    }

    void growCell(ImageView iv) {
        cellWidth += 1;
        cellHeight += 1;
        recalculateGrid(iv);
    }

    void shrinkCell(ImageView iv) {
        cellWidth -= 1;
        cellHeight -= 1;
        recalculateGrid(iv);
    }
}
