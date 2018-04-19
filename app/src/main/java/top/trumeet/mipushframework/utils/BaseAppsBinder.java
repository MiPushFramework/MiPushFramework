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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import me.drakeet.multitype.ItemViewBinder;
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

    private LruCache<String, Drawable> mIconMemoryCaches;

    public BaseAppsBinder() {
        super();
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSizes = maxMemory / 5;

        mIconMemoryCaches = new LruCache<>(cacheSizes);
    }

    @NonNull
    @Override
    protected final ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.preference_app, parent,
                false));
    }

    public final void fillData(String pkgName, boolean fillName, ViewHolder holder) {
        if (fillName) {
            PackageManager packageManager = holder.itemView.getContext().
                    getPackageManager();
            try {
                holder.title.setText(packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName,
                        0)));
            } catch (PackageManager.NameNotFoundException e) {
                holder.title.setText(pkgName);
            }
        }
        Drawable icon = getIconFromMemoryCache(pkgName);
        if (icon != null) {
            holder.icon.setImageDrawable(icon);
        } else {
            new IconWorkerTask(holder.icon)
                    .execute(pkgName);
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

    private void addDrawableToMemoryCache(@Nullable String pkg, @NonNull Drawable icon) {
        if (getIconFromMemoryCache(pkg) == null) {
            if (pkg == null) pkg = "";
            mIconMemoryCaches.put(pkg, icon);
        }
    }

    private Drawable getIconFromMemoryCache(@Nullable String pkg) {
        if (pkg == null) pkg = "";
        return mIconMemoryCaches.get(pkg);
    }

    private class IconWorkerTask extends AsyncTask<String, Void, Drawable> {
        private Context context;

        private ImageView imageView;

        IconWorkerTask(ImageView imageView) {
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
                if (BuildConfig.DEBUG) {
                    Bitmap whiteIconBitmap = IconCache.getInstance().getWhiteIconBitmap(context, pkg);
                    if (whiteIconBitmap != null) {
                        icon = new BitmapDrawable(context.getResources(), whiteIconBitmap);
                    } else {
                        icon = ContextCompat.getDrawable(context, android.R.mipmap.sym_def_app_icon);
                    }
                } else {
                    try {
                        icon = context.getPackageManager()
                                .getApplicationIcon(pkg);
                    } catch (PackageManager.NameNotFoundException ignore) {
                        icon = ContextCompat.getDrawable(context,
                                android.R.mipmap.sym_def_app_icon);
                    }
                }

            }
            if (icon == null) {
                return null;
            }
            addDrawableToMemoryCache(pkg, icon);
            return icon;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (imageView != null) {
                imageView.setImageDrawable(drawable);
                if (BuildConfig.DEBUG) {
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
