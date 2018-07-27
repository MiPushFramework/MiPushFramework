package top.trumeet.common.ita.impl;

import android.content.Context;

import top.trumeet.common.ita.ITopActivity;

/**
 * Created by zts1993 on 2018/2/18.
 */

public class FakeImpl implements ITopActivity {

    @Override
    public boolean isEnabled(Context context) {
        return true;
    }

    @Override
    public void guideToEnable(Context context) {
    }

    @Override
    public boolean isAppForeground(Context context, String packageName) {
        return false;
    }

}
