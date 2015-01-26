package com.placeholder.julien.udp_remote_control;

import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.View;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamically generates a new row (LinearLayout horizontal) and fill it with components of choice.
 */
public class DynamicInterfaceHandler {

    //components to add to the new row, in order of appearance
    private ArrayList<Integer> componentIds;
    private ArrayList<String> componentTypes;
    private ArrayList<LinearLayout.LayoutParams> componentParams;

    //strings used, in order of appearance
    private ArrayList<String> textContents;
    //integers used, in order of appearance
    private ArrayList<Integer> intContents;
    //spinners content, in order of appearance
    private ArrayList<List<String>> spinnersContent;

    public DynamicInterfaceHandler(ArrayList<Integer> componentIds, ArrayList<String> componentTypes,ArrayList<LinearLayout.LayoutParams> componentParams,
                                   ArrayList<String> textContents, ArrayList<Integer> intContents, ArrayList<List<String>> spinnersContent) {
        this.componentIds = componentIds;
        this.componentTypes = componentTypes;
        this.componentParams = componentParams;
        this.textContents = textContents;
        this.intContents = intContents;
        this.spinnersContent = spinnersContent;
    }

    public View createBlank (Context context, LinearLayout.LayoutParams blankParams){
        View blankDyn=new View(context);
        blankDyn.setLayoutParams(blankParams);
        return blankDyn;
    }

    public EditText createEditTextNumber (Context context, LinearLayout.LayoutParams textParams, int textId, Integer preText, String textHint){
        EditText editNumberDyn=new EditText(context);
        editNumberDyn.setId(textId);
        //if there is some pre-entered text to display
        if(preText!=null) {
            editNumberDyn.setText(preText.toString(), TextView.BufferType.EDITABLE);
        }
        editNumberDyn.setHint(textHint);
        editNumberDyn.setInputType(InputType.TYPE_CLASS_NUMBER);
        editNumberDyn.setLayoutParams(textParams);
        return editNumberDyn;
    }

    public EditText createEditText (Context context, LinearLayout.LayoutParams textParams, int textId, String preText, String textHint){
        EditText editNumberDyn=new EditText(context);
        editNumberDyn.setId(textId);
        //if there is some pre-entered text to display
        if(preText!=null) {
            editNumberDyn.setText(preText, TextView.BufferType.EDITABLE);
        }
        editNumberDyn.setHint(textHint);
        editNumberDyn.setLayoutParams(textParams);
        return editNumberDyn;
    }

    public TextView createTextView (Context context, LinearLayout.LayoutParams textParams, int textId, String text){
        TextView viewTextDyn=new TextView(context);
        viewTextDyn.setId(textId);
        viewTextDyn.setText(text);
        viewTextDyn.setLayoutParams(textParams);
        return viewTextDyn;
    }

    public Spinner createSpinner (Context context, LinearLayout.LayoutParams spinnerParams, int spinnerId, List<String> spinnerContent, String selectedValue){
        Spinner spinnerDyn=new Spinner(context);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, spinnerContent);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDyn.setId(spinnerId);
        spinnerDyn.setAdapter(dataAdapter);
        //if there is a specific item to select
        if(selectedValue!=null){
            spinnerDyn.setSelection(dataAdapter.getPosition(selectedValue));
        }
        spinnerDyn.setLayoutParams(spinnerParams);
        return spinnerDyn;
    }

    public ImageButton createRemoveButton (Context context, LinearLayout.LayoutParams buttonParams, int buttonId, String tag){
        ImageButton buttonRemoveDyn = new ImageButton(context);
        buttonRemoveDyn.setImageResource(android.R.drawable.ic_delete);
        buttonRemoveDyn.setLayoutParams(buttonParams);
        buttonRemoveDyn.setId(buttonId);
        //if there is a tag, apply it
        if(tag!=null){
            buttonRemoveDyn.setTag(tag);
        } else {
            //else, its a simple hide button
            buttonRemoveDyn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout completeRow = (LinearLayout) v.getParent();
                    ((ViewManager) completeRow.getParent()).removeView(completeRow);
                }
            });
        }
        return buttonRemoveDyn;
    }

    public ImageButton createConfigButton (Context context, LinearLayout.LayoutParams buttonParams, String seqName){
        ImageButton buttonConfigDyn = new ImageButton(context);
        buttonConfigDyn.setImageResource(android.R.drawable.ic_menu_manage);
        buttonConfigDyn.setLayoutParams(buttonParams);
        buttonConfigDyn.setTag(seqName);
        buttonConfigDyn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ModifySequenceActivity.class);
                intent.putExtra("seqName",(String)v.getTag());
                v.getContext().startActivity(intent);
            }
        });
        return buttonConfigDyn;
    }

    public LinearLayout createRow(Context context){
        //create a new row
        LinearLayout newRow = new LinearLayout(context);
        newRow.setOrientation(LinearLayout.HORIZONTAL);
        // width and height of the new line
        LinearLayout.LayoutParams newRowParams = new LinearLayout.LayoutParams(
                //android:layout_width
                LinearLayout.LayoutParams.MATCH_PARENT,
                //android:layout_height
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        newRow.setLayoutParams(newRowParams);

        //create views to populate the new row
        int indexTextContent = 0;
        int indexIntContent = 0;
        int indexSpinnerContent = 0;
        int indexId = 0;
        for(int i=0;i<componentTypes.size();i++){
            switch (componentTypes.get(i)){
                case "blank":
                    View addedBlank = createBlank(context, componentParams.get(i));
                    newRow.addView(addedBlank);
                    break;
                case "editTextNumber":
                    EditText addedEditTextNumber = createEditTextNumber(context, componentParams.get(i), componentIds.get(indexId), intContents.get(indexIntContent), textContents.get(indexTextContent));
                    indexTextContent++;
                    indexIntContent++;
                    indexId++;
                    newRow.addView(addedEditTextNumber);
                    break;
                case "editText":
                    EditText addedEditText = createEditText(context, componentParams.get(i), componentIds.get(indexId), textContents.get(indexIntContent), textContents.get(indexTextContent+1));
                    indexTextContent+=2;
                    indexId++;
                    newRow.addView(addedEditText);
                    break;
                case "textView":
                    TextView addedTextView = createTextView(context, componentParams.get(i), componentIds.get(indexId), textContents.get(indexTextContent));
                    indexTextContent++;
                    indexId++;
                    newRow.addView(addedTextView);
                    break;
                case "spinner":
                    Spinner addedSpinner = createSpinner(context, componentParams.get(i),componentIds.get(indexId), spinnersContent.get(indexSpinnerContent),textContents.get(indexTextContent));
                    indexSpinnerContent++;
                    indexTextContent++;
                    indexId++;
                    newRow.addView(addedSpinner);
                    break;
                case "removeButton":
                    ImageButton removeButton = createRemoveButton(context, componentParams.get(i),componentIds.get(indexId),textContents.get(indexTextContent));
                    indexTextContent++;
                    indexId++;
                    newRow.addView(removeButton);
                    break;
                case "configButton":
                    ImageButton configButton = createConfigButton(context, componentParams.get(i),textContents.get(indexTextContent));
                    indexTextContent++;
                    newRow.addView(configButton);
                    break;
            }

        }
        return newRow;
    }
}
