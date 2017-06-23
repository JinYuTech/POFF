package com.example.bajie.poff.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bajie.poff.R;
import com.example.bajie.poff.bean.SensorData;

import java.util.List;

/**
 * Created by bajie on 2017/6/24.
 */

public class DataAdapter extends RecyclerView.Adapter< DataAdapter.ViewHolder> {
    private Context mContext;
    private List<SensorData> mDataList;

    public DataAdapter(List<SensorData> dataList){
        mDataList = dataList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView texCollectTime;
        TextView tevData;

        public ViewHolder(View view){
            super(view);
            cardView = (CardView) view;
            texCollectTime = (TextView) view.findViewById(R.id.list_item_data_collectTime);
            tevData = (TextView)view.findViewById(R.id.list_item_data);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_data_item,parent,false);
        ViewHolder holder=new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "请点击右上角！", Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
            SensorData sensorData = mDataList.get(position);
            if(sensorData.getDeviceId() == 1){
                holder.tevData.setText("温度："+sensorData.getData());
            }else{
                holder.tevData.setText("气体："+sensorData.getData());
            }
            holder.texCollectTime.setText("采集时间："+sensorData.getCollectTime());
        }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

}
