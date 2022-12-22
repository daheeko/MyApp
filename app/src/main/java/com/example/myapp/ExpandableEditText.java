package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class ExpandableEditText extends RelativeLayout {

    LayoutInflater inflater = null;
    EditText editText;
    Button button;

    public ExpandableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayout();
    }

    public ExpandableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayout();
    }

    public ExpandableEditText(Context context) {
        super(context);
        setLayout();
    }

    private void setLayout(){
        inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.expandable_edit_text, this, true);

        editText = (EditText) findViewById(R.id.expandable_edit);
        button = (Button) findViewById(R.id.expand_button);

        expandView();
    }

    private void expandView(){
        String text;
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public Editable getText(){
        Editable text = editText.getText();
        return text;
    }
}
