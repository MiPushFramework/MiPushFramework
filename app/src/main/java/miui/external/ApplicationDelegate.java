package miui.external;

import android.content.ComponentCallbacks;
import android.content.ComponentCallbacks2;
import android.content.ContextWrapper;
import android.content.res.Configuration;

public abstract class ApplicationDelegate extends ContextWrapper implements ComponentCallbacks2 {
    private Application f3d;

    public ApplicationDelegate() {
        super(null);
    }

    void m10a(Application application) {
        this.f3d = application;
        attachBaseContext(application);
    }

    public Application getApplication() {
        return this.f3d;
    }

    public void onConfigurationChanged(Configuration configuration) {
        this.f3d.m6a(configuration);
    }

    public void onCreate() {
        this.f3d.m7d();
    }

    public void onLowMemory() {
        this.f3d.m9f();
    }

    public void onTerminate() {
        this.f3d.m8e();
    }

    public void onTrimMemory(int i) {
        this.f3d.m5a(i);
    }

    public void registerComponentCallbacks(ComponentCallbacks componentCallbacks) {
        this.f3d.registerComponentCallbacks(componentCallbacks);
    }

    public void unregisterComponentCallbacks(ComponentCallbacks componentCallbacks) {
        this.f3d.unregisterComponentCallbacks(componentCallbacks);
    }
}
