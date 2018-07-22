package com.xiaomi.xmsf.push.control;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import moe.yuuta.mipush.sdk.app.ControlAPIProvider;

import static com.xiaomi.xmsf.push.control.PushControllerUtils.isAllEnable;
import static com.xiaomi.xmsf.push.control.PushControllerUtils.isPrefsEnable;
import static com.xiaomi.xmsf.push.control.PushControllerUtils.setAllEnable;
import static top.trumeet.common.Constants.PUSH_SERVICE_VERSION_CODE;
import static top.trumeet.common.push.PushController.ARG_STRICT;

public class APIProvider extends ControlAPIProvider {

    @Override
    public int getVersion(@Nullable Bundle args) {
        return PUSH_SERVICE_VERSION_CODE;
    }

    @Override
    public boolean isEnable(@Nullable Bundle args) {
        Context context = getContext();
        boolean strict = args.getBoolean(ARG_STRICT, false);
        return strict ? isAllEnable(context) : isPrefsEnable(context);
    }

    @Override
    public void setEnable(boolean enable, @Nullable Bundle args) {
        setAllEnable(enable, getContext());
    }
}
