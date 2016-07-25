package com.example.sp.demo4.recycler;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.example.sp.demo4.R;
import com.example.sp.demo4.activities.MainActivity;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by sp on 2016/7/18.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder>{

    private final Context context;
    private final SortedList<File> items;
    private final SparseBooleanArray selectedItems;
    private final RecyclerCallback recyclerCallback;
    private Integer itemLayout;
    private Integer spanCount;
    private RecyclerOnItemClickListener onItemClickListener;
    private RecyclerOnSelectionListener onSelectionListener;
    public boolean flag=false;
    public boolean isCheck;

    public RecyclerAdapter(Context context){

        this.context=context;
        this.recyclerCallback = new RecyclerCallback(context,this);
        this.items=new SortedList<>(File.class, this.recyclerCallback);
        this.selectedItems=new SparseBooleanArray();
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView= LayoutInflater.from(context).inflate(itemLayout,parent,false);
        switch (itemLayout){
            case R.layout.item1:
                return new RecyclerViewHolder0(context,onItemClickListener,itemView);
            default:
                return null;
        }
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        recyclerView.setLayoutManager(new GridLayoutManager(context,spanCount));
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.setData(get(position),getSelected(position),flag);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void  setOnItemClickListener(RecyclerOnItemClickListener onItemClickListenr){
        this.onItemClickListener=onItemClickListenr;
    }

    public void setOnSelectionListener(RecyclerOnSelectionListener onSelectionListener)
    {
        this.onSelectionListener = onSelectionListener;
    }

    public boolean anySelected()
    {
        return selectedItems.size() > 0;
    }

    public void select(ArrayList<Integer> positions){

        selectedItems.clear();

        for (int i:positions){
            selectedItems.append(i,true);
            notifyItemChanged(i);
        }

        onSelectionListener.onSelectionChanged();
    }

    public void toggle(int position)
    {
        if (getSelected(position)) selectedItems.delete(position);

        else selectedItems.append(position, true);

        notifyItemChanged(position);

        onSelectionListener.onSelectionChanged();
    }

    private boolean getSelected(int position){
        return selectedItems.get(position);
    }

    public void setItemLayout(int itemLayout){
        this.itemLayout=itemLayout;
    }

    public void setSpanCount(int spanCount){
        this.spanCount=spanCount;
    }

    public void remove(File file){
        items.remove(file);
    }

    public void removeAll(Collection<File> files){
        for (File file:files) remove(file);
    }

    public void removeItemAt(int index){
        items.removeItemAt(index);
    }

    public void updateItemAt(int index,File file){
        items.updateItemAt(index,file);
    }

    public void add(File file)
    {
        items.add(file);
    }

    public void addAll(File...files){
        items.addAll(files);
    }

    public void addAll(Collection<File> files){
        items.addAll(files);
    }

    public void refresh()
    {
        for (int i = 0; i < getItemCount(); i++)
        {
            notifyItemChanged(i);
        }
    }

    public void clear(){
        while(items.size()>0) removeItemAt(items.size()-1);
    }

    public void clearSelection()
    {
        ArrayList<Integer> selectedPositions = getSelectedPositions();

        selectedItems.clear();

        for (int i : selectedPositions) notifyItemChanged(i);

        onSelectionListener.onSelectionChanged();
    }

    private ArrayList<File> getItems()
    {
        ArrayList<File> list = new ArrayList<>();

        for (int i = 0; i < getItemCount(); i++)
        {
            list.add(get(i));
        }

        return list;
    }

    public ArrayList<Integer> getSelectedPositions()
    {
        ArrayList<Integer> list = new ArrayList<>();

        for (int i = 0; i < getItemCount(); i++)
        {
            if (getSelected(i)) list.add(i);
        }

        return list;
    }

    public void update(int criteria)
    {
        if (recyclerCallback.update(criteria))
        {
            ArrayList<File> list = getItems();

            clear();

            addAll(list);
        }
    }

    public int getSelectedItemCount()
    {
        return selectedItems.size();
    }

    public int indexOf(File file){
        return items.indexOf(file);
    }

    public ArrayList<File> getSelectedItems()
    {
        ArrayList<File> list = new ArrayList<>();

        for (int i = 0; i < getItemCount(); i++)
        {
            if (getSelected(i)) list.add(get(i));
        }

        return list;
    }

    public File get(int index)
    {
        return items.get(index);
    }
}
