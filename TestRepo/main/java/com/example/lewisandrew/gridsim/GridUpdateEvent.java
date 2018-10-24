package com.example.lewisandrew.gridsim;


import org.json.JSONArray;

/**
 * Created by lewisandrew on 2/27/2017.
 */

public class GridUpdateEvent {
    private JSONArray mJSONArray;

    public GridUpdateEvent(JSONArray arr) { mJSONArray = arr; }
    public JSONArray getJSONArray() { return mJSONArray; }
}
