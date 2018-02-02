/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.widget;

import android.annotation.IdRes;
import android.annotation.UserIdInt;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.applications.LayoutPreference;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import top.trumeet.common.register.RegisteredApplication;
import top.trumeet.common.utils.Utils;
import top.trumeet.mipush.R;

public class EntityHeaderController {

    @IntDef({ActionType.ACTION_NONE,
            ActionType.ACTION_APP_INFO,
            ActionType.ACTION_APP_PREFERENCE,
            ActionType.ACTION_NOTIF_PREFERENCE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {
        int ACTION_NONE = 0;
        int ACTION_APP_INFO = 1;
        int ACTION_APP_PREFERENCE = 2;
        int ACTION_NOTIF_PREFERENCE = 3;
    }

    public static final String PREF_KEY_APP_HEADER = "pref_app_header";

    private static final String TAG = "AppDetailFeature";

    private final Context mAppContext;
    private final Fragment mFragment;
    private final View mHeader;
    private RecyclerView mRecyclerView;
    private Drawable mIcon;
    private String mIconContentDescription;
    private CharSequence mLabel;
    private CharSequence mSummary;
    private String mPackageName;
    private Intent mAppNotifPrefIntent;
    @UserIdInt
    private int mUid = UserHandle.USER_NULL;
    @ActionType
    private int mAction1;
    @ActionType
    private int mAction2;

    /**
     * Creates a new instance of the controller.
     *
     * @param fragment The fragment that header will be placed in.
     * @param header   Optional: header view if it's already created.
     */
    public static EntityHeaderController newInstance(AppCompatActivity activity, Fragment fragment,
            View header) {
        return new EntityHeaderController(activity, fragment, header);
    }

    private EntityHeaderController(AppCompatActivity activity, Fragment fragment, View header) {
        mAppContext = activity.getApplicationContext();
        mFragment = fragment;
        if (header != null) {
            mHeader = header;
        } else {
            mHeader = LayoutInflater.from(fragment.getContext())
                    .inflate(R.layout.settings_entity_header, null /* root */);
        }
    }

    public EntityHeaderController setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        return this;
    }

    /**
     * Set the icon in the header. Callers should also consider calling setIconContentDescription
     * to provide a description of this icon for accessibility purposes.
     */
    public EntityHeaderController setIcon(Drawable icon) {
        if (icon != null) {
            mIcon = icon.getConstantState().newDrawable(mAppContext.getResources());
        }
        return this;
    }

    /**
     * Convenience method to set the header icon from an ApplicationsState.AppEntry. Callers should
     * also consider calling setIconContentDescription to provide a description of this icon for
     * accessibility purposes.
     */
    public EntityHeaderController setIcon(RegisteredApplication appEntry) {
        mIcon = appEntry.getIcon(mAppContext);
        return this;
    }

    public EntityHeaderController setIconContentDescription(String contentDescription) {
        mIconContentDescription = contentDescription;
        return this;
    }

    public EntityHeaderController setLabel(CharSequence label) {
        mLabel = label;
        return this;
    }

    public EntityHeaderController setLabel(RegisteredApplication appEntry) {
        mLabel = appEntry.getLabel(mAppContext);
        return this;
    }

    public EntityHeaderController setSummary(CharSequence summary) {
        mSummary = summary;
        return this;
    }

    public EntityHeaderController setSummary(PackageInfo packageInfo) {
        if (packageInfo != null) {
            mSummary = packageInfo.versionName;
        }
        return this;
    }

    public EntityHeaderController setButtonActions(@ActionType int action1,
            @ActionType int action2) {
        mAction1 = action1;
        mAction2 = action2;
        return this;
    }

    public EntityHeaderController setPackageName(String packageName) {
        mPackageName = packageName;
        return this;
    }

    public EntityHeaderController setUid(int uid) {
        mUid = uid;
        return this;
    }

    public EntityHeaderController setAppNotifPrefIntent(Intent appNotifPrefIntent) {
        mAppNotifPrefIntent = appNotifPrefIntent;
        return this;
    }

    /**
     * Done mutating entity header, rebinds everything and return a new {@link LayoutPreference}.
     */
    public LayoutPreference done(AppCompatActivity activity, Context uiContext) {
        final LayoutPreference pref = new LayoutPreference(uiContext, done(activity));
        // Makes sure it's the first preference onscreen.
        pref.setOrder(-1000);
        pref.setKey(PREF_KEY_APP_HEADER);
        return pref;
    }

    /**
     * Done mutating entity header, rebinds everything (optionally skip rebinding buttons).
     */
    public View done(AppCompatActivity activity, boolean rebindActions) {
        styleActionBar(activity);
        ImageView iconView = mHeader.findViewById(R.id.entity_header_icon);
        if (iconView != null) {
            iconView.setImageDrawable(mIcon);
            iconView.setContentDescription(mIconContentDescription);
        }
        setText(R.id.entity_header_title, mLabel);
        setText(R.id.entity_header_summary, mSummary);

        if (rebindActions) {
            bindHeaderButtons();
        }

        return mHeader;
    }

    /**
     * Only binds entity header with button actions.
     */
    public EntityHeaderController bindHeaderButtons() {
        ImageButton button1 = mHeader.findViewById(android.R.id.button1);
        ImageButton button2 = mHeader.findViewById(android.R.id.button2);

        bindButton(button1, mAction1);
        bindButton(button2, mAction2);
        return this;
    }

    public EntityHeaderController styleActionBar(AppCompatActivity activity) {
        if (activity == null) {
            Log.w(TAG, "No activity, cannot style actionbar.");
            return this;
        }
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) {
            Log.w(TAG, "No actionbar, cannot style actionbar.");
            return this;
        }
        actionBar.setBackgroundDrawable(
                new ColorDrawable(Utils.getColorAttr(activity, R.attr.colorSettings)));
        actionBar.setElevation(0);
        //if (mRecyclerView != null && mLifecycle != null) {
        //    ActionBarShadowController.attachToRecyclerView(mActivity, mLifecycle, mRecyclerView);
        //}

        return this;
    }

