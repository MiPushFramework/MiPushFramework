package top.trumeet.common.event.type;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import top.trumeet.common.R;
import top.trumeet.common.event.Event;

/**
 * 对应 {@link top.trumeet.common.event.Event.Type#SendMessage}
 *
 * Created by Trumeet on 2018/2/7.
 */

public class NotificationType extends EventType {
    private final String mNotificationTitle;
    private final String mNotificationDetail;

    public NotificationType(String mInfo, String pkg, String mNotificationTitle, String mNotificationDetail) {
        super(Event.Type.Notification, mInfo, pkg);
        this.mNotificationTitle = mNotificationTitle;
        this.mNotificationDetail = mNotificationDetail;
    }

    @Override
    @NonNull
    public CharSequence getTitle (Context context) {
        return mNotificationTitle == null ? super.getTitle(context) :
                mNotificationTitle;
    }


    @Nullable
    @Override
    public CharSequence getSummary(Context context) {
        return mNotificationDetail == null ? context.getString(R.string.event_push) :
                mNotificationDetail;
    }

    public String getNotificationTitle() {
        return mNotificationTitle;
    }

    public String getNotificationDetail() {
        return mNotificationDetail;
    }

    @NonNull
    @Override
    public Event fillEvent (@NonNull Event original) {
        original.setNotificationTitle(mNotificationTitle);
        original.setNotificationSummary(mNotificationDetail);
        return original;
    }
}
