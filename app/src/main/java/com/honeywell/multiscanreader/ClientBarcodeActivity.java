package com.honeywell.multiscanreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.aidc.*;

public class ClientBarcodeActivity extends Activity implements BarcodeReader.BarcodeListener,
        BarcodeReader.TriggerListener, AdapterView.OnItemSelectedListener {

    AidcManager manager;
    private com.honeywell.aidc.BarcodeReader barcodeReader;
    private ListView barcodeList;

    static String TAG="ClientBarcodeActivity";

    Context context=this;

    Database database;

    TextView textViewMandant;
    String MandantCurrent="N/A";

    int uniqueSerials=0;
    boolean bTriggerStatusLast=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        if(Build.MODEL.startsWith("VM1A")) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // create the AidcManager providing a Context and a
        // CreatedCallback implementation.
        AidcManager.create(this, new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                try{
                    barcodeReader = manager.createBarcodeReader();
                }
                catch (InvalidScannerNameException e){
                    Toast.makeText(context, "Invalid Scanner Name Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    Toast.makeText(context, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (barcodeReader != null) {

            // register bar code event listener
            barcodeReader.addBarcodeListener(this);

            // set the trigger mode to client control
            try {
               barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                       BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);

            } catch (UnsupportedPropertyException e) {
                Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
            }
            // register trigger state change listener
            barcodeReader.addTriggerListener(this);

            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Disable bad read response, handle in onFailureEvent
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, false);
            // Sets time period for decoder timeout in any mode
            properties.put(BarcodeReader.PROPERTY_DECODER_TIMEOUT,  400);
            // Apply the settings
            barcodeReader.setProperties(properties);
        }

        textViewMandant=(TextView)findViewById(R.id.label_mandant);
        // get initial list
        barcodeList = (ListView) findViewById(R.id.listViewBarcodeData);
        database=new Database(this.context);
        Spinner spinner = (Spinner) findViewById(R.id.Mandanten_Spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.mandanten_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        Button btnExport=(Button) findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.getDataCount();
                database.exportCSV();
                database.exportDB();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.client_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_data:
                Intent dbviewIntent = new Intent(context, DataBaseView.class);
                startActivity(dbviewIntent);
                break;
            case R.id.export_csv:
                database.exportCSV();
                break;
            case R.id.clear_data:
                showDialog("Delete data", "Wirklich alle Daten löschen?");

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    boolean showDialog(String title, String question){
        final boolean[] bRet = {false};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);
        builder.setMessage(question);

        builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                bRet[0] =true;
                database.clearData();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NEIN", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                bRet[0]=false;
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        return bRet[0];
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        MandantCurrent=parent.getItemAtPosition(pos).toString();
        textViewMandant.setText(MandantCurrent);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String data = event.getBarcodeData();
                if (database.findData(data).length()>0){

                    Log.d(TAG, "Skipped adding data:" + data);
                    List<String> list = new ArrayList<String>();
                    list.add("SKIPPED ADDING: " + data);
                    list.add("Anzahl: " + Integer.toString(uniqueSerials));
                    final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
                            ClientBarcodeActivity.this, android.R.layout.simple_list_item_1, list);

                    barcodeList.setAdapter(dataAdapter);
                }
                else{
                    database.writeData(data, MandantCurrent);
                    Log.d(TAG, "Added new data:" + data);
                    uniqueSerials++;

                    List<String> list = new ArrayList<String>();
                    list.add("Barcode data: " + event.getBarcodeData());
                    list.add("Anzahl: " + Integer.toString(uniqueSerials));

                    final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
                            ClientBarcodeActivity.this, android.R.layout.simple_list_item_1, list);

                    barcodeList.setAdapter(dataAdapter);
                }                // update UI to reflect the data

            }
        });
        if(bScanContinuous)
            startstopScan(true);
        else
            startstopScan(false);
    }

    boolean bScanContinuous=false;
    // When using Automatic Trigger control do not need to implement the
    // onTriggerEvent function
    @Override
    public void onTriggerEvent(TriggerStateChangeEvent event) {
        bScanContinuous=event.getState();
        startstopScan(event.getState());

    }

    void startstopScan(boolean start){

        try {
            // only handle trigger presses
            // turn on/off aimer, illumination and decoding
            barcodeReader.aim(start);
            barcodeReader.light(start);
            barcodeReader.decode(start);

        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
            Toast.makeText(this, "Scanner is not claimed", Toast.LENGTH_SHORT).show();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
            Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent arg0) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(ClientBarcodeActivity.this, "No data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            barcodeReader.release();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (barcodeReader != null) {
            startstopScan(false);
            // unregister barcode event listener
            barcodeReader.removeBarcodeListener(this);            // close BarcodeReader to clean up resources.
            // unregister trigger state change listener
            barcodeReader.removeTriggerListener(this);
            barcodeReader.close();
            barcodeReader = null;
        }

        if (manager != null) {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }
        Log.d(TAG, "datenbank anzahl: " + Long.toString(database.getDataCount()));
        database.close();
    }
}
