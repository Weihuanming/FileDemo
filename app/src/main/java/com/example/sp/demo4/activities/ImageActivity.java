package com.example.sp.demo4.activities;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.sp.demo4.R;
import com.example.sp.demo4.adapters.RecyclerAdapter;
import com.example.sp.demo4.adapters.ViewpageAdapter;
import com.example.sp.demo4.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class ImageActivity extends AppCompatActivity{

    private RecyclerAdapter recyclerAdapter;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private List<View> list_view;
    private ViewpageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        iniAppBarLayout();
        iniViewPager();
    }

    private void iniAppBarLayout(){
        toolbar = (Toolbar) findViewById(R.id.image_toolbar);
        ImageView back=(ImageView)toolbar.findViewById(R.id.close_image);
        setTitle(null);
        back.setOnClickListener(v -> finish());
        setSupportActionBar(toolbar);
    }

    private void iniViewPager(){
        viewPager=(ViewPager)findViewById(R.id.viewPager);
        recyclerAdapter=new RecyclerAdapter(this);
        final Context context=this;
        recyclerAdapter.addAll(FileUtils.getImageLibrary(context));
        list_view=new ArrayList<>();
        for(int i=0;i<recyclerAdapter.getItemCount();i++){

            View view=LayoutInflater.from(this).inflate(R.layout.image_view,null);
            ImageView imageView=(ImageView)view.findViewById(R.id.image_view);

            Glide.with(this).load(recyclerAdapter.get(i)).fitCenter().into(imageView);
            list_view.add(view);
        }
        adapter=new ViewpageAdapter(list_view);
        viewPager.setAdapter(adapter);
        Bundle bundle = getIntent().getExtras();
        viewPager.setCurrentItem(bundle.getInt("which"));
        viewPager.setPageMargin(35);
    }
}
