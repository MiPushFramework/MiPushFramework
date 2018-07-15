package com.xiaomi.xmsf.push.control;

import android.os.Bundle;
import android.support.annotation.Nullable;

import moe.yuuta.mipush.sdk.app.ControlAPIProvider;

import static top.trumeet.common.Constants.PUSH_SERVICE_VERSION_CODE;
import static top.trumeet.common.push.PushController.ARG_STRICT;

public class APIProvider extends ControlAPIProvider {

    @Override
    public int getVersion(@Nullable Bundle args) {
        return PUSH_SERVICE_VERSION_CODE;
    }

    @Override
    public boolean isEnable(@Nullable Bundle args) {
        boolean strict = args.getBoolean(ARG_STRICT, false);
        return strict ?
                PushControllerUtils.isAllEnable(getContext())
                :
                (PushControllerUtils.isPrefsEnable(getContext())
                        && PushControllerUtils.isServiceRunning(getContext())
                        && PushControllerUtils.isBootReceiverEnable(getContext()));
    }

    @Override
    public void setEnable(boolean enable, @Nullable Bundle args) {
        PushControllerUtils.setAllEnable(enable, getContext());
    }
}
