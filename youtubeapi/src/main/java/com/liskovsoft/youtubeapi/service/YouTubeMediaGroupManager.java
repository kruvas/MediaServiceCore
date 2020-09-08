package com.liskovsoft.youtubeapi.service;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.mediaserviceinterfaces.MediaGroupManager;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.youtubeapi.browse.models.sections.BrowseSection;
import com.liskovsoft.youtubeapi.browse.models.sections.BrowseTab;
import com.liskovsoft.youtubeapi.browse.models.sections.TabbedBrowseResultContinuation;
import com.liskovsoft.youtubeapi.service.data.YouTubeMediaGroup;
import com.liskovsoft.youtubeapi.service.internal.MediaGroupManagerInt;
import com.liskovsoft.youtubeapi.service.internal.YouTubeMediaGroupManagerSigned;
import com.liskovsoft.youtubeapi.service.internal.YouTubeMediaGroupManagerUnsigned;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

import java.util.ArrayList;
import java.util.List;

public class YouTubeMediaGroupManager implements MediaGroupManager {
    private static final String TAG = YouTubeMediaGroupManager.class.getSimpleName();
    private static YouTubeMediaGroupManager sInstance;
    private final YouTubeSignInManager mSignInManager;
    private MediaGroupManagerInt mMediaGroupManagerReal;

    private YouTubeMediaGroupManager() {
        Log.d(TAG, "Starting...");

        mSignInManager = YouTubeSignInManager.instance();
    }

    public static MediaGroupManager instance() {
        if (sInstance == null) {
            sInstance = new YouTubeMediaGroupManager();
        }

        return sInstance;
    }

    @Override
    public MediaGroup getSearch(String searchText) {
        checkSigned();

        return mMediaGroupManagerReal.getSearch(searchText);
    }

    @Override
    public Observable<MediaGroup> getSearchObserve(String searchText) {
        return Observable.fromCallable(() -> getSearch(searchText));
    }

    @Override
    public MediaGroup getSubscriptions() {
        Log.d(TAG, "Getting subscriptions...");

        checkSigned();

        return mMediaGroupManagerReal.getSubscriptions();
    }

    @Override
    public Observable<MediaGroup> getSubscriptionsObserve() {
        return Observable.fromCallable(this::getSubscriptions);
    }

    @Override
    public MediaGroup getRecommended() {
        Log.d(TAG, "Getting recommended...");

        checkSigned();

        BrowseTab homeTab = mMediaGroupManagerReal.getHomeTab();

        BrowseSection recommended = null;

        if (homeTab.getSections() != null) {
            recommended = homeTab.getSections().get(0); // first one is recommended
        }

        return YouTubeMediaGroup.from(recommended);
    }

    @Override
    public Observable<MediaGroup> getRecommendedObserve() {
        return Observable.fromCallable(this::getRecommended);
    }

    @Override
    public MediaGroup getHistory() {
        Log.d(TAG, "Getting history...");

        checkSigned();

        return mMediaGroupManagerReal.getHistory();
    }

    @Override
    public Observable<MediaGroup> getHistoryObserve() {
        return Observable.fromCallable(this::getHistory);
    }

    @Override
    public List<MediaGroup> getHome() {
        checkSigned();

        BrowseTab tab = mMediaGroupManagerReal.getHomeTab();

        List<MediaGroup> result = new ArrayList<>();

        String nextPageKey = tab.getNextPageKey();
        List<MediaGroup> groups = YouTubeMediaGroup.from(tab.getSections());

        if (groups.isEmpty()) {
            Log.e(TAG, "Home group is empty");
        }

        while (!groups.isEmpty()) {
            result.addAll(groups);
            TabbedBrowseResultContinuation continuation = mMediaGroupManagerReal.continueTab(nextPageKey);

            if (continuation == null) {
                break;
            }

            nextPageKey = continuation.getNextPageKey();
            groups = YouTubeMediaGroup.from(continuation.getSections());
        }

        return result;
    }

    @Override
    public Observable<List<MediaGroup>> getHomeObserve() {
        return Observable.create(emitter -> {
            checkSigned();

            BrowseTab tab = mMediaGroupManagerReal.getHomeTab();

            emitGroups(emitter, tab);
        });
    }

    @Override
    public Observable<List<MediaGroup>> getMusicObserve() {
        return Observable.create(emitter -> {
            checkSigned();

            BrowseTab tab = mMediaGroupManagerReal.getMusicTab();

            emitGroups(emitter, tab);
        });
    }

    @Override
    public Observable<List<MediaGroup>> getNewsObserve() {
        return Observable.create(emitter -> {
            checkSigned();

            BrowseTab tab = mMediaGroupManagerReal.getNewsTab();

            emitGroups(emitter, tab);
        });
    }

    @Override
    public Observable<List<MediaGroup>> getGamingObserve() {
        return Observable.create(emitter -> {
            checkSigned();

            BrowseTab tab = mMediaGroupManagerReal.getGamingTab();

            emitGroups(emitter, tab);
        });
    }

    private void emitGroups(ObservableEmitter<List<MediaGroup>> emitter, BrowseTab tab) {
        String nextPageKey = tab.getNextPageKey();
        List<MediaGroup> groups = YouTubeMediaGroup.from(tab.getSections());

        if (groups.isEmpty()) {
            Log.e(TAG, "Music group is empty");
        }

        while (!groups.isEmpty()) {
            emitter.onNext(groups);
            TabbedBrowseResultContinuation continuation = mMediaGroupManagerReal.continueTab(nextPageKey);

            if (continuation == null) {
                break;
            }

            nextPageKey = continuation.getNextPageKey();
            groups = YouTubeMediaGroup.from(continuation.getSections());
        }

        emitter.onComplete();
    }

    @Override
    public MediaGroup continueGroup(MediaGroup mediaGroup) {
        checkSigned();

        return mMediaGroupManagerReal.continueGroup(mediaGroup);
    }

    @Override
    public Observable<MediaGroup> continueGroupObserve(MediaGroup mediaGroup) {
        return Observable.create(emitter -> {
            emitter.onNext(continueGroup(mediaGroup));

            emitter.onComplete();
        });
    }

    private void checkSigned() {
        if (mSignInManager.isSigned()) {
            Log.d(TAG, "User signed.");

            mMediaGroupManagerReal = YouTubeMediaGroupManagerSigned.instance();
            YouTubeMediaGroupManagerUnsigned.unhold();
        } else {
            Log.d(TAG, "User doesn't signed.");

            mMediaGroupManagerReal = YouTubeMediaGroupManagerUnsigned.instance();
            YouTubeMediaGroupManagerSigned.unhold();
        }
    }
}
