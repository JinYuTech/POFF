package com.example.bajie.poff.activity;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.bajie.poff.R;
import com.example.bajie.poff.bean.SensorData;
import com.example.bajie.poff.util.ApiStrUtil;
import com.example.bajie.poff.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RealTimeDataActivity extends AppCompatActivity {

    private LineChartView lineChartView;
    private List<PointValue> tempDataPoints = new ArrayList<>();
    private List<PointValue> gasDataPoints = new ArrayList<>();
    private LineChartData mChartData;                   //展示区域的数据
    private Axis axisX;
    private List<Line> lines = new ArrayList<>();
    private int tempCount = 0;
    private int gasCount = 0;
    private int count = 0;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_data);

        initView();
        timer = new Timer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                count++;
                if(count % 2 == 0){
                    requestLatestData(ApiStrUtil.firstTempData);
                }else{
                    requestLatestData(ApiStrUtil.firstGasData);
                }
            }
        }, 1000, 1000);
    }

    public void initView(){
        lineChartView = (LineChartView) findViewById(R.id.line_chart);

        axisX = new Axis()
                .setLineColor(Color.parseColor("#20b2aa"))
                .setTextColor(Color.parseColor("#20b2aa"));

        mChartData = new LineChartData();
        mChartData.setAxisXBottom(axisX);
        lineChartView.setLineChartData(mChartData);

        Viewport port = initViewPort(0, 10);
        lineChartView.setCurrentViewportWithAnimation(port);
        lineChartView.setZoomEnabled(true);//设置是否支持缩放
        lineChartView.setZoomType(ZoomType.HORIZONTAL);
        lineChartView.setInteractive(true);//设置图表是否可以与用户互动,默认true
        lineChartView.setValueTouchEnabled(true);
        //设置是否允许在动画进行中或设置完表格数据后，自动计算viewport的大小。如果禁止，则需要可以手动设置
        lineChartView.setViewportCalculationEnabled(false);
        //设置是否允许图表在父容器中滑动
        lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        //开始以动画的形式更新图表数据
        lineChartView.startDataAnimation();
    }

    /**
     * 请求新最新数据
     */
    public void requestLatestData(String url){
        HttpUtil.sendOkhttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Toast.makeText(RealTimeDataActivity.this, "请求数据失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resopnseStr = response.body().string();
                try {
                    JSONObject obj = new JSONObject(resopnseStr);
                    final SensorData sensorData=new SensorData();
                    sensorData.setDeviceId(obj.getInt("deviceId"));
                    sensorData.setData(obj.getString("data"));
                    sensorData.setCollectTime(obj.getString("collectTime"));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run(){
                            addPointDynamic(sensorData);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addPointDynamic(SensorData sensorData){
        if(sensorData.getDeviceId() == 1){
            PointValue pointValue = new PointValue(tempCount, Float.parseFloat(sensorData.getData()));
            pointValue.setLabel(sensorData.getData()+"℃");//点的标签信息
            tempDataPoints.add(pointValue);//实时添加新的点
            tempCount ++;
        }else {
            PointValue pointValue = new PointValue(gasCount, Float.parseFloat(sensorData.getData()));
            pointValue.setLabel(sensorData.getData()+"ppm");//点的标签信息
            gasDataPoints.add(pointValue);
            gasCount++;
        }
        float x = tempCount >= gasCount ? tempCount : gasCount ;

        //根据新的点的集合画出新的线
        Line lineTemperature = new Line(tempDataPoints);
        Line lineGas = new Line(gasDataPoints);
        lineTemperature.setColor(Color.parseColor("#ffa07a"))
                .setShape(ValueShape.CIRCLE)
                .setCubic(true)//曲线是否平滑，即是曲线还是折线
                .setHasPoints(true)//是否显示点
                .setStrokeWidth(2)
                .setPointRadius(4)
                .setHasLabels(true);

        lineGas.setColor(Color.parseColor("#90ee90"))
                .setShape(ValueShape.CIRCLE)
                .setCubic(true)//曲线是否平滑，即是曲线还是折线
                .setHasPoints(true)//是否显示点
                .setStrokeWidth(2)
                .setPointRadius(4)
                .setHasLabels(true);

        lines.clear();
        lines.add(lineTemperature);
        lines.add(lineGas);

        mChartData = new LineChartData(lines);
        mChartData.setAxisXBottom(axisX);
        mChartData.setValueLabelBackgroundEnabled(false);//下面两行代码的前提
        mChartData.setValueLabelBackgroundColor(Color.TRANSPARENT);//设置点的标签的背景
        mChartData.setValueLabelsTextColor(Color.parseColor("#20b2aa"));//设置标签的字体颜色
        lineChartView.setLineChartData(mChartData);

        //根据点的横坐实时变幻坐标的视图范围
        Viewport port;
        if (x > 10) {
            port = initViewPort(x-9, x+1);
        } else {
            port = initViewPort(0, 10);
        }
        lineChartView.setCurrentViewport(port);//当前窗口

        Viewport maPort = initMaxViewPort(x);
        //设置最大化的viewport。注意，该方法应在设置完chartview的数据后再调用
        lineChartView.setMaximumViewport(maPort);
    }


    /**
     * 当前显示区域
     */
    private Viewport initViewPort(float left, float right) {
        Viewport port = new Viewport();
        port.top = 100;
        port.bottom = 10;
        port.left = left;
        port.right = right;
        return port;
    }

    /**
     * 最大显示区域
     */
    private Viewport initMaxViewPort(float right) {
        Viewport port = new Viewport();
        port.top = 100;
        port.bottom = 10;
        port.left = 0;
        port.right = right + 10;
        return port;
    }
}
