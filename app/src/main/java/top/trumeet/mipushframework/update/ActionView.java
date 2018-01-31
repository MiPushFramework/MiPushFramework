package top.trumeet.mipushframework.update;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import top.trumeet.mipush.R;

/**
 * Created by Trumeet on 2017/8/30.
 */

public class ActionView extends LinearLayout {
    private ImageView icon;
    private TextView title;

    public ActionView(Context context) {
        this(context, null);
    }

    public ActionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext())
                .inflate(R.layout.item_action, this);
        initView();

        TypedArray tArray = getContext()
                .obtainStyledAttributes(attrs ,R.styleable.ActionView);
        String title = tArray.getString(R.styleable.ActionView_text);
        Drawable icon = tArray.getDrawable(R.styleable.ActionView_icon);
        Action action = new Action();
        action.setTitle(title);
        action.setIcon(icon);
        tArray.recycle();
        setAction(action);
    }

    public void setAction(Action action) {
        icon.setImageDrawable(action.getIcon(getContext()));
        title.setText(action.getTitle(getContext()));
    }

    private void initView() {
        icon = findViewById(android.R.id.icon);
        title = findViewById(android.R.id.title);
    }

    public static class Action {
        @DrawableRes
        private int iconRes;

        private Drawable icon;

        @StringRes
        private int titleRes;

        private CharSequence title;

        public void setTitle (CharSequence title) {
            this.title = title;
            titleRes = 0;
        }

        public void setIcon (Drawable icon) {
            this.icon = icon;
            iconRes = 0;
        }

        public void setTitle(@StringRes int title) {
            this.title = null;
            titleRes = title;
        }

        public void setIcon(@DrawableRes int icon) {
            this.icon = null;
            iconRes = icon;
        }

        public CharSequence getTitle(Context context) {
            if (titleRes == 0) {
                return title;
            }
            return context.getString(titleRes);
        }

        public Drawable getIcon(Context context) {
            if (iconRes == 0) {
                return icon;
            }
            return ContextCompat.getDrawable(context,
                    iconRes);
        }
    }
}
