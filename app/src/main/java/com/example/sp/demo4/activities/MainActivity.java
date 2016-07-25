package com.example.sp.demo4.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sp.demo4.R;
import com.example.sp.demo4.recycler.RecyclerAdapter;
import com.example.sp.demo4.recycler.RecyclerOnItemClickListener;
import com.example.sp.demo4.ui.DividerItemDecoration;
import com.example.sp.demo4.ui.InputDialog;
import com.example.sp.demo4.utils.FileUtils;
import com.example.sp.demo4.utils.PreferenceUtils;

import java.io.File;
import java.util.List;

import static com.example.sp.demo4.utils.FileUtils.getInternalStorage;
import static com.example.sp.demo4.utils.FileUtils.getMimeType;
import static com.example.sp.demo4.utils.FileUtils.getName;
import static com.example.sp.demo4.utils.FileUtils.getPath;
import static com.example.sp.demo4.utils.FileUtils.removeExtension;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_NAME = "com.example.sp.demo4.EXTRA_NAME";
    private static final String SAVED_DIRECTORY = "com.example.sp.demo4.SAVED_DIRECTORY";
    private static final String SAVED_SELECTION = "com.example.sp.demo4.SAVED_SELECTION";
    private Toolbar toolbar;
    private CheckBox checkBox;
    private TextView path;
    private RecyclerAdapter recyclerAdapter;
    private File currentDirectory;
    private CoordinatorLayout coordinatorLayout;
    private String name;
    private String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initActivityFromIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniAppBarLayout();
        initCoordinatorLayout();
        iniRecycleView();
        loadIntoRecyclerView();
    }

    @Override
    protected void onResume()
    {
        if (recyclerAdapter != null) recyclerAdapter.refresh();

        super.onResume();
    }

    @Override
    public void onBackPressed() {

        if (recyclerAdapter.flag)
        {
            recyclerAdapter.flag=false;
            recyclerAdapter.clearSelection();
            recyclerAdapter.notifyDataSetChanged();
            return;
        }

        if (!FileUtils.isStorage(currentDirectory))
        {
            setPath(currentDirectory.getParentFile());

            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        recyclerAdapter.select(savedInstanceState.getIntegerArrayList(SAVED_SELECTION));

        String path = savedInstanceState.getString(SAVED_DIRECTORY, getInternalStorage().getPath());

        if (currentDirectory != null) setPath(new File(path));

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putIntegerArrayList(SAVED_SELECTION, recyclerAdapter.getSelectedPositions());

        outState.putString(SAVED_DIRECTORY, getPath(currentDirectory));

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_search:
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_edit:
                actionEdit();
                return true;
            case R.id.action_create:
                actionCreate();
                return true;
            case R.id.action_sort:
                actionSort();
                return true;
            case R.id.action_delete:
                actionDelete();
                return true;
            case R.id.action_rename:
                actionRename();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (recyclerAdapter!=null){
            int count=recyclerAdapter.getSelectedItemCount();
            menu.findItem(R.id.action_search).setVisible(count==0&&recyclerAdapter.flag==false);
            menu.findItem(R.id.action_settings).setVisible(count==0&&recyclerAdapter.flag==false);
            menu.findItem(R.id.action_delete).setVisible(count>=1);
            menu.findItem(R.id.action).setVisible(count>=1);
            menu.findItem(R.id.action_rename).setVisible(count==1);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void initActivityFromIntent() {
        name = getIntent().getStringExtra(EXTRA_NAME);
    }

    private void loadIntoRecyclerView(){
        String permission= Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if(PackageManager.PERMISSION_GRANTED!= ContextCompat.checkSelfPermission(this,permission)){
            ActivityCompat.requestPermissions(this, new String[]{permission}, 0);
            return;
        }
        final Context context=this;

        if(name!=null){

            recyclerAdapter.addAll(FileUtils.searchFilesName(context, name));

            return;

        }
        setPath(getInternalStorage());
    }

    private void iniAppBarLayout(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        path=(TextView)findViewById(R.id.path);
    }

    private void initCoordinatorLayout()
    {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
    }

    private void iniRecycleView(){
        recyclerAdapter=new RecyclerAdapter(this);
        recyclerAdapter.setOnItemClickListener(new OnItemClickListener(this));
        recyclerAdapter.setOnSelectionListener(() ->
        {
             invalidateOptionsMenu();

             invalidateTitle();

             invalidateToolbar();
        });
        recyclerAdapter.setItemLayout(R.layout.item1);
        recyclerAdapter.setSpanCount(getResources().getInteger(R.integer.span_count0));

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycle_view);
        if(recyclerView!=null){
            recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST));
            recyclerView.setAdapter(recyclerAdapter);
        }
    }

    private void invalidateTitle(){
        if(recyclerAdapter.anySelected()){
            int selectedItemCount=recyclerAdapter.getSelectedItemCount();
            toolbar.setTitle(String.format("%s",selectedItemCount));
        }
        else {
            toolbar.setTitle("Demo4");
        }
    }

    private void invalidateToolbar(){

        if(path.getText().toString()!="�豸�洢") {
            toolbar.setNavigationIcon(R.drawable.ic_back);
            toolbar.setNavigationOnClickListener(v -> {
                if (!FileUtils.isStorage(currentDirectory)) {
                    setPath(currentDirectory.getParentFile());
                    return;
                }
            });
        }
        else {
            toolbar.setNavigationIcon(null);
        }
    }

    private void actionEdit(){
     //   checkBox=(CheckBox)toolbar.findViewById(R.id.check);
        recyclerAdapter.flag=true;
        /*if (recyclerAdapter.flag){
            checkBox.setVisibility(View.VISIBLE);
        }else {
            checkBox.setVisibility(View.GONE);
        }*/
        recyclerAdapter.notifyDataSetChanged();

        invalidateOptionsMenu();
    }

    private void actionCreate(){

        InputDialog inputDialog=new InputDialog(this,"�½�","�½��ļ���") {
            @Override
            public void onActionClick(String text) {
                try {
                    File directory= FileUtils.createDirectory(currentDirectory,text);

                    recyclerAdapter.clearSelection();

                    recyclerAdapter.add(directory);

                }catch (Exception e){
                    showMessage(e);
                }
            }
        };

        inputDialog.show();
    }

    private void actionDelete(){
        actionDelete(recyclerAdapter.getSelectedItems());

        recyclerAdapter.clearSelection();
    }

    private void actionDelete(final List<File> files){
        final File sourceDirectory=currentDirectory;

        String message = String.format("%s���ļ�����ɾ��", files.size());

        String sMessage=String.format("%s���ļ���ɾ���Ƿ���",files.size());

        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setTitle(message);

        alert.setNegativeButton("ȡ��",null);

        alert.setPositiveButton("ȷ��",(dialog, which) -> {

            recyclerAdapter.removeAll(files);

            Snackbar.make(coordinatorLayout,sMessage,Snackbar.LENGTH_LONG)
                    .setAction("����",v -> {
                        if (currentDirectory==null||currentDirectory.equals(sourceDirectory))
                        {
                            recyclerAdapter.addAll(files);
                        }
                    })
                    .setCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {

                            if (event!=DISMISS_EVENT_ACTION){
                                try {
                                    for (File file:files) FileUtils.deleteFile(file);
                                }
                                catch (Exception e){
                                    showMessage(e);
                                }
                            }
                            super.onDismissed(snackbar, event);
                        }
                    })
                    .show();
        });
        alert.show();
    }

    private void  actionSort(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        int checkedItem = PreferenceUtils.getInteger(this, "pref_sort", 1);

        String sorting[] = {"ʱ��", "����", "��С"};

        final int[] whichCheck = new int[1];

        final Context context = this;

        builder.setSingleChoiceItems(sorting, checkedItem, (dialog, which) ->
        {
            PreferenceUtils.putInt(context, "pref_sort", which);
            whichCheck[0] =which;
        });

        builder.setNegativeButton("ȡ��",null);

        builder.setPositiveButton("���",(dialog, which) ->{
            which=whichCheck[0];
            recyclerAdapter.update(which);
            dialog.dismiss();
        });

        builder.setTitle("����ʽ");

        builder.show();
    }

    private void actionRename(){
        final List<File> selectedItems=recyclerAdapter.getSelectedItems();

        InputDialog inputDialog=new InputDialog(this,"������","�������ļ���") {
            @Override
            public void onActionClick(String text) {
                recyclerAdapter.clearSelection();

                try {

                    File file=selectedItems.get(0);

                    int index=recyclerAdapter.indexOf(file);

                    recyclerAdapter.updateItemAt(index,FileUtils.renameFile(file,text));

                }catch (Exception e){
                    showMessage(e);
                }
            }
        };

        if (selectedItems.size()==1){
            inputDialog.setDefault(removeExtension(selectedItems.get(0).getName()));
        }

        inputDialog.show();
    }

    private void transferFiles(final List<File> files, final Boolean delete){

    }

    private void showMessage(Exception e)
    {
        showMessage(e.getMessage());
    }

    private void showMessage(String message)
    {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void setName(String name)
    {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra(EXTRA_NAME, name);

        startActivity(intent);
    }

    private void setPath(File directory){

        if(!directory.exists()){
            Toast.makeText(this,"Ŀ¼������",Toast.LENGTH_SHORT).show();
            return;
        }

        currentDirectory=directory;
        path.setText(currentDirectory.getAbsolutePath());
        str=path.getText().toString();
        if (str.equals("/storage/emulated/0")){
            path.setText("�豸�洢");
        }
        else {
            str=str.replaceAll("/storage/emulated/0","�豸�洢");
            str=str.replaceAll("/"," -> ");
            path.setText(str);
        }
        recyclerAdapter.clear();
        recyclerAdapter.clearSelection();
        recyclerAdapter.addAll(FileUtils.getChildren(directory));
        //invalidateToolbar();
    }

    private final class OnItemClickListener implements RecyclerOnItemClickListener
    {
        private final Context context;

        private OnItemClickListener(Context context)
        {
            this.context = context;
        }

        @Override
        public void onItemClick(int position)
        {
            final File file = recyclerAdapter.get(position);

            if (recyclerAdapter.flag)
            {
                recyclerAdapter.toggle(position);

                return;
            }
            if (file.isDirectory())
            {
                if (file.canRead())
                {
                    setPath(file);
                }
                else
                {
                    showMessage("Cannot open directory");
                }
            }
            else
            {
                if (Intent.ACTION_GET_CONTENT.equals(getIntent().getAction()))
                {
                    Intent intent = new Intent();

                    intent.setDataAndType(Uri.fromFile(file), getMimeType(file));

                    setResult(Activity.RESULT_OK, intent);

                    finish();
                }
                /*else if (FileUtils.FileType.getFileType(file) == FileUtils.FileType.ZIP)
                {
                    final ProgressDialog dialog = ProgressDialog.show(context, "", "Unzipping", true);

                    Thread thread = new Thread(() -> {
                        try
                        {
                            setPath(unzip(file));

                            runOnUiThread(dialog::dismiss);
                        }
                        catch (Exception e)
                        {
                            showMessage(e);
                        }
                    });

                    thread.run();
                }*/
                else
                {
                    try
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW);

                        intent.setDataAndType(Uri.fromFile(file), getMimeType(file));

                        startActivity(intent);
                    }
                    catch (Exception e)
                    {
                        showMessage(String.format("���ܴ�%s", getName(file)));
                    }
                }
            }
        }

        @Override
        public boolean onItemLongClick(int position)
        {
            recyclerAdapter.toggle(position);

            recyclerAdapter.flag=true;

            recyclerAdapter.notifyDataSetChanged();

            return true;
        }
    }

}