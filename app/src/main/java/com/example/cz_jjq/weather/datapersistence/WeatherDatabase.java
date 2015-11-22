package com.example.cz_jjq.weather.datapersistence;

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
        sQliteOpenHelper=new CitySQLiteOpenHelper(1);
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

    private  String DB_NAME="weather.db";
    public  String CITY_TABLE="city";
    private  class CitySQLiteOpenHelper extends SQLiteOpenHelper{

        private  String CREATE_TABLE_CITY="create table "+CITY_TABLE+"("
                +"id integer primary key autoincrement"
                +",parent_code text"
                +",code text"
                +",name text"
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
                db.setTransactionSuccessful();
            }catch (Exception e){

            }finally {
                db.endTransaction();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
