package com.sunyardraofa.cameraandalbumlibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class Camera {
    public static final int TAKE_PHOTO = 5167;
    public static final int CHOOSE_PHOTO = 7165;
    private static Uri imageUri;
    private static String path ;
    
    public static void takePhoto(Activity activity,String authority){
        File outputImage = new File(activity.getExternalCacheDir(),"sunyardraofaoutput_image.jpg");
        path = outputImage.getPath();
        try {
            if(outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch(IOException e){
            e.printStackTrace();
        }

        if(Build.VERSION.SDK_INT >= 24){
            imageUri = FileProvider.getUriForFile(activity,authority,outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        activity.startActivityForResult(intent,TAKE_PHOTO);
    }
    
    public static String getCameraBmpAtResult(Activity activity){
        return path;
    }
    
    
    public static void chooseFrmoAlbum(Activity activity){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        activity.startActivityForResult(intent,CHOOSE_PHOTO);
    }
    
    public static String getAlbumbmpAtResult(Activity activity,Intent data){
        String path = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            path = handlerImageOnKitkat(activity,data);
        } else {
            Uri uri = data.getData();
            path = getImagePath(activity,uri,null);
            
        }
        return path;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String  handlerImageOnKitkat(Activity activity,Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(activity,uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" +id;
                imagePath = getImagePath(activity,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contenturi = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(activity,contenturi,null);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(activity,uri,null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    private static String getImagePath(Activity activity,Uri uri ,String selection) {
        String path = null;
        Cursor cursor = activity.getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
