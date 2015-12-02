package com.example.cz_jjq.weather.util;

import android.util.Xml;

import com.example.cz_jjq.baselibrary.util.HttpCallbackListener;
import com.example.cz_jjq.baselibrary.util.HttpUtil;
import com.example.cz_jjq.baselibrary.util.LogUtil;
import com.example.cz_jjq.weather.listener.WeatherInfoListener;
import com.example.cz_jjq.weather.model.WeatherInfo;
import com.example.cz_jjq.weather.model.YahooWeatherContent;
import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public String getCityDataUrl(String parent_code){
        return String.format("http://www.weather.com.cn/data/list3/city%s.xml",parent_code);
    }

    public String getWeatherDataUrl(String cityid){
        return String.format("http://www.weather.com.cn/data/cityinfo/%s.html",cityid);
    }

    public String getYahooWeatherDataUrl(String woeid){
        //常州.woeid=2137085
        return String.format("https://query.yahooapis.com/v1/public/yql?q=select * from weather.forecast where woeid=%s and u='c'&format=xml",woeid);

        //根据经纬度查询的地址
        //http://query.yahooapis.com/v1/public/yql?q=select * from weather.forecast where woeid in (select woeid from geo.placefinder where text='22.50508166666666666667,113.92853333333333333333' and gflags = 'R') and u='c'&format=xml&diagnostics=true&callback=
    }

    public String getYahooWoeidDataUrl(String cityname){
        return String.format("https://query.yahooapis.com/v1/public/yql?q=select * from geo.placefinder where text='%s'&format=xml",cityname);
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

    public WeatherInfo getWeatherInfo(String responseString){
        WeatherInfo weatherInfo=null;
        Pattern pattern = Pattern.compile("^\\{\"weatherinfo\":(.*)\\}$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(responseString);
        if (matcher.find()) {
            LogUtil.d("WeatherHttpUtil", matcher.group(1));
            String json = matcher.group(1);
            Gson gson = new Gson();
            weatherInfo = gson.fromJson(json, WeatherInfo.class);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            weatherInfo.setPdate(format.format(new Date()));
            LogUtil.d("WeatherHttpUtil", String.format("weather.city=%s,weather.cityid=%s,temp1=%s,temp2=%s"
                    , weatherInfo.getCity()
                    , weatherInfo.getCityid()
                    , weatherInfo.getTemp1()
                    , weatherInfo.getTemp2()
            ));
        }
        return weatherInfo;
    }

    public void getWeatherData(final String cityid,final WeatherInfoListener weatherInfoListener){
        httpUtil.sendSyncHttpRequest(getWeatherDataUrl(cityid), new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                LogUtil.d("WeatherHttpUtil", response);
                weatherInfoListener.onReceive(getWeatherInfo(response));
            }

            @Override
            public void onError(Exception e) {
                LogUtil.d("WeatherHttpUtil", e.toString());
                weatherInfoListener.onError(e);
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

    public Map<String,String> splitCityData(String data){
        //数据格式为 "01|北京,02|上海,03|天津";
        Map<String,String> map=new HashMap<>();
        String[] itemList=data.split(",");
        for(String item:itemList){
            String[] pair=item.split("\\|");//需要这样表示regex的转义
            map.put(pair[0], pair[1]);
        }
        return map;
    }

    public boolean isCityIdData(Map<String,String> map){
        boolean ret=false;
        if (map!=null&&map.size()==1){
            //如果name全是数字，则认为是cityid数据
            for(Map.Entry<String,String> entry:map.entrySet()){
                Pattern pattern=Pattern.compile("[0-9]+");
                if(pattern.matcher(entry.getValue()).matches())
                    ret=true;
            }
        }
        return ret;
    }

    public List<YahooWeatherContent.YahooWeatherItem> getYahooWeatherInfo(String xml){
        List<YahooWeatherContent.YahooWeatherItem> list=new ArrayList<>();
        XmlPullParser parser = Xml.newPullParser();//得到Pull解析器
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        try {
            parser.setInput(stream, "UTF-8");

            int eventType=parser.getEventType();
            while(eventType!=XmlPullParser.END_DOCUMENT){
                String tagName=parser.getName();
                switch(eventType){
                    case XmlPullParser.START_DOCUMENT:
                        //books = new ArrayList<Book>();
                        break;
                    case XmlPullParser.START_TAG:
                        if (tagName.equals("forecast")) {
                            //Integer code=Integer.parseInt(parser.getAttributeValue(null,"code"));
                            String code=parser.getAttributeValue(null,"code");
                            String date=parser.getAttributeValue(null,"date");
                            String day=parser.getAttributeValue(null,"day");
                            String high=parser.getAttributeValue(null,"high");
                            String low=parser.getAttributeValue(null,"low");
                            String text=parser.getAttributeValue(null, "text");
                            list.add(new YahooWeatherContent.YahooWeatherItem(code,date,day,high,low,text));
                            //book = new Book();
                        } else if (tagName.equals("id")) {
                            //eventType = parser.next();
                            //book.setId(Integer.parseInt(parser.getText()));
                        } else if (tagName.equals("name")) {
                            //eventType = parser.next();
                            //book.setName(parser.getText());
                        } else if (tagName.equals("price")) {
                            //eventType = parser.next();
                            //book.setPrice(Float.parseFloat(parser.getText()));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (tagName.equals("book")) {
                            //books.add(book);
                            //book = null;
                        }
                        break;
                }
                eventType=parser.next();
            }
        } catch (XmlPullParserException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return list;
    }
}
