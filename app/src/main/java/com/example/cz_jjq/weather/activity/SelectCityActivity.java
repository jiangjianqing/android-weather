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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.cz_jjq.baselibrary.activity.BaseActivity;
import com.example.cz_jjq.baselibrary.util.LogUtil;
import com.example.cz_jjq.weather.R;
import com.example.cz_jjq.weather.model.CityListContent;
import com.example.cz_jjq.weather.service.CityService;

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

            downloadCity("");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            downloadCityBinder=null;
        }
    };

    private Stack<String> cityStack=new Stack<>();
    private ArrayAdapter<CityListContent.CityItem> cityItemArrayAdapter;
    private ListView cityListView;

    private void downloadCity(String cityId){
        cityStack.push(cityId);
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

        CityListContent.clear();//将上次查看的数据clear

        cityItemArrayAdapter=new ArrayAdapter<CityListContent.CityItem>(SelectCityActivity.this
                ,android.R.layout.simple_list_item_1,CityListContent.ITEMS);
        cityListView=(ListView)findViewById(R.id.city_list);
        cityListView.setAdapter(cityItemArrayAdapter);
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityListContent.CityItem city = CityListContent.ITEMS.get(position);
                downloadCity(city.code);
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
        cityStack.pop();//将当前城市弹出Stack
        if(cityStack.size()>0){
            String cityId=cityStack.pop();//获取上一个cityid
            LogUtil.d("SelectCityActivity", String.format(" pop from stack cityId=%s", cityId));
            downloadCity(cityId);
        }else {
            super.onBackPressed();
        }
    }
}
