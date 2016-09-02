package com.babyukiss.cameratexture.camera;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

/**
 * CameraInterface is used to handle camera interface.
 * Encapsulate method of Camera, and do some work about business.
 * 
 * @author hongen
 *
 */

public class camera implements PreviewCallback{
    private static final String TAG = "camera_android";
    private static camera sCameraInterface;
    private Camera mCamera;
    private Camera.Parameters mParams;
//    private SurfaceTexture mSurfaceTexture;
    
    private int mCameraDefaultWidth = 640;
    private int mCameraDefaultHeight = 480;
//    private int mCameraDefaultWidth = 1280;
//    private int mCameraDefaultHeight = 720;
    private int mCameraWidth;
    private int mCameraHeight;
    private int mDataLengthY;   // length of y
    private int mDataLengthUV;    // length of uv
    private int mTextureY;
    private int mTextureUV;

    private boolean mIsPreviewing = false;
    private byte[] mPreviewBuffer = null;

    // store yuv data for y and uv
    private ByteBuffer mBufferY;
    private ByteBuffer mBufferUV;

//    private AvcEncoder avcCodec;
//    private int mFrameRate = 30;
//    private int mBitRate = 125000;

    private camera(){

    }

    public void test() {
        
    }

    /**
     * keep camera instance unique.
     * @return CameraInterface
     */
    public static synchronized camera getInstance() {
        if (null == sCameraInterface) {
            sCameraInterface = new camera();
        }
        return sCameraInterface;
    }

