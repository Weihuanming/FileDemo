package com.example.sp.demo4.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.sp.demo4.R;

/**
 * Created by sp on 2016/7/28.
 */
public abstract class CheckDialog extends AlertDialog.Builder {

    private TextView rename;
    private TextView replace;
    private TextView cancel;
    //private final CheckBox check_to;
    public abstract void setCancel();
    public abstract void setReplace();
    public abstract void setRename();

    public CheckDialog(Context context) {
        super(context);

        View view=View.inflate(context, R.layout.dialog_copy_check,null);
        rename=(TextView)view.findViewById(R.id.rename);
        replace=(TextView)view.findViewById(R.id.replace);
        cancel=(TextView)view.findViewById(R.id.exit);
        setView(view);
        replace.setOnClickListener(v -> {
            setReplace();
        });
        rename.setOnClickListener(v->{
            setRename();
        });
        cancel.setOnClickListener(v -> {
            setCancel();
        });
        setTitle(R.string.name_used);
    }


}
