/**
 * 
 */
package com.babyukiss.cameratexture;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.babyukiss.cameratexture.camera.camera;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.AttributeSet;
import android.util.Log;
import android.graphics.SurfaceTexture;

/**
 * @author hongen
 *
 */
public class CameraGLView extends GLSurfaceView implements Renderer, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "CameraGLView";
    private Context mContext;
    private SurfaceTexture mSurfaceTexture;
    private int mTextureID = -1;
    float[] mMtx;
    private CameraDrawer mDrawer;

    public CameraGLView(Context context) {
        super(context);
    }

    public CameraGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mMtx = new float[16];
        setEGLContextClientVersion(2);  
        setRenderer(this);  
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    // SurfaceTexture call back
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.i(TAG, TAG + "onFrameAvailable...");  
        this.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, TAG+"onSurfaceCreated");
        mTextureID = createTextureID();
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mDrawer = new CameraDrawer(mTextureID);
        camera.getInstance().arcameraInitCamera(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, TAG+"onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
        mDrawer.updateProjection(width, height);
        if (!camera.getInstance().isPreViewing()) {
            camera.getInstance().arcameraStart(mSurfaceTexture);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, TAG+ "onDrawFrame");
        GLES20.glClearColor(0, 0, 1, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mMtx);
        mDrawer.draw(mMtx);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        camera.getInstance().arcameraDeinitCamera();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }


    private int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }
}
