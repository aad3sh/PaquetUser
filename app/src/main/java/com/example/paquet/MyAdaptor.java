package com.example.paquet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class MyAdaptor extends BaseAdapter {
    private final ArrayList mData;

    public MyAdaptor(Map<String, Object> map) {
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, Object> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_adaptor_item, parent, false);
        } else {
            result = convertView;
        }

        Map.Entry<String, Object> item = getItem(position);

        // TODO replace findViewById by ViewHolder
        ((TextView) result.findViewById(R.id.ordername)).setText(item.getKey());
        ((TextView) result.findViewById(R.id.orderquantity)).setText(item.getValue().toString());

        return result;
    }
}

