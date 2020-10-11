/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

public class AppHeader {

    public static final String EXTRA_HIDE_INFO_BUTTON = "hideInfoButton";
    // constant value that can be used to check return code from sub activity.
    private static final int INSTALLED_APP_DETAILS = 1;

    public static boolean includeAppInfo(final Fragment fragment) {
        Bundle args = fragment.getArguments();
        boolean showInfo = true;
        if (args != null && args.getBoolean(EXTRA_HIDE_INFO_BUTTON, false)) {
            showInfo = false;
        }
        Intent intent = fragment.getActivity().getIntent();
        if (intent != null && intent.getBooleanExtra(EXTRA_HIDE_INFO_BUTTON, false)) {
            showInfo = false;
        }
        return showInfo;
    }
}
