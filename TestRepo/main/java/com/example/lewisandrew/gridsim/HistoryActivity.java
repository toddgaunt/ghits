package com.example.lewisandrew.gridsim;

import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.example.lewisandrew.gridsim.Model.GardenerItem;
import com.example.lewisandrew.gridsim.Model.GridCell;

import org.w3c.dom.Text;

import de.greenrobot.event.EventBus;

/**
 * Created by lewisandrew on 2/28/2017.
 */
public class HistoryActivity extends Activity {
    private SimGridFacade mSimGridFacade;
    private TextView mTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity);

        mSimGridFacade = SimGridFacade.getInstance();

        mTextView = (TextView) findViewById(R.id.history_textView);
        mTextView.setMovementMethod(new ScrollingMovementMethod());
        mTextView.setText(mSimGridFacade.getItemSelectedHistory());

        EventBus.getDefault().register(this);
    }

    /**
     * When Poller posts event, this will be called. This method will get the JSON array stored in
     * the event object, then update the sim grid with it.
     * @param event
     */
    public void onEvent(GridUpdateEvent event){
        GridCell cell = mSimGridFacade.initialCellPressed;
        if(cell instanceof GardenerItem) {
            GardenerItem item = (GardenerItem)cell;
            String type = item.getCellType();
            String info = item.getCellInfo() + '\n';
            String history = item.getHistory();
            mTextView.setText(type + ' ' + info + history);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        EventBus.getDefault().unregister(this);
    }
}