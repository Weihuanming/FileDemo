package com.example.sp.demo4.utils;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.webkit.MimeTypeMap;

import com.example.sp.demo4.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    private static Context context;

    public FileUtils(Context context){
        FileUtils.context =context;
    }

    public enum FileType
    {
        DIRECTORY, MISC_FILE, IMAGE, ZIP;

        public static FileType getFileType(File file)
        {
            if (file.isDirectory())
                return FileType.DIRECTORY;

            String mime = FileUtils.getMimeType(file);

            if (mime == null)
                return FileType.MISC_FILE;

            if (mime.startsWith("image"))
                return FileType.IMAGE;

            if (mime.startsWith("application/zip"))
                return FileType.ZIP;

            return FileType.MISC_FILE;
        }
    }

    public static String getMimeType(File file)
    {
        //returns the mime type for the given file or null iff there is none

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(file.getName()));
    }

    public static boolean isStorage(File dir)
    {
        return dir == null || dir.equals(getInternalStorage()) || dir.equals(getExternalStorage());
    }

    public static int compareDate(File file1,File file2){
        long lastModified1=file1.lastModified();

        long lastModified2=file2.lastModified();

        return Long.compare(lastModified2,lastModified1);
    }

    public static int compareName(File file1, File file2)
    {
        String name1 = file1.getName();

        String name2 = file2.getName();

        return name1.compareToIgnoreCase(name2);
    }

    public static int compareSize(File file1,File file2){

        long length1=file1.length();

        long length2=file2.length();

        return Long.compare(length2,length1);
    }

    public static File copyFile(File src, File path) throws Exception
    {
        try
        {
            if (src.isDirectory())
            {
                if (src.getPath().equals(path.getPath())) throw new Exception();

                File check=new File(path,src.getName());

                if (check.exists()){
                    for (File file : src.listFiles()) copyFile(file, check);

                    return check;
                }
                else{
                    File directory = createDirectory(path, src.getName());

                    for (File file : src.listFiles()) copyFile(file, directory);

                    return directory;
                }
            }
            else
            {
                File file = new File(path, src.getName());

                FileChannel channel = new FileInputStream(src).getChannel();

                channel.transferTo(0, channel.size(), new FileOutputStream(file).getChannel());

                return file;
            }
        }
        catch (Exception e)
        {
            throw new Exception(context.getString(R.string.cannot_copy));
        }
    }

    public static File createDirectory(File path, String name) throws Exception
    {
        File directory = new File(path, name);

        if (directory.mkdirs()) return directory;

        if (directory.exists()) throw new Exception(String.format("%s", name)+context.getString(R.string.already_exist));

        throw new Exception(context.getString(R.string.cannot_create)+String.format("%s", name));
    }

    public static File deleteFile(File file) throws Exception{
        if (file.isDirectory()){
            for (File child:file.listFiles()) deleteFile(child);
        }

        if (file.delete()) return file;

        throw new Exception(context.getString(R.string.cannot_delete));
    }

    public static File renameFile(File file,String name) throws Exception{
        String extension=getExtension(file.getName());

        if (!extension.isEmpty()){
            name+="."+extension;
        }

        File newFile=new File(file.getParent(),name);

        if (file.renameTo(newFile)){
            return newFile;
        }

        throw new Exception(context.getString(R.string.cannot_rename)+String.format("'%s'",file.getName()));
    }

    public static String getPath(File file)
    {
        //returns the path of the given file or null if the file is null

        return file != null ? file.getPath() : null;
    }

    public static File getInternalStorage()
    {
        //returns the path to the internal storage

        return Environment.getExternalStorageDirectory();
    }

    public static File getExternalStorage()
    {
        //returns the path to the external storage or null if it doesn't exist

        String path = System.getenv("SECONDARY_STORAGE");

        return path != null ? new File(path) : null;
    }

    private static String getExtension(String filename)
    {
        //returns the file extension or an empty string iff there is no extension

        return filename.contains(".") ? filename.substring(filename.lastIndexOf(".") + 1) : "";
    }

    public static String removeExtension(String filename)
    {
        int index = filename.lastIndexOf(".");

        return index != -1 ? filename.substring(0, index) : filename;
    }

    public static String getStorageUsage(Context context){
        File internal=getInternalStorage();

        File external=getExternalStorage();

        long f=internal.getFreeSpace();

        long t=internal.getTotalSpace();

        if (external!=null){
            f+=external.getFreeSpace();

            t+=external.getTotalSpace();
        }

        String use= Formatter.formatFileSize(context,t-f);

        String tot=Formatter.formatFileSize(context,t);

        return String.format("%s  ",tot)+context.getString(R.string.used_of)+String.format("  %s",use);
    }

    public static File getPublicDirectory(String type){
        return Environment.getExternalStoragePublicDirectory(type);
    }

    public static File[] getChildren(File directory)
    {
        if (!directory.canRead()) return null;

        return directory.listFiles(pathname -> pathname.exists() && !pathname.isHidden());
    }

    public static String getName(File file)
    {
        //returns the name of the file hiding extensions of known file types

        switch (FileType.getFileType(file))
        {
            case DIRECTORY:
                return file.getName();

            case MISC_FILE:
                return file.getName();

            default:
                return removeExtension(file.getName());
        }
    }

    public static ArrayList<File> getImageLibrary(Context context){

        ArrayList<File> list=new ArrayList<>();

        Uri uri=MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] datas=new String[]{MediaStore.Images.Media.DATA};

        Cursor cursor=new CursorLoader(context,uri,datas,null,null,null).loadInBackground();

        if (cursor!=null){

            while (cursor.moveToNext()){
                File file=new File(cursor.getString(cursor.getColumnIndex(datas[0])));
                if (file.exists()){
                    list.add(file);
                }
            }
            cursor.close();
        }

        return list;
    }

    public static ArrayList<File> searchFilesName(Context context,String name){
        ArrayList<File> list=new ArrayList<>();
        Uri uri= MediaStore.Files.getContentUri("external");
        String data[]=new String[]{MediaStore.Files.FileColumns.DATA};
        Cursor cursor = new CursorLoader(context, uri, data, null, null, null).loadInBackground();

        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                File file = new File(cursor.getString(cursor.getColumnIndex(data[0])));

                if (file.exists() && file.getName().startsWith(name)) list.add(file);
            }

            cursor.close();
        }

        return list;
    }

    public static File unzip(File zip) throws Exception
    {
        File directory = createDirectory(zip.getParentFile(), removeExtension(zip.getName()));

        FileInputStream fileInputStream = new FileInputStream(zip);

        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        try (ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream))
        {
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                byte[] buffer = new byte[1024];

                File file = new File(directory, zipEntry.getName());

                if (zipEntry.isDirectory())
                {
                    if (!file.mkdirs()) throw new Exception("Error uncompressing");
                }
                else
                {
                    int count;

                    try (FileOutputStream fileOutputStream = new FileOutputStream(file))
                    {
                        while ((count = zipInputStream.read(buffer)) != -1)
                        {
                            fileOutputStream.write(buffer, 0, count);
                        }
                    }
                }
            }
        }

        return directory;
    }
}
