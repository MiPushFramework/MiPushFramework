package top.trumeet.mipushframework.widgets;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceViewHolder;

public class InfoPreference extends Preference {
    public InfoPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public InfoPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InfoPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InfoPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView text = holder.itemView.findViewById(android.R.id.summary);
        text.setMovementMethod(new LinkMovementMethod());
        text.setClickable(true);
        text.setLongClickable(false);
        holder.itemView.setClickable(false);
    }
}
