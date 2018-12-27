package com.xiaomi.xmsf.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.formatter.message.json.DefaultJsonFormatter;
import com.elvishew.xlog.formatter.message.xml.DefaultXmlFormatter;
import com.elvishew.xlog.formatter.stacktrace.DefaultStackTraceFormatter;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.xiaomi.xmsf.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import top.trumeet.common.Constants;

import static android.content.Intent.EXTRA_STREAM;

/**
 * Created by Trumeet on 2017/8/28.
 *
 * @author Trumeet
 */

public class LogUtils {
    public static void init (@NonNull Context context) {
        LogConfiguration configuration = new LogConfiguration.Builder()
                .tag("Xmsf")
                .logLevel(LogLevel.ALL)
                .jsonFormatter(new DefaultJsonFormatter())
                .xmlFormatter(new DefaultXmlFormatter())
                .stackTraceFormatter(new DefaultStackTraceFormatter())
                .build();
        Printer androidPrinter = new AndroidPrinter();
        Printer filePrinter = new FilePrinter.Builder(LogUtils.getLogFolder(context))
                .fileNameGenerator(new DateFileNameGenerator())
                .cleanStrategy(new FileLastModifiedCleanStrategy(7 * 24 * 60 * 60 * 1000 /* 7 days */))
                .build();
        XLog.init(configuration, androidPrinter, filePrinter);
    }

    public static String getLogFolder (@NonNull Context context) {
        return context.getCacheDir().getAbsolutePath() + "/logs";
    }

    public static void clearLog(Context context) {
        File file = new File(getLogFolder(context));
        if (!file.exists() || file.length() <= 0) {
            Toast.makeText(context, R.string.log_none
                    , Toast.LENGTH_SHORT).show();
            return;
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            }
        }
        Toast.makeText(context, context.getString(R.string.log_clear_done), Toast.LENGTH_SHORT).show();
        // Re create files
        // init(context);
    }

    @Nullable
    public static Intent getShareIntent(Context context) {
        File zipFile = new File(context.getExternalCacheDir().getAbsolutePath() + "/logs/logs-" +
                new SimpleDateFormat("yyyy-mm-dd-H-m-s", Locale.US).format(new Date()) + ".zip");
        try {
            com.elvishew.xlog.LogUtils.compress(getLogFolder(context),
                    zipFile.getAbsolutePath());
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    Constants.AUTHORITY_FILE_PROVIDER,
                    zipFile);
            if (fileUri == null || !zipFile.exists()) {
                throw new NullPointerException();
            }
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String type = context.getContentResolver().getType(fileUri);
            if (type == null || type.trim().equals("")) {
                type = "application/zip";
            }
            System.out.println("Zip " + type + " , " + fileUri.toString());
            intent.setType(type);
            intent.putExtra(EXTRA_STREAM, fileUri);
            return intent;
        } catch (IOException | NullPointerException e) {
            // TODO: Maybe some error are occurred?
            return null;
        }
    }
}
