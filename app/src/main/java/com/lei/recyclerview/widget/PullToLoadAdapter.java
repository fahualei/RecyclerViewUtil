package com.lei.recyclerview.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lei.recyclerview.BaseRecyclerAdapter;
import com.lei.recyclerview.BaseViewHolder;
import com.lei.recyclerview.R;

/**
 * Created by dell on 2017/2/7.
 */

class PullToLoadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private BaseRecyclerAdapter adapter;
    private boolean showFooter = false;
    private boolean mProgressBarShow = true;
    private String mFooterInfo = "加载更多";

    public PullToLoadAdapter(BaseRecyclerAdapter adapter) {
        this.adapter = adapter;
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }
        });
    }

    public void setShowFooter(boolean showFooter) {
        this.showFooter = showFooter;
    }

    public void setFooterInfo(boolean proBarShow, String footerInfo) {
        this.mProgressBarShow = proBarShow;
        this.mFooterInfo = footerInfo;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == -1) {
            View view = adapter.getInflater().inflate(R.layout.layout_bottom_load, parent, false);
            return new FooterHolder(view);
        }
        return adapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == -1) {
            FooterHolder viewHolder = (FooterHolder) holder;
            if (mProgressBarShow) {
                viewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                viewHolder.progressBar.setVisibility(View.GONE);
            }
            viewHolder.tvInfo.setText(mFooterInfo);
        } else {
            adapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showFooter && position == adapter.getItemCount()) {
            return -1;
        } else {
            return adapter.getItemViewType(position);
        }
    }

    @Override
    public int getItemCount() {
        if (showFooter && adapter.getItemCount() > 0) {
            return adapter.getItemCount() + 1;
        } else {
            return adapter.getItemCount();
        }
    }

    private class FooterHolder extends BaseViewHolder {

        public ProgressBar progressBar;
        public TextView tvInfo;

        public FooterHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            tvInfo = (TextView) itemView.findViewById(R.id.tv_info);
        }
    }
}
