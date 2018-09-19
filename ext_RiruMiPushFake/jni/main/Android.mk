LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE     := libriru_mipushfake
LOCAL_C_INCLUDES := \
	$(LOCAL_PATH) \
	jni/external/include
LOCAL_CPPFLAGS += $(CPPFLAGS)
LOCAL_STATIC_LIBRARIES := xhook
LOCAL_LDLIBS += -ldl -llog
LOCAL_LDFLAGS := -Wl

LOCAL_SRC_FILES:= main.cpp hook.cpp misc.cpp

include $(BUILD_SHARED_LIBRARY)