package com.example.lewisandrew.gridsim;

import java.sql.Timestamp;

/**
 * This class represents a location of a gardener item.
 * Created by lewisandrew on 2/28/2017.
 */

public class Location {
    private int mColumn, mRow;
    private Timestamp mTime;
    private String mText;

    public Location(int c, int r) {
        mColumn = c; mRow = r;
        mTime = new Timestamp(System.currentTimeMillis());
        String loc = "(" + c + ", " + r + ")";
        mText = loc + " [" + mTime + "]";
    }

    public int getCol() { return mColumn; }
    public int getRow() { return mRow; }
    public String getString(){ return mText; }
}
