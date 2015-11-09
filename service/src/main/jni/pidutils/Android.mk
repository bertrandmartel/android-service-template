LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := pidutils

LOCAL_SRC_FILES := pid.c

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
