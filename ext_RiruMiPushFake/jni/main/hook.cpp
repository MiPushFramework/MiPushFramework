#include <cstdio>
#include <cstring>
#include <chrono>
#include <fcntl.h>
#include <unistd.h>
#include <sys/vfs.h>
#include <sys/stat.h>
#include <dirent.h>
#include <dlfcn.h>
#include <cstdlib>
#include <string>
#include <sys/system_properties.h>
#include <xhook/xhook.h>

#include "logging.h"

#define XHOOK_REGISTER(NAME) \
    if (xhook_register(".*", #NAME, (void*) new_##NAME, (void **) &old_##NAME) != 0) \
        LOGE("failed to register hook " #NAME "."); \

#define NEW_FUNC_DEF(ret, func, ...) \
    static ret (*old_##func)(__VA_ARGS__); \
    static ret new_##func(__VA_ARGS__)

NEW_FUNC_DEF(int, __system_property_get, const char *key, char *value) {
    int res = old___system_property_get(key, value);
    if (key) {
        if (strcmp("ro.miui.ui.version.name", key) == 0) {
            strcpy(value, "V9");
            LOGI("system_property_get: %s -> %s", key, value);
        } else if (strcmp("ro.miui.ui.version.code", key) == 0) {
            strcpy(value, "7");
            LOGI("system_property_get: %s -> %s", key, value);
        } else if (strcmp("ro.miui.version.code_time", key) == 0) {
            strcpy(value, "1527550858");
            LOGI("system_property_get: %s -> %s", key, value);
        } else if (strcmp("ro.miui.internal.storage", key) == 0) {
            strcpy(value, "/sdcard/");
            LOGI("system_property_get: %s -> %s", key, value);
        } else if (strcmp("ro.product.manufacturer", key) == 0) {
            strcpy(value, "Xiaomi");
            LOGI("system_property_get: %s -> %s", key, value);
        } else if (strcmp("ro.product.brand", key) == 0) {
            strcpy(value, "Xiaomi");
            LOGI("system_property_get: %s -> %s", key, value);
        } else if (strcmp("ro.product.name", key) == 0) {
            strcpy(value, "Xiaomi");
            LOGI("system_property_get: %s -> %s", key, value);
        }

    }
    return res;
}

NEW_FUNC_DEF(std::string, _ZN7android4base11GetPropertyERKNSt3__112basic_stringIcNS1_11char_traitsIcEENS1_9allocatorIcEEEES9_, const std::string &key, const std::string &default_value) {
    std::string res = old__ZN7android4base11GetPropertyERKNSt3__112basic_stringIcNS1_11char_traitsIcEENS1_9allocatorIcEEEES9_(key, default_value);
    
    if (strcmp("ro.miui.ui.version.name", key.c_str()) == 0) {
        res = "V9";
        LOGI("android::base::GetProperty: %s -> %s", key.c_str(), res.c_str());
    } else if (strcmp("ro.miui.ui.version.code", key.c_str()) == 0) {
        res = "7";
        LOGI("android::base::GetProperty: %s -> %s", key.c_str(), res.c_str());
    } else if (strcmp("ro.miui.version.code_time", key.c_str()) == 0) {
        res = "1527550858";
        LOGI("android::base::GetProperty: %s -> %s", key.c_str(), res.c_str());
    } else if (strcmp("ro.miui.internal.storage", key.c_str()) == 0) {
        res = "/sdcard/";
        LOGI("android::base::GetProperty: %s -> %s", key.c_str(), res.c_str());
    } else if (strcmp("ro.product.manufacturer", key.c_str()) == 0) {
        res = "Xiaomi";
        LOGI("android::base::GetProperty: %s -> %s", key.c_str(), res.c_str());
    } else if (strcmp("ro.product.brand", key.c_str()) == 0) {
        res = "Xiaomi";
        LOGI("android::base::GetProperty: %s -> %s", key.c_str(), res.c_str());
    } else if (strcmp("ro.product.name", key.c_str()) == 0) {
        res = "Xiaomi";
        LOGI("android::base::GetProperty: %s -> %s", key.c_str(), res.c_str());
    }
    return res;
}

void install_hook(const char *package_name, int user) {
    LOGI("install hook for %s (%d)", package_name, user);

    XHOOK_REGISTER(__system_property_get);

    char sdk[PROP_VALUE_MAX + 1];
    if (__system_property_get("ro.build.version.sdk", sdk) > 0 && atoi(sdk) >= 28)
        XHOOK_REGISTER(_ZN7android4base11GetPropertyERKNSt3__112basic_stringIcNS1_11char_traitsIcEENS1_9allocatorIcEEEES9_);

    if (xhook_refresh(0) == 0)
        xhook_clear();
    else
        LOGE("failed to refresh hook");
}