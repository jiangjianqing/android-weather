package com.example.cz_jjq.weather.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.cz_jjq.weather.datapersistence.WeatherDatabase;
import com.example.cz_jjq.weather.model.CityListContent;
import com.example.cz_jjq.weather.util.WeatherHttpUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.Iterator;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ztxs on 15-11-25.
 */
public class CityService extends Service {
    public interface DownloadCityListener{
        void downloadOk(Map<String,String> data);
        void findCityId(String cityId);
    }

    public class DownloadCityBinder extends Binder{
        public void downloadCity(String code,final DownloadCityListener listener){
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(WeatherHttpUtil.getInstance().getCityDataUrl(code), new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Map<String,String> map=WeatherHttpUtil.getInstance().splitCityData(responseString);
                    if(WeatherHttpUtil.getInstance().isCityIdData(map)){
                        //这里是cityid流程
                        Iterator entries = map.entrySet().iterator();
                        Map.Entry<String,String> entry = (Map.Entry) entries.next();
                        listener.findCityId(entry.getValue());
                    }else{
                        /*
                        CityListContent.clear();
                        for (Map.Entry<String,String> item:map.entrySet()){
                            CityListContent.CityItem city=new CityListContent.CityItem("",item.getKey(),item.getValue());
                        }*/
                        listener.downloadOk(map);
                    }


                    //WeatherDatabase.getInstance().insertWeatherInfo(cityId,responseString);
                    //listener.downloadWeatherOk(cityId);
                }
            });

        }
    }

    private DownloadCityBinder downloadCityBinder=new DownloadCityBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return downloadCityBinder;
    }
}
