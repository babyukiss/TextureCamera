package com.babyukiss.cameratexture;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

public class MainActivity extends Activity {
    CameraGLView glSurfaceView = null;
    ImageButton shutterBtn;
    int mScreenWidth;
    int mScreenHeight;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        glSurfaceView = (CameraGLView)findViewById(R.id.camera_textureview);
        shutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
        LayoutParams params = glSurfaceView.getLayoutParams();
        DisplayMetrics metrix = getResources().getDisplayMetrics();
        mScreenWidth = metrix.widthPixels;
        mScreenHeight = metrix.heightPixels;
        params.width = mScreenWidth;
        params.height = mScreenHeight;
        glSurfaceView.setLayoutParams(params);

        //手动设置拍照ImageButton的大小为120dip×120dip,原图片大小是64×64
        LayoutParams p2 = shutterBtn.getLayoutParams();
        p2.width = 100;
        p2.height = 100;
        shutterBtn.setLayoutParams(p2); 
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
