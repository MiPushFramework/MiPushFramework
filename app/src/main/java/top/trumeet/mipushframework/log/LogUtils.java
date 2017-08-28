package top.trumeet.mipushframework.log;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.xiaomi.xmsf.R;

import java.io.File;

import top.trumeet.mipushframework.Constants;

/**
 * Created by Trumeet on 2017/8/28.
 * @author Trumeet
 */

public class LogUtils {
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
        Intent result = new Intent(Intent.ACTION_VIEW);
        try {
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    Constants.AUTHORITY_FILE_PROVIDER,
                    file);
            result.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
            result.setDataAndType(fileUri,
                    context.getContentResolver().getType(fileUri));
            context.startActivity(result);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
