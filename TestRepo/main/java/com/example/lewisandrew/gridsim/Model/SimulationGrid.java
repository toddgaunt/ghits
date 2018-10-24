package com.example.lewisandrew.gridsim.Model;

import android.util.Log;

import com.example.lewisandrew.gridsim.GridCellFactory;
import com.example.lewisandrew.gridsim.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by lewisandrew on 2/16/2017.
 *
 * A collection that holds GridCells in array form for quicker access
 */

public class SimulationGrid {

    private int numCols, numRows, size;
    private GridCell[][] mGridCells, mPausedGrid;
    private GridCellFactory mGridCellFactory = GridCellFactory.getInstance();

    /**
     * Default constructor. Initializes to empty grid cells.
     */
    public SimulationGrid(int cols, int rows) {
        mGridCells = new GridCell[cols][rows];
        mPausedGrid = new GridCell[cols][rows];

        numCols = cols; numRows = rows;
        size = cols * rows;

        int index = 0;
        for(int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++)
                mGridCells[c][r] = mGridCellFactory.createGridCell(0, index++, c, r);
        }
    }

    public void updateArray(JSONArray arr) {
        // Parse the JSON array
        try {
            // Get each int from each row (JSON Array)
            for (int row = 0; row < arr.length(); row++) {
                JSONArray rowOfInts = arr.getJSONArray(row);

                for (int col = 0; col < rowOfInts.length(); col++) {
                    int val = rowOfInts.getInt(col); // get int from JSONArray
                    int index = row*16+col;
                    this.updateCell(val, index, col, row);
                }
            }
        }
        catch(JSONException e) {
            Log.d("SimulationGrid","JSONArray Parse Error");
        }
    }

    public void updateCell(int val, int index, int col, int row) {
        if (mGridCells[col][row].getValue() != val) // only update cell if its value changes
            mGridCells[col][row] = mGridCellFactory.createGridCell(val, index, col, row);
    }

    public void pauseGrid() {
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++)
                mPausedGrid[col][row] = mGridCells[col][row];
        }
    }

    /**
     * Change the pointer at the corresponding index to point to the given 'cell' pointer.
     * @param index
     * @param cell
     */
    public void setCell(int index, GridCell cell) {
        int r = index / 16; // y index of cell
        int c = index % 16; // x index of cell
        mGridCells[c][r] = cell;
    }

    /**
     * Get pointer to the corresponding cell in the array of grid cells
     * @param index
     * @return GridCell
     */
    public GridCell getCell(int index) {
        int r = index / 16; // y index of cell
        int c = index % 16; // x index of cell

        if (MainActivity.PAUSED) // if paused return cell to paused grid
            return mPausedGrid[c][r];
        else
            return mGridCells[c][r];
    }

    public int getNumRows() { return numRows; } // return the number of rows in the grid
    public int getNumCols() { return numCols; } // return the number of columns in the grid
    public int size() { return size; } // return the number of cells in the grid
}
