package com.zymixx.cameratesttask;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static android.content.ContentValues.TAG;

public class WorkAPI  {

    public static String getResponsFromURL(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static String parsing(String respons) {
        String statusJson = null;
        EnterInAccount.arrayOfCamInfo.removeAll(EnterInAccount.arrayOfCamInfo);
        try {
            JSONObject responsJson = new JSONObject(respons);
            statusJson = responsJson.getString("status");
            JSONObject camArray = responsJson.getJSONObject("data");
            JSONArray rlyArray = camArray.getJSONArray("cameras");
            for (int i = 0; i < 6; i++) {
                JSONObject firsJson = rlyArray.getJSONObject(i);
                if (rlyArray.getJSONObject(i) != null) {
                    String camera_public = firsJson.getString("camera_public");
                    String camera_name = firsJson.getString("camera_name");
                    String camera_connected_server = firsJson.getString("camera_connected_server");
                    String stream_url = firsJson.getString("stream_url");
                    String camera_model = firsJson.getString("camera_model");
                    String preview_url = firsJson.getString("preview_url");
                    JSONObject timezoneObj = firsJson.getJSONObject("timezone");
                    String raw_offset = timezoneObj.getString("raw_offset");
                    String id_uts = timezoneObj.getString("id");

                    CameraInfo camera = new CameraInfo(camera_public, camera_name,
                            camera_connected_server, stream_url,
                            camera_model, preview_url,
                            raw_offset, id_uts);

                    EnterInAccount.arrayOfCamInfo.add(camera);
                } else {
                    System.out.println(i + 1 + " всего элементов в масиве");
                    break;
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "parsing: " + e);
        }
        finally {return statusJson;}
    }

    public static void updateCamers (){
        String userName = "test_task";
        String password = "test_task";
        try {
            URL url = new URL("https://ms1.dev.camdrive.org/mobile/api_native/login/?username="+ userName + "&password="+ password);
            String respons = getResponsFromURL(url);
            parsing(respons);
        } catch (IOException e) {
            Log.e(TAG, "updateCamers: ", e );
        }

    }


}
