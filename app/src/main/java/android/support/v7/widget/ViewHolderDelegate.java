package androidx.appcompat.widget;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by dell on 2017/2/23.
 */

public abstract class ViewHolderDelegate {

    private ViewHolderDelegate() {
        throw new UnsupportedOperationException("no instances");
    }

    public static void setPosition(RecyclerView.ViewHolder viewHolder, int position) {
        viewHolder.mPosition = position;
    }
}
