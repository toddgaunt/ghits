package com.example.lewisandrew.gridsim.Model;

import com.example.lewisandrew.gridsim.R;

/**
 * Created by lewisandrew on 2/16/2017.
 *
 * This child class of GridCell represents all trees, bushes and small plants, as appropriate
 */

public class Plant extends GridCell {

    /**
     * Constructor for a Plant cell.
     * @param val
     * @param ind
     * @param c
     * @param r
     */
    public Plant(int val, int ind, int c, int r) {
        super(val, ind, c, r);
    }

    /**
     * Method that initializes plant cell.
     */
    @Override
    protected void initializeCell() {
        if(value == 1000) {
            type = "Tree";
            resourceID = R.drawable.tree;
        }
        else if(value > 1000 && value < 2000) {
            type = "Bush";
            resourceID = R.drawable.bushes;
        }
        else if(value == 2002) {
            type = "Clover";
            resourceID = R.drawable.clover;
        }
        else if(value == 2003) {
            type = "Mushroom";
            resourceID = R.drawable.mushroom;
        }
        else if(value == 3000) {
            type = "Sunflower";
            resourceID = R.drawable.sunflower;
        }
        else
            type = "Unknown plant type";
    }
}
