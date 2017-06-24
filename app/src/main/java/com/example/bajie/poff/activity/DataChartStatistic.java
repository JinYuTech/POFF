package com.example.bajie.poff.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.bajie.poff.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

public class DataChartStatistic extends AppCompatActivity {

    private LineChartView lineChartView;
    private PreviewLineChartView previewLineChartView;
    private List<PointValue> tempDataPoints = new ArrayList<>();
    private List<PointValue> gasDataPoints = new ArrayList<>();
    private LineChartData mChartData;                   //展示区域的数据
    private LineChartData mPreChartData;                //预览区域的数据
    private Axis axisY, axisX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_chart_statistic);

        initView();
        initData();
        initListener();

    }

    /**
     * 初始化视图
     */
    public void initView(){
        lineChartView = (LineChartView)findViewById(R.id.lcv_main);
        previewLineChartView = (PreviewLineChartView)findViewById(R.id.plcv_main);
    }

    /**
     * 初始化数据
     */
    public void initData(){

        handleJsonStr();

        List<Line> lines = new ArrayList<>();
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

        lines.add(lineTemperature);
        lines.add(lineGas);

        mChartData = new LineChartData(lines);
        mChartData.setValueLabelBackgroundEnabled(false);//下面两行代码的前提
        mChartData.setValueLabelBackgroundColor(Color.TRANSPARENT);//设置点的标签的背景
        mChartData.setValueLabelsTextColor(Color.parseColor("#ee82ee"));//设置标签的字体颜色
        //禁用详细图的缩放和滑动效果，这部分功能仅仅取决于预览区
        lineChartView.setLineChartData(mChartData);
        lineChartView.setZoomEnabled(false);
        lineChartView.setScrollEnabled(false);


        //将相同数据也设置到预览区
        mPreChartData = new LineChartData(mChartData);
        mPreChartData.getLines()
                .get(0)
                .setColor(ChartUtils.DEFAULT_DARKEN_COLOR)//更改颜色
                .setHasPoints(false)  //太密了，不画点
                .setFilled(false)
                .setStrokeWidth(1);
        mPreChartData.getLines()
                .get(1)
                .setColor(ChartUtils.DEFAULT_DARKEN_COLOR)
                .setHasPoints(false)
                .setFilled(false)
                .setStrokeWidth(1);

        previewLineChartView.setLineChartData(mPreChartData);
        previewLineChartView.setPreviewColor(Color.parseColor("#20b2aa"));//设置预选框颜色


        preViewX();
    }

    /**
     * 解析json数据,根据deviceId分别放入tempDataPoints和gasDataPoints
     */
    public void handleJsonStr(){//object.getString("collectTime")
        String jsonStr = getDataJson();
        tempDataPoints = new ArrayList<>();
        gasDataPoints = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            int tempCount = 0;
            int gasCount = 0;
            for(int i = 0;i<jsonArray.length();i++){
                JSONObject object = jsonArray.getJSONObject(i);
                if(object.getInt("deviceId") == 1){
                    PointValue point = new PointValue(tempCount,Float.parseFloat(object.getString("data")));
                    point.setLabel(object.getString("data")+"℃");
                    tempDataPoints.add(point);
                    tempCount ++;
                }else{
                    PointValue point = new PointValue(gasCount,Float.parseFloat(object.getString("data")));
                    point.setLabel(object.getString("data")+"ppm");
                    gasDataPoints.add(point);
                    gasCount ++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *从Intent获取json字符串
     */
    public String getDataJson(){
        Intent intent = getIntent();
        String dataJson = intent.getStringExtra("dataJson");
        return dataJson;
    }

    /**
     * 设置沿X轴方向预览
     */
    public void preViewX(){
        Viewport viewPort = new Viewport(lineChartView.getMaximumViewport());
        int maxCount = tempDataPoints.size() > gasDataPoints.size() ? tempDataPoints.size() : gasDataPoints.size();
        float N = (float)maxCount / 8;
        float dx = viewPort.width() * (N-1) / (2*N);
        viewPort.inset(dx,0);
        previewLineChartView.setCurrentViewportWithAnimation(viewPort);
        previewLineChartView.setZoomType(ZoomType.HORIZONTAL);//水平缩放
    }

    /**
     * 绑定监听器
     */
    public void initListener(){
        previewLineChartView.setViewportChangeListener(new ViewportListener());
    }

    private class ViewportListener implements ViewportChangeListener{
        @Override
        public void onViewportChanged(Viewport viewport) {
            lineChartView.setCurrentViewport(viewport);
            //切记不要使用动画，预览图时不需要动画跟流畅
        }
    }
}
