//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.app;

import android.os.IBinder;

public class AppOpsManager {
    public static final int MODE_ALLOWED = 0;
    public static final int MODE_DEFAULT = 3;
    public static final int MODE_ERRORED = 2;
    public static final int MODE_IGNORED = 1;
    public static final String OPSTR_ADD_VOICEMAIL = "android:add_voicemail";
    public static final String OPSTR_ANSWER_PHONE_CALLS = "android:answer_phone_calls";
    public static final String OPSTR_BODY_SENSORS = "android:body_sensors";
    public static final String OPSTR_CALL_PHONE = "android:call_phone";
    public static final String OPSTR_CAMERA = "android:camera";
    public static final String OPSTR_COARSE_LOCATION = "android:coarse_location";
    public static final String OPSTR_FINE_LOCATION = "android:fine_location";
    public static final String OPSTR_GET_USAGE_STATS = "android:get_usage_stats";
    public static final String OPSTR_MOCK_LOCATION = "android:mock_location";
    public static final String OPSTR_MONITOR_HIGH_POWER_LOCATION = "android:monitor_location_high_power";
    public static final String OPSTR_MONITOR_LOCATION = "android:monitor_location";
    public static final String OPSTR_PICTURE_IN_PICTURE = "android:picture_in_picture";
    public static final String OPSTR_PROCESS_OUTGOING_CALLS = "android:process_outgoing_calls";
    public static final String OPSTR_READ_CALENDAR = "android:read_calendar";
    public static final String OPSTR_READ_CALL_LOG = "android:read_call_log";
    public static final String OPSTR_READ_CELL_BROADCASTS = "android:read_cell_broadcasts";
    public static final String OPSTR_READ_CONTACTS = "android:read_contacts";
    public static final String OPSTR_READ_EXTERNAL_STORAGE = "android:read_external_storage";
    public static final String OPSTR_READ_PHONE_NUMBERS = "android:read_phone_numbers";
    public static final String OPSTR_READ_PHONE_STATE = "android:read_phone_state";
    public static final String OPSTR_READ_SMS = "android:read_sms";
    public static final String OPSTR_RECEIVE_MMS = "android:receive_mms";
    public static final String OPSTR_RECEIVE_SMS = "android:receive_sms";
    public static final String OPSTR_RECEIVE_WAP_PUSH = "android:receive_wap_push";
    public static final String OPSTR_RECORD_AUDIO = "android:record_audio";
    public static final String OPSTR_SEND_SMS = "android:send_sms";
    public static final String OPSTR_SYSTEM_ALERT_WINDOW = "android:system_alert_window";
    public static final String OPSTR_USE_FINGERPRINT = "android:use_fingerprint";
    public static final String OPSTR_USE_SIP = "android:use_sip";
    public static final String OPSTR_WRITE_CALENDAR = "android:write_calendar";
    public static final String OPSTR_WRITE_CALL_LOG = "android:write_call_log";
    public static final String OPSTR_WRITE_CONTACTS = "android:write_contacts";
    public static final String OPSTR_WRITE_EXTERNAL_STORAGE = "android:write_external_storage";
    public static final String OPSTR_WRITE_SETTINGS = "android:write_settings";

    public AppOpsManager() {
        throw new RuntimeException("Stub!");
    }

    public static String permissionToOp(String permission) {
        throw new RuntimeException("Stub!");
    }

    public void startWatchingMode(String op, String packageName, android.app.AppOpsManager.OnOpChangedListener callback) {
        throw new RuntimeException("Stub!");
    }

    public void stopWatchingMode(android.app.AppOpsManager.OnOpChangedListener callback) {
        throw new RuntimeException("Stub!");
    }

