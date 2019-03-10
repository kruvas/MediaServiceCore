package com.liskovsoft.youtubeapi.common;

import com.liskovsoft.youtubeapi.auth.BrowserAuth;
import com.liskovsoft.youtubeapi.common.models.videos.VideoItem;
import com.liskovsoft.youtubeapi.content.ContentManager;
import com.liskovsoft.youtubeapi.search.SearchManager;
import com.liskovsoft.youtubeapi.search.SearchParams;
import com.liskovsoft.youtubeapi.search.models.NextSearchResult;
import com.liskovsoft.youtubeapi.search.models.SearchResult;
import com.liskovsoft.youtubeapi.support.utils.RetrofitHelper;
import retrofit2.Call;

import java.util.List;

/**
 * Wraps result from the {@link BrowserAuth}, {@link ContentManager} and {@link SearchManager}
 */
public class FrontendService {
    private SearchManager mSearchManager;
    private String mNextSearchPageKey;

    public List<VideoItem> startSearch(String searchText) {
        SearchManager manager = getSearchManager();

        Call<SearchResult> wrapper = manager.getSearchResult(SearchParams.getSearchKey(), SearchParams.getSearchQuery(searchText));
        SearchResult searchResult = RetrofitHelper.get(wrapper);


        if (searchResult == null) {
            throw new IllegalStateException("Invalid search result for text " + searchText);
        }

        mNextSearchPageKey = searchResult.getNextPageKey();

        return searchResult.getVideoItems();
    }

    /**
     * Method uses results from the {@link #startSearch(String)} call
     * @return video items
     */
    public List<VideoItem> getNextSearchPage() {
        if (mNextSearchPageKey == null) {
            throw new IllegalStateException("Can't get next search page. Next search key is empty.");
        }

        SearchManager manager = getSearchManager();
        Call<NextSearchResult> wrapper = manager.getNextPage(SearchParams.getSearchKey(), SearchParams.getNextSearchQuery(mNextSearchPageKey));
        NextSearchResult searchResult = RetrofitHelper.get(wrapper);

        if (searchResult == null) {
            throw new IllegalStateException("Invalid next page search result for key " + mNextSearchPageKey);
        }

        mNextSearchPageKey = searchResult.getNextPageKey();

        return searchResult.getVideoItems();
    }

    private SearchManager getSearchManager() {
        if (mSearchManager == null) {
            mSearchManager = RetrofitHelper.withJsonPath(SearchManager.class);
        }

        return mSearchManager;
    }
}