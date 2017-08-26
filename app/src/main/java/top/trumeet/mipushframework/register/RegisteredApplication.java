package top.trumeet.mipushframework.register;

import android.support.annotation.IntDef;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Trumeet on 2017/8/26.
 * A registered application. Can 3 types: {@link Type#ALLOW}, {@link Type#DENY}
 * or {@link Type#ASK}.
 * Default is Ask: show a dialog to request push permission.
 * It will auto create using ask type when application register push.
 *
 * This entity will also save application's push permissions.  TODO
 *
 * @author Trumeet
 */

@Entity
public class RegisteredApplication {
    @IntDef({Type.ASK, Type.ALLOW, Type.DENY})
    @Retention(SOURCE)
    @Target({ElementType.PARAMETER, ElementType.TYPE,
            ElementType.FIELD, ElementType.METHOD})
    public @interface Type {
        int ASK = 0;
        int ALLOW = 2;
        int DENY = 3;
    }

    @Id
    private Long id;

    @Unique
    @Property(nameInDb = "pkg")
    private String packageName;

    @Type
    @Property(nameInDb = "type")
    private int type = Type.ASK;

    @Generated(hash = 1647835259)
    public RegisteredApplication(Long id, String packageName, int type) {
        this.id = id;
        this.packageName = packageName;
        this.type = type;
    }

    @Generated(hash = 1216470554)
    public RegisteredApplication() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }


}
