package top.trumeet.mipushframework.register;

import android.support.annotation.NonNull;

import top.trumeet.mipushframework.utils.BaseAppsBinder;

/**
 * Created by Trumeet on 2017/8/26.
 * @author Trumeet
 */

public class RegisteredApplicationBinder extends BaseAppsBinder<RegisteredApplication> {
    public RegisteredApplicationBinder() {
        super();
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder
            , @NonNull RegisteredApplication item) {
        fillData(item.getPackageName(),
                holder);
        // TODO
    }
}
