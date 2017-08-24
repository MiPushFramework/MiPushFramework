package com.xiaomi.xmsf;

import miui.external.ApplicationDelegate;

public class Application extends miui.external.Application {
    public ApplicationDelegate onCreateApplicationDelegate() {
        return new XmsfApp();
    }
}
