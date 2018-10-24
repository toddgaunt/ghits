package com.example.lewisandrew.gridsim.Model;

import com.example.lewisandrew.gridsim.R;

/**
 * Created by lewisandrew on 2/7/2017.
 *
 * This is the base class for anything that appears in GridView. It is responsible
 * for knowing its raw server value and its row and column
 */

public class GridCell extends Object {

    protected int resourceID;
    protected int value;
    protected int index, row, col;
    protected String type, info;

    /**
     * Constructor for a GridCell.
     * @param val
     * @param ind
     * @param c
     * @param r
     */
    public GridCell(int val, int ind, int c, int r) {
        value = val;
        index = ind;
        row = r; col = c;
        info = "(" + col + ", " + row + ")";
        initializeCell();
    }

    /**
     * Method that initializes cell. This will be overridden by Plant and GardenerItem.
     */
    protected void initializeCell() {
        resourceID = R.drawable.blank;

        if(value == 0)
            type = "Empty Cell";
        else if(value > 4000 && value < 10000000)
            type = "Reserved Cell";
        else
            type = "Unknown cell type";
    }

    // returns the appropriate image resource identifier. Default: Empty
    public int getResourceID() { return resourceID; }

    // returns a string description of the object type in the cell
    public String getCellType(){ return type; }

    // returns a string describing other info about the object (such as location)
    public String getCellInfo(){ return info; }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getValue() { return value; }
}

