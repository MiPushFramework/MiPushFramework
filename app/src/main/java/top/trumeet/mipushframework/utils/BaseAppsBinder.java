package top.trumeet.mipushframework.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import me.drakeet.multitype.ItemViewBinder;
import top.trumeet.common.cache.ApplicationNameCache;
import top.trumeet.common.cache.IconCache;
import top.trumeet.mipush.R;

/**
 * Created by Trumeet on 2017/8/26.
 * Base application list recycler binder with async load icon
 *
 * @author Trumeet
 */

public abstract class BaseAppsBinder<T> extends ItemViewBinder<T, BaseAppsBinder.ViewHolder> {

    private static final boolean DEBUG_ICON = false;

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

        IconCache cache = IconCache.getInstance();

        if (DEBUG_ICON) {
            Bitmap rawIconBitmap = cache.getRawIconBitmap(context, pkgName);
            Bitmap icon = new IconCache.WhiteIconProcess().convert(context, rawIconBitmap);
            holder.icon.setImageBitmap(icon);
            holder.icon.setBackgroundColor(Color.BLACK);
            return;
        }

        Bitmap icon = cache.getRawIconBitmapWithoutLoader(context, pkgName);
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
                icon = IconCache.getInstance().getRawIconBitmap(context, pkg);
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
