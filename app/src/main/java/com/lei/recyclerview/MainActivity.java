package com.lei.recyclerview;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lei.recyclerview.divider.ItemDividerDecoration;
import com.lei.recyclerview.widget.PullCallback;
import com.lei.recyclerview.widget.PullToLoadView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PullCallback {

    PullToLoadView loadView;
    ListAdapter adapter;

    List<String> list = new ArrayList<>();
    int j = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadView = (PullToLoadView) findViewById(R.id.list);
        loadView.setLayoutManager(new LinearLayoutManager(this));
        loadView.addItemDecoration(new ItemDividerDecoration.Builder(this).size(1).color(Color.BLUE).build());
        loadView.setPullCallback(this);
        for (int i = 0; i < 10; i++) {
            list.add("测试");
        }
        adapter = new ListAdapter(this, list);
        loadView.setAdapter(adapter);
        loadView.hasMoreItems(true);
    }

    @Override
    public void onLoadMore() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    list.add("测试");
                }
                j++;
                if (j < 3) {
                    loadView.hasMoreItems(true);
                } else if (j == 3) {
                    loadView.hasMoreItems(false);
                    loadView.hasLoadedAllItems("已经没有更多数据了...");
                }
                loadView.setComplete();
                adapter.refresh(list, true);
            }
        }.execute();
    }

    @Override
    public void onRefresh() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    list.add("测试");
                }
                j = 0;
                loadView.hasMoreItems(true);
                loadView.setComplete();
                adapter.refresh(list);
            }
        }.execute();
    }

    class ListAdapter extends BaseRecyclerAdapter {

        public ListAdapter(Context context) {
            super(context);
        }

        public ListAdapter(Context context, List<String> s) {
            super(context, s);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(mContext);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120));
            textView.setGravity(Gravity.CENTER);
            return new ItemHolder(textView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ItemHolder itemHolder = (ItemHolder) holder;
            itemHolder.tvText.setText("测试" + position);
        }
    }

    class ItemHolder extends BaseViewHolder {

        public TextView tvText;

        public ItemHolder(View itemView) {
            super(itemView);
            tvText = (TextView) itemView;
        }
    }
}
