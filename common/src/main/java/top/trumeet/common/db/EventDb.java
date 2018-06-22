package top.trumeet.common.db;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.List;

import top.trumeet.common.Constants;
import top.trumeet.common.event.Event;
import top.trumeet.common.event.type.EventType;
import top.trumeet.common.utils.DatabaseUtils;
import top.trumeet.common.utils.Utils;

/**
 *
 * @author Trumeet
 * @date 2017/12/23
 */

public class EventDb {
    public static final String AUTHORITY = "top.trumeet.mipush.providers.EventProvider";
    public static final String BASE_PATH = "EVENT";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static DatabaseUtils getInstance(Context context) {
        return new DatabaseUtils(CONTENT_URI, context.getContentResolver());
    }

    @RequiresPermission(value = Constants.permissions.WRITE_SETTINGS)
    public static Uri insertEvent(Event event,
                                  Context context) {
        return getInstance(context)
                .insert(event.toValues());
    }

    @RequiresPermission(value = Constants.permissions.WRITE_SETTINGS)
    public static Uri insertEvent(@Event.ResultType int result,
                                  EventType type,
                                  Context context) {
        return insertEvent(type.fillEvent(new Event(null, type.getPkg(), type.getType(), Utils.getUTC().getTime()
                , result, null, null, type.getInfo())), context);
    }

    @RequiresPermission(value = Constants.permissions.READ_SETTINGS)
    public static List<Event> query(@Nullable Integer skip,
                                    @Nullable Integer limit,
                                    @Nullable String pkg,
                                    Context context,
                                    @Nullable CancellationSignal signal) {
        return getInstance(context)
                .queryAndConvert(signal, pkg == null ? null : Event.KEY_PKG + "=?",
                        pkg != null ? new String[]{pkg} : null,
                        DatabaseUtils.order(Event.KEY_DATE, "desc") +
                                DatabaseUtils.limitAndOffset(limit, skip),
                        new DatabaseUtils.Converter<Event>() {
                            @Override
                            @NonNull
                            public Event convert(@NonNull Cursor cursor) {
                                return Event.create(cursor);
                            }
                        });
    }

    @RequiresPermission(value = Constants.permissions.READ_SETTINGS)
    public static List<Event> queryRegistered(Context context, CancellationSignal signal) {
        String selection = Event.KEY_TYPE + "=" + Event.Type.REGISTER;
        return getInstance(context).queryAndConvert(signal, selection, null,
                DatabaseUtils.order(Event.KEY_DATE, "desc"),
                new DatabaseUtils.Converter<Event>() {
                    @Override
                    @NonNull
                    public Event convert(@NonNull Cursor cursor) {
                        return Event.create(cursor);
                    }
                });
    }

    @RequiresPermission(value = Constants.permissions.READ_SETTINGS)
    public static List<Event> query(String pkg, int page, Context context,
                                    CancellationSignal cancellationSignal) {
        int skip;
        int limit;
        skip = Constants.PAGE_SIZE * (page - 1);
        limit = Constants.PAGE_SIZE;
        return query(skip, limit, pkg, context, cancellationSignal);
    }
}
