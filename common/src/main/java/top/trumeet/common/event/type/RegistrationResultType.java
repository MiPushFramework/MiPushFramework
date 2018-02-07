package top.trumeet.common.event.type;

import android.content.Context;
import android.support.annotation.Nullable;

import top.trumeet.common.R;
import top.trumeet.common.event.Event;

/**
 * Created by Trumeet on 2018/2/7.
 */

public class RegistrationResultType extends EventType {
    public RegistrationResultType(String mInfo, String pkg) {
        super(Event.Type.RegistrationResult, mInfo, pkg);
    }

    @Nullable
    @Override
    public CharSequence getSummary(Context context) {
        return context.getString(R.string.event_register_result);
    }
}
