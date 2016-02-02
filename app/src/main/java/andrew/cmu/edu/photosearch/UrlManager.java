package andrew.cmu.edu.photosearch;

import android.net.Uri;

import java.net.URLEncoder;

/**
 * Author  : KAILIANG CHEN
 * Version : 0.1
 * Date    : 1/17/16
 *
 *
     Flickr API

     You can make this call to the Flickr API to return a JSON object with a list of photos.

     https://api.flickr.com/services/rest/?method=flickr.photos.search&
           api_key=178069b03af62f5735258c0a10a14d6e&format=json&nojsoncallback=1&text=kittens

     The text parameter should be replaced with the query that the user enters into the app.

     The JSON response you'll receive will have items described like this example.

     {
         "id": "23451156376",
             "owner": "28017113@N08",
             "secret": "8983a8ebc7",
             "server": "578",
             "farm": 1,
             "title": "Merry Christmas!",
             "ispublic": 1,
             "isfriend": 0,
             "isfamily": 0
     },
     You can use these parameters to get the full URL of the photo:

     http://farm{farm}.static.flickr.com/{server}/{id}_{secret}.jpg
     So, using our example from before, the URL would be

     http://farm1.static.flickr.com/578/23451156376_8983a8ebc7.jpg

     If interested, more documentation about the search endpoint can be found at
     https://www.flickr.com/services/api/explore/flickr.photos.search.

     You can generate your own at https://www.flickr.com/services/api/misc.api_keys.html.
 *
 */
public class UrlManager {
    private static final String TAG = UrlManager.class.getSimpleName();

    public static final String API_KEY = "178069b03af62f5735258c0a10a14d6e";
    public static final String PREF_SEARCH_QUERY ="searchQuery";

    private static final String ENDPOINT = "http://kantu.superlin.cc/api/zhihu_spider";
    private static final String METHOD_GETRECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";

    private static volatile UrlManager instance = null;
    private static final String DEFAULT_URL = "https://www.zhihu.com/question/36979922";
    private static final String PARAM = "?url=";

    private UrlManager() {

    }

    public static UrlManager getInstance() {
        if (instance == null) {
            synchronized (UrlManager.class) {
                if (instance == null) {
                    instance = new UrlManager();
                }
            }
        }
        return instance;
    }

    public static String getItemUrl(String query, int page) {
        String url;
        if (query != null) {
            url = ENDPOINT + PARAM + query;
        } else {
            url = ENDPOINT + PARAM + DEFAULT_URL;
        }
        return url;
    }
}
