package com.example.sp.demo4.utils;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.example.sp.demo4.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by sp on 2016/7/18.
 */
public class FileUtils {

    public enum FileType
    {
        DIRECTORY, MISC_FILE, AUDIO, IMAGE, VIDEO, DOC, PPT, XLS, PDF, TXT, ZIP;

        public static FileType getFileType(File file)
        {
            if (file.isDirectory())
                return FileType.DIRECTORY;

            String mime = FileUtils.getMimeType(file);

            if (mime == null)
                return FileType.MISC_FILE;

            if (mime.startsWith("audio"))
                return FileType.AUDIO;

            if (mime.startsWith("image"))
                return FileType.IMAGE;

            if (mime.startsWith("video"))
                return FileType.VIDEO;

            if (mime.startsWith("application/ogg"))
                return FileType.AUDIO;

            if (mime.startsWith("application/msword"))
                return FileType.DOC;

            if (mime.startsWith("application/vnd.ms-word"))
                return FileType.DOC;

            if (mime.startsWith("application/vnd.ms-powerpoint"))
                return FileType.PPT;

            if (mime.startsWith("application/vnd.ms-excel"))
                return FileType.XLS;

            if (mime.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml"))
                return FileType.DOC;

            if (mime.startsWith("application/vnd.openxmlformats-officedocument.presentationml"))
                return FileType.PPT;

            if (mime.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml"))
                return FileType.XLS;

            if (mime.startsWith("application/pdf"))
                return FileType.PDF;

            if (mime.startsWith("text"))
                return FileType.TXT;

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

    public static File createDirectory(File path, String name) throws Exception
    {
        File directory = new File(path, name);

        if (directory.mkdirs()) return directory;

        if (directory.exists()) throw new Exception(String.format("%s已经存在", name));

        throw new Exception(String.format("无法创建%s", name));
    }

    public static File deleteFile(File file) throws Exception{
        if (file.isDirectory()){
            for (File child:file.listFiles()) deleteFile(child);
        }

        if (file.delete()) return file;

        throw new Exception(String.format("Error deleting %s",file.getName()));
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

        throw new Exception(String.format("无法重命名 '%s'",file.getName()));
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

    public static int getColorResource(File file)
    {
        switch (FileType.getFileType(file))
        {
            case DIRECTORY:
                return R.color.directory;

            case MISC_FILE:
                return R.color.misc_file;

            case AUDIO:
                return R.color.audio;

            case IMAGE:
                return R.color.image;

            case VIDEO:
                return R.color.video;

            case DOC:
                return R.color.doc;

            case PPT:
                return R.color.ppt;

            case XLS:
                return R.color.xls;

            case PDF:
                return R.color.pdf;

            case TXT:
                return R.color.txt;

            case ZIP:
                return R.color.zip;

            default:
                return 0;
        }
    }

    public static int getImageResource(File file) {
        switch (FileType.getFileType(file)) {
            case DIRECTORY:
                return R.drawable.ic_directory;
            default:
                return 0;
        }
    }
}
