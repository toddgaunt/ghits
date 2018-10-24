package com.example.lewisandrew.gridsim.Model;

import com.example.lewisandrew.gridsim.Location;
import com.example.lewisandrew.gridsim.MainActivity;
import com.example.lewisandrew.gridsim.R;

import java.util.ArrayList;

/**
 * Created by lewisandrew on 2/16/2017.
 *
 * This child class of GridCell represents all gardeners, shovels, and carts.
 * The Gardener ID should be included in the info shown on the screen.
 * You can calculate the Gardener ID pretty simply using integer % and / operators
 */

public class GardenerItem extends GridCell {
    private int mGID;
    private int mPausedIndex = -1;
    private ArrayList<Location> mHistoryList = new ArrayList<Location>();

    /**
     * Constructor for a GardenerItem cell.
     * @param val
     * @param ind
     * @param c
     * @param r
     */
    public GardenerItem(int val, int ind, int c, int r) {
        super(val, ind, c, r);
    }

    /**
     * Method that initializes plant cell.
     */
    @Override
    protected void initializeCell() {
        if(value >= 1000000 && value < 2000000) {
            mGID = (value - 1000000) / 1000;
            type = "Gardener";
            info += "\nGardener ID: " + mGID;
            resourceID = R.drawable.gardender_icon;
        }
        else if(value >= 2000000 && value < 3000000) {
            mGID = (value - 2000000) / 1000;
            type = "Shovel";
            info += "\nGardener ID: " + mGID;
            resourceID = R.drawable.shovel_icon;
        }
        else if(value >= 10000000 && value < 20000000) {
            mGID = (value - 10000000) / 10000;
            type = "Cart";
            info += "\nGardener ID: " + mGID;
            resourceID = R.drawable.golfcart_icon;
        }
        else
            type = "Unknown gardener item";
    }

    /**
     * Update location of the gardener item, and add to its history of locations. If the item is
     * currently paused, only add to its history list.
     * @param c
     * @param r
     */
    public void newLocation(int c, int r) {
        mHistoryList.add(new Location(c, r));

        if(MainActivity.PAUSED) // if paused, do not update
            return;

        mPausedIndex++;
        col = c; row = r; index = r * 16 + c;
        String loc = "(" + col + ", " + row + ")";
        info = loc + "\nGardener ID: " + mGID;
    }

    public void refreshItem() {
        // Restore gardener item to latest location if it has ever moved
        if(mHistoryList.size() == 0) return;

        mPausedIndex = mHistoryList.size() - 1;
        Location lastLoc = mHistoryList.get(mPausedIndex);
        int c = lastLoc.getCol(); int r = lastLoc.getRow();
        col = c; row = r; index = r * 16 + c;
        String loc = "(" + col + ", " + row + ")";
        info = loc + "\nGardener ID: " + mGID;
    }

    public int getGID() { return mGID; } // get gardener id of this cell

    /**
     * Return a string of the entire history the item has been, up to the index paused.
     * @return String
     */
    public String getHistory() {
        String history = "History:\n";

        if(mHistoryList.size() == 0) return history;

        for (int i = 0; i <= mPausedIndex; i++) {
            Location loc = mHistoryList.get(i);
            history += loc.getString() + '\n';
        }

        return history;
    }
}
