package com.example.sp.demo4.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

import com.example.sp.demo4.R;

/**
 * Created by sp on 2016/7/21.
 */
public abstract class InputDialog extends AlertDialog.Builder{

    private final EditText editText;

    public abstract void onActionClick(String text);

    public InputDialog(Context context, String positive, String title){
        super(context);

        View view=View.inflate(context, R.layout.dialog_edit_text,null);

        editText=(EditText)view.findViewById(R.id.dialog_edit_text);

        setView(view);

        setNegativeButton("È¡Ïû",null);

        setPositiveButton(positive,(dialog, which) ->{
          if (editText.length()!=0) onActionClick(editText.getText().toString());
        });

        setTitle(title);
    }

    public void setDefault(String text)
    {
        editText.setText(text);

        editText.setSelection(editText.getText().length());
    }
}
