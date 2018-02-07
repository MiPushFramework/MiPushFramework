package top.trumeet.common.event.type;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;

import top.trumeet.common.event.Event;

/**
 * 喂给 {@link Event} 的详细信息。
 * 
 * Created by Trumeet on 2018/2/7.
 */

public abstract class EventType {
    @Event.Type
    private final int mType;

    /* buildContainer info */
    private final String mInfo;

    private final String pkg;

    public EventType(int mType, String mInfo, String pkg) {
        this.mType = mType;
        this.mInfo = mInfo;
        this.pkg = pkg;
    }

    @NonNull
    public CharSequence getTitle (Context context) {
        try {
            return context.getPackageManager().getApplicationLabel(context.getPackageManager()
                    .getApplicationInfo(pkg, PackageManager.GET_DISABLED_COMPONENTS));
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }

    @Nullable
    public abstract CharSequence getSummary (Context context);

    @Nullable
    public final CharSequence getInfo (Context context) {
        StringBuilder builder = new StringBuilder();
        CharSequence customInfo = getCustomInfo(context);
        if (customInfo != null) {
            builder.append(customInfo);
            builder.append("<br />");
        }
        if (mInfo != null && !mInfo.isEmpty()) {
            builder.append("<strong>Developer Info:</strong><br />");
            builder.append(mInfo);
        }
        String info = builder.toString();
        return info.trim().equals("") ?
                null : Html.fromHtml(info);
    }

    @Nullable
    public String getCustomInfo (Context context) {
        return null;
    }

    public int getType() {
        return mType;
    }

    public String getInfo() {
        return mInfo;
    }

    /**
     * Only used when type has meta data
     * @param original Event
     * @see top.trumeet.common.db.EventDb#insertEvent(int, EventType, Context)
     */
    @NonNull
    public Event fillEvent (@NonNull Event original) {
        return original;
    }

    public String getPkg() {
        return pkg;
    }

    @Override
    public String toString() {
        return "EventType{" +
                "mType=" + mType +
                ", mInfo='" + mInfo + '\'' +
                ", pkg='" + pkg + '\'' +
                '}';
    }
}
