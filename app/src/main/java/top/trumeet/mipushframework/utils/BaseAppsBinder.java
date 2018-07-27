package top.trumeet.mipushframework.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import me.drakeet.multitype.ItemViewBinder;
import top.trumeet.common.cache.ApplicationNameCache;
import top.trumeet.common.cache.IconCache;
import top.trumeet.mipush.BuildConfig;
import top.trumeet.mipush.R;

/**
 * Created by Trumeet on 2017/8/26.
 * Base application list recycler binder with async load icon
 *
 * @author Trumeet
 */

public abstract class BaseAppsBinder<T> extends ItemViewBinder<T, BaseAppsBinder.ViewHolder> {

    public static boolean debugIcon = BuildConfig.DEBUG;

    public BaseAppsBinder() {
        super();
    }

    @NonNull
    @Override
    protected final ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.preference_app, parent,
                false));
    }

    public final void fillData(String pkgName, boolean fillName, ViewHolder holder) {
        Context context = holder.itemView.getContext();
        if (fillName) {
            CharSequence appName = ApplicationNameCache.getInstance().getAppName(context, pkgName);
            if (appName == null) {
                appName = pkgName;
            }
            holder.title.setText(appName);
        }

        Bitmap icon = IconCache.getInstance().getRawIconBitmapWithoutLoader(context, pkgName);
        if (icon != null) {
            holder.icon.setImageBitmap(icon);
        } else {
            new IconWorkerTask(holder.icon).execute(pkgName);
        }
    }

    /**
     * Should use in {@link ItemViewBinder#onBindViewHolder(RecyclerView.ViewHolder, Object)}.
     * Load app title and icon
     *
     * @param pkgName Package name
     * @param holder  View holder
     * @deprecated Use #fillData(String, boolean, ViewHolder) instead
     */
    @Deprecated
    public final void fillData(String pkgName, ViewHolder holder) {
        fillData(pkgName, true, holder);
    }

    private class IconWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private Context context;

        private ImageView imageView;

        IconWorkerTask(ImageView imageView) {
            this.imageView = imageView;
            this.context = imageView.getContext();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String pkg = params[0];
            Bitmap icon = null;
            if (!TextUtils.isEmpty(pkg)) {
                if (debugIcon) {
                    icon = IconCache.getInstance().getWhiteIconBitmap(context, pkg);
                } else {
                    icon = IconCache.getInstance().getRawIconBitmap(context, pkg);
                }
            }
            if (icon == null) {
                Drawable drawable = ContextCompat.getDrawable(context, android.R.mipmap.sym_def_app_icon);

            }
            return icon;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
                if (debugIcon) {
                    imageView.setBackgroundColor(Color.BLACK);
                }
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView title;
        public TextView summary;
        public TextView text2;
        public TextView status;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(android.R.id.icon);
            title = itemView.findViewById(android.R.id.title);
            summary = itemView.findViewById(android.R.id.summary);
            text2 = itemView.findViewById(android.R.id.text2);
            status = itemView.findViewById(R.id.text_status);
        }
    }
}
