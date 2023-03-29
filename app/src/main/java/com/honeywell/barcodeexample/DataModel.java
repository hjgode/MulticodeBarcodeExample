package com.honeywell.barcodeexample;

public class DataModel {

    String ID;
    String Serial;
    String Mandant;

    public DataModel(String id, String s, String m ) {
        this.ID=id;
        this.Serial=s;
        this.Mandant=m;

    }

    public String getSerial() {
        return Serial;
    }

    public String getMandant() {
        return Mandant;
    }

    public String getID() {
        return ID;
    }
}