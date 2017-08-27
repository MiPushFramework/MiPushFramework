package top.trumeet.mipushframework.register;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

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
        fillData(item.getPackageName(),
                holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.itemView.getContext()
                        .startActivity(new Intent(holder.itemView.getContext(),
                                ManagePermissionsActivity.class)
                        .putExtra(ManagePermissionsActivity.EXTRA_PACKAGE_NAME,
                                item.getPackageName()));
            }
        });
    }
}
