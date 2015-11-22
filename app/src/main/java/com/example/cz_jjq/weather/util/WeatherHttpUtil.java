package com.example.cz_jjq.weather.util;

import com.example.cz_jjq.baselibrary.util.HttpCallbackListener;
import com.example.cz_jjq.baselibrary.util.HttpUtil;
import com.example.cz_jjq.baselibrary.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cz_jjq on 11/22/15.
 */
public class WeatherHttpUtil {
    private static WeatherHttpUtil weatherHttpUtil;
    private WeatherHttpUtil(){
        super();
    }
    public synchronized static WeatherHttpUtil getInstance(){
        if(weatherHttpUtil==null){
            weatherHttpUtil=new WeatherHttpUtil();
        }
        return weatherHttpUtil;
    }

    private HttpUtil httpUtil=new HttpUtil();


    public void getAllCity(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                getCityData("");
            }
        }).start();

    }

    private String getCityDataUrl(String parent_code){
        return String.format("http://www.weather.com.cn/data/list3/city%s.xml",parent_code);
    }

    private String getWeatherDataUrl(String weather_code){
        return String.format("http://www.weather.com.cn/data/cityinfo/%s.html",weather_code);
    }

    public void getCityData(final String parent_code){
        HttpCallbackListener listener=new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {

                LogUtil.d("WeatherHttpUtil", response);
                processCityString(parent_code,response);
                //Logger logger = LoggerFactory.getLogger(this.getClass());
                //logger.debug(response);
                //Toast.makeText(MyApplication.,"123123",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Exception e) {
                LogUtil.d("WeatherHttpUtil", e.toString());
            }
        };
        httpUtil.sendSyncHttpRequest(getCityDataUrl(parent_code), listener);//使用同步模式获取数据
    }

    public void getWeatherData(final String weather_code){
        httpUtil.sendAsyncHttpRequest(getWeatherDataUrl(weather_code), new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                LogUtil.d("WeatherHttpUtil",response);
            }

            @Override
            public void onError(Exception e) {
                LogUtil.d("WeatherHttpUtil",e.toString());
            }
        });
    }

    private void processCityString(String parent_code,String cityData){
        Map<String,String> map=splitCityData(cityData);
        String code="";
        String name="";
        for(Map.Entry<String,String> entry:map.entrySet()){
            code=entry.getKey();
            name=entry.getValue();

            //if(name.endsWith(code)){
            if(name.startsWith("101")){
                LogUtil.d("WeatherHttpUtil", String.format("遍历结束,天气指令为:%s",name));
                break;
            }

            getCityData(code);

        }
    }

    private Map<String,String> splitCityData(String data){
        //数据格式为 "01|北京,02|上海,03|天津";
        Map<String,String> map=new HashMap<>();
        String[] itemList=data.split(",");
        for(String item:itemList){
            String[] pair=item.split("\\|");//需要这样表示regex的转义
            map.put(pair[0], pair[1]);
        }
        return map;
    }
}
