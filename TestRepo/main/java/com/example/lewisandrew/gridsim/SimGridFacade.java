package com.example.lewisandrew.gridsim;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.lewisandrew.gridsim.Model.GardenerItem;
import com.example.lewisandrew.gridsim.Model.GridCell;
import com.example.lewisandrew.gridsim.Model.SimulationGrid;

import org.json.JSONArray;
import org.json.JSONException;

import de.greenrobot.event.EventBus;

/**
 * Created by lewisandrew on 2/15/2017.
 *
 * Hide all the details of using the SimGrid, GridAdapter, the GridView, TextView, and Poller.
 * Keeps track of the last pressed cell. Subscribes to event bus in order to get JSON array from
 * the Poller, then updates the Simulation Grid with the JSON Array.
 */

public class SimGridFacade {
    private SimulationGrid mSimGrid = new SimulationGrid(16, 16); // 16 by 16 grid
    private GridView mGridView;
    private TextView mTextView;
    private GridAdapter mGridAdapter;
    private GridCell mCellPressed;
    public GridCell initialCellPressed;
    private int mIndexPressed = -1;

    private static SimGridFacade mInstance = null;

    public static SimGridFacade getInstance(Context c, GridView gv, TextView tv) {
        if(mInstance == null)
            mInstance = new SimGridFacade(c, gv, tv);
        else
            mInstance.updateContext(c, gv, tv);
        return mInstance;
    }

    public static SimGridFacade getInstance() { return mInstance; }

    public SimGridFacade(Context c, GridView gv, TextView tv) {
        this.updateContext(c, gv, tv);
        EventBus.getDefault().register(this);
    }

    /**
     * When Poller posts event, this will be called. This method will get the JSON array stored in
     * the event object, then update the sim grid with it.
     * @param event
     */
    public void onEvent(GridUpdateEvent event){
        mSimGrid.updateArray(event.getJSONArray());
        mGridAdapter.notifyDataSetChanged();

        if(mIndexPressed == -1) // Do not update the text view if nothing has been pressed
            return;

        // Update text view with index of last pressed cell
        this.updateTVandLP();
    }

    public String getItemSelectedHistory() {
        if(mIndexPressed == -1)
            return "Nothing has been pressed.";
        else if(mCellPressed instanceof GardenerItem) {
            GardenerItem item = (GardenerItem)mCellPressed;
            String type = item.getCellType();
            String info = item.getCellInfo() + '\n';
            String history = item.getHistory();
            return type + ' ' + info + history;
        }
        else
            return "Not a gardener item.";
    }

    public void updateTVandLP() {
        if (mIndexPressed == -1)
            return;
        GridCell cell = mSimGrid.getCell(mIndexPressed);
        mTextView.setText("Selected " + cell.getCellType() + "\n" + "Location: " +
                cell.getCellInfo());
        mCellPressed = cell;
    }

    public void pauseGrid() { mSimGrid.pauseGrid(); }

    public void updateContext(Context c, GridView gv, TextView tv) {
        mGridView = gv;
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                mIndexPressed = position;
                GridCell cell = mSimGrid.getCell(position);
                mCellPressed = cell; initialCellPressed = cell;
                mTextView.setText("Selected " + cell.getCellType() + "\n" + "Location: " +
                        cell.getCellInfo());
            }
        });

        mTextView = tv; mTextView.setText("");
        tv.bringToFront();
        mGridAdapter = new GridAdapter(c, mSimGrid);
        mGridView.setAdapter(mGridAdapter);

        this.updateTVandLP();
    }
}
