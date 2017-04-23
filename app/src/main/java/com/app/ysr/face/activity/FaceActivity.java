package com.app.ysr.face.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.app.ysr.face.R;
import com.app.ysr.face.ui.FaceView;
import com.app.ysr.face.util.FaceUtil;
import com.app.ysr.face.util.NdkUtil;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * Created by yousi on 2017/1/31 0031.
 * 相机的Activity界面
 * 调用CameraSurfaceView类
 * 调用FaceView类
 * 来获得图像和判别人脸
 * 通过MainHandler
 */


public class FaceActivity extends Activity implements FaceView.OnFaceDetectorListener{
    private static final String TAG = FaceActivity.class.getSimpleName();
    private ImageButton captureBtn;
    private ImageButton switchBtn;
    private FaceView mOpenCVFaceView;

    private static boolean isGettingFace = false;
    private Bitmap mBitmapFace1;
    private Bitmap mBitmapFace2;
    private FaceUtil mFaceFaceUtil;
    private ImageView mImageViewFace1;
    private ImageView mImageViewFace2;
    private TextView mCmpPic;
    private double cmp;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式  
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏  
        // 检测人脸的View
        initUI();
        initViewPramas();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.face_menu, menu);
        return true;
    }

    /**
     * 初始化界面
     */
    private void initUI(){
        captureBtn = (ImageButton) findViewById(R.id.btn_shutter);
        //captureBtn.setOnClickListener(new BtnListener());
        mOpenCVFaceView = (FaceView) findViewById(R.id.opencv_faceview);
        if (mOpenCVFaceView != null) {
            mOpenCVFaceView.setOnFaceDetectorListener(this);
        }
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGettingFace = true;
            }
        });
        // 人脸特征处理的工具类
        mFaceFaceUtil = new FaceUtil(this);
        // 显示的View
        mImageViewFace1 = (ImageView) findViewById(R.id.face1);
        mImageViewFace2 = (ImageView) findViewById(R.id.face2);
        mCmpPic = (TextView) findViewById(R.id.text_view);
    }

    //OpenCV回调函数
    private BaseLoaderCallback mOpenCVCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV Manager已安装，可以使用OpenCV啦。");
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // 初始化OpenCV
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11,this,mOpenCVCallback);
    }

    /**
     * 初始化View的参数
     */
    private void initViewPramas(){

    }

    /**
     * 拍照
     */
    private void takePicture(){}


    /**
     * 检测到人脸
     *
     * @param mat  Mat
     * @param rect Rect
     */
    @Override
    public void onFace(Mat mat, Rect rect) {
        if (isGettingFace) {
            Log.i(TAG, "onFace: ");
            if (null == mBitmapFace1 || null != mBitmapFace2) {
                mBitmapFace1 = null;
                mBitmapFace2 = null;
                // 保存人脸信息并显示
                mFaceFaceUtil.saveImage(mat, rect, "100");
                mBitmapFace1 = mFaceFaceUtil.getImage("100");
                //Log.i(TAG, "............. " +mBitmapFace1.toString());
                //
                //mFaceFaceUtil.CmpPic();
                mBitmapFace2 = mFaceFaceUtil.getImage(""+ NdkUtil.getCmpPic());
                cmp = 0.0d;
            } else {
                /*mFaceFaceUtil.saveImage(mat, rect, "12");
                mBitmapFace2 = mFaceFaceUtil.getImage("12");
                mFaceFaceUtil.CmpPic();
                // 计算相似度
                //cmp = mFaceFaceUtil.CmpPic("face1", "face2");
                //mFaceFaceUtil.CmpPic();
                Log.i(TAG, "............. " +mBitmapFace1.toString());
                //Log.i(TAG, "onFace: 相似度 : " + cmp);*/
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (null == mBitmapFace1) {
                        mImageViewFace1.setImageResource(R.mipmap.ic_contact_picture);
                    } else {
                        mImageViewFace1.setImageBitmap(mBitmapFace1);
                    }
                    if (null == mBitmapFace2) {
                        mImageViewFace2.setImageResource(R.mipmap.ic_contact_picture);
                    } else {
                        mImageViewFace2.setImageBitmap(mBitmapFace2);
                    }
                    mCmpPic.setText(String.format("相似度 :  %.2f", cmp) + "%");
                }
            });

            isGettingFace = false;
        }
    }

    private class BtnListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            isGettingFace =true;
        }
    }


}

