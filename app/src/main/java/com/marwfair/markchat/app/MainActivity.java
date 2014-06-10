package com.marwfair.markchat.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

/**
 * MarkChat is a chat app. Even though you can't actually chat with other users, it is
 * a good example of how fragments work and populating a listvew from a SQLite database.
 * It also parses out the user's message to find all mentions, emoticons, and web links.
 * MarkChat will download, cache, and display the emoticons in the chat messages, highlights
 * mentions, and even grabs the title of web pages.
 *
 * Created by marwfair on 6/5/2014.
 */
public class MainActivity extends ActionBarActivity implements SignInFragment.OnSignInListener, ChatFragment.OnChatListener {

    // MarkChat settings
    public static final String MARKCHAT_PREFS = "MarkChatPrefs";
    public static String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restore preferences, which for now, is just the username.
        final SharedPreferences settings = getSharedPreferences(MARKCHAT_PREFS, 0);
        username = settings.getString("username", "");

        if (savedInstanceState == null) {

            /*
             * Check if a user is signed in. If not, go to the sign-in fragment.
             */
            if (username.equals("")) {
                this.getSupportActionBar().hide();
                getSupportFragmentManager().beginTransaction().add(R.id.container, new SignInFragment()).commit();
            } else {
                getSupportFragmentManager().beginTransaction().add(R.id.container, new ChatFragment()).commit();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {

            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.about_markchat))
                    .setMessage(getText(R.string.about_message))
                    .setCancelable(true)
                    .setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

            return true;
        }
        if (id == R.id.action_signout) {

            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.are_you_sure))
                    .setMessage(getText(R.string.signout_message))
                    .setCancelable(true)
                    .setPositiveButton(getText(R.string.yes), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Set username to be blank.
                            SharedPreferences settings = getSharedPreferences(MARKCHAT_PREFS, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("username", "");
                            editor.apply();

                            // Empty the database.
                            MarkChatDBHelper markChatDbHelper = new MarkChatDBHelper(getApplicationContext());
                            SQLiteDatabase markChatDb = markChatDbHelper.getWritableDatabase();
                            markChatDb.delete(MarkChatContract.MarkChatEntry.TABLE_NAME, null, null);

                            // Go to sign in fragment.
                            getSupportActionBar().hide();
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, new SignInFragment()).commit();
                        }
                    })
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    * This method is called from the ChatFragment.
    */
    @Override
    public void onChatListener() {

    }

    /*
     * This method is called from the SignInFragment. This will sign-in the user, and switch to the
     * ChatFragment.
    */
    @Override
    public void onSignIn(String username) {

        // Close the keyboard.
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        SharedPreferences settings = getSharedPreferences(MARKCHAT_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", username);
        editor.apply();

        this.username = username;

        this.getSupportActionBar().show();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new ChatFragment()).commit();
    }
}
