package com.org.tickes.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.org.tickes.R;
import com.org.tickes.entities.PersonEntity;

import java.util.List;

public class AdapterPerson extends BaseQuickAdapter<PersonEntity, BaseViewHolder> {
    public AdapterPerson(int layoutResId, @Nullable List<PersonEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PersonEntity item) {
        helper.setText(R.id.tv_name,item.getName());
        helper.setText(R.id.tv_seat1,item.getSex());
        helper.addOnClickListener(R.id.tv_delete);
    }
}
