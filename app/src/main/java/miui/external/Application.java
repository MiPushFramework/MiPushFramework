package miui.external;

import android.content.Context;
import android.content.res.Configuration;

public class Application extends android.app.Application {
    private boolean f1b;
    private ApplicationDelegate f2c;

    final void m5a(int i) {
        super.onTrimMemory(i);
    }

    final void m6a(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        this.f2c = onCreateApplicationDelegate();
        if (this.f2c != null) {
            this.f2c.m10a(this);
        }
        this.f1b = true;
    }

    final void m7d() {
        super.onCreate();
    }

    final void m8e() {
        super.onTerminate();
    }

    final void m9f() {
        super.onLowMemory();
    }

    public final void onConfigurationChanged(Configuration configuration) {
        if (this.f2c != null) {
            this.f2c.onConfigurationChanged(configuration);
        } else {
            m6a(configuration);
        }
    }

    public final void onCreate() {
        if (!this.f1b) {
            return;
        }
        if (this.f2c != null) {
            this.f2c.onCreate();
        } else {
            m7d();
        }
    }

    public ApplicationDelegate onCreateApplicationDelegate() {
        return null;
    }

    public final void onLowMemory() {
        if (this.f2c != null) {
            this.f2c.onLowMemory();
        } else {
            m9f();
        }
    }

    public final void onTerminate() {
        if (this.f2c != null) {
            this.f2c.onTerminate();
        } else {
            m8e();
        }
    }

    public final void onTrimMemory(int i) {
        if (this.f2c != null) {
            this.f2c.onTrimMemory(i);
        } else {
            m5a(i);
        }
    }
}
