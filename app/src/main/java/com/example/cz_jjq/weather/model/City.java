package com.example.cz_jjq.weather.model;

import com.example.cz_jjq.baselibrary.util.LogUtil;
import com.example.cz_jjq.weather.datapersistence.WeatherDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cz_jjq on 11/21/15.
 */
public class City {

    private WeatherDatabase cityDatabase= WeatherDatabase.getInstance();

    public City(){
        super();
    }

    public void inputCityString(String data){
        Map<String,String> map=splitData(data);
        String code="";
        String name="";
        for(Map.Entry<String,String> entry:map.entrySet()){
            code=entry.getKey();
            name=entry.getValue();
            insertCity(code, name);
        }
    }
    //----------------------瞬态数据处理部分-------------------

    private Map<String,String> splitData(String data){
        Map<String,String> map=new HashMap<>();
        String[] itemList=data.split(",");
        for(String item:itemList){
            String[] pair=item.split("\\|");//需要这样表示regex的转义
            map.put(pair[0],pair[1]);
        }
        return map;
    }

    private void insertCity(String code,String name){
        //Log.d("City", "insertProvince invoked: code=" + code + ",name=" + name);
        LogUtil.d("City", String.format("LogUtil  insertProvince invoked: code=%s,name=%s", code, name));
    }

    //-------------------以下为持久化数据处理部分------------------


    public static class CityItem{
        public int id;
        public String parent_code;
        public String code;
        public String name;

        public CityItem(String parent_code,String code,String name){
            this.parent_code=parent_code;
            this.code=code;
            this.name=name;
        }

        @Override
        public String toString() {
            return String.format("%s_%s", code, name);
        }

        @Override
        public boolean equals(Object obj) {
            return this.code.equals(((CityItem) obj).code);
        }

    }
}
