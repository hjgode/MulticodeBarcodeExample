package com.honeywell.multiscanreader;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DataBaseView extends Activity implements AdapterView.OnItemSelectedListener {
    Context context=this;
    Database database;
    String MandantCurrent="N/A";
    TextView textViewMandant;
    TextView txtNumRows;

    ListView lv;
    ArrayList list = new ArrayList();
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_base_view);

        textViewMandant = (TextView) findViewById(R.id.dbview_mandant);
        // get initial list
        database=new Database(this.context);
        Spinner spinner = (Spinner) findViewById(R.id.dbview_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(this,
                R.array.mandanten_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapterSpinner);
        spinner.setOnItemSelectedListener(this);

        lv = findViewById(R.id.dbListView);

        txtNumRows=(TextView)findViewById(R.id.txtNumRows);

        showlist();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        MandantCurrent=parent.getItemAtPosition(pos).toString();
        textViewMandant.setText(MandantCurrent);
        showlist();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    ArrayList<DataModel> dataModels;
    private static CustomAdapter customAdapter;
    public void showlist()
    {
        long lc = database.getRowCount(MandantCurrent);
        txtNumRows.setText(Long.toString(lc));
        list.clear();
        Cursor cursor = database.showdata(MandantCurrent);
        dataModels=new ArrayList<>();
        if(cursor.getCount() == 0)
        {
            Toast.makeText(context, "No Data", Toast.LENGTH_SHORT).show();
        }

        while(cursor.moveToNext())
        {
            dataModels.add(new DataModel(cursor.getString(0), cursor.getString(1),cursor.getString(2)));
//            list.add(cursor.getString(0));
//            list.add(cursor.getString(1));
//            list.add(cursor.getString(2));
        }
//        adapter = new ArrayAdapter(context,android.R.layout.simple_list_item_1,list);
        customAdapter=new CustomAdapter(dataModels, context);
        lv.setAdapter(customAdapter);
//        lv.setAdapter(adapter);
    }


}