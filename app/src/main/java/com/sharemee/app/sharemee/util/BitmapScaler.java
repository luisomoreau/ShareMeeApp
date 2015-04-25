package com.sharemee.app.sharemee.util;

import android.graphics.Bitmap;

/**
 * Created by Marin on 25/04/2015.
 */
public class BitmapScaler
{
    // Scale and maintain aspect ratio given a desired width
    // BitmapScaler.scaleToFitWidth(bitmap, 100);
    public static Bitmap scaleToFit(Bitmap b, int width, int height)
    {
        float factor = width / (float) b.getWidth();
        float factor2 = height /(float) b.getHeight();

        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor2), (int) (b.getHeight() * factor), true);
    }


    // Scale and maintain aspect ratio given a desired height
    // BitmapScaler.scaleToFitHeight(bitmap, 100);
    public static Bitmap scaleToFitHeight(Bitmap b, int height)
    {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }

    // ...
}
