package com.example.cz_jjq.weather.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cz_jjq.baselibrary.activity.BaseActivity;
import com.example.cz_jjq.baselibrary.util.FileUtil;
import com.example.cz_jjq.baselibrary.util.LogUtil;
import com.example.cz_jjq.weather.R;
import com.example.cz_jjq.weather.listener.WeatherInfoListener;
import com.example.cz_jjq.weather.model.WeatherInfo;
import com.example.cz_jjq.weather.util.WeatherHttpUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

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

    public static void startAction(Context context,String cityid){
        Intent intent=new Intent(context,WeatherActivity.class);
        intent.putExtra("cityid",cityid);
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
                refreshWeatherInfo(current_cityid);
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

        boolean isFromNotification=intent.getBooleanExtra("fromNotification",false);
        if(isFromNotification==false)
            refreshWeatherInfo(cityid);
        else{
            WeatherInfo weatherInfo=FileUtil.loadObjFromFile(WeatherActivity.this,"current_weatherinfo.dat",WeatherInfo.class);
            if(weatherInfo!=null){
                showWeatherInfo(weatherInfo);
            }
        }
    }

    private void refreshWeatherInfo(String cityid){
        weatherInfoLayout.setVisibility(View.INVISIBLE);

        publishText.setText("同步中...");
        publishText.setVisibility(View.VISIBLE);

        //new DownloadWeatherInfoTask().execute(cityid);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WeatherHttpUtil.getInstance().getWeatherDataUrl(cityid), new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                WeatherInfo weatherInfo=WeatherHttpUtil.getInstance().getWeatherInfo(responseString);
                if (weatherInfo!=null) {
                    FileUtil.SaveObjToFile(WeatherActivity.this,weatherInfo,"current_weatherinfo.dat");
                    showWeatherInfo(weatherInfo);
                    showNotification(weatherInfo);
                }
            }
        });
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
