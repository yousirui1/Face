package com.app.ysr.face.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

//import com.googlecode.javacv.cpp.opencv_core;
//import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;


/**
 * Created by yousi on 2017/2/5 0005.
 */

public class FaceUtil {
    private final static String TAG = FaceUtil.class.getSimpleName();
    private Context mContext;
    private static final File parentPath = Environment.getExternalStorageDirectory();
    private static String storagePath = "";
    private static final String DST_FOLDER_NAME = "FaceDetect";
    public FaceUtil(Context context){
        mContext = context;
    }


    /**
     * 特征保存
     * @param image    Mat
     * @param rect     人脸信息
     * @param fileName 文件名字
     * @return 保存是否成功
     */
    public boolean saveImage(Mat image, Rect rect,String fileName){
        try{
            // 原图置灰
            Mat grayMat = new Mat();
            Imgproc.cvtColor(image,grayMat,Imgproc.COLOR_BGR2GRAY);
            // 把检测到的人脸重新定义大小后保存成文件
            Mat sub  =grayMat.submat(rect);
            Mat mat = new Mat();
            Size size = new Size(180,200);
            Imgproc.resize(sub,mat,size);
            return Imgcodecs.imwrite(getFilePath(fileName),mat);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 提取特征
     * @param fileName 文件名
     * @return 特征图片
     */
    public Bitmap getImage(String fileName){
        try{
            Log.e(TAG,"getImage true");
            return BitmapFactory.decodeFile(getFilePath(fileName));
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG,"getImage false");
            return null;
        }
    }



    /*public double CmpPic(String file1,String file2) {
        try {
        int l_bins = 20;
        int hist_size[] = { l_bins };

        float v_ranges[] = { 0, 100 };
        float ranges[][] = { v_ranges };

        opencv_core.IplImage Image1 = cvLoadImage(file1, CV_LOAD_IMAGE_GRAYSCALE);
        opencv_core.IplImage Image2 = cvLoadImage(file2, CV_LOAD_IMAGE_GRAYSCALE);

        opencv_core.IplImage imageArr1[] = { Image1 };
        opencv_core.IplImage imageArr2[] = { Image2 };

        opencv_imgproc.CvHistogram Histogram1 = opencv_imgproc.CvHistogram.create(1, hist_size,
                CV_HIST_ARRAY, ranges, 1);
        opencv_imgproc.CvHistogram Histogram2 = opencv_imgproc.CvHistogram.create(1, hist_size,
                CV_HIST_ARRAY, ranges, 1);

        cvCalcHist(imageArr1, Histogram1, 0, null);
        cvCalcHist(imageArr2, Histogram2, 0, null);

        cvNormalizeHist(Histogram1, 100.0);
        cvNormalizeHist(Histogram2, 100.0);

        double c1 = cvCompareHist(Histogram1, Histogram2, CV_COMP_CORREL) * 100;
        double c2 = cvCompareHist(Histogram1, Histogram2, CV_COMP_INTERSECT);
        return (c1 + c2) / 2;
    } catch (Exception e) {
        e.printStackTrace();
        return -1;
    }

    }*/

    public int CmpPic()
    {
        Log.e(TAG,".........."+NdkUtil.getCmpPic());
        return NdkUtil.getCmpPic();
    }





    private String getFilePath(String fileName){
        if(storagePath.equals("")){
            storagePath =parentPath.getAbsolutePath()+"/"+DST_FOLDER_NAME;
            File f = new File(storagePath);
            if(!f.exists()){
                f.mkdir();
            }
        }
        try {
            return storagePath +"/"+fileName+".jpg";
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public void setPicId(int id,Context context){
        SharedPreferences sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);//其中"config"文件名，MODE_PRIVATE为文件的权限；
        SharedPreferences.Editor editor = sp.edit();//获得编辑这个文件的编辑器；
        editor.putInt("picid",id); //利用编辑器编辑内容
        editor.commit(); //调用这个方法提交保存数据。
    }


    public int getPicId(Context context){
        SharedPreferences sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        int picid = sp.getInt("picid",0);
        return picid;
    }

}
