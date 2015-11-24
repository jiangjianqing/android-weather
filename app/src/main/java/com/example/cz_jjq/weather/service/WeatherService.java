package com.example.cz_jjq.weather.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.cz_jjq.weather.datapersistence.WeatherDatabase;
import com.example.cz_jjq.weather.util.WeatherHttpUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * Created by cz_jjq on 11/24/15.
 */
public class WeatherService extends Service {

    public interface DownloadListener{
        void downloadWeatherOk(String cityId);
    }

    public class DownloadBinder extends Binder{

        public void downloadWeather(final String cityId,final DownloadListener listener){
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(WeatherHttpUtil.getInstance().getWeatherDataUrl(cityId), new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    WeatherDatabase.getInstance().insertWeatherInfo(cityId,responseString);
                    listener.downloadWeatherOk(cityId);
                }
            });

        }
    }

    private DownloadBinder downloadBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return downloadBinder;
    }

    @Override
    public void onCreate() {
        downloadBinder=new DownloadBinder();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //服务未必需要自定义onStartCommand
        boolean isImmediate=intent.getBooleanExtra("isImmediate",true);
        if (isImmediate){

        }
        return super.onStartCommand(intent, flags, startId);
    }
}
