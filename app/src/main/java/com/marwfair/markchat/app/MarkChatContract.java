package com.marwfair.markchat.app;

import android.provider.BaseColumns;

/**
 * Created by marwfair on 6/5/2014.
 */
public class MarkChatContract {

    public MarkChatContract() { };

    public abstract class MarkChatEntry implements BaseColumns {
        public static final String TABLE_NAME = "conversation";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_JSON_FORMAT = "json_format";
        public static final String COLUMN_HTML_FORMAT = "html_format";
        public static final String TEXT_TYPE = " TEXT";
        public static final String COMMA = ", ";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY" + COMMA +
                COLUMN_USERNAME + TEXT_TYPE + COMMA +
                COLUMN_DATE + TEXT_TYPE + COMMA +
                COLUMN_MESSAGE + TEXT_TYPE + COMMA +
                COLUMN_JSON_FORMAT + TEXT_TYPE + COMMA +
                COLUMN_HTML_FORMAT + TEXT_TYPE + ")";
        public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