    public int checkOp(String op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    public int checkOpNoThrow(String op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    public int noteOp(String op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    public int noteProxyOp(String op, String proxiedPackageName) {
        throw new RuntimeException("Stub!");
    }

    public int noteProxyOpNoThrow(String op, String proxiedPackageName) {
        throw new RuntimeException("Stub!");
    }

    public int startOp(String op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    public int startOpNoThrow(String op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    public void finishOp(String op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    public interface OnOpChangedListener {
        void onOpChanged(String var1, String var2);
    }


    
    public void setUidMode(int code, int uid, int mode) {
        throw new RuntimeException("Stub!");
    }

    
    public void setUidMode(String appOp, int uid, int mode) {
        throw new RuntimeException("Stub!");
    }

    
    public void setUserRestriction(int code, boolean restricted, IBinder token) {
        throw new RuntimeException("Stub!");
    }

    
    public void setUserRestriction(int code, boolean restricted, IBinder token,
                                   String[] exceptionPackages) {
        throw new RuntimeException("Stub!");
    }

    
    public void setUserRestrictionForUser(int code, boolean restricted, IBinder token,
                                          String[] exceptionPackages, int userId) {
        throw new RuntimeException("Stub!");
    }

    
    public void setMode(int code, int uid, String packageName, int mode) {
        throw new RuntimeException("Stub!");
    }

    
    public void setRestriction(int code, int usage, int mode,
                               String[] exceptionPackages) {
        throw new RuntimeException("Stub!");
    }

    
    public void resetAllModes() {
        throw new RuntimeException("Stub!");
    }
    
    public void startWatchingMode(int op, String packageName, final OnOpChangedListener callback) {
        throw new RuntimeException("Stub!");
    }

    public int noteOpNoThrow(String op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    public int checkOp(int op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }


    /**
     * Like {@link #checkOp} but instead of throwing a {@link SecurityException} it
     * returns {@link #MODE_ERRORED}.
     * @hide
     */
    public int checkOpNoThrow(int op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Do a quick check to validate if a package name belongs to a UID.
     *
     * @throws SecurityException if the package name doesn't belong to the given
     *             UID, or if ownership cannot be verified.
     */
    public void checkPackage(int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Like {@link #checkOp} but at a stream-level for audio operations.
     * @hide
     */
    public int checkAudioOp(int op, int stream, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Like {@link #checkAudioOp} but instead of throwing a {@link SecurityException} it
     * returns {@link #MODE_ERRORED}.
     * @hide
     */
    public int checkAudioOpNoThrow(int op, int stream, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Make note of an application performing an operation.  Note that you must pass
     * in both the uid and name of the application to be checked; this function will verify
     * that these two match, and if not, return {@link #MODE_IGNORED}.  If this call
     * succeeds, the last execution time of the operation for this app will be updated to
     * the current time.
     * @param op The operation to note.  One of the OP_* constants.
     * @param uid The user id of the application attempting to perform the operation.
     * @param packageName The name of the application attempting to perform the operation.
     * @return Returns {@link #MODE_ALLOWED} if the operation is allowed, or
     * {@link #MODE_IGNORED} if it is not allowed and should be silently ignored (without
     * causing the app to crash).
     * @throws SecurityException If the app has been configured to crash on this op.
     * @hide
     */
    public int noteOp(int op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Make note of an application performing an operation on behalf of another
     * application when handling an IPC. Note that you must pass the package name
     * of the application that is being proxied while its UID will be inferred from
     * the IPC state; this function will verify that the calling uid and proxied
     * package name match, and if not, return {@link #MODE_IGNORED}. If this call
     * succeeds, the last execution time of the operation for the proxied app and
     * your app will be updated to the current time.
     * @param op The operation to note. One of the OPSTR_* constants.
     * @param proxiedPackageName The name of the application calling into the proxy application.
     * @return Returns {@link #MODE_ALLOWED} if the operation is allowed, or
     * {@link #MODE_IGNORED} if it is not allowed and should be silently ignored (without
     * causing the app to crash).
     * @throws SecurityException If the proxy or proxied app has been configured to
     * crash on this op.
     *
     * @hide
     */
    public int noteProxyOp(int op, String proxiedPackageName) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Like {@link #noteProxyOp(int, String)} but instead
     * of throwing a {@link SecurityException} it returns {@link #MODE_ERRORED}.
     * @hide
     */
    public int noteProxyOpNoThrow(int op, String proxiedPackageName) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Like {@link #noteOp} but instead of throwing a {@link SecurityException} it
     * returns {@link #MODE_ERRORED}.
     * @hide
     */
    public int noteOpNoThrow(int op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    /** @hide */
    public int noteOp(int op) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Report that an application has started executing a long-running operation.  Note that you
     * must pass in both the uid and name of the application to be checked; this function will
     * verify that these two match, and if not, return {@link #MODE_IGNORED}.  If this call
     * succeeds, the last execution time of the operation for this app will be updated to
     * the current time and the operation will be marked as "running".  In this case you must
     * later call {@link #finishOp(int, int, String)} to report when the application is no
     * longer performing the operation.
     * @param op The operation to start.  One of the OP_* constants.
     * @param uid The user id of the application attempting to perform the operation.
     * @param packageName The name of the application attempting to perform the operation.
     * @return Returns {@link #MODE_ALLOWED} if the operation is allowed, or
     * {@link #MODE_IGNORED} if it is not allowed and should be silently ignored (without
     * causing the app to crash).
     * @throws SecurityException If the app has been configured to crash on this op.
     * @hide
     */
    public int startOp(int op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Like {@link #startOp} but instead of throwing a {@link SecurityException} it
     * returns {@link #MODE_ERRORED}.
     * @hide
     */
    
    public int startOpNoThrow(int op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    /** @hide */
    
    public int startOp(int op) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Report that an application is no longer performing an operation that had previously
     * been started with {@link #startOp(int, int, String)}.  There is no validation of input
     * or result; the parameters supplied here must be the exact same ones previously passed
     * in when starting the operation.
     * @hide
     */
    
    public void finishOp(int op, int uid, String packageName) {
        throw new RuntimeException("Stub!");
    }

    /** @hide */
    
    public void finishOp(int op) {
        throw new RuntimeException("Stub!");
    }

}
