package com.lei.recyclerview;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * @author wellor
 * @Version
 * @Date 2015-10-18
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mItemClickListener;

    public void setOnItemClickListener(
            final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public BaseViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(v, getLayoutPosition());
                }
            }
        });
    }

    protected View getView(View itemView, int id) {
        View v = itemView.findViewById(id);
        return v;
    }

}
