package top.trumeet.mipushframework.wizard.support;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.setupwizardlib.SetupWizardListLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.xiaomi.xmsf.BuildConfig;
import com.xiaomi.xmsf.R;

import java.util.ArrayList;
import java.util.List;

import top.trumeet.mipushframework.Constants;
import top.trumeet.mipushframework.utils.ShellUtils;
import top.trumeet.mipushframework.wizard.CheckDozeActivity;

/**
 * Created by Trumeet on 2017/8/24.
 * Check application support status
 */

public class CheckAppSupportActivity extends AppCompatActivity implements NavigationBar.NavigationBarListener {
    private TextView mTvStatus;
    private SetupWizardListLayout mLayout;

    private CheckAppSupportTask mTask;
    private Adapter mAdapter;
    private List<SupportStatus> mResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayout = new SetupWizardListLayout(this);
        mLayout.getNavigationBar()
                .setNavigationBarListener(this);
        mTvStatus = new TextView(this);
        int padding = (int) getResources().getDimension(R.dimen.suw_glif_margin_sides);
        mTvStatus.setPadding(padding, padding, padding, padding);
        TextView tv = new TextView(this);
        tv.setText(Html.fromHtml(getString(R.string.wizard_descr_check_app_support)));
        tv.setPadding(padding, padding, padding, padding);

        mLayout.getListView().addHeaderView(tv);
        mLayout.getListView().addHeaderView(mTvStatus);
        mLayout.setHeaderText(R.string.wizard_title_check_app_support);
        setContentView(mLayout);
        mResult = new ArrayList<>(0);
        mAdapter = new Adapter(mResult);
        mLayout.setAdapter(mAdapter);
        mLayout.getListView()
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                        final SupportStatus supportStatus = ((SupportStatus)adapterView.getItemAtPosition(i));
                        if (supportStatus.status == SupportStatus.Status.FAIL_CAN_FIX) {
                            FixDialogFragment fixDialogFragment = FixDialogFragment.newInstance(supportStatus.pkgName);
                            fixDialogFragment.setCallback(new FixDialogFragment.Callback() {
                                @Override
                                public void onFinish(boolean success) {
                                    Toast.makeText(CheckAppSupportActivity.this,
                                            success ? R.string.success : R.string.fail,
                                            Toast.LENGTH_SHORT).show();
                                    // TODO: not modified?
                                    mResult.set(i, CheckSupportUtils.check(supportStatus.pkgName,
                                            getPackageManager()));
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                            fixDialogFragment.show(getSupportFragmentManager(),
                                    "Fix");
                        }
                    }
                });

