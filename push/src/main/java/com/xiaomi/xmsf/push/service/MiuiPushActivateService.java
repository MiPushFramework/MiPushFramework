package com.xiaomi.xmsf.push.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import me.pqpo.librarylog4a.Log4a;

public class MiuiPushActivateService extends IntentService {
    private static final String TAG = MiuiPushActivateService.class.getSimpleName();


    static final Signature[] MIUI_PLATFORM_SIGNATURES = new Signature[]{new Signature("3082046c30820354a003020102020900e552a8ecb9011b7c300d06092a864886f70d0101050500308180310b300906035504061302434e3110300e060355040813074265696a696e673110300e060355040713074265696a696e67310f300d060355040a13065869616f6d69310d300b060355040b13044d495549310d300b060355040313044d495549311e301c06092a864886f70d010901160f6d697569407869616f6d692e636f6d301e170d3131313230363033323632365a170d3339303432333033323632365a308180310b300906035504061302434e3110300e060355040813074265696a696e673110300e060355040713074265696a696e67310f300d060355040a13065869616f6d69310d300b060355040b13044d495549310d300b060355040313044d495549311e301c06092a864886f70d010901160f6d697569407869616f6d692e636f6d30820120300d06092a864886f70d01010105000382010d00308201080282010100c786568a9aff253ad74c5d3e6fbffa12fed44cd3244f18960ec5511bb551e413115197234845112cc3df9bbacd3e0f4b3528cd87ed397d577dc9008e9cbc6a25fc0664d3a3f440243786db8b250d40f6f148c9a3cd6fbc2dd8d24039bd6a8972a1bdee28c308798bfa9bb3b549877b10f98e265f118c05f264537d95e29339157b9d2a31485e0c823521cca6d0b721a8432600076d669e20ac43aa588b52c11c2a51f04c6bb31ad6ae8573991afe8e4957d549591fcb83ec62d1da35b1727dc6b63001a5ef387b5a7186c1e68da1325772b5307b1bc739ef236b9efe06d52dcaf1e32768e3403e55e3ec56028cf5680cfb33971ccf7870572bc47d3e3affa385020103a381e83081e5301d0603551d0e0416041491ae2f8c72e305f92aa9f7452e2a3160b841a15c3081b50603551d230481ad3081aa801491ae2f8c72e305f92aa9f7452e2a3160b841a15ca18186a48183308180310b300906035504061302434e3110300e060355040813074265696a696e673110300e060355040713074265696a696e67310f300d060355040a13065869616f6d69310d300b060355040b13044d495549310d300b060355040313044d495549311e301c06092a864886f70d010901160f6d697569407869616f6d692e636f6d820900e552a8ecb9011b7c300c0603551d13040530030101ff300d06092a864886f70d010105050003820101003b3a699ceb497300f2ab86cbd41c513440bf60aa5c43984eb1da140ef30544d9fbbb3733df24b26f2703d7ffc645bf598a5e6023596a947e91731542f2c269d0816a69c92df9bfe8b1c9bc3c54c46c12355bb4629fe6020ca9d15f8d6155dc5586f5616db806ecea2d06bd83e32b5f13f5a04fe3e5aa514f05df3d555526c63d3d62acf00adee894b923c2698dc571bc52c756ffa7a2221d834d10cb7175c864c30872fe217c31442dff0040a67a2fb1c8ba63eac2d5ba3d8e76b4ff2a49b0db8a33ef4ae0dd0a840dd2a8714cb5531a56b786819ec9eb1051d91b23fde06bd9d0708f150c4f9efe6a416ca4a5e0c23a952af931ad3579fb4a8b19de98f64bd9"), new Signature("308204a830820390a003020102020900b3998086d056cffa300d06092a864886f70d0101040500308194310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e20566965773110300e060355040a1307416e64726f69643110300e060355040b1307416e64726f69643110300e06035504031307416e64726f69643122302006092a864886f70d0109011613616e64726f696440616e64726f69642e636f6d301e170d3038303431353232343035305a170d3335303930313232343035305a308194310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e20566965773110300e060355040a1307416e64726f69643110300e060355040b1307416e64726f69643110300e06035504031307416e64726f69643122302006092a864886f70d0109011613616e64726f696440616e64726f69642e636f6d30820120300d06092a864886f70d01010105000382010d003082010802820101009c780592ac0d5d381cdeaa65ecc8a6006e36480c6d7207b12011be50863aabe2b55d009adf7146d6f2202280c7cd4d7bdb26243b8a806c26b34b137523a49268224904dc01493e7c0acf1a05c874f69b037b60309d9074d24280e16bad2a8734361951eaf72a482d09b204b1875e12ac98c1aa773d6800b9eafde56d58bed8e8da16f9a360099c37a834a6dfedb7b6b44a049e07a269fccf2c5496f2cf36d64df90a3b8d8f34a3baab4cf53371ab27719b3ba58754ad0c53fc14e1db45d51e234fbbe93c9ba4edf9ce54261350ec535607bf69a2ff4aa07db5f7ea200d09a6c1b49e21402f89ed1190893aab5a9180f152e82f85a45753cf5fc19071c5eec827020103a381fc3081f9301d0603551d0e041604144fe4a0b3dd9cba29f71d7287c4e7c38f2086c2993081c90603551d230481c13081be80144fe4a0b3dd9cba29f71d7287c4e7c38f2086c299a1819aa48197308194310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e20566965773110300e060355040a1307416e64726f69643110300e060355040b1307416e64726f69643110300e06035504031307416e64726f69643122302006092a864886f70d0109011613616e64726f696440616e64726f69642e636f6d820900b3998086d056cffa300c0603551d13040530030101ff300d06092a864886f70d01010405000382010100572551b8d93a1f73de0f6d469f86dad6701400293c88a0cd7cd778b73dafcc197fab76e6212e56c1c761cfc42fd733de52c50ae08814cefc0a3b5a1a4346054d829f1d82b42b2048bf88b5d14929ef85f60edd12d72d55657e22e3e85d04c831d613d19938bb8982247fa321256ba12d1d6a8f92ea1db1c373317ba0c037f0d1aff645aef224979fba6e7a14bc025c71b98138cef3ddfc059617cf24845cf7b40d6382f7275ed738495ab6e5931b9421765c491b72fb68e080dbdb58c2029d347c8b328ce43ef6a8b15533edfbe989bd6a48dd4b202eda94c6ab8dd5b8399203daae2ed446232e4fe9bd961394c6300e5138e3cfd285e6e4e483538cb8b1b357"), new Signature("3082035f30820247a00302010202045ebcfb14300d06092a864886f70d01010b0500305f310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e67311a3018060355040a131144756f6b616e20546563686e6f6c6f67793110300e0603550403130757616e672059693020170d3131303831343039313431375a180f32303631303830313039313431375a305f310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e67311a3018060355040a131144756f6b616e20546563686e6f6c6f67793110300e0603550403130757616e6720596930820122300d06092a864886f70d01010105000382010f003082010a0282010100a2054effddadade500d8f80f09c23a979bb7c3351c3b265373f719b37b0a9c3686eb51f328c286fbe76400ff65a34044b62028d2806837de4538830d30d1857843f494d3093304deebc52d1e0f0c8c95354d2c7b1ba7dd7b80596ad17c4b0b0ffb287c567cdb97a48a084dacb6089bc795320559e2eb1c8e9f724955acbeb78c7d58e46322e6daa0e75c6d27a00ef1f352d7420b8a27a052b1942d0d2c10c2a48a3dd4d7d55818c01eed0dc2e18ef7c7203078e1d4d2dc3660efb1e3fc01a8b29b2b64bba7a0f5bba56c7f8666717fc1ba80cf72823487c5164ef8ecea3a9e1e9434cfc7e985ac2601addd1d2bb03b8e31d4e93adf6be93d090c83df4bd749db0203010001a321301f301d0603551d0e041604140fd5297793ef0c30b382c46a946202b2d3eeb3d2300d06092a864886f70d01010b050003820101002c393ac2352ec41602d86dc83384c5562d014e155feacc46dcda1f4f8d060cdd57a348616b20c0e0fc6db9c730112326db9796dbf797ebc45c6cad0cf42e285752bd6ec1d51b13f9583e45be4ec153ae81815aa826fa5c70ab51c228f9b1491919b4b28e747bc706eb0c400c884b01a70fade2679d2b0ca40c39ff241b953728851435ac6567cd363788fd77c6b3fe420c90ad098fde2c3b199855b6bed171b86d9cef091d2691e872ef3a3f63b96c1ff58bfd2b5c7b45f6fb80b5cd58a7fd5bf42df1543009a6c7b3c97fc28da5d9ee91879fa6b213531216997f4ea7e4c52dccfbfa1de79a6724ed7bb3c073029cf137b953e909b15ad57b075011fdda7ce8")};
    private Handler handler;

