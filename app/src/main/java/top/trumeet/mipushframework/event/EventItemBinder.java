package top.trumeet.mipushframework.event;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.xiaomi.xmsf.R;

import java.util.Date;

import top.trumeet.common.event.Event;
import top.trumeet.common.utils.Utils;
import top.trumeet.mipushframework.permissions.ManagePermissionsActivity;
import top.trumeet.mipushframework.utils.BaseAppsBinder;
import top.trumeet.mipushframework.utils.ParseUtils;

/**
 * Created by Trumeet on 2017/8/26.
 * @see Event
 * @see EventFragment
 * @author Trumeet
 */

public class EventItemBinder extends BaseAppsBinder<Event> {

    private boolean clickEnabled = true;
    EventItemBinder(boolean clickEnabled) {
        super();
        this.clickEnabled = clickEnabled;
    }

    @Override
    protected void onBindViewHolder(final @NonNull ViewHolder holder, final @NonNull Event item) {
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
                        Utils.getUTC(), holder.itemView.getContext()));
        holder.status.setText(status);

        if (clickEnabled) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.itemView.getContext()
                            .startActivity(new Intent(holder.itemView.getContext(),
                                    ManagePermissionsActivity.class)
                                    .putExtra(ManagePermissionsActivity.EXTRA_PACKAGE_NAME,
                                            item.getPkg()));
                }
            });
        }
    }

}
