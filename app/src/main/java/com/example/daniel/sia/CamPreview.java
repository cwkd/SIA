package com.example.daniel.sia;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by Daniel on 22/10/2017.
 */

public class CamPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    public Camera mCamera;

    public CamPreview(Context context, Camera cameraDevice) {
        super(context);
        mCamera = cameraDevice;
        mCamera.setDisplayOrientation(90);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
            }
        } catch (IOException ex) {
            Log.d(TAG, "Error setting camera preview" + ex.getMessage());
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (mHolder.getSurface() == null) return;
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        } catch (Exception ex) {
            Log.d(TAG, "Error stopping camera preview" + ex.getMessage());
        }
        try {
            if (mCamera != null) {
               /* Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewFormat(format);
                parameters.setPreviewSize(width , height);
                mCamera.setParameters(parameters);*/
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            }
        } catch (IOException ex) {
            Log.d(TAG, "Error changing camera preview" + ex.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception ex) {
            Log.d(TAG, "Error destroying camera preview" + ex.getMessage());
        }
    }
}
