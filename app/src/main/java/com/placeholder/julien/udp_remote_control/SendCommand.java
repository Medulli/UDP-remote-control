package com.placeholder.julien.udp_remote_control;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SendCommand extends ActionBarActivity implements OnClickListener{

    private final List<String> messageHead= Arrays.asList("start", "stop");
    private final List<String> messageTail= Arrays.asList("video1","video2","fade");

    public void onClick(View v) {
        LinearLayout layoutDynamic = (LinearLayout)findViewById(R.id.main_layout);
        switch (v.getId()) {

            case R.id.button_add:
                //create a new row and fill it with the relevant views
                LinearLayout newRow=createNewRow();
                layoutDynamic.addView(newRow);
            break;

            //retrieve all the messages data and implements the UDP server
            case R.id.button_send:
                //retrieve the data and store it in delayArray and msgArray
                ArrayList<Integer> delayArray = new ArrayList<>();
                //delay of 0ms for the first message
                delayArray.add(0);
                ArrayList<String> msgArray = new ArrayList<>();
                for(int i = 0; i < layoutDynamic.getChildCount(); i++) {
                    //for each child in the selected layout
                    LinearLayout messageLayout = (LinearLayout) layoutDynamic.getChildAt(i);
                    //retrieve delay as an Integer
                    EditText delayRow = (EditText) messageLayout.findViewById(R.id.text_delay);
                    if(delayRow!=null){
                        delayArray.add(Integer.parseInt(delayRow.getText().toString()));
                    }
                    //retrieve message as a String
                    Spinner spinnerHeadRow = (Spinner) messageLayout.findViewById(R.id.spinner_head);
                    Spinner spinnerTailRow = (Spinner) messageLayout.findViewById(R.id.spinner_tail);
                    if(spinnerHeadRow!=null && spinnerTailRow!=null){
                        msgArray.add(spinnerHeadRow.getSelectedItem().toString() +" "+ spinnerTailRow.getSelectedItem().toString() +"\n");
                    }
                }
                //UDP server creation
                //retrieving server settings
                SharedPreferences serverSettings = PreferenceManager.getDefaultSharedPreferences(this);
                String serverHost = serverSettings.getString("pref_host", "");
                String serverPortString = serverSettings.getString("pref_port", "-1");
                int serverPort=Integer.parseInt(serverPortString);
                //UI message
                Toast t = Toast.makeText(this, "Sending: " + msgArray.toString(), Toast.LENGTH_SHORT);
                t.show();
                //Sending
                UDPSendMessage udpServer = new UDPSendMessage(this,msgArray,delayArray,serverHost,serverPort);
                udpServer.execute();
            break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_command);

        //fill the default spinners
        addSpinnerContent("spinner_head",messageHead);
        addSpinnerContent("spinner_tail",messageTail);

        //set listeners
        ImageButton buttonAdd = (ImageButton) findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(this);
        Button buttonSend = (Button) findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);
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

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ServerSettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    // add content to spinner from spinner defined in xml layout
    private void addSpinnerContent(String selectedSpinnerID, List<String> list) {

        int spinID = getResources().getIdentifier(selectedSpinnerID, "id", getPackageName());
        Spinner selectedSpinner = (Spinner) findViewById(spinID);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectedSpinner.setAdapter(dataAdapter);
    }

    private LinearLayout createNewRow(){
        //create a new row
        LinearLayout newRow = new LinearLayout(this);
        newRow.setOrientation(LinearLayout.HORIZONTAL);
        // width and height of the new line
        LayoutParams newRowParams = new LayoutParams(
                //android:layout_width
                LayoutParams.MATCH_PARENT,
                //android:layout_height
                LayoutParams.WRAP_CONTENT
        );
        newRow.setLayoutParams(newRowParams);

        //create views to populate the new line
        //delay params
        LayoutParams delayParams = new LayoutParams(
                //android:layout_width
                //0dp, according to pixels = (int) (dps * scale + 0.5f) where scale = Resources.DisplayMetrics.Density
                (int)0.5f,
                //android:layout_height
                LayoutParams.WRAP_CONTENT,
                //android:layout_weight
                1
        );
        //delay
        EditText editDelay=new EditText(this);
        editDelay.setId(R.id.text_delay);
        editDelay.setHint(R.string.delay_hint);
        editDelay.setInputType(InputType.TYPE_CLASS_NUMBER);
        editDelay.setLayoutParams(delayParams);
        newRow.addView(editDelay);

        //ms label
        LayoutParams msTextParams = new LayoutParams(
                //android:layout_width
                LayoutParams.WRAP_CONTENT,
                //android:layout_height
                LayoutParams.WRAP_CONTENT,
                //android:layout_weight
                (float)0.15
        );
        TextView msLabel=new TextView(this);
        msLabel.setId(R.id.text_ms);
        msLabel.setText(R.string.text_view_ms);
        msLabel.setLayoutParams(msTextParams);
        newRow.addView(msLabel);

        //spinner params
        LayoutParams spinnerParams = new LayoutParams(
                //android:layout_width
                //0dp, according to pixels = (int) (dps * scale + 0.5f) where scale = Resources.DisplayMetrics.Density
                (int)0.5f,
                //android:layout_height
                LayoutParams.WRAP_CONTENT,
                //android:layout_weight
                2
        );
        //first spinner
        Spinner spinnerHead=new Spinner(this);
        ArrayAdapter<String> dataAdapterHead = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, messageHead);
        dataAdapterHead.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHead.setId(R.id.spinner_head);
        spinnerHead.setAdapter(dataAdapterHead);
        spinnerHead.setLayoutParams(spinnerParams);
        newRow.addView(spinnerHead);

        //second spinner
        Spinner spinnerTail=new Spinner(this);
        ArrayAdapter<String> dataAdapterTail = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, messageTail);
        dataAdapterTail.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTail.setId(R.id.spinner_tail);
        spinnerTail.setAdapter(dataAdapterTail);
        spinnerTail.setLayoutParams(spinnerParams);
        newRow.addView(spinnerTail);

        //remove button
        LayoutParams buttonParams = new LayoutParams(
                //android:layout_width
                LayoutParams.WRAP_CONTENT,
                //android:layout_height
                LayoutParams.WRAP_CONTENT
        );
        ImageButton buttonRemove = new ImageButton(this);
        buttonRemove.setImageResource(android.R.drawable.ic_delete);
        buttonRemove.setLayoutParams(buttonParams);
        buttonRemove.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout completeRow = (LinearLayout) v.getParent();
                ((ViewManager)completeRow.getParent()).removeView(completeRow);
            }
        });
        newRow.addView(buttonRemove);

        return newRow;
    }
}
