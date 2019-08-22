package com.sunyardraofa.permisiondemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.BaseMenuPresenter;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.sunyardraofa.cameraandalbumlibrary.Camera;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private List<String> contactslist = new ArrayList<>();
    private Button call;
    private ListView contacts_list;
    private NotificationManager notificationManager;
    private Notification notification;
    private Button chat,tuijian;
    private Button take_photo , choose_from_album ;
    private ImageView picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        call = findViewById(R.id.call);
        contacts_list = findViewById(R.id.contacts_list);
        chat = findViewById(R.id.chat);
        tuijian = findViewById(R.id.tuijian);
        take_photo = findViewById(R.id.take_photo);
        choose_from_album = findViewById(R.id.choose_from_album);
        picture = findViewById(R.id.picture);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            String id = "chat";
            String name ="消息";
            int impotrance = NotificationManager.IMPORTANCE_HIGH;
            creatrNotifificationChannel(id,name,impotrance);
            
            id = "subsribe";
            name = "推荐";
            impotrance = NotificationManager.IMPORTANCE_DEFAULT;
            creatrNotifificationChannel(id,name,impotrance);
        }
        
//        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactslist);
//        contacts_list.setAdapter(adapter);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_CONTACTS}, 2);
        } else {
            readContacts();
        }


        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CALL_PHONE}, 1);
                } else {
                    call();
                }
            }
        });
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,0
                ,intent,0);
                Notification notification = new NotificationCompat.Builder(MainActivity.this, "chat")
                        .setContentTitle("收到一条聊天消息")
                        .setContentText("今天中午吃什么？")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build();
                notificationManager.notify(1,notification);
            }
        });

        tuijian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,0
                        ,intent,0);
                Notification notification = new NotificationCompat.Builder(MainActivity.this, "subsribe")
                        .setContentTitle("收到一条订阅消息")
                        .setContentText("震惊！全国人民沸腾了!")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .build();
                notificationManager.notify(2,notification);
            }
        });
        
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.takePhoto(MainActivity.this,"com.sunyardraofa.permisiondemo.fileprovider");
            }
        });
        
        choose_from_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                 != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},3);
                } else {
                    Camera.chooseFrmoAlbum(MainActivity.this);
                }
            }
        });
        
    }

   

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode){
        case Camera.TAKE_PHOTO:
            if(resultCode == RESULT_OK){
              String path = Camera.getCameraBmpAtResult(this);
              displayImage(path);
            }else {
                Toast.makeText(MainActivity.this,"拍照失败",Toast.LENGTH_SHORT).show();
            }
            break;
        case Camera.CHOOSE_PHOTO:
            if(resultCode == RESULT_OK){
                String path = Camera.getAlbumbmpAtResult(MainActivity.this,data);
                displayImage(path);
            }
            break;
            default:
                break;
        }
    }


    private void displayImage(String imagePath) {
        if(imagePath != null){
            int targetW = picture.getWidth();
            int targetH = picture.getHeight();
            BitmapFactory.Options bmOption = new BitmapFactory.Options();
            bmOption.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath,bmOption);
            int photoW = bmOption.outWidth;
            int photoH = bmOption.outHeight;
            
            int scaleFactor = Math.min(photoW/targetW,photoH/targetH);
            bmOption.inJustDecodeBounds = false;
            bmOption.inSampleSize = scaleFactor;
            bmOption.inPurgeable = true;
            
            
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath,bmOption);
            picture.setImageBitmap(bitmap);
        } else {
            Toast.makeText(MainActivity.this,"获取图片失败",Toast.LENGTH_SHORT).show();
        }
    }
    
    private void readContacts() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if(cursor != null) {
                while(cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String num = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contactslist.add(name + "\n" + num);
                }
                adapter.notifyDataSetChanged();
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
    }

    private void call() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:10086"));
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
        case 1:
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                call();
            } else {
                if(!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CALL_PHONE)) {
                    getPeimission();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        case 2:
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readContacts();
            } else {
                if(!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {
                    getPeimission();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        case 3:
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readContacts();
            } else {
                if(!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {
                        Camera.chooseFrmoAlbum(this);
                    } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        default:
            break;
        }
    }

    private void getPeimission() {
        new AlertDialog.Builder(MainActivity.this).setMessage("需要开启权限才能使用此功能").setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //引导用户到设置中去进行设置
                Intent intent = new Intent();
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);


            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create().show();
    }
    
    @TargetApi(Build.VERSION_CODES.O)
    private void creatrNotifificationChannel(String channelId, String channelName, int importance){
        NotificationChannel channel = new NotificationChannel(channelId,channelName,importance);
        notificationManager.createNotificationChannel(channel);
    }
    
}
