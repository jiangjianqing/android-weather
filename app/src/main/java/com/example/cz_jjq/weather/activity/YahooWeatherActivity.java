package com.example.cz_jjq.weather.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.cz_jjq.baselibrary.util.FileUtil;
import com.example.cz_jjq.weather.R;
import com.example.cz_jjq.weather.model.YahooWeatherContent;
import com.example.cz_jjq.weather.service.YahooWeatherService;

import java.util.List;

public class YahooWeatherActivity extends AppCompatActivity {

    private YahooWeatherContent weatherContent;
    private YahooWeatherService.DownloadBinder downloadBinder;
    private ServiceConnection serviceConnection=new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder=(YahooWeatherService.DownloadBinder)service;


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            downloadBinder=null;
        }
    };

    private YahooWeatherService.DownloadListener downloadListener=new YahooWeatherService.DownloadListener() {
        @Override
        public void downloadOk(List<YahooWeatherContent.YahooWeatherItem> list) {
            weatherContent.setList(list);
            weatherItemArrayAdapter.notifyDataSetChanged();
            FileUtil.saveObjToFile(YahooWeatherActivity.this, weatherContent, "yahoo_weather.obj");
            showNotification(weatherContent.ITEMS.get(0));
        }
    };

    public static void startAction(Context context,String woeid){
        Intent intent=new Intent(context,YahooWeatherService.class);
        intent.putExtra("woeid",woeid);
        context.startActivity(intent);
    }

    private ListView weatherListView;
    private ArrayAdapter<YahooWeatherContent.YahooWeatherItem> weatherItemArrayAdapter;
    private String current_woeid;
    private boolean isFromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yahoo_weather);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //立即下载天气
                downloadBinder.downloadWeather("2137085", downloadListener);
                Snackbar.make(view, "开始下载天气数据", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

        Intent activityIntent=getIntent();
        current_woeid=activityIntent.getStringExtra("woeid");
        isFromNotification=activityIntent.getBooleanExtra("fromNotification", false);

        //如果是从Notification入口过来，则从文件加载
        if (isFromNotification) {
            weatherContent = FileUtil.loadObjFromFile(this, "yahoo_weather.obj", YahooWeatherContent.class);
        }
        if(weatherContent==null)
            weatherContent=new YahooWeatherContent();


        weatherListView=(ListView)findViewById(R.id.weather_list);
        weatherItemArrayAdapter=new ArrayAdapter<YahooWeatherContent.YahooWeatherItem>(this
                ,android.R.layout.simple_list_item_1,weatherContent.ITEMS);
        weatherListView.setAdapter(weatherItemArrayAdapter);


        Intent serviceIntent=new Intent(this,YahooWeatherService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);


    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    private void showNotification(YahooWeatherContent.YahooWeatherItem weatherItem){
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        //underlying deprecated
        //Notification notification = new Notification(R.drawable.ic_launcher, "This is ticker text", System.currentTimeMillis());
        //notification.setLatestEventInfo(this, "This is content title","This is content text", null);

        //将intent action放在string resource中
        String action=this.getResources().getString(R.string.intent_action_yahoo_weatheractivity_start);
        //LogUtil.d("YahooWeatherActivity", String.format("启动action  %s", action));
        //Intent intent2=new Intent(action);
        Intent intent2=new Intent(this,YahooWeatherActivity.class);
        intent2.putExtra("woeid",current_woeid);
        intent2.putExtra("fromNotification",true);
        PendingIntent pi=PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
        //PendingIntent.getService(...)//调用Service
        //PendingIntent.getActivities(...)//调用Activitiy
        //PendingIntent.getBroadcast(...)//调用Broadcast
        Notification.Builder builder=new Notification.Builder(this);
        builder.setTicker("收到最新的天气信息")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(String.format("%s   %s","常州",weatherItem.text))
                .setContentInfo(String.format("%s,%s", weatherItem.date, weatherItem.day))
                .setContentText(String.format("温度 %s ~ %s", weatherItem.high, weatherItem.low))
                .setContentIntent(pi)
                .setWhen(System.currentTimeMillis() + 2000);

        //trigger notification with sound
        //Uri soundUri = Uri.fromFile(new File("/system/media/audio/ringtones/Basic_tone.ogg"));
        //builder.setSound(soundUri);

        //trigger notification with vibrate
        long[] vibrates={0,1000,1000,1000};
        builder.setVibrate(vibrates);

        //determinate all effects via system config
        //builder.setDefaults(Notification.DEFAULT_ALL);

        //argb用于控制 LED 灯的颜色,一般有红绿蓝三种颜色可选
        //OnMS 用于指定 LED 灯亮起的时长,以毫秒为单位
        //OffMS用于指定 LED 灯暗去的时长,以毫秒为单位
        builder.setLights(Color.GREEN,1000,500);

        Notification notification = builder.build();
        int notificationId=1;
        notificationManager.notify(notificationId, notification);
        //notification需要在调用cancel后才能消失
        //notificationManager.cancel(notificationId);
    }
}
