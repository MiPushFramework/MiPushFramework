package top.trumeet.common.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import top.trumeet.common.R;

/**
 * Created by Trumeet on 2017/12/30.
 */

public class LinkAlertDialog extends AlertDialog {
    protected LinkAlertDialog(Context context) {
        super(context);
    }

    protected LinkAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    protected LinkAlertDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder extends AlertDialog.Builder {

        public Builder(Context context) {
            super(context);
        }

        public Builder(Context context, int themeResId) {
            super(context, themeResId);
        }

        /**
         * Set the message to display.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        @Override
        public Builder setMessage(CharSequence message) {
            TextView textView = new TextView(getContext());
            int padding = (int) getContext().getResources()
                    .getDimension(R.dimen.abc_dialog_padding_material);
            textView.setPadding(padding, padding, padding, padding);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(message);
            setView(textView);
            return this;
        }
    }
}
