package com.tomhedges.bamboo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Used to retrieve data from the remote location, and parse JSON data from the response.
 * 
 * Incorporates code sourced from:  http://www.mybringback.com/tutorial-series/12924/android-tutorial-using-remote-databases-php-and-mysql-part-1/
 * And also:  http://stackoverflow.com/questions/693997/how-to-set-httpresponse-timeout-for-android-in-java
 * 
 * @see			RemoteDBTableRetrieval
 * @author      Tom Hedges
 */

public class JSONParser {
 
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
 
    public JSONParser() {
 
    }
    
    public JSONObject makeHttpRequest(String url, String method, List<NameValuePair> params) {

        Log.d(JSONParser.class.getName(), "Attempting retrieval from: " + url + ", using method: " + method);
        // Making HTTP request
        try {
        	HttpParams httpParameters = new BasicHttpParams();
        	// Set the timeout in milliseconds until a connection is established.
        	// The default value is zero, that means the timeout is not used. 
        	int timeoutConnection = 5000;
        	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        	// Set the default socket timeout (SO_TIMEOUT) 
        	// in milliseconds which is the timeout for waiting for data.
        	int timeoutSocket = 5000;
        	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        	DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

            // check for request method
            if(method == "POST"){
                // request method is POST
                // defaultHttpClient
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
 
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
 
            } else if(method == "GET"){
                // request method is GET
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);
 
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }           

        } catch (ConnectTimeoutException e) {
        	Log.e(JSONParser.class.getName(), "Connection could not be made - timed out.");
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
        	Log.e(JSONParser.class.getName(), "HTTP connection error.");
            e.printStackTrace();
            return null;
        } catch (ClientProtocolException e) {
        	Log.e(JSONParser.class.getName(), "HTTP connection error.");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
        	Log.e(JSONParser.class.getName(), "HTTP connection error.");
            e.printStackTrace();
            return null;
        } catch (Error e) {
        	Log.e(JSONParser.class.getName(), "HTTP connection error.");
            e.printStackTrace();
            return null;
        }
 
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
 
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
 
        return jObj;
    }
}

