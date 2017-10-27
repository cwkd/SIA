package com.example.daniel.sia;

import org.opencv.core.Rect;

import java.util.Comparator;

/**
 * Created by Daniel on 26/10/2017.
 */

public class RectComparator implements Comparator<Rect> {

    @Override
    public int compare(Rect rect, Rect t1) {
        return (int) (rect.tl().x - t1.tl().x);
    }
}