    public MiuiPushActivateService() {
        this("miui_push_activate_service");
    }

    public MiuiPushActivateService(String str) {
        super(str);
        this.handler = new Handler(Looper.getMainLooper());
    }

    public static void awakePushActivateService(Context context, String str) {
        try {
            Intent intent = new Intent(context, MiuiPushActivateService.class);
            intent.setPackage(context.getPackageName());
            intent.setAction(str);
            context.startService(intent);
        } catch (Throwable th) {
            Log4a.e(TAG, "unable to start service" + th.getMessage());
        }
    }

    private List<String> getPackages() {
        List<String> arrayList = new ArrayList();
        List<ApplicationInfo> installedApplications = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        if (installedApplications != null) {
            for (ApplicationInfo applicationInfo : installedApplications) {
                if (applicationInfo.metaData != null && applicationInfo.metaData.containsKey("miui_push_app") && verifySignatures(applicationInfo.packageName)) {
                    arrayList.add(applicationInfo.packageName);
                }
            }
        }
        return arrayList;
    }

    private boolean isPackageRegistered(String str) {
        return getSharedPreferences("pref_registered_pkg_names", 0).contains(str);
    }

    private boolean verifySignatures(String str) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(str, PackageManager.GET_SIGNATURES);
            if (packageInfo.signatures != null) {
                for (Object obj : packageInfo.signatures) {
                    for (Signature equals : MIUI_PLATFORM_SIGNATURES) {
                        if (equals.equals(obj)) {
                            return true;
                        }
                    }
                }
            }
        } catch (NameNotFoundException e) {
        }
        return false;
    }

    public void addRegisteredPackage(String str, String str2) {
        getSharedPreferences("pref_registered_pkg_names", 0).edit().putString(str, str2).commit();
    }

    protected void onHandleIntent(Intent intent) {
        if ("com.xiaomi.xmsf.push.SCAN".equals(intent.getAction())) {
            long j = 0;
            for (final String str : getPackages()) {
                if (!isPackageRegistered(str)) {
                    j += 60000;
                    this.handler.postDelayed(new Runnable() {
                        public void run() {
                            try {
                                Intent intent = new Intent("com.xiaomi.xmsf.push.SCAN");
                                intent.setPackage(str);
                                MiuiPushActivateService.this.startService(intent);
                            } catch (Throwable th) {
                                Log4a.e(TAG, "unable to start service" + th.getMessage());
                            }
                        }
                    }, j);
                }
            }
        } else if ("com.xiaomi.xmsf.push.ACCOUNT_CHANGE".equals(intent.getAction())) {
            for (String str2 : getPackages()) {
                if (isPackageRegistered(str2)) {
                    try {
                        Intent intent2 = new Intent("com.xiaomi.xmsf.push.ACCOUNT_CHANGE");
                        intent2.setPackage(str2);
                        startService(intent2);
                    } catch (Throwable th) {
                        Log4a.e(TAG, "unable to start service" + th.getMessage());
                    }
                }
            }
        } else if ("com.xiaomi.xmsf.push.APP_REGISTERED".equals(intent.getAction())) {
            String stringExtra = intent.getStringExtra("source_package");
            String stringExtra2 = intent.getStringExtra("app_id");
            if (!TextUtils.isEmpty(stringExtra) && !TextUtils.isEmpty(stringExtra2)) {
                addRegisteredPackage(stringExtra, stringExtra2);
            }
        }
    }
}