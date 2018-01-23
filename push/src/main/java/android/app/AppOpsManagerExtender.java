package android.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.IBinder;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by Trumeet on 2018/1/22.
 */

public class AppOpsManagerExtender extends AppOpsManager {
    private static final String TAG = "AppOpsManagerExtender";

    @Override
    public void setUidMode(int code, int uid, int mode) {
        delegate().setUidMode(code, uid, mode);
    }

    @Override
    public void setUidMode(String appOp, int uid, int mode) {
        delegate().setUidMode(appOp, uid, mode);
    }

    @Override
    public void setUserRestriction(int code, boolean restricted, IBinder token) {
        delegate().setUserRestriction(code, restricted, token);
    }

    @Override
    public void setUserRestriction(int code, boolean restricted, IBinder token,
                                   String[] exceptionPackages) {
        delegate().setUserRestriction(code, restricted, token, exceptionPackages);
    }

    @Override
    public void setUserRestrictionForUser(int code, boolean restricted, IBinder token,
                                          String[] exceptionPackages, int userId) {
        delegate().setUserRestrictionForUser(code, restricted, token, exceptionPackages, userId);
    }

    @Override
    public void setMode(int code, int uid, String packageName, int mode) {
        delegate().setMode(code, uid, packageName, mode);
    }

    @Override
    public void setRestriction(int code, @AudioAttributes.AttributeUsage int usage, int mode,
                               String[] exceptionPackages) {
        delegate().setRestriction(code, usage, mode, exceptionPackages);
    }

    @Override
    public void resetAllModes() {
        delegate().resetAllModes();
    }

    @Override
    public void startWatchingMode(String op, String packageName,
                                  final OnOpChangedListener callback) {
        delegate().startWatchingMode(op, packageName, callback);
    }

    @Override
    public void startWatchingMode(int op, String packageName, final OnOpChangedListener callback) {
        delegate().startWatchingMode(op, packageName, callback);
    }

    @Override
    public void stopWatchingMode(OnOpChangedListener callback) {
        delegate().stopWatchingMode(callback);
    }

    @Override
    public int checkOp(String op, int uid, String packageName) {
        return delegate().checkOp(op, uid, packageName);
    }

    @Override
    public int checkOpNoThrow(String op, int uid, String packageName) {
        return delegate().checkOpNoThrow(op, uid, packageName);
    }

    @Override
    public int noteOp(String op, int uid, String packageName) {
        return delegate().noteOp(op, uid, packageName);
    }

    @Override
    public int noteOpNoThrow(String op, int uid, String packageName) {
        return delegate().noteOpNoThrow(op, uid, packageName);
    }

    @Override
    @TargetApi(M)
    public int noteProxyOp(String op, String proxiedPackageName) {
        return delegate().noteProxyOp(op, proxiedPackageName);
    }

    @Override
    @TargetApi(M)
    public int noteProxyOpNoThrow(String op, String proxiedPackageName) {
        return delegate().noteProxyOpNoThrow(op, proxiedPackageName);
    }

    @Override
    public int startOp(String op, int uid, String packageName) {
        return delegate().startOp(op, uid, packageName);
    }

    @Override
    public int startOpNoThrow(String op, int uid, String packageName) {
        return delegate().startOpNoThrow(op, uid, packageName);
    }

    @Override
    public void finishOp(String op, int uid, String packageName) {
        delegate().finishOp(op, uid, packageName);
    }

    @Override
    public int checkOp(int op, int uid, String packageName) {
        return delegate().checkOp(op, uid, packageName);
    }


    /**
     * Like {@link #checkOp} but instead of throwing a {@link SecurityException} it
     * returns {@link #MODE_ERRORED}.
     * @hide
     */
    public int checkOpNoThrow(int op, int uid, String packageName) {
        return delegate().checkOpNoThrow(op, uid, packageName);
    }

    /**
     * Do a quick check to validate if a package name belongs to a UID.
     *
     * @throws SecurityException if the package name doesn't belong to the given
     *             UID, or if ownership cannot be verified.
     */
    public void checkPackage(int uid, String packageName) {
        delegate().checkPackage(uid, packageName);
    }

    /**
     * Like {@link #checkOp} but at a stream-level for audio operations.
     * @hide
     */
    public int checkAudioOp(int op, int stream, int uid, String packageName) {
        return delegate().checkAudioOp(op, stream, uid, packageName);
    }

    /**
     * Like {@link #checkAudioOp} but instead of throwing a {@link SecurityException} it
     * returns {@link #MODE_ERRORED}.
     * @hide
     */
    public int checkAudioOpNoThrow(int op, int stream, int uid, String packageName) {
        return delegate().checkAudioOpNoThrow(op, stream, uid, packageName);
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
        return delegate().noteOp(op, uid, packageName);
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
        return delegate().noteProxyOp(op, proxiedPackageName);
    }

    /**
     * Like {@link #noteProxyOp(int, String)} but instead
     * of throwing a {@link SecurityException} it returns {@link #MODE_ERRORED}.
     * @hide
     */
    public int noteProxyOpNoThrow(int op, String proxiedPackageName) {
        return noteProxyOpNoThrow(op, proxiedPackageName);
    }

    /**
     * Like {@link #noteOp} but instead of throwing a {@link SecurityException} it
     * returns {@link #MODE_ERRORED}.
     * @hide
     */
    public int noteOpNoThrow(int op, int uid, String packageName) {
        return noteOpNoThrow(op, uid, packageName);
    }

    /** @hide */
    public int noteOp(int op) {
        return delegate().noteOp(op);
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
        return delegate().startOp(op, uid, packageName);
    }

    /**
     * Like {@link #startOp} but instead of throwing a {@link SecurityException} it
     * returns {@link #MODE_ERRORED}.
     * @hide
     */
    @Override
    public int startOpNoThrow(int op, int uid, String packageName) {
        return delegate().startOpNoThrow(op, uid, packageName);
    }

    /** @hide */
    @Override
    public int startOp(int op) {
        return delegate().startOp(op);
    }

    /**
     * Report that an application is no longer performing an operation that had previously
     * been started with {@link #startOp(int, int, String)}.  There is no validation of input
     * or result; the parameters supplied here must be the exact same ones previously passed
     * in when starting the operation.
     * @hide
     */
    @Override
    public void finishOp(int op, int uid, String packageName) {
        delegate().finishOp(op, uid, packageName);
    }

    /** @hide */
    @Override
    public void finishOp(int op) {
        delegate().finishOp(op);
    }


    public AppOpsManagerExtender(final Context context) {
        super(null, null);
        mDelegate = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
    }

    private AppOpsManager delegate() { return mDelegate; }

    private final AppOpsManager mDelegate;
}
