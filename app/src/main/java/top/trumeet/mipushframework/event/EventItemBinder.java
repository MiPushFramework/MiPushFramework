package top.trumeet.mipushframework.event;

import android.support.annotation.NonNull;

import com.xiaomi.xmsf.R;

import java.util.Date;

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
            case Event.Type.RECEIVE_PUSH:
                text = holder.itemView.getContext()
                        .getString(R.string.event_push);
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
        holder.text2.setText(
                ParseUtils.getFriendlyDateString(new Date(item.getDate()),
                        EventDB.getUTC(), holder.itemView.getContext()));
        holder.status.setText(status);
    }

}
