package com.honeywell.multiscanreader;

import android.provider.BaseColumns;

public final class DataContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DataContract() {}

    /* Inner class that defines the table contents */
    public static class DataEntry implements BaseColumns {
        public static final String TABLE_NAME = "seriennummern";
        public static final String COLUMN_NAME_SERIAL = "serial";
        public static final String COLUMN_NAME_MANDANT = "mandant";
    }
}
