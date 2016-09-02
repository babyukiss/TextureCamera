/**
 * 
 */
package com.babyukiss.cameratexture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * @author hongen
 *
 */
public class CameraDrawer {
    /**
     * 归一化处理，需要增加uniform
     */
    private final String vertexShaderCode = 
            "attribute vec4 vPosition; \n" +
            "attribute vec2 inputTextureCoordinate; \n" +
            "uniform mat4 u_Matrix; \n" +
            "varying vec2 textureCoordinate; \n" +
            "void main() \n" +
            "{ \n" +
                "gl_Position = u_Matrix * vPosition; \n" +
                "textureCoordinate = inputTextureCoordinate; \n" +
            "}";

    private final String fragmentShaderCode = 
            "#extension GL_OES_EGL_image_external : require \n" +
            "precision mediump float; \n" +
            "varying vec2 textureCoordinate; \n" +
            "uniform samplerExternalOES s_texture; \n" +
            "void main() \n" +
            "{ \n" +
                "gl_FragColor = texture2D(s_texture, textureCoordinate); \n" +
            "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mTextureCoordHandle;
    private int uMatrixHandle;
    
    private short[] drawOrder = {0, 1, 2, 0, 2, 3};
    private static final int COORDS_PER_VERTEX = 2;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private float[] projectionMatrix = new float[16];
    
    static float[] squareCoords = {
        -1.0f, 1.0f,
        -1.0f,-1.0f,
         1.0f,-1.0f,
         1.0f, 1.0f,
    };
    
    /*static float[] squareCoords = {
        -0.5f, 0.5f,
        -0.5f,-0.5f,
        0.5f,-0.5f,
        0.5f, 0.5f,
    };*/
    
    static float[] textureVertices = {
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
        0.0f, 0.0f,
    };

    private int mTexture;

    public CameraDrawer(int texture) {
        this.mTexture = texture;
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer= bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        ByteBuffer dlb  = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        
        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);
        
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragShader);
        GLES20.glLinkProgram(mProgram);
        
        uMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_Matrix");
    }
    
    public void updateProjection(int width, int height) {
        final float aspectRatio = width > height ? (float)width / (float) height : (float)height / (float)width;
        if (width > height) {
//            Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
            Matrix.frustumM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 1f, 10f);
        } else {
//            Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
            Matrix.frustumM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, 1f, 10f);
        }
        Matrix.setLookAtM(projectionMatrix, 0, 0, 0, 3f, 0f, 0f, 0f, 0f, 1.0f, 0f);
    }

    public void draw(float[] mtx) {
        GLES20.glUseProgram(mProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTexture);

        GLES20.glUniformMatrix4fv(uMatrixHandle, 1, false, projectionMatrix, 0);
        // get vertex shader vPosition
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);

//        textureVerticesBuffer.clear();
//        textureVerticesBuffer.put( transformTextureCoordinates( textureVertices, mtx ));
//        textureVerticesBuffer.position(0);

        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
    
    private float[] transformTextureCoordinates(float[] coords, float[] matrix) {
        float[] result = new float[coords.length];
        float[] vt = new float[4];

        for (int i = 0; i < coords.length; i += 2) {
            float[] v = { coords[i], coords[i + 1], 0, 1 };
            Matrix.multiplyMV(vt, 0, matrix, 0, v, 0);
            result[i] = vt[0];
            result[i + 1] = vt[1];
        }
        return result;
    }
}
