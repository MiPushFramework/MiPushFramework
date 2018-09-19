#include <cstdio>
#include <unistd.h>
#include <fcntl.h>
#include <jni.h>
#include <cstring>
#include <cstdlib>
#include <sys/mman.h>
#include <array>
#include <thread>
#include <vector>
#include <utility>
#include <string>
#include <sys/system_properties.h>

#include "logging.h"
#include "hook.h"
#include "misc.h"

static char package_name[256];
static int uid;
static int enable_hook = true;

void nativeForkAndSpecialize(int res, int enable_hook, const char *package_name, jint uid) {
    if (res == 0 && enable_hook) {
        install_hook(package_name, uid / 100000);
    }
}

extern "C" {
__attribute__((visibility("default"))) void nativeForkAndSpecializePre(JNIEnv *env, jclass clazz,
                                                                       jint _uid, jint gid,
                                                                       jintArray gids,
                                                                       jint runtime_flags,
                                                                       jobjectArray rlimits,
                                                                       jint _mount_external,
                                                                       jstring se_info,
                                                                       jstring se_name,
                                                                       jintArray fdsToClose,
                                                                       jintArray fdsToIgnore,
                                                                       jboolean is_child_zygote,
                                                                       jstring instructionSet,
                                                                       jstring appDataDir) {
    uid = _uid;
}

__attribute__((visibility("default"))) int nativeForkAndSpecializePost(JNIEnv *env, jclass clazz,
                                                                       jint res) {
    nativeForkAndSpecialize(res, enable_hook, package_name, uid);
    return !enable_hook;
}
}
