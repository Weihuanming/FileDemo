package com.example.sp.demo4.recycler;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sp.demo4.R;

import java.io.File;

import static com.example.sp.demo4.utils.FileUtils.getName;
import static com.example.sp.demo4.utils.PreferenceUtils.getBoolean;


public class RecyclerViewHolder1_1 extends RecyclerViewHolder{

    private TextView name;

    public RecyclerViewHolder1_1(Context context, RecyclerOnItemClickListener listener, View view) {
        super(context, listener, view);
    }

    @Override
    protected void loadCheck() {
    }

    @Override
    protected void loadIcon() {
        image=(ImageView)itemView.findViewById(R.id.large_image);
    }

    @Override
    protected void loadName() {
        name=(TextView)itemView.findViewById(R.id.large_image_name);
    }

    @Override
    protected void bindCheck(boolean flag, Boolean selected) {
    }

    @Override
    protected void bindIcon(File file, Boolean selected) {

        Glide.with(context).load(file).into(image);
    }

    @Override
    protected void bindName(File file) {
        boolean extension = getBoolean(context, "pref_extension", false);
        name.setText(extension ? getName(file) : file.getName());
    }
}
