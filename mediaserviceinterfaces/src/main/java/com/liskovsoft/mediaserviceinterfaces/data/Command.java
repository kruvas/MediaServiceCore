package com.liskovsoft.mediaserviceinterfaces.data;

public interface Command {
    int TYPE_OPEN_VIDEO = 0;
    int TYPE_SEEK = 1;
    int TYPE_PLAY = 2;
    int TYPE_PAUSE = 3;
    int TYPE_GET_STATE = 4;
    int getType();
    String getVideoId();
    String getPlaylistId();
    long getCurrentTimeMs();
}
