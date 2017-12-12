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

package com.android.settings.applications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.xiaomi.xmsf.R;

import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceViewHolder;

public class LayoutPreference extends Preference {

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onClick(View v) {
            performClick(v);
        }
    };

    @VisibleForTesting
    View mRootView;

    public LayoutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference);
        //a.recycle();

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.Preference, 0, 0);
        int layoutResource = a.getResourceId(R.styleable.Preference_layout,
                0);
        if (layoutResource == 0) {
            throw new IllegalArgumentException("LayoutPreference requires a layout to be defined");
        }
        a.recycle();

        // Need to create view now so that findViewById can be called immediately.
        final View view = LayoutInflater.from(getContext())
                .inflate(layoutResource, null, false);
        setView(view);
    }

    public LayoutPreference(Context context, int resource) {
        this(context, LayoutInflater.from(context).inflate(resource, null, false));
    }

    public LayoutPreference(Context context, View view) {
        super(context);
        setView(view);
    }

    private void setView(View view) {
        setLayoutResource(R.layout.layout_preference_frame);
        /*
        final ViewGroup allDetails = view.findViewById(R.id.all_details);
        if (allDetails != null) {
            Utils.forceCustomPadding(allDetails, true /* additive padding *);
        }
        */
        mRootView = view;
        setShouldDisableView(false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        holder.itemView.setOnClickListener(mClickListener);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
        //holder.setDividerAllowedAbove(mAllowDividerAbove);
        //holder.setDividerAllowedBelow(mAllowDividerBelow);

        FrameLayout layout = (FrameLayout) holder.itemView;
        layout.removeAllViews();
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        layout.addView(mRootView);
    }

    public <T extends View> T findViewById(int id) {
        return mRootView.findViewById(id);
    }

}
