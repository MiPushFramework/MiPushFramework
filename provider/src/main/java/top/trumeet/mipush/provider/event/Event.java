package top.trumeet.mipush.provider.event;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;

/**
 * Created by Trumeet on 2017/8/26.
 * App event model
 * @author Trumeet
 */

@Entity
public class Event {
    /**
     * Id
     */
    @Id
    private Long id;

    /**
     * Package name
     */
    @Property(nameInDb = "pkg")
    @NotNull
    private String pkg;

    /**
     * Event type
     * @see top.trumeet.common.event.Event.Type
     */
    @Property(nameInDb = "type")
    @top.trumeet.common.event.Event.Type
    @NotNull
    private int type;

    /**
     * Event date time (UTC)
     */
    @Property(nameInDb = "date")
    @NotNull
    private long date;

    /**
     * Operation result
     */
    @Property(nameInDb = "result")
    @top.trumeet.common.event.Event.ResultType
    private int result;

    @Property(nameInDb = "dev_info")
    private String info;

    @Property(nameInDb = "noti_title")
    private String notificationTitle;

    @Property(nameInDb = "noti_summary")
    private String notificationSummary;

    @Generated(hash = 344677835)
    public Event() {
    }

    @Generated(hash = 722821452)
    public Event(Long id, @NotNull String pkg, int type, long date, int result, String info, String notificationTitle,
            String notificationSummary) {
        this.id = id;
        this.pkg = pkg;
        this.type = type;
        this.date = date;
        this.result = result;
        this.info = info;
        this.notificationTitle = notificationTitle;
        this.notificationSummary = notificationSummary;
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


    public top.trumeet.common.event.Event convertTo () {
        return convertTo(this);
    }

    /**
     * 将 Provider 中的 Model 转换成 Xmsf 中用的 {@link top.trumeet.common.event.Event}
     * （非常辣鸡
     * @param original Original model
     * @return Target model
     */
    @NonNull
    public static top.trumeet.common.event.Event convertTo
    (@NonNull Event original) {
        return new top.trumeet.common.event.Event(original.id,
                original.pkg,
                original.type, original.date,
                original.result,
                original.notificationTitle,
                original.notificationSummary,
                original.info);
    }

    @NonNull
    public static Event from (@NonNull top.trumeet.common.event.Event original) {
        return new Event(original.getId(), original.getPkg(), original.getType(),
                original.getDate(), original.getResult(), original.getNotificationTitle(), original.getNotificationSummary(),
                original.getInfo());
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