    /**
     * open camera
     * @param camera 0 is background
     * @return false camera error
     */
    public int arcameraInitCamera(int camera) {
        try {
            mCamera = Camera.open(camera);
            mParams = mCamera.getParameters();
            doInitPreviewParams(mCameraDefaultWidth, mCameraDefaultHeight);
            
//            avcCodec = new AvcEncoder(mCameraDefaultWidth, mCameraDefaultHeight, mFrameRate, mBitRate);

            int[] tempTextures = new int[2];
            GLES20.glGenTextures(2, tempTextures, 0);
            mTextureY = tempTextures[0];
            mTextureUV = tempTextures[1];
            Log.d(TAG, "arcameraInitCamera ok");
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    /**
     * need call this before start preview
     * @param width
     * @param height
     * @return
     */
    public boolean doInitPreviewParams(int width, int height) {
        if (mCamera != null) {
            float previewRate = (width > height) ? (((float)width) / ((float)height)) : (((float)height) / ((float)width));
            int previewWidth = (width < height) ? height : width;
            Size previewSize = CameraParamUitl.getInstance()
                    .getPropPreviewSize(mParams.getSupportedPreviewSizes(), previewRate, previewWidth);
            mParams.setPreviewSize(previewSize.width, previewSize.height);
            mParams.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            mParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mParams.setPreviewFormat(ImageFormat.NV21) ;
            mCamera.setParameters(mParams);

            mCamera.setDisplayOrientation(90);
//            mSurfaceTexture = new SurfaceTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
            /*int bufSize = mCameraWidth * mCameraHeight * 3 / 2;
            mPreviewBuffer = new byte[bufSize];
            mCamera.addCallbackBuffer(mPreviewBuffer);
            mCamera.setPreviewCallbackWithBuffer(this);*/
//            try {
//                mCamera.setPreviewTexture(mSurfaceTexture);
//                Log.d(TAG, "preview format:" +mCamera.getParameters().getPictureFormat());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            mCameraWidth = mParams.getPreviewSize().width;
            mCameraHeight = mParams.getPreviewSize().height;
            Log.d(TAG, " doInitPrew width=" + mCameraWidth + ",height=" + mCameraDefaultHeight);
            int dataSize = mCameraWidth * mCameraHeight;
            int bufSize = dataSize * 3 / 2;
            mPreviewBuffer = new byte[bufSize];
            mDataLengthY = dataSize;
            mDataLengthUV = dataSize / 2;
            mBufferY = ByteBuffer.allocateDirect(mDataLengthY)
                    .order(ByteOrder.nativeOrder());
            mBufferUV = ByteBuffer.allocateDirect(mDataLengthUV)
                    .order(ByteOrder.nativeOrder());
        }
        return true;
    }

    public int arcameraSetFlashTorchMode(int on) {
        if (mCamera != null) {
            return 1;
        }
        return -1;
    }

    /**
     * do start preview, and show image to surface
     * @return
     */
    public int arcameraStart(SurfaceTexture surfaceTexture) {
        if (mIsPreviewing) {
            Log.w(TAG, "arcameraStart");
            return 1;
        }
        if (mCamera != null) {
            mCamera.addCallbackBuffer(mPreviewBuffer);
            mCamera.setPreviewCallbackWithBuffer(this);
            try {
                mCamera.setPreviewTexture(surfaceTexture);
//                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                mCamera.cancelAutoFocus();
                mIsPreviewing = true;
                mParams = mCamera.getParameters();
//                avcCodec.StartEncoderThread();
                Log.d(TAG, "arcameraStart ok");
                return 1;
            } catch (RuntimeException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
          Log.e(TAG, "camera is null");
          return -1;
        }
        return 1;
    }
    
    public int arcameraStop() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
//            avcCodec.StopThread();
            mIsPreviewing = false;
            Log.d(TAG, "arcameraStop ok");
            return 1;
        }
        return -1;
    }

    /**
     * Deinit camera
     * @return 0 is ok
     */
    public int arcameraDeinitCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mIsPreviewing = false;
            int[] temp = {mTextureY, mTextureUV};
            GLES20.glDeleteTextures(2, temp, 0);
            Log.d(TAG, "arcameraDeinitCamera ok");
            return 1;
        }
        return -1;
    }
    
    public void doTakePicture() {
        if (mIsPreviewing && null != mCamera) {
            mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
        }
    }
    
    // call back to control sound
    ShutterCallback mShutterCallback = new ShutterCallback() {
        
        @Override
        public void onShutter() {
            Log.d(TAG, "onShutter");
        }
    };
    
    // call back to control jpeg file
    PictureCallback mJpegPictureCallback = new PictureCallback() {
        
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken");
            Bitmap b = null;
            if (null != data) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);
                mCamera.stopPreview();
                mIsPreviewing = false;
            }
            if (null != b) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90.0f);
                Bitmap rotateBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);

                String path = "/sdcard/11/test.jpg";
                try {
                    FileOutputStream fout = new FileOutputStream(path);
                    BufferedOutputStream bos = new BufferedOutputStream(fout);
                    b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mCamera.startPreview();
            mIsPreviewing = true;
        }
    };

    public boolean isPreViewing() {
        return mIsPreviewing;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(TAG, "onPreviewFrame");
        camera.addCallbackBuffer(mPreviewBuffer);

        synchronized (this) {
            byte[] yData = new byte[mDataLengthY];
            byte[] uvData = new byte[mDataLengthUV];

            System.arraycopy(data, 0, yData, 0, mDataLengthY);
            System.arraycopy(data, mDataLengthY, uvData, 0, mDataLengthUV);

            mBufferY.clear();
            mBufferY.put(yData).position(0);
            mBufferUV.clear();
            mBufferUV.put(uvData).position(0);
        }
    }

    public synchronized int arcameraUpdateCameraTexture_y() {
//        int[] tempTextures = new int[1];
//        GLES20.glGenTextures(1, tempTextures,0);
//        int tempTexture = tempTextures[0];
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY);
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1) ;
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mCameraDefaultWidth, mCameraDefaultHeight, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, mBufferY);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return mTextureY;
    }

    public synchronized int arcameraUpdateCameraTexture_uv() {
//        int[] tempTextures = new int[1];
//        GLES20.glGenTextures(1, tempTextures,0);
//        int tempTexture = tempTextures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureUV);
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1) ;
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, mCameraDefaultWidth / 2,
                mCameraDefaultHeight / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, mBufferUV);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return mTextureUV;
    }

}
