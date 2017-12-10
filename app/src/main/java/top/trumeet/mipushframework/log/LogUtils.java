package top.trumeet.mipushframework.log;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.xiaomi.xmsf.R;

import java.io.File;

import me.pqpo.librarylog4a.Log4a;
import me.pqpo.librarylog4a.Logger;
import me.pqpo.librarylog4a.appender.AndroidAppender;
import me.pqpo.librarylog4a.appender.FileAppender;
import top.trumeet.mipushframework.Constants;

/**
 * Created by Trumeet on 2017/8/28.
 * @author Trumeet
 */

public class LogUtils {

    public static void configureLog (Context context) {
        AndroidAppender.Builder androidBuild = new AndroidAppender.Builder();

        String log_path = getLogFile(context);
        FileAppender.Builder fileBuild = new FileAppender.Builder(context)
                .setLogFilePath(log_path);

        Logger logger = new Logger.Builder()
                .enableAndroidAppender(androidBuild)
                .enableFileAppender(fileBuild)
                .create();

        Log4a.setLogger(logger);
    }

    public static String getLogFile (Context context) {
        return context.getCacheDir().getAbsolutePath().concat(Constants.LOG_FILE);
    }

    public static void shareFile (Context context) {
        File file = new File(getLogFile(context));
        if (!file.exists() || file.length() <= 0) {
            Toast.makeText(context, R.string.log_none
                    , Toast.LENGTH_SHORT).show();
            return;
        }
        Intent result = new Intent(Intent.ACTION_SEND);
        result.setType("text/*");
        try {
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    Constants.AUTHORITY_FILE_PROVIDER,
                    file);
            result.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
            result.putExtra(Intent.EXTRA_STREAM, fileUri);
            context.startActivity(result);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, context.getString(R.string.log_share_error,
                    e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }
}
