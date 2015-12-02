package com.example.cz_jjq.weather.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.cz_jjq.weather.util.WeatherHttpUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * Created by cz_jjq on 12/2/15.
 */
public class YahooWeatherService extends Service {
    private AsyncHttpClient client=new AsyncHttpClient();

    public interface DownloadListener{
        void downloadOk();
    }

    public class DownloadBinder extends Binder {

        public void downloadWeather(final String woeid,final DownloadListener listener){
            client.get(WeatherHttpUtil.getInstance().getYahooWeatherDataUrl(woeid), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    //LogUtil.d("YahooWeatherService",responseString);
                    if (WeatherHttpUtil.getInstance().getYahooWeatherInfo(responseString))
                    {
                        listener.downloadOk();
                    }
                    //WeatherDatabase.getInstance().insertWeatherInfo(cityId,responseString);
                    //listener.downloadWeatherOk(cityId);
                }
            });

        }
    }

    private DownloadBinder downloadBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(downloadBinder==null)
            downloadBinder=new DownloadBinder();
        return downloadBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
