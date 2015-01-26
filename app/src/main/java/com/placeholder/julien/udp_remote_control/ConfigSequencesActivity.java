package com.placeholder.julien.udp_remote_control;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;


public class ConfigSequencesActivity extends ActionBarActivity implements View.OnClickListener {

    private static VideoSequenceDBAdapter vidSeqDBAdapter;

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add_seq:
                Intent intent = new Intent(v.getContext(), ModifySequenceActivity.class);
                v.getContext().startActivity(intent);
                break;
            case R.id.button_remove:
                String seqName=(String)v.getTag();
                if(seqName!=null){
                    //hide the row
                    LinearLayout completeRow = (LinearLayout) v.getParent();
                    ((ViewManager) completeRow.getParent()).removeView(completeRow);
                    //delete the sequence's content from the db
                    vidSeqDBAdapter.emptyVideoSequence(seqName);
                    //remove the sequence from the db
                    vidSeqDBAdapter.deleteEntry(seqName,"videosequence");
                }
                break;
            case R.id.button_done:
                Intent intentMainActivity = new Intent(v.getContext(), SendCommandActivity.class);
                v.getContext().startActivity(intentMainActivity);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_sequences);

        //set listeners
        ImageButton buttonAddSeq = (ImageButton) findViewById(R.id.button_add_seq);
        buttonAddSeq.setOnClickListener(this);

        Button buttonDone = (Button) findViewById(R.id.button_done);
        buttonDone.setOnClickListener(this);

        //retrieve sequences names from db
        this.vidSeqDBAdapter = new VideoSequenceDBAdapter(this);
        this.vidSeqDBAdapter.open();

        //display all the sequences available, as well as a config and a remove button for each
        displaySequences();
    }

    private void displaySequences(){
        //retrieves data from db
        ArrayList<String> possibleSequencesNames=vidSeqDBAdapter.getPossibleEntries("videosequence");

        //container layout
        LinearLayout contentLayout = (LinearLayout)findViewById(R.id.seq_list_layout);

        //type of objects list (skeleton of a row)
        ArrayList<String> compType = new ArrayList<>();
        compType.add("textView");
        compType.add("configButton");
        compType.add("removeButton");

        //id list
        ArrayList<Integer> compIds = new ArrayList<>();
        compIds.add(R.id.seq_name);
        compIds.add(R.id.button_remove);

        //params list
        ArrayList<LinearLayout.LayoutParams> compParams = new ArrayList<>();
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                //android:layout_width
                //0dp, according to pixels = (int) (dps * scale + 0.5f) where scale = Resources.DisplayMetrics.Density
                (int)0.5f,
                //android:layout_height
                LinearLayout.LayoutParams.WRAP_CONTENT,
                //android:layout_weight
                1
        );
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                //android:layout_width
                LinearLayout.LayoutParams.WRAP_CONTENT,
                //android:layout_height
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        compParams.add(nameParams);
        compParams.add(buttonParams);
        compParams.add(buttonParams);

        //populate layout
        for (int i=0; i<possibleSequencesNames.size();i++) {
            //put the name in a list to match type
            ArrayList<String> contentText = new ArrayList<>();
            //added once for the textView, once for each button's tag
            //this behaviour is needed as a row can have several different texts
            contentText.add(possibleSequencesNames.get(i));
            contentText.add(possibleSequencesNames.get(i));
            contentText.add(possibleSequencesNames.get(i));
            //create handler
            DynamicInterfaceHandler interfaceHandler = new DynamicInterfaceHandler(compIds, compType, compParams, contentText, null, null);
            //create row
            LinearLayout newRow = interfaceHandler.createRow(this);
            //set removeButton's listener
            ImageButton removeButton = (ImageButton) newRow.findViewById(R.id.button_remove);
            removeButton.setOnClickListener(this);
            //add to the content layout
            contentLayout.addView(newRow);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_command, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.action_settings:
                Intent intentSettings = new Intent(this, ServerSettingsActivity.class);
                startActivity(intentSettings);
                break;
            case R.id.action_manage:
                Intent intentManage = new Intent(this, ConfigSequencesActivity.class);
                startActivity(intentManage);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close The Database
        this.vidSeqDBAdapter.close();
    }

}
