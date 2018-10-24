package com.example.lewisandrew.gridsim;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.*;

import de.greenrobot.event.EventBus;

/**
 * Created by lewisandrew on 2/25/2017.
 *
 * Class that will run handle threads that will do GET request every 500ms
 */

public class Poller {
    private static Poller mPollerInstance = null;
    private JsonObjectRequest postRequest, mGetRequest;

    public static Poller getInstance(SimGridFacade gF, Context c) {
        if(mPollerInstance == null)
            mPollerInstance = new Poller(gF, c);
        return mPollerInstance;
    }

    private SimGridFacade mGridFacade;
    private Context mContext;

    public Poller(SimGridFacade gF, Context c) {
        mGridFacade = gF; mContext = c;

        // Do a POST request before starting the GET requests.
        postRequest = new JsonObjectRequest(Request.Method.POST,
                MainActivity.url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("JSONRequest","JSON Post request failed.");
                    }
                });
        Volley.newRequestQueue(mContext).add(postRequest);

        // Create GET request to be used every 500 ms
        mGetRequest = new JsonObjectRequest( Request.Method.GET,
                MainActivity.url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Poller", "Response Received");
                        // Parse the JSON array and make new GridCell array with it
                        try {
                            // Get the Array of JSONArrays. Each JSONArray is a row in the grid
                            JSONArray arrayOfRows = response.getJSONArray("grid");
                            EventBus.getDefault().post(new GridUpdateEvent(arrayOfRows));
                        }
                        catch(JSONException e) {
                            Log.d("Async","JSONArray Parse Error");
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("JSONRequest","JSON GET request failed.");
                    }
                });

        // Setup and start the JSON GET request to be done periodically.
        ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(3);
        Runnable periodicTask = new Runnable(){
            @Override
            public void run() {
                new JSONParse().execute();
            }
        };
        ScheduledFuture<?> periodicFuture = sch.scheduleAtFixedRate(periodicTask, 0, 500,
                TimeUnit.MILLISECONDS);
    }

    public void setGridFacade(SimGridFacade gridFacade) { mGridFacade = gridFacade; }

    /**
     * Asynchronous task subclass. This class will handle requesting a JSON using Volley.
     * The JSONObject will be sent to the GridAdapter to be parsed. The values parsed will be
     * used to update the gridview.
     */
    private class JSONParse extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... args) {
            Volley.newRequestQueue(mContext).add(mGetRequest);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

        }
    }
}
