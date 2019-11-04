package com.lei.recyclerview;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <T>
 * @param <T>
 * @author wellor
 * @Version
 * @Date 2015-10-18
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {

    protected Context mContext;

    protected LayoutInflater mInflater;

    protected List<T> mList;


    public BaseRecyclerAdapter(Context context) {
        this(context, new ArrayList<T>());
    }

    public BaseRecyclerAdapter(Context context, List<T> list) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mList = list;
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    @Override
    public int getItemCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    public T getItem(int position) {
        if (mList != null && position >= 0 && position < mList.size()) {
            return mList.get(position);
        }
        return null;
    }

    /**
     * 刷新列表
     *
     * @param list
     * @param append 是否将新数据追加到尾部
     */
    public BaseRecyclerAdapter<T> refresh(List<T> list, boolean append) {
        if (list == null) {
            list = new ArrayList<>();
        }
        if (!append) {
            this.mList.clear();
        }
        this.mList.addAll(list);
        notifyDataSetChanged();
        return this;
    }

    public void addList(List<T> content, int position) {
        if (content != null && content.size() > 0) {
            for (T t : content) {
                mList.add(position, t);
            }
            notifyDataSetChanged();
        }
    }

    /**
     * 单个添加数据
     *
     * @param content
     * @param position
     */
    public void addItem(T content, int position) {
        mList.add(position, content);
        notifyItemInserted(position); // Attention!
    }

    /**
     * 单个删除
     *
     * @param model
     */
    public void removeItem(T model) {
        int position = mList.indexOf(model);
        mList.remove(position);
        notifyItemRemoved(position);// Attention!
    }

    /**
     * 单个删除
     *
     * @param position
     */
    public void removeItem(int position) {
        if (position >= 0 && position < mList.size()) {
            mList.remove(position);
            notifyItemRemoved(position);// Attention!
        }
    }

    /**
     * 单个删除
     *
     * @param position
     */
    public void removeItemUnSafe(int position) {
        mList.remove(position);
        notifyItemRemoved(position);// Attention!
    }

    /**
     * 刷新列表
     * <p/>
     * 新元素会替换到所有旧元素
     *
     * @param list
     */
    public BaseRecyclerAdapter<T> refresh(List<T> list) {
        refresh(list, false);
        return this;
    }

    public List<T> getList() {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        return mList;
    }

}
