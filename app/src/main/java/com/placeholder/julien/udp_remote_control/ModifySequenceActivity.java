package com.placeholder.julien.udp_remote_control;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;


public class ModifySequenceActivity extends ActionBarActivity implements View.OnClickListener {

    public String seqName;
    public VideoSequenceDBAdapter vidSeqDBAdapter;

    //rows content
    private List<String> possibleCmd;
    private List<String> possibleVid;

    private ArrayList<Integer> delayList;
    private ArrayList<String> commandList;
    private ArrayList<String> videoList;

    //rows display parameters
    private LinearLayout.LayoutParams bigBlankParams;
    private LinearLayout.LayoutParams smallBlankParams;
    private LinearLayout.LayoutParams delayParams;
    private LinearLayout.LayoutParams msTextParams;
    private LinearLayout.LayoutParams spinnerParams;
    private LinearLayout.LayoutParams buttonParams;

    //row container
    private LinearLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_sequence);
        if (savedInstanceState == null) {
            //db access
            this.vidSeqDBAdapter = new VideoSequenceDBAdapter(this);
            this.vidSeqDBAdapter.open();

            //set listener
            Button submitButton = (Button) findViewById(R.id.button_submit_seq);
            submitButton.setOnClickListener(this);

            //retrieve possible parameters
            Bundle extras = getIntent().getExtras();
            //modify a sequence
            if (extras != null)
            {
                //if a sequence name was retrieved, we retrieve it
                this.seqName = extras.getString("seqName");

                //populate the layout
                configureRows();
                for (int i=0;i<commandList.size();i++){
                    addDisplayRow(this,i);
                }
            }
            //add a new sequence
            else
            {
                this.seqName="";
                configureRows();
                //sequence name
                ArrayList<String> compType = new ArrayList<>();
                compType.add("editText");

                ArrayList<String> contentText = new ArrayList<>();
                contentText.add(null);
                contentText.add(getString(R.string.new_seq_name));

                ArrayList<LinearLayout.LayoutParams> compParams = new ArrayList<>();
                LinearLayout.LayoutParams nameParams=new LinearLayout.LayoutParams(
                        //android:layout_width
                        //0dp, according to pixels = (int) (dps * scale + 0.5f) where scale = Resources.DisplayMetrics.Density
                        (int)0.5f,
                        //android:layout_height
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        //android:layout_weight
                        1
                );
                compParams.add(nameParams);

                ArrayList<Integer> compIds = new ArrayList<>();
                compIds.add(R.id.seq_name);

                ArrayList<Integer> contentInts = new ArrayList<>();
                contentInts.add(null);

                //create handler
                DynamicInterfaceHandler interfaceHandler = new DynamicInterfaceHandler(compIds, compType, compParams, contentText, contentInts, null);
                //create row
                LinearLayout newRow = interfaceHandler.createRow(this);
                contentLayout.addView(newRow);

                //add the first content row
                addDisplayRow(this, 0);
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button_add:
                //create a new row and fill it with the relevant views
                addDisplayRow(v.getContext(),-1);
                break;
            case R.id.button_submit_seq:
                //collects the data and submit it
                collectDataAndSubmit();
                //back to the previous activity
                Intent intent = new Intent(v.getContext(), ConfigSequencesActivity.class);
                v.getContext().startActivity(intent);
                break;
        }
    }

    //loads data from the database and configures the display parameters
    private void configureRows(){
        //retrieve data from db
        if(!seqName.equals("")) {
            VideoSequence vidSeq = vidSeqDBAdapter.getVideoSequence(seqName);
            this.delayList = vidSeq.getDelay();
            this.commandList = vidSeq.getCommand();
            this.videoList = vidSeq.getVideo();
        }
        //spinners content
        //retrieve spinners content
        this.possibleCmd=vidSeqDBAdapter.getPossibleEntries("command");
        this.possibleVid=vidSeqDBAdapter.getPossibleEntries("video");

        //first blank params
        this.bigBlankParams = new LinearLayout.LayoutParams(
                //android:layout_width
                //0dp, according to pixels = (int) (dps * scale + 0.5f) where scale = Resources.DisplayMetrics.Density
                (int)0.5f,
                //android:layout_height
                (int)0.5f,
                //android:layout_weight
                1
        );
        //second blank params
        this.smallBlankParams = new LinearLayout.LayoutParams(
                //android:layout_width
                //0dp, according to pixels = (int) (dps * scale + 0.5f) where scale = Resources.DisplayMetrics.Density
                (int)0.5f,
                //android:layout_height
                (int)0.5f,
                //android:layout_weight
                (float)0.3
        );
        //delay params
        this.delayParams = new LinearLayout.LayoutParams(
                //android:layout_width
                //0dp, according to pixels = (int) (dps * scale + 0.5f) where scale = Resources.DisplayMetrics.Density
                (int)0.5f,
                //android:layout_height
                LinearLayout.LayoutParams.WRAP_CONTENT,
                //android:layout_weight
                1
        );
        //ms label
        this.msTextParams = new LinearLayout.LayoutParams(
                //android:layout_width
                LinearLayout.LayoutParams.WRAP_CONTENT,
                //android:layout_height
                LinearLayout.LayoutParams.WRAP_CONTENT,
                //android:layout_weight
                (float)0.15
        );
        //spinners params
        this.spinnerParams = new LinearLayout.LayoutParams(
                //android:layout_width
                //0dp, according to pixels = (int) (dps * scale + 0.5f) where scale = Resources.DisplayMetrics.Density
                (int)0.5f,
                //android:layout_height
                LinearLayout.LayoutParams.WRAP_CONTENT,
                //android:layout_weight
                2
        );
        //button params
        this.buttonParams = new LinearLayout.LayoutParams(
                //android:layout_width
                LinearLayout.LayoutParams.WRAP_CONTENT,
                //android:layout_height
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        //the layout to populate
        this.contentLayout = (LinearLayout)findViewById(R.id.seq_content_layout);
    }

    //create a row and fill it with the data from the db
    //an indexRow of -1 means it is a blank new row created by the user
    private void addDisplayRow(Context context,int indexRow){
        //type of objects list (skeleton of a row)
        ArrayList<String> compType = new ArrayList<>();
        //first row has 2 blank spaces as the first delay is always 0ms
        if (indexRow == 0) {
            compType.add("blank");
            compType.add("blank");
        } else {
            compType.add("editTextNumber");
            compType.add("textView");
        }
        compType.add("spinner");
        compType.add("spinner");
        //can't remove the first row
        if (indexRow!=0) {
            compType.add("removeButton");
        }

        //spinners
        ArrayList<List<String>> spinnersContent = new ArrayList<>();
        spinnersContent.add(possibleCmd);
        spinnersContent.add(possibleVid);
        //content text :
        //delay hint, ms label, and selected spinner items
        ArrayList<String> contentText = new ArrayList<>();
        //text fields for a normal row
        if(indexRow!=0) {
            contentText.add(getString(R.string.delay_hint));
            contentText.add(getString(R.string.text_view_ms));
        }
        //if not an empty row added by the user
        //spinners pre selection
        if(indexRow>=0 && !seqName.equals("")) {
            contentText.add(commandList.get(indexRow));
            contentText.add(videoList.get(indexRow));
        } else {
            contentText.add(null);
            contentText.add(null);
        }
        //remove button tag
        if (indexRow!=0) {
            contentText.add(null);
        }

        //layout params
        ArrayList<LinearLayout.LayoutParams> compParams = new ArrayList<>();
        if(indexRow == 0){
            compParams.add(bigBlankParams);
            compParams.add(smallBlankParams);
        } else {
            compParams.add(delayParams);
            compParams.add(msTextParams);
        }
        compParams.add(spinnerParams);
        compParams.add(spinnerParams);
        //no remove button on the first row!
        if(indexRow!=0) {
            compParams.add(buttonParams);
        }

        //id list
        ArrayList<Integer> compIds = new ArrayList<>();
        if(indexRow!=0){
            compIds.add(R.id.text_delay);
            compIds.add(R.id.text_ms);
        }
        compIds.add(R.id.spinner_head);
        compIds.add(R.id.spinner_tail);
        //remove button id
        if (indexRow!=0) {
            compIds.add(R.id.button_remove);
        }

        //pre filled delay fields
        ArrayList<Integer> contentInts = new ArrayList<>();
        if(indexRow>0) {
            contentInts.add(delayList.get(indexRow));
        } else {
            contentInts.add(null);
        }

        //create handler
        DynamicInterfaceHandler interfaceHandler = new DynamicInterfaceHandler(compIds, compType, compParams, contentText, contentInts, spinnersContent);
        //create row
        LinearLayout newRow = interfaceHandler.createRow(context);

        //the add button if it is the first row
        if (indexRow == 0) {
            ImageButton buttonAdd = new ImageButton(context);
            buttonAdd.setImageResource(android.R.drawable.ic_input_add);
            buttonAdd.setId(R.id.button_add);
            buttonAdd.setLayoutParams(buttonParams);
            buttonAdd.setOnClickListener(this);
            newRow.addView(buttonAdd);
        }

        //add to the content layout
        contentLayout.addView(newRow);
    }

    //collects the sequence content and submit it to the database through a VideoSequence instance
    protected void collectDataAndSubmit() {
        //retrieve the data in contentLayout and store it in delayArray and msgArray
        ArrayList<Integer> delayArray = new ArrayList<>();
        //delay of 0ms for the first message
        delayArray.add(0);
        ArrayList<String> commandArray = new ArrayList<>();
        ArrayList<String> videoArray = new ArrayList<>();
        boolean newSequence=false;
        for (int i = 0; i < contentLayout.getChildCount(); i++) {
            //for each child in the selected layout
            LinearLayout messageLayout = (LinearLayout) contentLayout.getChildAt(i);
            //retrieve the sequence name if not already filled
            if(seqName.equals("")){
                EditText nameRow = (EditText) messageLayout.findViewById(R.id.seq_name);
                if (nameRow != null) {
                    seqName=nameRow.getText().toString();
                    newSequence=true;
                }
            }
            //retrieve delay as an Integer
            EditText delayRow = (EditText) messageLayout.findViewById(R.id.text_delay);
            if (delayRow != null) {
                delayArray.add(Integer.parseInt(delayRow.getText().toString()));
            }
            //retrieve selected spinners content (command and video)
            Spinner spinnerHeadRow = (Spinner) messageLayout.findViewById(R.id.spinner_head);
            Spinner spinnerTailRow = (Spinner) messageLayout.findViewById(R.id.spinner_tail);
            if (spinnerHeadRow != null && spinnerTailRow != null) {
                commandArray.add(spinnerHeadRow.getSelectedItem().toString());
                videoArray.add(spinnerTailRow.getSelectedItem().toString());
            }
        }
        VideoSequence vidSeq = new VideoSequence(seqName,delayArray,commandArray,videoArray);
        if(newSequence){
            vidSeqDBAdapter.insertBasicEntry(seqName,"videosequence");
        }else {
            //purge the db of existing entries concerning the relevant video sequence
            vidSeqDBAdapter.emptyVideoSequence(seqName);
        }
        //insert the new content
        vidSeqDBAdapter.fillVideoSequence(vidSeq);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close The Database
        this.vidSeqDBAdapter.close();
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

}
