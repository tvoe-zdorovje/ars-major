package by.tvoe_zdorovje;

import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class StorageManager {
    private static final Logger LOGGER = Logger.getLogger("StorageManager");

    private static final String PROJECT_ID = "ars-major";
    private static final String BUCKET_NAME = "ars-major.appspot.com"; // host

    private static Bucket bucket;

    private static final Map<String, String> CACHE = new HashMap<>(5);

    static {
        try {
            LOGGER.info("[GET] init bucket");

            Credentials credentials = GoogleCredentials.getApplicationDefault();
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(PROJECT_ID).build().getService();
            bucket = storage.get(BUCKET_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getMediaLinks(String resource) {
        LOGGER.info("[GET] media links: " + resource);

        String cached;
        synchronized (CACHE) {
            cached = CACHE.get(resource);
        }
        if (null != cached && !cached.isBlank()) {
            LOGGER.info("get [" + resource + "] from cache");
            return cached;
        }

        Page<Blob> blobPage = bucket.list(Storage.BlobListOption.prefix(resource));

        int initCapacity = 150; // average number of images for the near future [ 01-2021 ] ( actual ~130 )
        ArrayList<String> carouselLinks = new ArrayList<>();
        ArrayList<String> galleryLinks = new ArrayList<>(initCapacity);

        Map<String, List<String>> links = new HashMap<>();
        links.put("carousel", carouselLinks);
        links.put("gallery", galleryLinks);

        blobPage.getValues().forEach(blob -> {
            String URL = blob.getName();
            if (URL.contains("carousel-")) {
                carouselLinks.add(blob.getMediaLink());
            } else {
                galleryLinks.add(blob.getMediaLink());
            }
        });

        String jsonLinks = JSONObject.valueToString(links);
        CACHE.put(resource, jsonLinks);

        return jsonLinks;
    }

    // not expected
    public static String getMediaLink(String resource) {
        LOGGER.info("[GET] media links: " + resource);
        Blob blob = bucket.get(resource);
        return blob.getMediaLink();
    }

    public static void invalidateCache(String theme) {
        StringBuilder prefix = new StringBuilder("resources/images/")
                .append(theme)
                .append("/");
        synchronized (CACHE) {
            CACHE.remove(prefix.toString());
        }
    }
}
