package top.trumeet.mipushframework.register;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;

import top.trumeet.common.register.RegisteredApplication;
import com.xiaomi.xmsf.R;
import top.trumeet.mipushframework.permissions.ManagePermissionsActivity;
import top.trumeet.mipushframework.utils.BaseAppsBinder;

/**
 * Created by Trumeet on 2017/8/26.
 * @author Trumeet
 */

public class RegisteredApplicationBinder extends BaseAppsBinder<RegisteredApplication> {
    RegisteredApplicationBinder() {
        super();
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder
            , @NonNull final RegisteredApplication item) {
        fillData(item.getPackageName(), true,
                holder);
        //todo res color
        switch (item.getRegisteredType()) {
            case 1: {
                holder.text2.setText(R.string.app_registered);
                holder.text2.setTextColor(Color.parseColor("#FF0B5B27"));
                break;
            }
            case 2: {
                holder.text2.setText(R.string.app_registered_error);
                holder.text2.setTextColor(Color.parseColor("#FFF41804"));
                break;
            }
            case 0: {
                holder.text2.setText(R.string.status_app_not_registered);
                break;
            }
        }
        holder.itemView.setOnClickListener(view -> holder.itemView.getContext()
                .startActivity(new Intent(holder.itemView.getContext(),
                        ManagePermissionsActivity.class)
                .putExtra(ManagePermissionsActivity.EXTRA_PACKAGE_NAME,
                        item.getPackageName())
                .putExtra(ManagePermissionsActivity.EXTRA_IGNORE_NOT_REGISTERED, true)));
    }
}
