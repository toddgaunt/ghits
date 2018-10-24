package com.example.lewisandrew.gridsim;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.lewisandrew.gridsim.Model.GridCell;
import com.example.lewisandrew.gridsim.Model.SimulationGrid;

/**
 * Created by lewisandrew on 2/7/2017.
 */
public class GridAdapter extends BaseAdapter {
    private Context mContext;
    private SimulationGrid mSimGrid;

    public GridAdapter(Context c, SimulationGrid sg) {
        mContext = c;
        mSimGrid = sg;
    }

    public void setSimGrid(SimulationGrid sg) { mSimGrid = sg; }
    public int getCount() { return mSimGrid.size(); }
    public Object getItem(int position) { return mSimGrid.getCell(position); }
    public long getItemId(int position) { return mSimGrid.getCell(position).getResourceID(); }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(50, 50)); // 50x50 pixel
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(mSimGrid.getCell(position).getResourceID());
        return imageView;
    }
}

