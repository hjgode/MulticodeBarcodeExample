package com.honeywell.multiscanreader;

import static com.honeywell.multiscanreader.ClientBarcodeActivity.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Database {
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + DataContract.DataEntry.TABLE_NAME + " (" +
                    DataContract.DataEntry._ID + " INTEGER PRIMARY KEY," +
                    DataContract.DataEntry.COLUMN_NAME_SERIAL + " TEXT," +
                    DataContract.DataEntry.COLUMN_NAME_MANDANT + " TEXT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DataContract.DataEntry.TABLE_NAME;

    Context _context;
    DataReaderDbHelper dbHelper;
    public Database(Context context){
        _context=context;
        dbHelper = new DataReaderDbHelper(_context);

    }

    public void close() {
        dbHelper.close();
    }

    public void writeData(String serial, String mandant){
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DataContract.DataEntry.COLUMN_NAME_SERIAL, serial);
        values.put(DataContract.DataEntry.COLUMN_NAME_MANDANT, mandant);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(DataContract.DataEntry.TABLE_NAME, null, values);

    }

    public long getRowCount(String m){
        long lRet=0;
        lRet = DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(),DataContract.DataEntry.TABLE_NAME,"mandant=\""+m+"\"");
        return lRet;
    }
    public long getDataCount(){
        long c = DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(),DataContract.DataEntry.TABLE_NAME,"serial NOT NULL");

        return c;
    }

    public List<String> getData(){
        List items=new ArrayList<String>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                DataContract.DataEntry.COLUMN_NAME_SERIAL,
                DataContract.DataEntry.COLUMN_NAME_MANDANT
        };
        Cursor cursor = db.query(
                DataContract.DataEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        while (cursor.moveToNext()) {
            String itemSerial = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.DataEntry.COLUMN_NAME_SERIAL));
            String itemMandant = cursor.getString(cursor.getColumnIndexOrThrow(DataContract.DataEntry.COLUMN_NAME_MANDANT));
            items.add("\"" + itemSerial + "\",\"" + itemMandant + "\"");
        }
        cursor.close();

        return items;
    }
    public String findData(String findstr) {
        String sRet = "";
        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                DataContract.DataEntry.COLUMN_NAME_SERIAL,
                DataContract.DataEntry.COLUMN_NAME_MANDANT
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = DataContract.DataEntry.COLUMN_NAME_SERIAL + " = ?";
        String[] selectionArgs = {findstr};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DataContract.DataEntry.COLUMN_NAME_MANDANT + " DESC";

        Cursor cursor = db.query(
                DataContract.DataEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        List itemIds = new ArrayList<>();
        while (cursor.moveToNext()) {
            String itemId = cursor.getString(
                    cursor.getColumnIndexOrThrow(DataContract.DataEntry.COLUMN_NAME_SERIAL));
            itemIds.add(itemId);
        }
        cursor.close();
        if(itemIds.size()>0)
            sRet = itemIds.get(0).toString();
        return sRet;
    }
    
    public void updatData(String serial, String mandant){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

// New value for one column
        String title = serial; //new
        ContentValues values = new ContentValues();
        values.put(DataContract.DataEntry.COLUMN_NAME_SERIAL, title);

// Which row to update, based on the title
        String selection = DataContract.DataEntry.COLUMN_NAME_SERIAL + " LIKE ?";
        String[] selectionArgs = { serial };//old

        int count = db.update(
                DataContract.DataEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

    }

    public void exportCSV(){
        File sd = Environment.getExternalStorageDirectory();
        List<String> items=getData();
        File csvFile = new File(sd, "hellweg.csv");
        try {
            OutputStreamWriter streamWriter = new FileWriter(csvFile);
            for (String s : items) {
                streamWriter.write(s + "\n");
                Log.d(TAG, s);
            }
            streamWriter.close();

        }catch(Exception ex){
            Log.d(TAG, ex.getMessage());
        }
    }

    public void clearData(){
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1,1);
        Toast.makeText(_context, "Daten gelöscht",Toast.LENGTH_LONG);
    }

    public Cursor showdata(String sMandant)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        if(sMandant.equals(""))
            cursor = db.rawQuery("select * from "+DataContract.DataEntry.TABLE_NAME,null);
        else
            cursor = db.rawQuery("select * from "+DataContract.DataEntry.TABLE_NAME +" WHERE mandant=?",new String[]{sMandant});
        return cursor;
    }
    //exporting database
    public void exportDB() {
        // TODO Auto-generated method stub

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = _context.getDatabasePath(DataReaderDbHelper.DATABASE_NAME);

            if (sd.canWrite()) {
                String  currentDBPath= dbHelper.getDatabaseName();
                String backupDBPath  = "/" + "hellweg.sqlite";
                File currentDB = data;//new File(data);//, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(_context, backupDB.toString(),
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

            Toast.makeText(_context, e.toString(), Toast.LENGTH_LONG)
                    .show();

        }
    }

}

