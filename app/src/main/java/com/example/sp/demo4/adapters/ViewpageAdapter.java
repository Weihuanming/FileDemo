package com.example.sp.demo4.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.example.sp.demo4.R;
import com.example.sp.demo4.activities.ImageActivity;
import com.example.sp.demo4.utils.FileUtils;

import java.util.List;

public class ViewpageAdapter extends PagerAdapter {

    private RecyclerAdapter recyclerAdapter;
    private Context context;

    public ViewpageAdapter(Context context,RecyclerAdapter recyclerAdapter){
        this.context=context;
        this.recyclerAdapter=recyclerAdapter;
    }

    @Override
    public int getCount() {
        return recyclerAdapter.getItemCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView view=new PhotoView(context);
        view.enable();
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Glide.with(context).load(recyclerAdapter.get(position)).centerCrop().into(view);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }
}
