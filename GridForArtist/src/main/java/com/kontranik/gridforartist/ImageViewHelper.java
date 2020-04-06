package com.kontranik.gridforartist;

import android.graphics.Matrix;
import android.widget.ImageView;

public class ImageViewHelper {

    /**
     * Returns the bitmap position inside an imageView.
     * @param imageView source ImageView
     * @return Params object
     */
    public static Params getBitmapPositionInsideImageView(Matrix matrix, ImageView imageView) {
        Params ret = new Params();

        float[] values = new float[9];
        matrix.getValues(values);
        ret.left = values[Matrix.MTRANS_X];
        ret.top = values[Matrix.MTRANS_Y];
        ret.scaleX = values[Matrix.MSCALE_X];
        ret.scaleY = values[Matrix.MSCALE_Y];
        ret.width = values[Matrix.MSCALE_X] * imageView.getDrawable().getIntrinsicWidth();
        ret.height = values[Matrix.MSCALE_Y] * imageView.getDrawable().getIntrinsicHeight();
        ret.skewX = values[Matrix.MSKEW_X];
        ret.skewY = values[Matrix.MSKEW_Y];
        return ret;
    }
}
