package com.example.cz_jjq.weather.model;

import com.example.cz_jjq.baselibrary.util.LogUtil;
import com.example.cz_jjq.weather.datapersistence.WeatherDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cz_jjq on 11/21/15.
 */
public class CityListContent {

    //private WeatherDatabase cityDatabase= WeatherDatabase.getInstance();


    public static List<CityItem> ITEMS = new ArrayList<CityItem>();

    public static Map<String, CityItem> ITEM_MAP = new HashMap<String, CityItem>();

    static {
        // Add 3 sample items.
/*
        if(ITEMS.size()==0) {
            addItem(new CityItem("","百度", "http://www.baidu.com"));
            addItem(new CityItem("","2", "Item 2"));
            addItem(new CityItem("","3", "Item 3"));
        }*/
    }


    public static void addItem(CityItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.code, item);
        /*ContentValues values=new ContentValues();
        values.put("name",item.name);
        values.put("url",item.url);
        db.beginTransaction();
        try{
            db.insert("website",null,values);
            ITEMS.add(item);
            ITEM_MAP.put(item.name, item);
            db.setTransactionSuccessful();//事务成功
        }catch (Exception e){
            LogUtil.e("WebSiteContent", e.getMessage());
        }finally {
            db.endTransaction();
        }*/
    }

    public static void clear(){
        ITEMS.clear();
        ITEM_MAP.clear();
    }


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
            return name;
            //return String.format("%s_%s", code, name);
        }

        @Override
        public boolean equals(Object obj) {
            return this.code.equals(((CityItem) obj).code);
        }

    }
}
