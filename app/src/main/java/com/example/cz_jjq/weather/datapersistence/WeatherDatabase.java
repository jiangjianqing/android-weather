package com.example.cz_jjq.weather.datapersistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cz_jjq.baselibrary.util.MyApplication;

/**
 * Created by cz_jjq on 11/21/15.
 */
public class WeatherDatabase {

    private static WeatherDatabase weatherDB;

    private SQLiteOpenHelper sQliteOpenHelper;
    private SQLiteDatabase db;
    private WeatherDatabase(){
        sQliteOpenHelper=new CitySQLiteOpenHelper(2);
        db=sQliteOpenHelper.getWritableDatabase();
    }

    /**
     * 获取唯一实例，and ensure thread safe
     * @return
     */
    public synchronized static WeatherDatabase getInstance(){
        if(weatherDB ==null){
            weatherDB =new WeatherDatabase();
        }
        return weatherDB;
    }


    //-------------------------database operate apart-----------------

    /**
     * 清空city数据库
     * @param db
     */
    private void clearCity(SQLiteDatabase db){
        db.execSQL(String.format("delete from %s",DB_NAME));
    }

    public void insertWeatherInfo(String cityId,String weatherInfo){
        db.beginTransaction();
        try{
            db.delete(WEATHERINFO_TABLE, "cityid=?", new String[]{cityId});
            ContentValues values=new ContentValues();
            values.put("cityid",cityId);
            values.put("weatherinfo",weatherInfo);
            db.insert(WEATHERINFO_TABLE,null,values);
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
    }

    public String queryWeatherInfo(String cityId){
        String ret="";
        Cursor cursor =db.query(WEATHERINFO_TABLE,null,"cityid=?",new String[]{cityId},null,null,null);
        if(cursor.moveToFirst()){
            ret=cursor.getString(cursor.getColumnIndex("weatherinfo"));
        }
        cursor.close();
        return ret;
    }

    private  String DB_NAME="weather.db";
    public  String CITY_TABLE="city";
    public String WEATHERINFO_TABLE="weather";
    private  class CitySQLiteOpenHelper extends SQLiteOpenHelper{

        private  String CREATE_TABLE_CITY="create table "+CITY_TABLE+"("
                +"id integer primary key autoincrement"
                +",parent_code text"
                +",code text"
                +",name text"
                +")";

        private String CREATE_TABLE_WEATHERINFO="create table "+WEATHERINFO_TABLE+"("
                +"cityid text primary key"
                +",weatherinfo text"
                +",dt DATETIME default (datetime('now','localtime'))"
                +")";

        private  String CREATE_INDEX_CITY="create index city_parent_code_index on "+CITY_TABLE+"(parent_code)";

        public CitySQLiteOpenHelper(int version) {
            super(MyApplication.getContext(), DB_NAME, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try{
                db.execSQL(CREATE_TABLE_CITY);
                db.execSQL(CREATE_INDEX_CITY);
                db.execSQL(CREATE_TABLE_WEATHERINFO);
                db.setTransactionSuccessful();
            }catch (Exception e){

            }finally {
                db.endTransaction();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch(newVersion){
                case 2:
                    db.execSQL(CREATE_TABLE_WEATHERINFO);
                default:
                    break;
            }
        }
    }
}
