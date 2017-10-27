package com.example.daniel.sia;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Daniel on 26/10/2017.
 */

public class ImageProcessor {

    static {
        System.loadLibrary("opencv_java3");
    }

    public ImageProcessor() {
    }

    public static Bitmap getBitmapFromFile(Uri pictureUri) throws NullPointerException {
        File pictureFile = new File(pictureUri.getPath());
        if (pictureFile.exists()) {
            return BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
        } else {
            throw new NullPointerException("File cannot be found");
        }
    }

    public static Mat getCannyImage(Bitmap bitmap) {
        int iHeight = bitmap.getHeight();
        int iWidth = bitmap.getWidth();
        Mat mRgba = new Mat(iHeight, iWidth, CvType.CV_8UC4);
        Mat mGrey = new Mat(iHeight, iWidth, CvType.CV_8UC1);
        Mat mCanny = new Mat(iHeight, iWidth, CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, mRgba);
        Imgproc.cvtColor(mRgba, mGrey, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(mGrey, mCanny, new Size(3, 3));
        Imgproc.Canny(mCanny, mCanny, 50, 150, 3, false);

        Mat dest = new Mat();
        Core.add(dest, Scalar.all(0), dest);
        mRgba.copyTo(dest, mCanny);
        return mCanny;
    }

    public static Mat getForegroundObject(Bitmap bitmap) {
        int iHeight = bitmap.getHeight();
        int iWidth = bitmap.getWidth();
        Mat mRgba = new Mat(iHeight, iWidth, CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, mRgba);
        Mat mHsv = new Mat();
        List<Mat> hsvPlanes = new ArrayList<>();
        Mat mThreshold = new Mat();
        int thresh_type = Imgproc.THRESH_BINARY_INV;

        // threshold the image with the average hue value
        mHsv.create(mRgba.size(), CvType.CV_8U);
        Imgproc.cvtColor(mRgba, mHsv, Imgproc.COLOR_BGR2HSV);
        Core.split(mHsv, hsvPlanes);

        // get average hue value of the image
        double threshValue = getHistAverage(mHsv, hsvPlanes.get(0));
        Imgproc.threshold(hsvPlanes.get(0), mThreshold, threshValue, 179.0, thresh_type);
        Imgproc.blur(mThreshold, mThreshold, new Size(5, 5));

        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(mThreshold, mThreshold, new Mat(), new Point(-1,-1), 1);
        Imgproc.erode(mThreshold, mThreshold, new Mat(), new Point(-1,-1), 3);

        Imgproc.threshold(mThreshold, mThreshold, threshValue, 179.0, Imgproc.THRESH_BINARY);

        // create new image
        Mat mForeground = new Mat(mRgba.size(), CvType.CV_8UC3, new Scalar(255,255,255));
        mRgba.copyTo(mForeground, mThreshold);

        return mForeground;
    }

    private static double getHistAverage(Mat hsvImg, Mat hueValues) {
        double average = 0;
        Mat hist_hue = new Mat();
        MatOfInt histSize = new MatOfInt(180);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));
        for (int h = 0; h < 180; h++) {
            average += (hist_hue.get(h, 0)[0]*h);
        }
        return average / hsvImg.size().height/ hsvImg.size().width;
    }

    private static Point getMidpoint(Point ptA, Point ptB) {
        return new Point((ptA.x + ptB.x)*0.5, (ptA.y + ptB.y)*0.5);
    }

    private static double getDist(Point ptA, Point ptB) {
        return Math.sqrt(Math.pow(ptA.x - ptB.x, 2) + Math.pow(ptA.y - ptB.y, 2));
    }

    public static double[] getDimensions(Bitmap bitmap) {
        int iHeight = bitmap.getHeight();
        int iWidth = bitmap.getWidth();
        Mat original = new Mat(iHeight, iWidth, CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, original);
        Mat grey = new Mat(iHeight, iWidth, CvType.CV_8UC1);
        Mat edged = new Mat(iHeight, iWidth, CvType.CV_8UC1);
        Mat mContours = original.clone();
        List<MatOfPoint> listOfContours = new ArrayList<>();

        Imgproc.cvtColor(original, grey, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(grey, grey, new Size (5,5), 0);

        Imgproc.Canny(grey, edged, 50, 150);
        Imgproc.dilate(edged, edged, new Mat());
        Imgproc.erode(edged, edged, new Mat());

        Imgproc.findContours(edged, listOfContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<Rect> listOfRekts = new ArrayList<>();
        for (int w = 0; w < listOfContours.size(); w++) {
            listOfRekts.add(Imgproc.boundingRect(listOfContours.get(w)));
        }
        // sort contours left to right
        Comparator<Rect> pointsComparator = new RectComparator();
        Collections.sort(listOfRekts, pointsComparator);

        List<RotatedRect> listOfRotatedRekts = new ArrayList<>();
        for (Rect rect: listOfRekts) {
            if (rect.area() < 100) {
                continue;
            } else {
                Mat origCopy = original.clone();
                Point tl = rect.tl();
                Point tr = new Point(tl.x+rect.width, tl.y);
                Point br = rect.br();
                Point bl = new Point(br.x-rect.width, br.y);
                MatOfPoint2f origRect = new MatOfPoint2f();
                origRect.fromArray(tl, tr, br, bl);
                RotatedRect box = Imgproc.minAreaRect(origRect);
                listOfRotatedRekts.add(box);
                Imgproc.boxPoints(box, new Mat());
                Imgproc.rectangle(mContours, tl, br, new Scalar(255, 0, 0), 2);
            }
        }
        // get dimensions of reference object
        try {
            RotatedRect referenceObject = listOfRotatedRekts.get(0);
            Point[] refObjPoints = new Point[4];
            referenceObject.points(refObjPoints);
            Point tltrMid = getMidpoint(refObjPoints[0], refObjPoints[3]);
            Point blbrMid = getMidpoint(refObjPoints[1], refObjPoints[2]);
            Point tlblMid = getMidpoint(refObjPoints[0], refObjPoints[1]);
            Point trbrMid = getMidpoint(refObjPoints[3], refObjPoints[2]);
            double dL = getDist(tltrMid, blbrMid);
            double dS = getDist(tlblMid, trbrMid);
            double pixelPerMetric = dS / 210;
            //double dimL = dL / pixelPerMetric;
            //double dimS = dS / pixelPerMetric;

            RotatedRect measuredObject = listOfRotatedRekts.get(1);
            double height = measuredObject.size.height / pixelPerMetric;
            double width = measuredObject.size.width / pixelPerMetric;
            return new double[]{height, width};
        } catch (Exception e) {
            return null;
        }
    }

    public static Mat getContoursImage(Bitmap bitmap) {
        int iHeight = bitmap.getHeight();
        int iWidth = bitmap.getWidth();
        Mat original = new Mat(iHeight, iWidth, CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, original);
        Mat grey = new Mat(iHeight, iWidth, CvType.CV_8UC1);
        Mat edged = new Mat(iHeight, iWidth, CvType.CV_8UC1);
        Mat mContours = original.clone();
        List<MatOfPoint> listOfContours = new ArrayList<>();

        Imgproc.cvtColor(original, grey, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(grey, grey, new Size (5,5), 0);

        Imgproc.Canny(grey, edged, 50, 150);
        Imgproc.dilate(edged, edged, new Mat());
        Imgproc.erode(edged, edged, new Mat());

        Imgproc.findContours(edged, listOfContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<Rect> listOfRekts = new ArrayList<>();
        for (int w = 0; w < listOfContours.size(); w++) {
            listOfRekts.add(Imgproc.boundingRect(listOfContours.get(w)));
        }
        // sort contours left to right
        Comparator<Rect> pointsComparator = new RectComparator();
        Collections.sort(listOfRekts, pointsComparator);

        List<RotatedRect> listOfRotatedRekts = new ArrayList<>();
        for (Rect rect: listOfRekts) {
            if (rect.area() < 100) {
                continue;
            } else {
                Mat origCopy = original.clone();
                Point tl = rect.tl();
                Point tr = new Point(tl.x+rect.width, tl.y);
                Point br = rect.br();
                Point bl = new Point(br.x-rect.width, br.y);
                MatOfPoint2f origRect = new MatOfPoint2f();
                origRect.fromArray(tl, tr, br, bl);
                RotatedRect box = Imgproc.minAreaRect(origRect);
                listOfRotatedRekts.add(box);
                Imgproc.boxPoints(box, new Mat());
                Imgproc.rectangle(mContours, tl, br, new Scalar(255, 0, 0), 2);
            }
        }
        // get dimensions of reference object
        try {
            RotatedRect referenceObject = listOfRotatedRekts.get(0);
            Point[] refObjPoints = new Point[4];
            referenceObject.points(refObjPoints);
            Point tltrMid = getMidpoint(refObjPoints[0], refObjPoints[3]);
            Point blbrMid = getMidpoint(refObjPoints[1], refObjPoints[2]);
            Point tlblMid = getMidpoint(refObjPoints[0], refObjPoints[1]);
            Point trbrMid = getMidpoint(refObjPoints[3], refObjPoints[2]);
            double dL = getDist(tltrMid, blbrMid);
            double dS = getDist(tlblMid, trbrMid);
            double pixelPerMetric = dS / 210;
            //double dimL = dL / pixelPerMetric;
            //double dimS = dS / pixelPerMetric;

            RotatedRect measuredObject = listOfRotatedRekts.get(1);
            double height = measuredObject.size.height / pixelPerMetric;
            double width = measuredObject.size.width / pixelPerMetric;
            return mContours;
        } catch (Exception e) {
            return null;
        }
    }
}
