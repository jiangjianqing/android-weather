package com.example.cz_jjq.weather.listener;

import com.example.cz_jjq.weather.model.WeatherInfo;

/**
 * Created by ztxs on 15-11-23.
 */
public interface WeatherInfoListener {
    WeatherInfo info=null;
    void onReceive(WeatherInfo weatherInfo);
    void onError(Exception e);
}
