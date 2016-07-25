package com.example.sp.demo4.recycler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sp.demo4.R;

import java.io.File;

import static com.example.sp.demo4.utils.FileUtils.getColorResource;
import static com.example.sp.demo4.utils.FileUtils.getImageResource;
import static com.example.sp.demo4.utils.FileUtils.getName;
import static com.example.sp.demo4.utils.PreferenceUtils.getBoolean;

/**
 * Created by sp on 2016/7/19.
 */
public class RecyclerViewHolder0 extends RecyclerViewHolder {

    private TextView name;



    public RecyclerViewHolder0(Context context, RecyclerOnItemClickListener listener, View view) {
        super(context, listener, view);
    }

    @Override
    protected void loadCheck() {
        checkbox=(CheckBox)itemView.findViewById(R.id.checkbox);
    }

    @Override
    protected void loadIcon() {
        image = (ImageView) itemView.findViewById(R.id.item_image);
    }

    @Override
    protected void loadName() {
        name=(TextView)itemView.findViewById(R.id.item_name);
    }

    @Override
    protected void bindCheck(boolean flag,Boolean selected) {


        if (flag){
            checkbox.setVisibility(View.VISIBLE);
        }else {
            checkbox.setVisibility(View.GONE);
        }

        checkbox.setOnClickListener(onActionClickListener);

        checkbox.setOnLongClickListener(onActionLongClickListener);

        if (selected){
            checkbox.setChecked(true);
        }else {
            checkbox.setChecked(false);
        }
    }

    @Override
    protected void bindIcon(File file,Boolean selected) {

        Drawable drawable = ContextCompat.getDrawable(context,R.drawable.ic_directory);

        image.setImageDrawable(drawable);

    }

    @Override
    protected void bindName(File file) {
        boolean extension = getBoolean(context, "pref_extension", true);
        name.setText(extension ? getName(file) : file.getName());
    }

    private ShapeDrawable getBackground(int color)
    {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());

        int size = (int) context.getResources().getDimension(R.dimen.avatar_size);

        shapeDrawable.setIntrinsicWidth(size);

        shapeDrawable.setIntrinsicHeight(size);

        shapeDrawable.getPaint().setColor(color);

        return shapeDrawable;
    }
}
