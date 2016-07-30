package com.example.sp.demo4.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.io.File;

public abstract class RecyclerViewHolder extends RecyclerView.ViewHolder {

    final Context context;

    ImageView image;

    CheckBox checkbox;

    View.OnClickListener onActionClickListener;

    private View.OnClickListener onClickListener;

    private View.OnLongClickListener onLongClickListener;

    public RecyclerViewHolder(Context context,RecyclerOnItemClickListener listener,View view) {
        super(view);
        this.context=context;
        setClickListener(listener);
        loadCheck();
        loadIcon();
        loadName();
    }

    protected abstract void loadCheck();

    protected abstract void loadIcon();

    protected abstract void loadName();

    protected abstract void bindCheck(boolean flag,Boolean selected);

    protected abstract void bindIcon(File file,Boolean selected);

    protected abstract void bindName(File file);

    private void setClickListener(final RecyclerOnItemClickListener listener){

        this.onActionClickListener=v->listener.onItemLongClick(getAdapterPosition());

        this.onClickListener=v -> listener.onItemClick(getAdapterPosition());

        this.onLongClickListener=v -> listener.onItemLongClick(getAdapterPosition());
    }

    public void setData(final File file,Boolean selected,boolean flag){

        itemView.setOnClickListener(onClickListener);

        itemView.setOnLongClickListener(onLongClickListener);

        itemView.setSelected(selected);

        bindCheck(flag,selected);

        bindIcon(file,selected);

        bindName(file);

    }
}
