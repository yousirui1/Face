package com.app.ysr.face.util;

/**
 * Created by yousirui on 17/3/21.
 */

public class NdkUtil {
   static {
       System.loadLibrary("Ndklib");
   }
   //public static native double getCmpPicValue(String file1,String file2);
   public static native int getCmpPic();
}
