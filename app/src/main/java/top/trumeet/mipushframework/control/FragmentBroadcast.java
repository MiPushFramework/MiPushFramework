package top.trumeet.mipushframework.control;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Trumeet on 2017/12/30.
 */

public class FragmentBroadcast {
    private Map<String, Fragment> mChildFragments;

    public void registerFragment (@NonNull String tag,
                                   @NonNull Fragment fragment) {
        if (mChildFragments == null)
            mChildFragments = new HashMap<>(1);
        mChildFragments.put(tag, fragment);
    }

    public void unregisterFragment (@NonNull String tag) {
        if (mChildFragments == null)
            return;
        mChildFragments.remove(tag);
        if (mChildFragments.isEmpty())
            mChildFragments = null;
    }

    public boolean hasFragment (@NonNull String tag) {
        return mChildFragments != null && mChildFragments.containsKey(tag);
    }

    public Fragment getFragment (@NonNull String tag) {
        return mChildFragments.get(tag);
    }

    public void broadcast (@OnConnectStatusChangedListener.Status int status) {
        if (mChildFragments != null) {
            for (Fragment fragment : mChildFragments.values()) {
                if (fragment instanceof OnConnectStatusChangedListener) {
                    ((OnConnectStatusChangedListener) fragment)
                            .onChange(status);
                }
            }
        }
    }

    public void unregisterAll () {
        if (mChildFragments != null)
        mChildFragments = null;
    }
}
