package top.trumeet.mipushframework.event.notification;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

/**
 * Created by Trumeet on 2017/8/26.
 * Push event notification info object
 * @see top.trumeet.mipushframework.event.Event
 * @author Trumeet
 */

@Entity
public class NotificationInfo {
    @Id
    private Long id;

    /**
     * Notification title
     */
    @Property(nameInDb = "title")
    private String title;

    /**
     * Notification text
     */
    @Property(nameInDb = "text")
    private String text;

    /**
     * Enable vibrate
     */
    @Property(nameInDb = "vibrate")
    private boolean vibrate;

    @Generated(hash = 999112323)
    public NotificationInfo(Long id, String title, String text, boolean vibrate) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.vibrate = vibrate;
    }

    @Generated(hash = 273180940)
    public NotificationInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getVibrate() {
        return this.vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }
}
