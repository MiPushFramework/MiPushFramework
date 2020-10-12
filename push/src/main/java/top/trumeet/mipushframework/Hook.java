package top.trumeet.mipushframework;

import android.util.Log;

public class Hook {

    public static void hookXM(Object o1) {
        Log.i("hookXM", "hooking: " + o1.toString());
    }

    public static void hookXM2(Object o1, Object o2) {

        Log.i("hookXM", "hooking: " + o1.getClass().getName() + "," + o2.toString());
    }
}
