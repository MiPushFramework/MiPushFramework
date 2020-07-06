package top.trumeet.common.event.type;

import android.content.Context;
import androidx.annotation.Nullable;
import top.trumeet.common.event.Event;

/**
 * 对应 {@link top.trumeet.common.event.Event.Type#SendMessage}
 *
 * Created by Trumeet on 2018/2/7.
 */

public class UnknownType extends EventType {

    public UnknownType(int mType, String mInfo, String pkg) {
        super(mType, mInfo, pkg);
    }

    @Nullable
    @Override
    public CharSequence getSummary(Context context) {
        switch (getType()) {
            case Event.Type.Registration :
                return "Registration";
            case Event.Type.Notification:
                return "Notification";
            case Event.Type.Command:
                return "Command";
            case Event.Type.AckMessage:
                return "AckMessage";
            case Event.Type.MultiConnectionBroadcast:
                return "MultiConnectionBroadcast";
                
            case Event.Type.MultiConnectionResult:
                return "MultiConnectionResult";
                
            case Event.Type.ReportFeedback:
                return "ReportFeedback";
                
            case Event.Type.UnRegistration:
                return "UnRegistration";
                
            case Event.Type.UnSubscription:
                return "UnSubscription";
            case Event.Type.SetConfig:
                return "SetConfig";
            case Event.Type.Subscription:
                return "Subscription";
            default:
                return null;
        }
    }
}
