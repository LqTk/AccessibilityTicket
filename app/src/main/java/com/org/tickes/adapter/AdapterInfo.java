package com.org.tickes.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.org.tickes.R;
import com.org.tickes.entities.PersonEntity;
import com.org.tickes.entities.TrainEntity;

import java.util.List;

public class AdapterInfo extends BaseQuickAdapter<TrainEntity, BaseViewHolder> {
    public AdapterInfo(int layoutResId, @Nullable List<TrainEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, TrainEntity item) {
        helper.setText(R.id.tv_name,item.getTrainNumber());
        List<String> seatNameList = item.getSeatNameList();
        for (int i=0;i<seatNameList.size();i++){
            String seatName = seatNameList.get(i);
            if (i==0){
                helper.setText(R.id.tv_seat1,seatName);
            }
            if (i==1){
                helper.setText(R.id.tv_seat2,seatName);
            }
            if (i==2){
                helper.setText(R.id.tv_seat3,seatName);
            }
            if (i==3){
                helper.setText(R.id.tv_seat4,seatName);
            }
        }
        helper.addOnClickListener(R.id.tv_delete);
    }
}
