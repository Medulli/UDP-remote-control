package com.placeholder.julien.udp_remote_control;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.List;


public class SendCommandActivity extends ActionBarActivity implements OnClickListener{

    public VideoSequenceDBAdapter vidSeqDBAdapter;
    public AdminDBAdapter adminDBAdapter;

    //code used to check if we asked the user to give his account name
    public static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    //logged user mail
    public String userEmail;
    public boolean isAdmin;

    public void onClick(View v) {
        switch (v.getId()) {
            //retrieve all the messages data and implements the UDP server
            case R.id.button_send:
                //collect data and send it to the UDP server
                collectDataAndSend();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_command);

        //open the databases
        this.vidSeqDBAdapter = new VideoSequenceDBAdapter(this);
        this.vidSeqDBAdapter = this.vidSeqDBAdapter.open();

        this.adminDBAdapter = new AdminDBAdapter(this);
        this.adminDBAdapter = this.adminDBAdapter.open();

        //if we have already the user account
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            //if a sequence name was retrieved, we retrieve it
            this.isAdmin = extras.getBoolean("isAdmin");
        }
        else {
            //get the user account
            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
            if (status != ConnectionResult.SUCCESS) {
                this.isAdmin = false;
            } else {
                pickUserAccount();
            }
        }

        //get the possible sequences
        List<String> possibleSequencesNames=vidSeqDBAdapter.getPossibleEntries("videosequence");
        //fill the spinners
        addSpinnerContent("spinner_1",possibleSequencesNames);
        addSpinnerContent("spinner_2",possibleSequencesNames);
        addSpinnerContent("spinner_3",possibleSequencesNames);

        //set listeners
        Button buttonSend = (Button) findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the admin menu; this adds items to the action bar if it is present.
        if(isAdmin) {
            getMenuInflater().inflate(R.menu.menu_send_command, menu);
        }
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
        this.adminDBAdapter.close();
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                this.userEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                this.isAdmin=adminDBAdapter.isAdmin(userEmail);
                //refresh the activity if there is a need to show extra content (admin mode)
                if(isAdmin) {
                    Intent intent = getIntent();
                    intent.putExtra("isAdmin", true);
                    finish();
                    startActivity(intent);
                }
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast t = Toast.makeText(this, R.string.pick_account, Toast.LENGTH_SHORT);
                t.show();
            }
        }
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

    protected void collectDataAndSend(){
        //retrieve the sequence names
        Spinner spinner1 = (Spinner) findViewById(R.id.spinner_1);
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner_2);
        Spinner spinner3 = (Spinner) findViewById(R.id.spinner_3);
        ArrayList<String> seqNameList = new ArrayList<>();
        seqNameList.add(spinner1.getSelectedItem().toString());
        seqNameList.add(spinner2.getSelectedItem().toString());
        seqNameList.add(spinner3.getSelectedItem().toString());

        //retrieve the corresponding content from the db
        ArrayList<ArrayList<Integer>> delaysList=new ArrayList<>();
        ArrayList<ArrayList<String>> messagesList=new ArrayList<>();
        for(int i=0;i<seqNameList.size();i++){
            VideoSequence vidSeq = vidSeqDBAdapter.getVideoSequence(seqNameList.get(i));
            delaysList.add(vidSeq.getDelay());
            messagesList.add(vidSeq.getMessages());
        }

        //UDP server creation
        //retrieving server settings
        SharedPreferences serverSettings = PreferenceManager.getDefaultSharedPreferences(this);
        String serverHost = serverSettings.getString("pref_host", "");
        String serverPortString = serverSettings.getString("pref_port", "-1");
        int serverPort=Integer.parseInt(serverPortString);
        String serverPayloadString = serverSettings.getString("pref_payload", "-1");
        int serverPayload=Integer.parseInt(serverPayloadString);
        //UI message
        Toast t = Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT);
        t.show();
        //Sending
        UDPSendMessage udpServer = new UDPSendMessage(this,messagesList,delaysList,serverHost,serverPort,serverPayload);
        udpServer.execute();

    }

}
