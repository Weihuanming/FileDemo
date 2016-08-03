package com.example.sp.demo4.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sp.demo4.R;
import com.example.sp.demo4.adapters.RecyclerAdapter;
import com.example.sp.demo4.recycler.RecyclerOnItemClickListener;
import com.example.sp.demo4.ui.DividerItemDecoration;
import com.example.sp.demo4.ui.InputDialog;
import com.example.sp.demo4.utils.FileUtils;
import com.example.sp.demo4.utils.PreferenceUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.sp.demo4.utils.FileUtils.getImageLibrary;
import static com.example.sp.demo4.utils.FileUtils.getInternalStorage;
import static com.example.sp.demo4.utils.FileUtils.getMimeType;
import static com.example.sp.demo4.utils.FileUtils.getName;
import static com.example.sp.demo4.utils.FileUtils.getPath;
import static com.example.sp.demo4.utils.FileUtils.getPublicDirectory;
import static com.example.sp.demo4.utils.FileUtils.getStorageUsage;
import static com.example.sp.demo4.utils.FileUtils.removeExtension;
import static com.example.sp.demo4.utils.FileUtils.unzip;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_NAME = "com.example.sp.demo4.EXTRA_NAME";
    private static final String EXTRA_TYPE="com.example.sp.demo4.EXTRA_TYPE";
    private static final String SAVED_DIRECTORY = "com.example.sp.demo4.SAVED_DIRECTORY";
    private static final String SAVED_SELECTION = "com.example.sp.demo4.SAVED_SELECTION";
    public Toolbar toolbar;
    public CheckBox checkBox;
    private TextView title;
    private TextView checkAll;
    private TextView path;
    private ArrayList<File> items;
    private List<File> selects;
    private RecyclerAdapter recyclerAdapter;
    private boolean isNull=false;
    private boolean isCopy;
    private File currentDirectory;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CoordinatorLayout coordinatorLayout;
    private String name;
    private String type;
    private boolean cpmv=false;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initActivityFromIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniAppBarLayout();
        initCoordinatorLayout();
        iniDrawerLayout();
        iniNavigationView();
        iniRecycleView();
        loadIntoRecyclerView();
        invalidateToolbar();
        invalidateTitle();
    }

    @Override
    protected void onResume()
    {
        if (recyclerAdapter != null) recyclerAdapter.refresh();
        super.onResume();
    }

    @Override
    public void onBackPressed() {

        checkBox=(CheckBox)toolbar.findViewById(R.id.check);
        checkAll=(TextView)toolbar.findViewById(R.id.check_all);

        if (drawerLayout.isDrawerOpen(navigationView))
        {
            drawerLayout.closeDrawers();

            return;
        }

        if (recyclerAdapter.flag)
        {
            recyclerAdapter.flag=false;
            recyclerAdapter.clearSelection();
            recyclerAdapter.notifyDataSetChanged();
            checkBox.setVisibility(View.GONE);
            checkAll.setText("");
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
                actionSearch();
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
            case R.id.action_copy:
                actionCopy();
                return true;
            case R.id.action_move:
                actionMove();
                return true;
            case R.id.cancel:
                cancel();
                return true;
            case R.id.done:
                done();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (recyclerAdapter!=null){
            int count=recyclerAdapter.getSelectedItemCount();
            menu.findItem(R.id.action_search).setVisible(count==0&&!recyclerAdapter.flag&&!cpmv&&type==null);
            boolean srch = false;
            menu.findItem(R.id.action_settings).setVisible(count==0&&!recyclerAdapter.flag&&!cpmv&&!srch);
            menu.findItem(R.id.action_create).setVisible(type==null);
            menu.findItem(R.id.action_edit).setVisible(!isNull);
            menu.findItem(R.id.action_sort).setVisible(!isNull);
            menu.findItem(R.id.action_delete).setVisible(count>=1);
            menu.findItem(R.id.action).setVisible(count>=1);
            menu.findItem(R.id.action_rename).setVisible(count==1);
            menu.findItem(R.id.action_copy).setVisible(type==null);
            menu.findItem(R.id.action_move).setVisible(type==null);
            menu.findItem(R.id.cancel).setVisible(cpmv);
            menu.findItem(R.id.done).setVisible(cpmv);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void initActivityFromIntent() {
        name = getIntent().getStringExtra(EXTRA_NAME);
        type = getIntent().getStringExtra(EXTRA_TYPE);
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
        if (type!=null){
            switch (type){
                case "image":
                    recyclerAdapter.addAll(getImageLibrary(context));
                    break;
                case "large_image":
                    recyclerAdapter.addAll(getImageLibrary(context));
                    break;
            }
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

    private void iniDrawerLayout(){
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);

        if (drawerLayout==null) return;

        if(name!=null||type!=null){
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void iniNavigationView(){
        navigationView=(NavigationView)findViewById(R.id.navigation_view);

        if (navigationView==null)return;

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.navigation_image:
                    setType("image");
                    return true;
                case R.id.navigation_largerimage:
                    setType("large_image");
                    return true;
            }
            drawerLayout.closeDrawers();
            switch (item.getItemId()){
                case R.id.navigation_directory_0:
                    setPath(getPublicDirectory(getString(R.string.DCIM)));
                    return true;
                default:
                    return true;
            }
        });

        TextView textView=(TextView)navigationView.getHeaderView(0).findViewById(R.id.header);

        textView.setText(getStorageUsage(this));

        textView.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)));
    }

    private void iniRecycleView(){
        recyclerAdapter=new RecyclerAdapter(this);
        new FileUtils(this);
        recyclerAdapter.setOnItemClickListener(new OnItemClickListener(this));
        recyclerAdapter.setOnSelectionListener(() ->
        {
            invalidateOptionsMenu();

            invalidateTitle();

            invalidateToolbar();
        });
        if (type!=null){
            switch (type){
                case "image":
                    recyclerAdapter.setItemLayout(R.layout.list_item1);
                    recyclerAdapter.setSpanCount(getResources().getInteger(R.integer.span_count1));
                    break;
                case "large_image":
                    recyclerAdapter.setItemLayout(R.layout.list_item1_1);
                    recyclerAdapter.setSpanCount(getResources().getInteger(R.integer.span_count2));
                    break;
            }
        }else {
            recyclerAdapter.setItemLayout(R.layout.list_item0);
            recyclerAdapter.setSpanCount(getResources().getInteger(R.integer.span_count0));
        }


        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.recycle_view);
        if(recyclerView!=null){
            recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST));
            recyclerView.setAdapter(recyclerAdapter);
        }
    }

    private void invalidateTitle(){
        setTitle(null);
        title=(TextView)toolbar.findViewById(R.id.cTitle);
        if(recyclerAdapter.anySelected()){
            int selectedItemCount=recyclerAdapter.getSelectedItemCount();
            title.setText(String.format("%s",selectedItemCount));
        } else if(recyclerAdapter.flag){
            title.setText(R.string.title_select);
        }else if (isCopy&&cpmv){
            title.setText(R.string.copy);
        }else if(!isCopy&&cpmv){
            title.setText(R.string.move);
        }
        else {
            title.setText("");
        }
    }

    private void invalidateToolbar(){

        checkBox=(CheckBox)toolbar.findViewById(R.id.check);
        checkAll=(TextView)toolbar.findViewById(R.id.check_all);
        if(recyclerAdapter.flag){
            items = recyclerAdapter.getItems();
            toolbar.setNavigationIcon(null);
            checkBox.setVisibility(View.VISIBLE);
            checkAll.setText(R.string.all_select);
            checkBox.setOnClickListener(v->{
                if (checkBox.isChecked()) {
                    for (int i = 0; i < items.size(); i++) {
                        recyclerAdapter.selectAll(i);
                    }
                }else {
                    for (int i=0;i<items.size();i++){
                        recyclerAdapter.cancelSelect(i);
                    }
                }
            });
            return;
        }
        else if(!path.getText().toString().equals(getString(R.string.device_store))&&!cpmv&&name==null&&type==null) {
            toolbar.setNavigationIcon(R.drawable.ic_back);
            toolbar.setNavigationOnClickListener(v -> {
                if (!FileUtils.isStorage(currentDirectory)) {
                    setPath(currentDirectory.getParentFile());
                    return;
                }
            });
        }else if (name==null&&type==null){
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setNavigationOnClickListener(v->drawerLayout.openDrawer(navigationView));

        } else if (name!=null||type!=null){
            toolbar.setNavigationIcon(R.drawable.ic_back);

            toolbar.setNavigationOnClickListener(v->finish());
        }
        else {
            toolbar.setNavigationIcon(null);
            return;
        }
    }

    private void actionSearch(){
        InputDialog inputDialog = new InputDialog(this,R.string.action_search ,R.string.action_search )
        {
            @Override
            public void onActionClick(String text)
            {
                setName(text);
            }
        };

        inputDialog.show();
    }

    private void actionEdit(){

        checkBox=(CheckBox)toolbar.findViewById(R.id.check);
        checkAll=(TextView)toolbar.findViewById(R.id.check_all);

        recyclerAdapter.flag=true;
        recyclerAdapter.notifyDataSetChanged();

        title.setText(R.string.title_select);
        checkBox.setVisibility(View.VISIBLE);
        checkAll.setText(R.string.all_select);
        checkBox.setOnClickListener(v->{
            items = recyclerAdapter.getItems();
            if (checkBox.isChecked()) {
                for (int i = 0; i < items.size(); i++) {
                    recyclerAdapter.selectAll(i);
                }
            }else {
                for (int i=0;i<items.size();i++){
                    recyclerAdapter.cancelSelect(i);
                }
            }
        });

        invalidateOptionsMenu();
        invalidateToolbar();
    }

    private void actionCreate(){

        InputDialog inputDialog=new InputDialog(this,R.string.create,R.string.action_create) {
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

    private void actionCreateAndsetPath(){

        InputDialog inputDialog=new InputDialog(this,R.string.create,R.string.action_create) {
            @Override
            public void onActionClick(String text) {
                try {
                    File directory= FileUtils.createDirectory(currentDirectory,text);

                    recyclerAdapter.clearSelection();

                    recyclerAdapter.add(directory);

                    setPath(directory);

                }catch (Exception e){
                    showMessage(e);
                }
            }
        };

        inputDialog.show();
    }

    private void actionDelete(){
        actionDelete(recyclerAdapter.getSelectedItems());
    }

    private void actionDelete(final List<File> files){
        final File sourceDirectory=currentDirectory;

        String message = String.format("%s", files.size())+getString(R.string.delete_message);

        String sMessage=String.format("%s",files.size()+getString(R.string.delete_undo));

        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setTitle(message);

        alert.setNegativeButton(R.string.cancal,null);

        alert.setPositiveButton(R.string.ok,(dialog, which) -> {

            recyclerAdapter.removeAll(files);
            clearCheckbox();
            Snackbar.make(coordinatorLayout,sMessage,Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo,v -> {
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

    private void actionSort(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        int checkedItem = PreferenceUtils.getInteger(this, "pref_sort", 1);

        String sorting[] = {getString(R.string.sort_lastmodified),
                getString(R.string.sort_name), getString(R.string.sort_size)};

        final int[] whichCheck = new int[1];

        final Context context = this;

        builder.setSingleChoiceItems(sorting, checkedItem, (dialog, which) ->
        {
            PreferenceUtils.putInt(context, "pref_sort", which);
            whichCheck[0] =which;
        });

        builder.setNegativeButton(R.string.cancal,null);

        builder.setPositiveButton(R.string.done,(dialog, which) ->{
            which=whichCheck[0];
            recyclerAdapter.update(which);
            dialog.dismiss();
        });

        builder.setTitle(R.string.action_sort);

        builder.show();
    }

    private void actionRename(){
        final List<File> selectedItems=recyclerAdapter.getSelectedItems();

        InputDialog inputDialog=new InputDialog(this,R.string.rename,R.string.action_rename) {
            @Override
            public void onActionClick(String text) {

                clearCheckbox();

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

    private void actionCopy()
    {
        List<File> selectedItems = recyclerAdapter.getSelectedItems();

        relativeLayout=(RelativeLayout)findViewById(R.id.create_bar);

        relativeLayout.setVisibility(View.VISIBLE);

        relativeLayout.setOnClickListener(v -> actionCreateAndsetPath());

        selects=selectedItems;

        clearCheckbox();

        isCopy=true;

        cpmv=true;

        invalidateTitle();
    }

    private void actionMove()
    {
        List<File> selectedItems = recyclerAdapter.getSelectedItems();

        relativeLayout=(RelativeLayout)findViewById(R.id.create_bar);

        relativeLayout.setVisibility(View.VISIBLE);

        relativeLayout.setOnClickListener(v -> actionCreateAndsetPath());

        selects=selectedItems;

        clearCheckbox();

        isCopy=false;

        cpmv=true;

        invalidateTitle();
    }

    private void cancel(){

        relativeLayout=(RelativeLayout)findViewById(R.id.create_bar);

        relativeLayout.setVisibility(View.GONE);

        selects.clear();

        cpmv=false;

        invalidateTitle();

        invalidateOptionsMenu();
    }

    private void done(){

        transferFiles(selects,!isCopy);

        relativeLayout=(RelativeLayout)findViewById(R.id.create_bar);

        relativeLayout.setVisibility(View.GONE);

        cpmv=false;

        invalidateTitle();

        invalidateOptionsMenu();
    }

    private void transferFiles(final List<File> files, final Boolean delete){

        try
        {
            for (File file : files)
            {
                File check=new File(currentDirectory,file.getName());
                if(check.exists()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    View view=View.inflate(this,R.layout.dialog_copy_check,null);
                    builder.setView(view);
                    builder.setTitle(R.string.name_used);
                    AlertDialog dialog=builder.show();
                    TextView exit = (TextView) dialog.findViewById(R.id.exit);
                    TextView replace = (TextView) dialog.findViewById(R.id.replace);
                    TextView rename = (TextView) dialog.findViewById(R.id.rename);
                    TextView used = (TextView) dialog.findViewById(R.id.used);
                    used.setText(getString(R.string.this_name)+String.format("(%s)",check.getName())+getString(R.string.already_used));
                    exit.setOnClickListener(v->{
                        cancel();
                        dialog.dismiss();
                    });
                    replace.setOnClickListener(v->{
                        try {
                            recyclerAdapter.addAll(FileUtils.copyFile(file, currentDirectory));
                            if (delete) FileUtils.deleteFile(file);
                        } catch (Exception e) {
                            showMessage(e);
                        }
                        dialog.dismiss();
                    });
                    rename.setOnClickListener(v->{
                        File directory= null;
                        int i=0;
                        try {
                            while (true){
                                if (new File(currentDirectory,file.getName()+String.format(" (%s)",i)).exists()){
                                    i++;
                                }
                                else {
                                    break;
                                }
                            }
                            directory = FileUtils.createDirectory(currentDirectory,file.getName()+String.format(" (%s)",i));
                        } catch (Exception e) {
                            showMessage(e);
                        }
                        recyclerAdapter.add(directory);
                        dialog.dismiss();
                    });
                }
                else {
                    recyclerAdapter.addAll(FileUtils.copyFile(file, currentDirectory));
                    if (delete) FileUtils.deleteFile(file);
                }
            }
            selects.clear();
        }
        catch (Exception e)
        {
            showMessage(e);
        }
    }

    private void clearCheckbox(){
        recyclerAdapter.clearSelection();
        recyclerAdapter.flag=false;
        checkBox.setVisibility(View.GONE);
        checkAll.setText("");
        recyclerAdapter.notifyDataSetChanged();
    }

    private void showMessage(Exception e)
    {
        showMessage(e.getMessage());
    }

    private void showMessage(String message)
    {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void showMess(String ms){
        Toast.makeText(this,ms,Toast.LENGTH_LONG).show();
    }
    private void setName(String name)
    {
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra(EXTRA_NAME, name);

        startActivity(intent);
    }

    private void setType(String type){
        Intent intent=new Intent(this,MainActivity.class);

        intent.putExtra(EXTRA_TYPE,type);

        startActivity(intent);
    }

    private void setPath(File directory){

        if(!directory.exists()){
            Toast.makeText(this,"directory is not exist",Toast.LENGTH_SHORT).show();
            return;
        }

        currentDirectory=directory;

        String[] files=currentDirectory.list();

        if (files.length==0){
            isNull=true;
        }
        else {
            isNull=false;
        }

        path.setText(currentDirectory.getAbsolutePath());
        String str = path.getText().toString();
        if (str.equals("/storage/emulated/0")){
            path.setText(R.string.device_store);
        }
        else {
            str = str.replaceAll("/storage/emulated/0",getString(R.string.device_store));
            str = str.replaceAll("/"," -> ");
            path.setText(str);
        }
        recyclerAdapter.clear();
        recyclerAdapter.clearSelection();
        recyclerAdapter.addAll(FileUtils.getChildren(directory));
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
            try{

                final File file = recyclerAdapter.get(position);

                if (recyclerAdapter.flag)
                {
                    recyclerAdapter.toggle(position);

                    checkBox.setChecked(false);

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
                    else if (FileUtils.FileType.getFileType(file) == FileUtils.FileType.ZIP)
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
                    }
                    else
                    {
                        try
                        {

                            if (getMimeType(file).startsWith("image")){
                                Intent intent=new Intent(MainActivity.this,ImageActivity.class);

                                intent.putExtra("which",position);

                                intent.setData(Uri.fromFile(file));

                                startActivity(intent);
                            }
                            else {
                                Intent intent = new Intent(Intent.ACTION_VIEW);

                                intent.setDataAndType(Uri.fromFile(file), getMimeType(file));

                                startActivity(intent);
                            }
                        }
                        catch (Exception e)
                        {
                            showMessage(getString(R.string.cannot_open)+String.format("%s", getName(file)));
                        }
                    }
                }
            }catch (Exception e){
                showMess(getString(R.string.double_click));
            }
        }

        @Override
        public boolean onItemLongClick(int position)
        {
            recyclerAdapter.toggle(position);

            recyclerAdapter.flag=true;

            checkBox.setChecked(false);

            recyclerAdapter.notifyDataSetChanged();

            invalidateToolbar();

            return true;
        }
    }

}