package top.trumeet.mipushframework.event;

import android.support.annotation.NonNull;

import com.xiaomi.xmsf.R;

import java.util.Date;

import top.trumeet.mipushframework.event.notification.NotificationInfo;
import top.trumeet.mipushframework.utils.BaseAppsBinder;
import top.trumeet.mipushframework.utils.ParseUtils;

/**
 * Created by Trumeet on 2017/8/26.
 * @see Event
 * @see EventFragment
 * @author Trumeet
 */

public class EventItemBinder extends BaseAppsBinder<Event> {


    EventItemBinder() {
        super();
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull Event item) {
        fillData(item.getPkg(), holder);
        String text;
        switch (item.getType()) {
            case Event.Type.REGISTER :
                text = holder.itemView.getContext()
                        .getString(R.string.event_register);
                break;
            case Event.Type.PUSH_MESSAGE :
                NotificationInfo info = item.getNotificationInfo();
                text = info != null ? holder.itemView.getContext()
                        .getString(R.string.event_push_message,
                                info.getTitle(), info.getText()) :
                        holder.itemView.getContext()
                                .getString(R.string.event_push_message, "", "");
                break;
            case Event.Type.PUSH_COMMAND:
                text = holder.itemView.getContext()
                        .getString(R.string.event_push_command);
                break;
            default:
                text = null;
                break;
        }
        holder.summary.setText(text);

        String status;
        switch (item.getResult()) {
            case Event.ResultType.OK :
                status = holder.itemView.getContext()
                        .getString(R.string.status_ok);
                break;
            case Event.ResultType.DENY_DISABLED:
                status = holder.itemView.getContext()
                        .getString(R.string.status_deny_disable);
                break;
            case Event.ResultType.DENY_USER:
                status = holder.itemView.getContext()
                        .getString(R.string.status_deny_user);
                break;
            default:
                status = "";
                break;
        }
        holder.text2.setText(holder.itemView.getContext()
        .getString(R.string.event_status,
                status,
                ParseUtils.getFriendlyDateString(new Date(item.getDate()),
                        EventDB.getUTC(), holder.itemView.getContext())));
    }

}
