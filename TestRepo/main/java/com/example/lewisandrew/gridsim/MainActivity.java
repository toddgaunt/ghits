package com.example.lewisandrew.gridsim;

        import android.content.Intent;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.GridView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.example.lewisandrew.gridsim.Model.GridCell;

        import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {


    private Button mButton1, mButton2;
    private SimGridFacade mGridFacade;
    private Poller mPoller;
    public static final String url = "http://stman1.cs.unh.edu:6191/games";
    public static boolean PAUSED = false;

    /**
     * Create GUI elements.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton1 = (Button) findViewById(R.id.history_button);
        mButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String history = mGridFacade.getItemSelectedHistory();
                Log.d("Show", history);
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        mButton2 = (Button) findViewById(R.id.pause_resume_button);
        mButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(PAUSED)
                    GridCellFactory.getInstance().refreshItems();
                else
                    mGridFacade.pauseGrid();
                PAUSED = !PAUSED;
            }
        });
        mButton1.bringToFront(); mButton2.bringToFront();

        mGridFacade = SimGridFacade.getInstance(this, (GridView) findViewById(R.id.gridview),
                    (TextView) findViewById(R.id.textView));

        mPoller = Poller.getInstance(mGridFacade, MainActivity.this);
        mPoller.setGridFacade(mGridFacade);
    }
}
