package com.example.cz_jjq.weather.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cz_jjq on 12/2/15.
 */
public class YahooWeatherContent implements Serializable {

    public static List<YahooWeatherItem> ITEMS=new ArrayList<>();

    public static void addItem(YahooWeatherItem item){
        ITEMS.add(item);
    }

    public static void clear(){
        ITEMS.clear();
    }

    public static class YahooWeatherItem{
        public String code;
        public String date;
        public String day;
        public String high;
        public String low;
        public String text;

        public YahooWeatherItem(String code,String date,String day,String high,String low,String text){
            this.code=code;
            this.date=date;
            this.day=day;
            this.high=high;
            this.low=low;
            this.text=text;
        }

        @Override
        public String toString() {
            return String.format("%s,%s,%s \ntemp-range:%s~%s",this.date,this.day,this.text,this.high,this.low);
        }

        @Override
        public boolean equals(Object o) {
            return this.date.equals(o);
        }
    }
}
