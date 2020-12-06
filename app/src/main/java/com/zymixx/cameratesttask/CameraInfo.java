package com.zymixx.cameratesttask;

import org.json.JSONObject;

public class CameraInfo {
    String camera_public;
    String camera_name;
    String camera_connected_server;
    String stream_url;
    String camera_model;
    String preview_url;
    String raw_offset;
    String id_uts;
    int idOfCam;
    static int counCam = 1;

    public CameraInfo(String camera_public, String camera_name,
                      String camera_connected_server, String stream_url,
                      String camera_model, String preview_url,
                      String raw_offset, String id_uts) {


        this.camera_public = camera_public;
        this.camera_name = camera_name;
        this.camera_connected_server = camera_connected_server;
        this.stream_url = stream_url;
        this.camera_model = camera_model;
        this.preview_url = preview_url;
        this.raw_offset = raw_offset;
        this.id_uts = id_uts;
        this.idOfCam = counCam;
        counCam++;
    }

}
