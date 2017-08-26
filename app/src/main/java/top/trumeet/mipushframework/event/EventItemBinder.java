package top.trumeet.mipushframework.event;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaomi.xmsf.R;

import java.util.Date;

import me.drakeet.multitype.ItemViewBinder;
import top.trumeet.mipushframework.event.notification.NotificationInfo;
import top.trumeet.mipushframework.utils.ParseUtils;

/**
 * Created by Trumeet on 2017/8/26.
 * @see Event
 * @see EventFragment
 * @author Trumeet
 */

public class EventItemBinder extends ItemViewBinder<Event,
        EventItemBinder.ViewHolder> {
    private LruCache<String, Drawable> mIconMemoryCaches;

    EventItemBinder () {
        super();
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSizes = maxMemory/5;

        mIconMemoryCaches = new LruCache<>(cacheSizes);
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.preference_app, parent,
                false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull Event item) {
        PackageManager packageManager = holder.itemView.getContext().
                getPackageManager();
        try {
            holder.title.setText(packageManager.getApplicationLabel(packageManager.getApplicationInfo(item.getPkg(),
                    0)));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            holder.title.setText(item.getPkg());
        }
        Drawable icon = getIconFromMemoryCache(item.getPkg());
        if (icon != null) {
            holder.icon.setImageDrawable(icon);
        } else {
            new IconWorkerTask(holder.icon)
                    .execute(item.getPkg());
        }
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


    private void addDrawableToMemoryCache (@Nullable String pkg, @NonNull Drawable icon) {
        if (getIconFromMemoryCache(pkg) == null) {
            if (pkg == null) pkg = "";
            mIconMemoryCaches.put(pkg, icon);
        }
    }

    private Drawable getIconFromMemoryCache (@Nullable String pkg) {
        if (pkg == null) pkg = "";
        return mIconMemoryCaches.get(pkg);
    }

    public class IconWorkerTask extends AsyncTask<String, Void, Drawable> {
        private Context context;

        private ImageView imageView;
        IconWorkerTask (ImageView imageView) {
            this.imageView = imageView;
            this.context = imageView.getContext();
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String pkg = params[0];
            Drawable icon;
            if (pkg == null || pkg.equals("")) {
                pkg = "";
                icon = ContextCompat.getDrawable(context,
                        android.R.mipmap.sym_def_app_icon);
            } else {
                try {
                    icon = context.getPackageManager()
                            .getApplicationIcon(pkg);
                } catch (PackageManager.NameNotFoundException ignore) {
                    icon = ContextCompat.getDrawable(context,
                            android.R.mipmap.sym_def_app_icon);
                }
            }
            if (icon == null) {
                return null;
            }
            addDrawableToMemoryCache(pkg, icon);
            return icon;
        }

        @Override
        protected void onPostExecute (Drawable drawable) {
            if (imageView != null) {
                imageView.setImageDrawable(drawable);
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView summary;
        TextView text2;
        ViewHolder (View itemView) {
            super(itemView);
            icon = itemView.findViewById(android.R.id.icon);
            title = itemView.findViewById(android.R.id.title);
            summary = itemView.findViewById(android.R.id.summary);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
