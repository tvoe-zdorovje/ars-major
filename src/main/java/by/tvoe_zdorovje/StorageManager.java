package by.tvoe_zdorovje;

import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageManager {
    private static final String STORAGE_HOST = "https://storage.cloud.google.com/";
    private static final String PROJECT_ID = "ars-major";
    private static final String BUCKET_NAME = "ars-major.appspot.com"; // host
    public static final String BUCKET_URL = STORAGE_HOST + BUCKET_NAME;

    private static Bucket bucket;

    private static final Map<String, String> CACHE = new HashMap<>();

    static {
        try {
            Credentials credentials = GoogleCredentials.getApplicationDefault();
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(PROJECT_ID).build().getService();
            bucket = storage.get(BUCKET_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // FIXME debug cache
    static {
        CACHE.put("resource/images/art-painting/", "[\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-1.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-10.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-11.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-12.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-13.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-14.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-15.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-16.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-17.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-18.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-2.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-3.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-4.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-5.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-6.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-7.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-8.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/art-painting/a-p-9.jpg\"]");
        CACHE.put("resource/images/dec-plaster/", "[\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/dec-plaster/d-p-1.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/dec-plaster/d-p-10.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/dec-plaster/d-p-11.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/dec-plaster/d-p-2.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/dec-plaster/d-p-3.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/dec-plaster/d-p-4.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/dec-plaster/d-p-5.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/dec-plaster/d-p-6.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/dec-plaster/d-p-7.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/dec-plaster/d-p-8.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/dec-plaster/d-p-9.jpg\"]");
        CACHE.put("resource/images/bas-relief/", "[\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-1.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-10.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-11.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-12.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-13.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-14.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-15.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-2.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-3.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-4.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-5.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-6.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-7.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-8.jpg\",\"https://storage.cloud.google.com/ars-major.appspot.com/resources/images/bas-relief/b-r-9.jpg\"]");
    }

    public static Map<String, String> getMailjetData() {
        return bucket.get("mailjet").getMetadata();
    }

    public static String getResourceList(String resource) {
        String cached = CACHE.get(resource);
        if (null != cached && !cached.isBlank()) {
            System.out.println("get [" + resource + "] from cache");
            return cached;
        }

        Page<Blob> blobPage = bucket.list(Storage.BlobListOption.prefix(resource));

        int initCapacity = 125; // average number of images for the near future [ 01-2021 ] ( real 100 )
        List<String> URLs = new ArrayList<>(initCapacity);

        blobPage.getValues().forEach(blob -> {
            String URL = blob.getName();
            if (!URL.contains("carousel-")) {
                URLs.add("\"" + BUCKET_URL + "/" + URL + "\"");
            }
        });

        String jsonURLs = "[" + String.join(",", URLs) + "]";
        CACHE.put(resource, jsonURLs);

        return jsonURLs;
    }
}
