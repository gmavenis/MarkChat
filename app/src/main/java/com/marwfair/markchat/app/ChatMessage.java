package com.marwfair.markchat.app;

import android.util.Log;
import android.util.Patterns;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ChatMessage is a class of static utility methods for creating
 * the JSON and HTML formats of the chat message.
 *
 * Created by marwfair on 6/6/2014.
 */
public final class ChatMessage {

    private ChatMessage() {

    }

    /**
     * Returns the JSON format of the message as a string. It must not
     * be called from the UI thread because of the network connection
     * to get the title of the web page.
     *
     * @param message
     * @return message
     */
    public static String getJSONString(String message) {
        ArrayList<String> emoticonList;
        ArrayList<String> mentionList;
        ArrayList<String> urlList;

        // Get emoticons
        emoticonList = getEmoticons(message);

        // Get mentions
        mentionList = getMentions(message);

        // Get URLs
        urlList = getURLs(message);

        // Create JSON arrays from the ArrayLists.
        JSONObject messageObject = new JSONObject();

        JSONArray emoticonArray = new JSONArray();
        for (String emoticon : emoticonList) {
            try {
                emoticonArray.put(emoticon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        JSONArray mentionArray = new JSONArray();
        for (String mention : mentionList) {
            try {
                mentionArray.put(mention.replaceAll("@", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        JSONArray urlArray = new JSONArray();
        for (String url : urlList) {
            try {
                JSONObject urlObject = new JSONObject();
                urlObject.put("url", url);
                urlObject.put("title", getPageTitle(url).replaceAll("<.*?title>", ""));
                urlArray.put(urlObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            messageObject.put("emoticons", emoticonArray);
            messageObject.put("mentions", mentionArray);
            messageObject.put("links", urlArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("JSON", messageObject.toString());

        return messageObject.toString();
    }

    /**
     * This method will format the chat message to to include
     * HTML tags and get the URL of the emoticon. This must not
     * be called from the UI thread because of the check to make
     * sure the emoticon exists.
     *
     * @param message
     * @return HTML formatted string of chat message.
     */
    public static String getHTMLString(String message) {
        ArrayList<String> emoticonList;
        ArrayList<String> mentionList;

        emoticonList = getEmoticons(message);

        for (String emoticon : emoticonList) {
            if (emoticonExists("https://dujrsrsgsd3nh.cloudfront.net/img/emoticons/" + emoticon + ".png")) {
                message = message.replaceAll("\\(" + emoticon + "\\)", "<img src='https://dujrsrsgsd3nh.cloudfront.net/img/emoticons/" + emoticon + ".png'/>");
            } else if (emoticonExists("https://dujrsrsgsd3nh.cloudfront.net/img/emoticons/" + emoticon + ".gif")) {
                message = message.replaceAll("\\(" + emoticon + "\\)", "<img src='https://dujrsrsgsd3nh.cloudfront.net/img/emoticons/" + emoticon + ".gif'/>");
            }
        }

        mentionList = getMentions(message);

        for (String mention : mentionList) {
            message = message.replaceAll(mention, "<font color='#FF00FF'>" + mention + "</font>");
        }

        return message;
    }

    private static ArrayList getEmoticons(String message) {
        ArrayList<String> emoticonList = new ArrayList<String>();

        Pattern emoticonPattern = Pattern.compile("\\([a-zA-Z]++\\)");
        Matcher emoticonMatcher = emoticonPattern.matcher(message);
        while (emoticonMatcher.find()) {
            String emoticon = emoticonMatcher.group();
            emoticonList.add(emoticon.replaceAll("[()]", ""));
        }

        return emoticonList;
    }

    private static ArrayList getMentions(String message) {
        ArrayList<String> mentionList = new ArrayList<String>();

        Pattern mentionPattern = Pattern.compile("@[a-zA-Z]++");
        Matcher mentionMatcher = mentionPattern.matcher(message);
        while (mentionMatcher.find()) {
            String mention = mentionMatcher.group();
            mentionList.add(mention);
        }

        return mentionList;
    }

    private static ArrayList getURLs(String message) {
        ArrayList<String> urlList = new ArrayList<String>();

        Matcher urlMatcher = Patterns.WEB_URL.matcher(message);
        while(urlMatcher.find()) {
            String urlString = urlMatcher.group();
            urlList.add(urlString);
        }

        return urlList;
    }

    // Get page title
    private static String getPageTitle(String urlString) {
        int responseCode;
        String pageTitle = "";

        HttpURLConnection urlConnection = null;
        try {
            if (!urlString.startsWith("http")) {
                urlString = "http://" + urlString;
            }
            URL url = new URL(urlString.toLowerCase());
            urlConnection = (HttpURLConnection)url.openConnection();

            urlConnection.connect();
            responseCode = urlConnection.getResponseCode();

            switch(responseCode) {
                case 200: // Successful

                    // Read in the HTML
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();

                    // Parse out the title.
                    Pattern p = Pattern.compile("<title>(.*?)</title>");
                    Matcher m = p.matcher(sb.toString());
                    while (m.find() == true) {
                        pageTitle = m.group();
                    }

                    break;
                default:
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        return pageTitle;
    }

    // Check if emoticon exists
    private static boolean emoticonExists(String urlString) {
        int responseCode;
        boolean doesExist = true;

        HttpURLConnection urlConnection = null;
        try {

            URL url = new URL(urlString.toLowerCase());
            urlConnection = (HttpURLConnection)url.openConnection();

            urlConnection.connect();
            responseCode = urlConnection.getResponseCode();

            if (responseCode != 200) {
                doesExist = false;
            }

        } catch(Exception e) {
            doesExist = false;
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        return doesExist;
    }
}