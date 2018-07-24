package top.trumeet.common.ita;

import android.content.Context;

/**
 * Created by zts1993 on 2018/2/18.
 */

public interface ITopActivity {

    boolean isEnabled(Context context);

    void guideToEnable(Context context);

    boolean isAppForeground(Context context, String packageName);
}
