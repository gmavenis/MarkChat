package com.marwfair.markchat.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ChatFragment extends Fragment {

    private OnChatListener onChatListener;

    // Views
    private EditText messageEt;
    private Button sendBtn;
    private ListView messageList;

    // Adapter
    private MessageAdapter messageAdapter;

    // Fonts
    private Typeface regularFont;
    private Typeface lightFont;

    // Database
    private MarkChatDBHelper markChatDbHelper;
    private SQLiteDatabase markChatDb;

    // Holds emoticon images
    private HashMap<String, Drawable> imageCache = new HashMap<String, Drawable>();

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment ChatFragment.
     */
    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }
    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.fragment_chat, container, false);

        messageList = (ListView)layout.findViewById(R.id.messagelist);
        messageEt = (EditText)layout.findViewById(R.id.message);
        sendBtn = (Button)layout.findViewById(R.id.send);

        // Assign fonts
        regularFont = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Regular.ttf");
        lightFont = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");

        // Get the database.
        markChatDbHelper = new MarkChatDBHelper(getActivity());
        markChatDb = markChatDbHelper.getWritableDatabase();

        // Populate the conversation list view.
        populateList();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = messageEt.getText().toString();

                if (!message.isEmpty()) {

                    /*
                     * This will create the JSON string and format the string as HTML;
                     * story the message in the database, then update the conversation
                     * listview.
                     */
                    new SendMessageTask().execute(message);

                    // Clear the message box.
                    messageEt.setText("");
                }
            }
        });

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onChatListener = (OnChatListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChatListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onChatListener = null;
    }

    /**
     * Populates the listview with all of the chat messages from the database.
     */
    private void populateList() {
        Cursor cursor = markChatDb.query(MarkChatContract.MarkChatEntry.TABLE_NAME, null, null, null, null, null, null);
        messageAdapter = new MessageAdapter(getActivity(), cursor, 0);
        messageList.setAdapter(messageAdapter);
    }

    /**
     * Adapter that populates the ListView with the messages
     * from the database. It will also parse the messages
     * to show mentions, emoticons, and web links.
     */
    private class MessageAdapter extends CursorAdapter {

        class ViewHolder {
            TextView usernameTv;
            TextView messageTv;
            TextView dateTv;
        }

        public MessageAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
        }

        @Override
        public View newView (Context context, Cursor cursor, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            View rowView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.message_row, parent, false);

            viewHolder.usernameTv = (TextView)rowView.findViewById(R.id.name);
            viewHolder.messageTv = (TextView)rowView.findViewById(R.id.message);
            viewHolder.dateTv = (TextView)rowView.findViewById(R.id.time);
            rowView.setTag(viewHolder);

            // Assign fonts for views
            viewHolder.usernameTv.setTypeface(regularFont);
            viewHolder.messageTv.setTypeface(regularFont);
            viewHolder.dateTv.setTypeface(lightFont);

            return rowView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder)view.getTag();

            viewHolder.usernameTv.setText(cursor.getString(cursor.getColumnIndexOrThrow(MarkChatContract.MarkChatEntry.COLUMN_USERNAME)));
            new ShowMessageTask(viewHolder.messageTv).execute(cursor.getString(cursor.getColumnIndexOrThrow(MarkChatContract.MarkChatEntry.COLUMN_HTML_FORMAT)));
            viewHolder.dateTv.setText(cursor.getString(cursor.getColumnIndexOrThrow(MarkChatContract.MarkChatEntry.COLUMN_DATE)));


            messageList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    Cursor cursor = markChatDb.rawQuery("SELECT * FROM " + MarkChatContract.MarkChatEntry.TABLE_NAME + " WHERE _ID = " + (position + 1), null);
                    cursor.moveToFirst();
                    String message = cursor.getString(cursor.getColumnIndexOrThrow(MarkChatContract.MarkChatEntry.COLUMN_JSON_FORMAT));

                    new AlertDialog.Builder(getActivity())
                            .setTitle(getText(R.string.json_string))
                            .setMessage(message)
                            .setCancelable(true)
                            .setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();

                    return false;
                }
            });
        }
    }

    public interface OnChatListener {
        public void onChatListener();
    }

    /**
     * Background task that generates the HTML and JSON strings of the message.
     * Then, it will update the database with the new record, and finally,
     * updates the listview.
     */
    class SendMessageTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... message) {

            String JSONMessage = ChatMessage.getJSONString(message[0]);
            String HTMLMessage = ChatMessage.getHTMLString(message[0]);

            // Create the timestamp.
            Date dNow = new Date( );
            SimpleDateFormat ft = new SimpleDateFormat("MMM'-'d h:mm a");
            String timeStamp = ft.format(dNow);

            // Save message to the database
            ContentValues values = new ContentValues();
            values.put(MarkChatContract.MarkChatEntry.COLUMN_USERNAME, MainActivity.username);
            values.put(MarkChatContract.MarkChatEntry.COLUMN_DATE, timeStamp);
            values.put(MarkChatContract.MarkChatEntry.COLUMN_MESSAGE, message[0]);
            values.put(MarkChatContract.MarkChatEntry.COLUMN_JSON_FORMAT, JSONMessage);
            values.put(MarkChatContract.MarkChatEntry.COLUMN_HTML_FORMAT, HTMLMessage);
            markChatDb.insert(MarkChatContract.MarkChatEntry.TABLE_NAME, null, values);

            return JSONMessage;
        }

        @Override
        protected void onPostExecute(String message) {

            // Add the message to the list view.
            Cursor cursor = markChatDb.rawQuery("SELECT * FROM " + MarkChatContract.MarkChatEntry.TABLE_NAME, null);
            cursor.moveToLast();
            messageAdapter.changeCursor(cursor);
        }
    }

    /**
     * Image getter for showing the emoticons in the message.
     */
    Html.ImageGetter imageGetter = new Html.ImageGetter() {
        public Drawable getDrawable(String source) {

            String fileName = Uri.parse(source).getLastPathSegment();

            try {
                Drawable drawable = imageCache.get(fileName);
                if (drawable == null) {
                    Bitmap bitmap = downloadImage(source);
                    drawable= new BitmapDrawable(getResources(), bitmap);

                    imageCache.put(fileName, drawable);
                }

                //drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                drawable.setBounds(0,0, 40, 40);
                return drawable;

            } catch(Exception e) {
                e.printStackTrace();;
                return null;
            }
        }
    };

    /**
     * Checks if emoticon already exists in the cache directory. If so, returns
     * the bitmap. If it does not exist, it will download the emoticon from the
     * url, then cache it in the cache directory.
     *
     * @param url
     * @return bitmap
     */
    private Bitmap downloadImage(String url) {
        String fileName = Uri.parse(url).getLastPathSegment();

        Bitmap bitmap = null;
        ByteArrayOutputStream outputStream;

        try {
            InputStream in = new java.net.URL(url).openStream();
            File imageFile = new File(getActivity().getCacheDir(), fileName);
            if (imageFile.exists()) {
                bitmap = BitmapFactory.decodeFile(imageFile.getPath());
            } else {

                outputStream = new ByteArrayOutputStream();

                int bytesRead;
                byte[] buffer = new byte[64];
                while ((bytesRead = in.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Write the file to the cache directory.
                FileOutputStream fos = new FileOutputStream(getActivity().getCacheDir() + fileName);
                fos.write(buffer);
                fos.close();

                // Create bitmap from the byte array.
                bitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.toByteArray().length);

                outputStream.flush();
                outputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * Populates the listview item with the message. This is done off of the UI thread so the
     * emoticons can be fetched and downloaded if needed.
     */
    class ShowMessageTask extends AsyncTask<String, Void, Spanned> {

        private TextView textView;

        public ShowMessageTask(TextView textView) {
            this.textView = textView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Spanned doInBackground(String... message) {

            Spanned spannedMessage = Html.fromHtml(message[0], imageGetter, null);
            return spannedMessage;
        }

        @Override
        protected void onPostExecute(Spanned spannedMessage) {
            textView.setText(spannedMessage);
        }
    }
}
