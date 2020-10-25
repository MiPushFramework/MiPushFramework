package top.trumeet.mipushframework.widgets;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.drakeet.multitype.ItemViewBinder;
import com.xiaomi.xmsf.R;

public class FooterItemBinder extends ItemViewBinder<Footer, FooterItemBinder.ViewHolder> {
    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_footer, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull Footer item) {
        holder.text1.setText(item.text);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView text1;
        public ViewHolder(View itemView) {
            super(itemView);
            this.text1 = itemView.findViewById(android.R.id.text1);
            text1.setMovementMethod(new LinkMovementMethod());
        }
    }
}