    /**
     * Done mutating entity header, rebinds everything.
     */
    @VisibleForTesting
    View done(AppCompatActivity activity) {
        return done(activity, true /* rebindActions */);
    }

    private void bindButton(ImageButton button, @ActionType int action) {
        if (button == null) {
            return;
        }
        switch (action) {
            case ActionType.ACTION_APP_INFO: {
                button.setContentDescription(
                        mAppContext.getString(R.string.application_info_label));
                button.setImageResource(R.drawable.ic_info);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.fromParts("package", mPackageName, null);
                        mAppContext.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(uri)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                });
                button.setVisibility(View.VISIBLE);
                return;
            }
            case ActionType.ACTION_NOTIF_PREFERENCE: {
                if (mAppNotifPrefIntent == null) {
                    button.setVisibility(View.GONE);
                } else {
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mFragment.startActivity(mAppNotifPrefIntent);
                        }
                    });
                    button.setVisibility(View.VISIBLE);
                }
                return;
            }
            case ActionType.ACTION_APP_PREFERENCE: {
                final Intent intent = resolveIntent(
                        new Intent(Intent.ACTION_APPLICATION_PREFERENCES)
                                .setPackage(mPackageName));
                if (intent == null) {
                    button.setVisibility(View.GONE);
                    return;
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFragment.startActivity(intent);
                    }
                });
                button.setVisibility(View.VISIBLE);
                return;
            }
            case ActionType.ACTION_NONE: {
                button.setVisibility(View.GONE);
                return;
            }
        }
    }

    private Intent resolveIntent(Intent i) {
        ResolveInfo result = mAppContext.getPackageManager().resolveActivity(i, 0);
        if (result != null) {
            return new Intent(i.getAction())
                    .setClassName(result.activityInfo.packageName, result.activityInfo.name);
        }
        return null;
    }

    private void setText(@IdRes int id, CharSequence text) {
        TextView textView = mHeader.findViewById(id);
        if (textView != null) {
            textView.setText(text);
            textView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        }
    }
}
