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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cz_jjq.baselibrary.activity.BaseActivity;
import com.example.cz_jjq.baselibrary.util.FileUtil;
import com.example.cz_jjq.baselibrary.util.LogUtil;
import com.example.cz_jjq.weather.R;
import com.example.cz_jjq.weather.datapersistence.WeatherDatabase;
import com.example.cz_jjq.weather.model.WeatherInfo;
import com.example.cz_jjq.weather.service.WeatherService;
import com.example.cz_jjq.weather.util.WeatherHttpUtil;

public class WeatherActivity extends BaseActivity {

    private String current_cityid;
    private LinearLayout weatherInfoLayout;
    private TextView publishText;

    private TextView cityNameText;

    /**
     * 用于显示天气描述信息
     */
    private TextView weatherDespText;
    /**
     * 用于显示气温1
     */
    private TextView temp1Text;
    /**
     * 用于显示气温2
     */
    private TextView temp2Text;
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;

    /** AsyncTask 用于获取Weather的范例

    private class DownloadWeatherInfoTask extends AsyncTask<String,Integer,WeatherInfo> implements WeatherInfoListener {

        private WeatherInfo weatherInfo;

        @Override
        protected WeatherInfo doInBackground(String... params) {
            WeatherHttpUtil.getInstance().getWeatherData(params[0],this);
            return weatherInfo;
        }

        @Override
        protected void onPostExecute(WeatherInfo weatherInfo) {
            //super.onPostExecute(weatherInfo);
            showWeatherInfo(weatherInfo);
            Toast.makeText(WeatherActivity.this,String.format("获取天气数据成功：%s",weatherInfo.getTemp1()),Toast.LENGTH_LONG).show();

        }

        @Override
        public void onReceive(WeatherInfo info) {
            this.weatherInfo=info;
        }

        @Override
        public void onError(Exception e) {

        }
    }
     */
    private WeatherService.DownloadBinder downloadBinder;
    private boolean isFromNotification;

    private ServiceConnection weatherServiceConnection =new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder=(WeatherService.DownloadBinder)service;

            if(isFromNotification==false) {
                downloadBinder.downloadWeather(current_cityid, downloadListener);
            }else {
                WeatherInfo weatherInfo=FileUtil.loadObjFromFile(WeatherActivity.this,"current_weatherinfo.dat",WeatherInfo.class);
                showWeatherInfo(weatherInfo);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private WeatherService.DownloadListener downloadListener=new WeatherService.DownloadListener() {
        @Override
        public void downloadWeatherOk(String cityId) {
            String weatherString=WeatherDatabase.getInstance().queryWeatherInfo(cityId);
            if(!TextUtils.isEmpty(weatherString)){
                WeatherInfo weatherInfo=WeatherHttpUtil.getInstance().getWeatherInfo(weatherString);
                showWeatherInfo(weatherInfo);
                if(!isFromNotification){
                    FileUtil.SaveObjToFile(WeatherActivity.this, weatherInfo, "current_weatherinfo.dat");
                    showNotification(weatherInfo);
                }
            }
        }
    };



    public static void startAction(Context context,String cityid){
        Intent intent=new Intent(context,WeatherActivity.class);
        intent.putExtra("cityid", cityid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //refreshWeatherInfo(current_cityid);
                downloadBinder.downloadWeather(current_cityid,downloadListener);
                Snackbar.make(view, "正在刷新天气数据", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
        publishText=(TextView)findViewById(R.id.publish_text);

        cityNameText = (TextView) findViewById(R.id.city_name);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);

        Intent intent=getIntent();
        String cityid=intent.getStringExtra("cityid");
        current_cityid=cityid;

        isFromNotification=intent.getBooleanExtra("fromNotification",false);


        if(isFromNotification==false) {
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            publishText.setText("同步中...");
            publishText.setVisibility(View.VISIBLE);
        }
        //由于改用Service，而其IBinder是异步获取的，所以这里不能执行任何代码
        /**
        if(isFromNotification==true)
            refreshWeatherInfo(cityid);
        else{

            //downloadBinder.downloadWeather(cityid);

            WeatherInfo weatherInfo=FileUtil.loadObjFromFile(WeatherActivity.this,"current_weatherinfo.dat",WeatherInfo.class);
            if(weatherInfo!=null){
                showWeatherInfo(weatherInfo);
            }
        }*/

        //importnent::使用bindService则必须在最后进行初始化，以防止ServiceConnection中使用了未初始化的变量
        Intent serviceIntent=new Intent(WeatherActivity.this,WeatherService.class);
        bindService(serviceIntent, weatherServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        unbindService(weatherServiceConnection);
        super.onDestroy();
    }

    protected void showWeatherInfo(WeatherInfo weatherInfo){

        cityNameText.setText(weatherInfo.getCity());
        weatherDespText.setText(weatherInfo.getWeather());
        temp1Text.setText(weatherInfo.getTemp1());
        temp2Text.setText(weatherInfo.getTemp2());

        currentDateText.setText(weatherInfo.getPdate());

        publishText.setText(String.format("今天 %s 发布", weatherInfo.getPtime()));
        weatherInfoLayout.setVisibility(View.VISIBLE);
    }

    private void showNotification(WeatherInfo weatherInfo){
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        //underlying deprecated
        //Notification notification = new Notification(R.drawable.ic_launcher, "This is ticker text", System.currentTimeMillis());
        //notification.setLatestEventInfo(this, "This is content title","This is content text", null);

        //将intent action放在string resource中
        String action=this.getResources().getString(R.string.intent_action_weatheractivity_start);
        LogUtil.d("WeatherActivity",String.format("启动action  %s",action));
        Intent intent2=new Intent(action);
        intent2.putExtra("cityid",current_cityid);
        intent2.putExtra("fromNotification",true);
        PendingIntent pi=PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
        //PendingIntent.getService(...)//调用Service
        //PendingIntent.getActivities(...)//调用Activitiy
        //PendingIntent.getBroadcast(...)//调用Broadcast
        Notification.Builder builder=new Notification.Builder(this);
        builder.setTicker("收到最新的天气信息")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(String.format("%s   %s",weatherInfo.getCity(),weatherInfo.getWeather()))
                .setContentInfo(weatherInfo.getPdate())
                .setContentText(String.format("温度 %s ~ %s", weatherInfo.getTemp2(), weatherInfo.getTemp1()))
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
