package com.liskovsoft.youtubeapi.service.internal;

import com.liskovsoft.youtubeapi.actions.ActionsService;
import com.liskovsoft.youtubeapi.next.WatchNextServiceSigned;
import com.liskovsoft.youtubeapi.next.models.WatchNextResult;
import com.liskovsoft.youtubeapi.service.YouTubeSignInManager;
import com.liskovsoft.youtubeapi.track.TrackingService;
import com.liskovsoft.youtubeapi.videoinfo.VideoInfoServiceSigned;
import com.liskovsoft.youtubeapi.videoinfo.models.VideoInfoResult;

public class YouTubeMediaItemManagerSigned implements MediaItemManagerInt {
    private static YouTubeMediaItemManagerSigned sInstance;
    private final WatchNextServiceSigned mWatchNextServiceSigned;
    private final YouTubeSignInManager mSignInManager;
    private final TrackingService mTrackingService;
    private final VideoInfoServiceSigned mVideoInfoServiceSigned;
    private final ActionsService mActionsService;

    private YouTubeMediaItemManagerSigned() {
        mWatchNextServiceSigned = WatchNextServiceSigned.instance();
        mSignInManager = YouTubeSignInManager.instance();
        mTrackingService = TrackingService.instance();
        mVideoInfoServiceSigned = VideoInfoServiceSigned.instance();
        mActionsService = ActionsService.instance();
    }

    public static YouTubeMediaItemManagerSigned instance() {
        if (sInstance == null) {
            sInstance = new YouTubeMediaItemManagerSigned();
        }

        return sInstance;
    }

    public static void unhold() {
        sInstance = null;
        WatchNextServiceSigned.unhold();
    }

    @Override
    public WatchNextResult getWatchNextResult(String videoId) {
        return mWatchNextServiceSigned.getWatchNextResult(videoId, mSignInManager.getAuthorizationHeader());
    }

    @Override
    public VideoInfoResult getVideoInfo(String videoId) {
        return mVideoInfoServiceSigned.getVideoInfo(videoId, mSignInManager.getAuthorizationHeader());
    }

    @Override
    public void updateHistoryPosition(String videoId, String lengthSec, String eventId, String vmData, float positionSec) {
        mTrackingService.updateWatchTime(
                videoId, positionSec, Float.parseFloat(lengthSec), eventId,
                vmData, mSignInManager.getAuthorizationHeader());
    }

    @Override
    public void setLike(String videoId) {
        mActionsService.setLike(videoId, mSignInManager.getAuthorizationHeader());
    }

    @Override
    public void removeLike(String videoId) {
        mActionsService.removeLike(videoId, mSignInManager.getAuthorizationHeader());
    }

    @Override
    public void setDislike(String videoId) {
        mActionsService.setDislike(videoId, mSignInManager.getAuthorizationHeader());
    }

    @Override
    public void removeDislike(String videoId) {
        mActionsService.removeDislike(videoId, mSignInManager.getAuthorizationHeader());
    }

    @Override
    public void subscribe(String channelId) {
        mActionsService.subscribe(channelId, mSignInManager.getAuthorizationHeader());
    }

    @Override
    public void unsubscribe(String channelId) {
        mActionsService.unsubscribe(channelId, mSignInManager.getAuthorizationHeader());
    }
}