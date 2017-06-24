package com.example.bajie.poff.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.bajie.poff.R;
import com.example.bajie.poff.adapter.DataAdapter;
import com.example.bajie.poff.bean.SensorData;
import com.example.bajie.poff.util.ApiStrUtil;
import com.example.bajie.poff.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private NavigationView navigationView;

    private List<SensorData> dataList = new ArrayList<>();
    DataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        toolbar = (Toolbar)findViewById(R.id.toolbar_main);
        fab = (FloatingActionButton)findViewById(R.id.fab_return_top_main);
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipeRefresh_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_main);
        navigationView = (NavigationView)findViewById(R.id.navigationView);

        /**
         * 初始化toolbar，设置它的home按钮
         */
        setSupportActionBar(toolbar);
        ActionBar actionBar =(ActionBar) getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_list);
        }

        navigationView.setCheckedItem(R.id.nav_menu_list_previous_data);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.nav_menu_list_previous_data:
                        drawerLayout.closeDrawers();
                        swipeRefresh.setRefreshing(true);
                        requestAllData(ApiStrUtil.allData);
                        break;
                    case R.id.nav_menu_chart_real_time_data:
                        drawerLayout.closeDrawers();

                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        fab = (FloatingActionButton)findViewById(R.id.fab_return_top_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v,"returning top!",Snackbar.LENGTH_LONG).show();
                recyclerView.smoothScrollToPosition(0);
            }
        });

        /**
         * 初始化网格布局管理器供recycleView使用
         */
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);

        /**
         * 初始化adapter供recycleView使用
         */
        adapter = new DataAdapter(dataList);
        recyclerView.setAdapter(adapter);

        /**
         * 配置刷新控件
         */
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestAllData(ApiStrUtil.allData);
            }
        });

        requestAllData(ApiStrUtil.allData);
    }

    /**
     * 请求所有数据
     */
    private void requestAllData(String url){
        HttpUtil.sendOkhttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("dataJson",responseStr);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showData(handleJsonAllData(responseStr));
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 解析json数据,allData
     */
    private List<SensorData> handleJsonAllData(String allData){
        List<SensorData> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(allData);
            for(int i = 0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                SensorData sensorData = new SensorData();
                sensorData.setDeviceId(object.getInt("deviceId"));
                sensorData.setCollectTime(object.getString("collectTime"));
                sensorData.setData(object.getString("data"));
                list.add(sensorData);
            }
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *在list中展示数据
     */
    private void showData(List<SensorData> list){
        dataList.clear();
        dataList.addAll(list);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "获得"+list.size()+"条数据！", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.chart_statistic:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String dataJson = prefs.getString("dataJson",null);
                if(dataJson == null){
                    Toast.makeText(this, "没有数据，请刷新一下", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(this,DataChartStatistic.class);
                    intent.putExtra("dataJson",dataJson);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
        return true;
    }
}
