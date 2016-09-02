package com.babyukiss.cameratexture.camera;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.hardware.Camera.Size;
import android.util.Log;

public class CameraParamUitl {
    private static final String TAG = "CameraParamUitl";
    private static CameraParamUitl mCameraParamUitl;
    private CameraSizeCompare mCompare = new CameraSizeCompare();
    class CameraSizeCompare implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            if(lhs.width == rhs.width){
                return 0;
            }
            else if(lhs.width > rhs.width){
                return 1;
            }
            else{
                return -1;
            }
        }
        
    }
    
    public static CameraParamUitl getInstance() {
        if (null == mCameraParamUitl) {
            mCameraParamUitl = new CameraParamUitl();
        }
        return mCameraParamUitl;
    }
    public Size getPropPreviewSize(List<Size> list, float hwRate, int width) {
        Collections.sort(list, mCompare);
        for (Size s : list) {
            Log.i(TAG, "getSupportted size:width = " + s.width + "height = " + s.height);
        }
        int i = 0;
        for (Size s : list) {
            if ((s.width >= width) && equalRate(s, hwRate)) {
                Log.i(TAG, "getPropPreviewSize:width = " + s.width + "height = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    public boolean equalRate(Size s, float rate) {
        float r =  (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.03) {
            return true;
        } else {
            return false;
        }
    }
    
}
