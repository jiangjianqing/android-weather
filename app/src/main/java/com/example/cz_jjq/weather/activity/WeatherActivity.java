package com.example.cz_jjq.weather.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.example.cz_jjq.baselibrary.activity.BaseActivity;
import com.example.cz_jjq.weather.R;
import com.example.cz_jjq.weather.listener.WeatherInfoListener;
import com.example.cz_jjq.weather.model.WeatherInfo;
import com.example.cz_jjq.weather.util.WeatherHttpUtil;

public class WeatherActivity extends BaseActivity {

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Intent intent=getIntent();
        String cityid=intent.getStringExtra("cityid");
        new DownloadWeatherInfoTask().execute(cityid);
    }

}
