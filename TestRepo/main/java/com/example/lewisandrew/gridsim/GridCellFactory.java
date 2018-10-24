package com.example.lewisandrew.gridsim;

import com.example.lewisandrew.gridsim.Model.GardenerItem;
import com.example.lewisandrew.gridsim.Model.GridCell;
import com.example.lewisandrew.gridsim.Model.Plant;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by lewisandrew on 2/16/2017.
 *
 * Implements the Simple Factory pattern. It should exist for the life of the MainActivity.
 * Needs to get the info about the cell's location to the new cell somehow
 */

public class GridCellFactory {
    private static GridCellFactory mFactoryInstance = null;
    HashMap<Integer, GardenerItem> mGardenerItems = new HashMap<Integer, GardenerItem>();

    public static GridCellFactory getInstance() {
        if(mFactoryInstance == null)
            mFactoryInstance = new GridCellFactory();
        return mFactoryInstance;
    }

    /**
     * Create and return the pointer to a specific grid cell based on the given cell value.
     * If the cell is a gardener item, then it needs to be kept track of by putting it in a list.
     * @param cellVal
     * @param cellIndex
     * @return GridCell
     */
    public GridCell createGridCell(int cellVal, int cellIndex, int c, int r) {
        GridCell cell;

        if(isPlant(cellVal))
            cell = new Plant(cellVal, cellIndex, c, r);

        else if(isGardenerItem(cellVal)) {
            int gardItemKey = cellVal/1000; // remove bottom 3 digits for key
            cell = mGardenerItems.get(gardItemKey);

            if(cell == null) { // if not in our list of gardener items, add it
                cell = new GardenerItem(cellVal, cellIndex, c, r);
                mGardenerItems.put(gardItemKey, (GardenerItem) cell);
            }

            // if location changed, update it in the cell
            if(cell.getRow() != r || cell.getCol() != c)
                ((GardenerItem)cell).newLocation(c, r);
        }

        else
            cell = new GridCell(cellVal, cellIndex, c, r);

        return cell;
    }

    /**
     * Toggle paused items. If items are paused, tell them to unpause, and vice versa.
     */
    public void refreshItems() {
        Iterator it = mGardenerItems.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            ((GardenerItem)pair.getValue()).refreshItem();
        }
    }

    /**
     * Check if the given value corresponds to a plant.
     * @param val
     * @return boolean
     */
    private boolean isPlant(int val) {
        if(val >= 1000 && val < 2000)
            return true;
        else if(val == 2002 || val == 2003 || val == 3000)
            return true;
        else
            return false;
    }

    /**
     * Check if the given value corresponds to a gardener item.
     * @param val
     * @return boolean
     */
    private boolean isGardenerItem(int val) {
        if(val >= 1000000 && val < 3000000)
            return true;
        else if(val >= 10000000 && val < 20000000)
            return true;
        else
            return false;
    }
}
