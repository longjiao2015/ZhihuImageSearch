package johnwan.test.photosearch;

import java.io.Serializable;

/**
 * Author  : KAILIANG CHEN
 * Version : 0.1
 * Date    : 1/17/16
 */
public class ZhihuGalleryItem implements Serializable {

    private String url;

    public ZhihuGalleryItem(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
