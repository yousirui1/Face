LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OPENCV_CAMERA_MODULES:=off
#OPENCV_INSTALL_MODULES:=off
#OPENCV_LIB_TYPE:=SHARED
ifeq ("$(wildcard $(OPENCV_MK_PATH))","")
include /Users/yousirui/Documents/opencv/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk
else
include $(OPENCV_MK_PATH)
endif
LOCAL_SRC_FILES := jni_proces.c
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl

LOCAL_MODULE     := Ndklib

include $(BUILD_SHARED_LIBRARY)
