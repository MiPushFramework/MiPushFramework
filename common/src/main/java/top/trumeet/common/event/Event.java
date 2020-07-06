package top.trumeet.common.event;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;
import top.trumeet.common.utils.DatabaseUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Trumeet on 2017/12/24.
 * Common 中的不带 greenDao 的模型。Too bad
 */

public class Event {
    // Name in database
    public static final String KEY_ID = DatabaseUtils.KEY_ID;
    public static final String KEY_PKG = "pkg";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DATE = "date";
    public static final String KEY_RESULT = "result";
    public static final String KEY_INFO = "dev_info";

    // Meta info
    public static final String KEY_NOTIFICATION_TITLE = "noti_title";
    public static final String KEY_NOTIFICATION_SUMMARY = "noti_summary";

    @androidx.annotation.IntDef({Type.Notification,
            Type.Command, Type.AckMessage, Type.Registration,
    Type.MultiConnectionBroadcast, Type.MultiConnectionResult,
    Type.UnRegistration, Type.ReportFeedback, Type.SetConfig, Type.Subscription,
    Type.UnSubscription, Type.RegistrationResult})
    @Retention(SOURCE)
    @Target({ElementType.PARAMETER, ElementType.TYPE,
            ElementType.FIELD, ElementType.METHOD})
    public @interface Type {
        @Deprecated
        int RECEIVE_PUSH = 0;
        @Deprecated
        int REGISTER = 2;
        @Deprecated
        int RECEIVE_COMMAND = 1;

        // Same to com.xiaomi.xmpush.thrift.ActionType
        int AckMessage = 6;
        int MultiConnectionBroadcast = 11;
        int MultiConnectionResult = 12;
        int SendMessage = RECEIVE_PUSH;
        int SetConfig = 7;
        int Subscription = 3;
        int UnSubscription = 4;
        int Notification = RECEIVE_PUSH;
        int Command = 10;
        int ReportFeedback = 8;

        // 和上面的重复（
        int Registration = REGISTER;
        int UnRegistration = 20;

        // Custom
        int RegistrationResult = 21;
    }

    @androidx.annotation.IntDef({ResultType.OK, ResultType.DENY_DISABLED, ResultType.DENY_USER})
    @Retention(SOURCE)
    @Target({ElementType.PARAMETER, ElementType.TYPE,
            ElementType.FIELD, ElementType.METHOD})
    public @interface ResultType {
        /**
         * Allowed
         */
        int OK = 0;

        /**
         * Deny because push is disabled by user
         */
        int DENY_DISABLED = 1;

        /**
         * User denied
         */
        int DENY_USER = 2;
    }

    /**
     * Id
     */
    private Long id;

    /**
     * Package name
     */
    private String pkg;

    /**
     * Event type
     * @see Type
     */
    @Type
    @NonNull
    private int type;

    /**
     * Event date time (UTC)
     */
    private long date;

    /**
     * Operation result
     */
    @ResultType
    private int result;

    private String notificationTitle;

    private String notificationSummary;

    private String info;

    public Event(Long id, @NonNull String pkg, int type, long date, int result, String notificationTitle, String notificationSummary, String info) {
        this.id = id;
        this.pkg = pkg;
        this.type = type;
        this.date = date;
        this.result = result;
        this.notificationTitle = notificationTitle;
        this.notificationSummary = notificationSummary;
        this.info = info;
    }

    public Event() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPkg() {
        return this.pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getDate() {
        return this.date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getResult() {
        return this.result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    /**
     * Create from cursor
     * @param cursor cursor
     * @return Event object
     */
    @NonNull
    public static Event create (@androidx.annotation.NonNull Cursor cursor) {
        return new Event(cursor.getLong(cursor.getColumnIndex(KEY_ID)) /* id */,
                cursor.getString(cursor.getColumnIndex(KEY_PKG)) /* pkg */,
                cursor.getInt(cursor.getColumnIndex(KEY_TYPE)) /* type */,
                cursor.getLong(cursor.getColumnIndex(KEY_DATE)) /* date */,
                cursor.getInt(cursor.getColumnIndex(KEY_RESULT)) /* result */,
                cursor.getString(cursor.getColumnIndex(KEY_NOTIFICATION_TITLE)) /* notification title */,
                cursor.getString(cursor.getColumnIndex(KEY_NOTIFICATION_SUMMARY)) /* notification summary */,
                cursor.getString(cursor.getColumnIndex(KEY_INFO)) /* dev info */);
    }

    /**
     * Convert to ContentValues
     * @return values object
     */
    @NonNull
    public ContentValues toValues () {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, getId());
        values.put(KEY_PKG, getPkg());
        values.put(KEY_TYPE, getType());
        values.put(KEY_DATE, getDate());
        values.put(KEY_RESULT, getResult());
        values.put(KEY_NOTIFICATION_TITLE, getNotificationTitle());
        values.put(KEY_NOTIFICATION_SUMMARY, getNotificationSummary());
        values.put(KEY_INFO, getInfo());
        return values;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationSummary() {
        return notificationSummary;
    }

    public void setNotificationSummary(String notificationSummary) {
        this.notificationSummary = notificationSummary;
    }
}
