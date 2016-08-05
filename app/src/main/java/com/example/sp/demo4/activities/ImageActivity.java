package com.example.sp.demo4.activities;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.example.sp.demo4.R;
import com.example.sp.demo4.adapters.RecyclerAdapter;
import com.example.sp.demo4.adapters.ViewpageAdapter;
import com.example.sp.demo4.utils.FileUtils;

public class ImageActivity extends AppCompatActivity{

    private RecyclerAdapter recyclerAdapter;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private ViewpageAdapter adapter;
    final Context context=this;

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
        viewPager=(ViewPager)findViewById(R.id.pager);

        recyclerAdapter=new RecyclerAdapter(this);
        recyclerAdapter.addAll(FileUtils.getImageLibrary(context));

        adapter=new ViewpageAdapter(this,recyclerAdapter);
        viewPager.setAdapter(adapter);
        Bundle bundle = getIntent().getExtras();

        viewPager.setCurrentItem(bundle.getInt("which"));
        viewPager.setPageMargin(35);
    }
}
