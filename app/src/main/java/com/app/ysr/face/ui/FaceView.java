package com.app.ysr.face.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;


import com.app.ysr.face.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by yousi on 2017/2/3 0003.
 * 调用OPenCV类库识别人脸
 */

public class FaceView extends JavaCameraView implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG = FaceView.class.getSimpleName();
    private CascadeClassifier mOpenCVDetect;
    private OnFaceDetectorListener mOnFaceDetectorListener;
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);//标识人脸矩阵的颜色

    private Mat mRgba;   //用于显示的彩色图
    private Mat mGray;   //灰度图

    private boolean mIsFrontCamera = false; //判断是否是前置摄像头
    private boolean isLoadSuccess = false; //载入OpenCV

    private int mAbsoluteFaceSize = 0;  // 脸部占屏幕多大面积的时候开始识别
    private static final float RELATIVE_FACE_SIZE = 0.2f;

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadOpenCV(context);
        setCvCameraViewListener(this);
    }

    //初始化并载入OpenCV
    private void loadOpenCV(Context context){
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11,context,mLoaderCallback);
    }

    //OpenCV回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getContext().getApplicationContext()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG,"OpenCV加载成功");
                    isLoadSuccess =true;
                    try{
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getContext().getApplicationContext().getDir("cascade", Context.MODE_PRIVATE);
                        File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(cascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                          }
                        is.close();
                        os.close();

                        mOpenCVDetect = new CascadeClassifier(cascadeFile.getAbsolutePath());
                        if (mOpenCVDetect.empty()) {
                            Log.e(TAG, "级联分类器加载失败");
                            mOpenCVDetect = null;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "没有找到级联分类器");
                    }
                    enableView();
            break;
            default:
            super.onManagerConnected(status);
            break;
            }
        }
    };

    @Override
    public void enableView() {
        if(isLoadSuccess)
        super.enableView();
    }

    @Override
    public void disableView() {
        if(!isLoadSuccess)
        super.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mGray = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray .release();
        mRgba .release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //子线程(非UI线程)
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        //如果为前置摄像头图像需要镜面旋转
        if(mIsFrontCamera){
            Core.flip(mRgba,mRgba,1);
        }

        //判断脸占屏幕的比例是否达到识别的需求
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * RELATIVE_FACE_SIZE) > 0) {
                mAbsoluteFaceSize = Math.round(height * RELATIVE_FACE_SIZE);
            }
        }
        // Java人脸检测器
        if (mOpenCVDetect != null) {
            MatOfRect faces = new MatOfRect();
            mOpenCVDetect.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
            // 检测到人脸
            Rect[] facesArray = faces.toArray();
            for (Rect aFacesArray : facesArray) {
                Imgproc.rectangle(mRgba, aFacesArray.tl(), aFacesArray.br(), FACE_RECT_COLOR, 3);
                if (null != mOnFaceDetectorListener) {
                    mOnFaceDetectorListener.onFace(mRgba, aFacesArray);
                }
            }
        }
        return mRgba;
    }

    public void setOnFaceDetectorListener(OnFaceDetectorListener listener) {
        mOnFaceDetectorListener = listener;
    }

    public interface OnFaceDetectorListener {
        // 检测到一个人脸的回调
        void onFace(Mat mat, Rect rect);
    }
}
