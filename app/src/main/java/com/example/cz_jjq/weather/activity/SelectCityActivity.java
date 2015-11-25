package com.example.cz_jjq.weather.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.cz_jjq.baselibrary.activity.BaseActivity;
import com.example.cz_jjq.baselibrary.util.LogUtil;
import com.example.cz_jjq.weather.R;
import com.example.cz_jjq.weather.model.CityListContent;
import com.example.cz_jjq.weather.service.CityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SelectCityActivity extends BaseActivity {

    public static void startAction(Context context){
        Intent intent=new Intent(context,SelectCityActivity.class);
        //intent.putExtra("param1",param1);
        //intent.putExtra("param2",param2);
        context.startActivity(intent);
    }

    private CityService.DownloadCityBinder downloadCityBinder;

    private CityService.DownloadCityListener downloadCityListener=new CityService.DownloadCityListener() {
        @Override
        public void downloadOk(Map<String, String> data) {
            CityListContent.clear();
            for(Map.Entry<String,String> entry:data.entrySet()){
                CityListContent.addItem(new CityListContent.CityItem("",entry.getKey(),entry.getValue()));
            }
            cityItemArrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void findCityId(String cityId) {
            WeatherActivity.startAction(SelectCityActivity.this,cityId);
            finish();
        }
    };
    public ServiceConnection cityServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadCityBinder=(CityService.DownloadCityBinder)service;

            downloadCity("",true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            downloadCityBinder=null;
        }
    };

    private Stack<String> cityStack=new Stack<>();
    private ArrayAdapter<CityListContent.CityItem> cityItemArrayAdapter;
    private ListView cityListView;

    private void downloadCity(String cityId,boolean pushInStack){
        if(pushInStack){
            cityStack.push(cityId);
        }
        downloadCityBinder.downloadCity(cityId, downloadCityListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        cityItemArrayAdapter=new ArrayAdapter<CityListContent.CityItem>(SelectCityActivity.this
                ,android.R.layout.simple_list_item_1,CityListContent.ITEMS);
        cityListView=(ListView)findViewById(R.id.city_list);
        cityListView.setAdapter(cityItemArrayAdapter);
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityListContent.CityItem city = CityListContent.ITEMS.get(position);
                downloadCity(city.code,true);
            }
        });

        Intent serviceIntent=new Intent(SelectCityActivity.this,CityService.class);
        bindService(serviceIntent, cityServiceConnection, BIND_AUTO_CREATE);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(cityServiceConnection);
    }

    @Override
    public void onBackPressed() {
        if(cityStack.size()>0){
            String cityId=cityStack.pop();
            LogUtil.d("SelectCityActivity", String.format(" cityId=%s", cityId));
            downloadCity(cityId,false);
        }else {
            super.onBackPressed();
        }
    }
}