        mTask = new CheckAppSupportTask();
        mTask.execute();
    }

    @Override
    public void onDestroy () {
        if (mTask != null && !mTask.isCancelled())
            mTask.cancel(true);
        super.onDestroy();
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        if (getIntent().getBooleanExtra(Constants.EXTRA_FINISH_ON_NEXT,
                false)) {
            finish();
        } else {
            startActivity(new Intent(this
                    , CheckDozeActivity.class));
        }
    }

    private class CheckAppSupportTask extends AsyncTask<Void, Void, List<SupportStatus>> {
        private static final String TAG = "CheckSupport";

        @Override
        protected void onPreExecute () {
            mLayout.setProgressBarShown(true);
            mTvStatus.setText(R.string.loading);
            mLayout.getNavigationBar().setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute (List<SupportStatus> statuses) {
            mLayout.setProgressBarShown(false);
            mTvStatus.setText(statuses.size() <= 0
            ? R.string.app_support_no_apps : R.string.app_support_result);
            mLayout.getNavigationBar().setVisibility(View.VISIBLE);
            mResult.clear();
            mResult.addAll(statuses);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected List<SupportStatus> doInBackground (Void... params) {
            List<PackageInfo> packageInfos = getPackageManager()
                    .getInstalledPackages(PackageManager.GET_SERVICES |
                            PackageManager.GET_RECEIVERS | PackageManager.GET_DISABLED_COMPONENTS);
            List<SupportStatus> supportStatuses = new ArrayList<>(packageInfos.size());
            for (PackageInfo info : packageInfos) {
                if (info.packageName.equals(BuildConfig.APPLICATION_ID)) {
                    continue;
                }
                SupportStatus supportStatus = CheckSupportUtils
                        .check(info, getPackageManager());
                if (supportStatus != null) {
                    supportStatuses.add(supportStatus);
                }
            }
            return supportStatuses;
        }
    }

    private class Adapter extends ArrayAdapter<SupportStatus> {
        private LruCache<String, Drawable> mIconMemoryCaches;

        Adapter (List<SupportStatus> list) {
            super(CheckAppSupportActivity.this, 0,
                    list);
            int maxMemory = (int) Runtime.getRuntime().maxMemory();
            int cacheSizes = maxMemory/5;

            mIconMemoryCaches = new LruCache<>(cacheSizes);
        }

        class ViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;
        }

        @Override
        @NonNull
        public View getView (int pos, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.preference_app, parent, false);
                holder = new ViewHolder();
                holder.icon = convertView.findViewById(android.R.id.icon);
                holder.title = convertView.findViewById(android.R.id.title);
                holder.summary = convertView.findViewById(android.R.id.summary);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            SupportStatus supportStatus = getItem(pos);
            PackageManager packageManager = getPackageManager();
            try {
                holder.title.setText(packageManager.getApplicationLabel(packageManager.getApplicationInfo(supportStatus.pkgName,
                        0)));
                Drawable icon = getIconFromMemoryCache(supportStatus.pkgName);
                if (icon != null) {
                    holder.icon.setImageDrawable(icon);
                } else {
                    new IconWorkerTask(holder.icon)
                            .execute(supportStatus.pkgName);
                }
                String text;
                switch (supportStatus.status) {
                    case SupportStatus.Status.OK :
                        text = getString(R.string.app_support_ok);
                        break;
                    case SupportStatus.Status.FAIL:
                        text = getString(R.string.app_support_fail);
                        break;
                    case SupportStatus.Status.FAIL_CAN_FIX:
                        text = getString(R.string.app_support_fail_can_fix);
                        break;
                    default:
                        text = null;
                        break;
                }
                holder.summary.setText(text);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return convertView;
        }

        private void addDrawableToMemoryCache (@Nullable String pkg, @NonNull Drawable icon) {
            if (getIconFromMemoryCache(pkg) == null) {
                if (pkg == null) pkg = "";
                mIconMemoryCaches.put(pkg, icon);
            }
        }

        private Drawable getIconFromMemoryCache (@Nullable String pkg) {
            if (pkg == null) pkg = "";
            return mIconMemoryCaches.get(pkg);
        }

        public class IconWorkerTask extends AsyncTask<String, Void, Drawable> {
            private Context context;

            private ImageView imageView;
            IconWorkerTask (ImageView imageView) {
                this.imageView = imageView;
                this.context = imageView.getContext();
            }

            @Override
            protected Drawable doInBackground(String... params) {
                String pkg = params[0];
                Drawable icon;
                if (pkg == null || pkg.equals("")) {
                    pkg = "";
                    icon = ContextCompat.getDrawable(context,
                            android.R.mipmap.sym_def_app_icon);
                } else {
                    try {
                        icon = context.getPackageManager()
                                .getApplicationIcon(pkg);
                    } catch (PackageManager.NameNotFoundException ignore) {
                        icon = ContextCompat.getDrawable(context,
                                android.R.mipmap.sym_def_app_icon);
                    }
                }
                if (icon == null) {
                    return null;
                }
                addDrawableToMemoryCache(pkg, icon);
                return icon;
            }

            @Override
            protected void onPostExecute (Drawable drawable) {
                if (imageView != null) {
                    imageView.setImageDrawable(drawable);
                }
            }
        }
    }

    public static class FixDialogFragment extends DialogFragment {
        private static final String EXTRA_PKG_NAME = FixDialogFragment.class.getName()
                + ".EXTRA_PKG_NAME";
        public static FixDialogFragment newInstance (String pkg) {
            FixDialogFragment fixDialogFragment = new FixDialogFragment();
            Bundle args = new Bundle();

            args.putString(EXTRA_PKG_NAME, pkg);
            fixDialogFragment.setArguments(args);
            return fixDialogFragment;
        }

        private String mPackageName;
        private Callback mCallback;

        @Override
        public void onCreate (Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPackageName = getArguments() == null ?
                    null : getArguments().getString(EXTRA_PKG_NAME, null);
        }

        @Override
        @NonNull
        public Dialog onCreateDialog (Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.fix_push_title)
                    .setMessage(getString(R.string.fix_push_descr, mPackageName))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new FixTask().execute();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }

        public interface Callback {
            void onFinish (boolean success);
        }

        public void setCallback (Callback callback) {
            this.mCallback = callback;
        }

        private class FixTask extends AsyncTask<Void, Void, Boolean> {
            private ProgressDialog mDialog;

            @Override
            protected Boolean doInBackground(Void... voids) {
                ActivityInfo info = getReceiverName(mPackageName, getActivity().getPackageManager());
                if (info == null)
                    return false;
                return ShellUtils.exec("pm enable " + info.packageName + "/" +
                info.name);
            }

            private ActivityInfo getReceiverName (String pkg,
                                                   PackageManager packageManager) {
                Intent intent = new Intent();
                intent.setPackage(pkg);
                //intent.setAction(Constants.ACTION_RECEIVE_MESSAGE);
                for (ResolveInfo resolveInfo : packageManager.queryBroadcastReceivers(intent,
                        PackageManager.GET_RESOLVED_FILTER |
                                PackageManager.GET_DISABLED_COMPONENTS)) {
                    ActivityInfo activityInfo = resolveInfo.activityInfo;
                    if (!activityInfo.packageName.equals(pkg))
                        continue;
                    IntentFilter filter = resolveInfo.filter;
                    if (filter != null && filter.hasAction(Constants.ACTION_RECEIVE_MESSAGE)) {
                        return activityInfo;
                    }
                }
                // Not found
                return null;
            }

            @Override
            protected void onPreExecute () {
                mDialog = new ProgressDialog(getActivity());
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.setCancelable(false);
                mDialog.show();
            }

            @Override
            protected void onPostExecute (Boolean result) {
                mDialog.dismiss();
                if (mCallback != null)
                    mCallback.onFinish(result);
                dismiss();
            }
        }
    }
}
