package by.tvoe_zdorovje;

import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.mailjet.client.Base64;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
    private static final Map<String, String> ENV_VARs = new HashMap<>();

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

    private static Map<String, String> getEnvVars() {
        if (ENV_VARs.size() == 0) {
            LOGGER.info("Load ENV_VARs metadata.");
            ENV_VARs.putAll(bucket.get("ENV_VARs").getMetadata());
        }
        return ENV_VARs;
    }

    public static String getMediaLinks(String resource) {
        LOGGER.info("[GET] media links: " + resource);

        String cached = CACHE.get(resource);
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

    // TODO implement compression
    public static void uploadResources(String theme, List<JSONObject> files) throws InterruptedException, IOException {
        LOGGER.info("Upload resources..");
        StringBuilder filenameBuilder = new StringBuilder("resources/images/")
                .append(theme)
                .append("/");

        String prefix = filenameBuilder.toString();

        for (JSONObject file : files) {

            filenameBuilder.append(theme)
                    .append("-");
            LocalDate date = LocalDate.now(ZoneOffset.ofHours(3)); // UTC+3
            filenameBuilder.append(date).append("-").append(System.currentTimeMillis() % 10000000);
            String filename = filenameBuilder.toString();
            filenameBuilder.delete(prefix.length(), filenameBuilder.length());

            LOGGER.info("processing " + filename);

            byte[] bytes = Base64.decode(file.getString("Base64Content"));
            bytes = ImageProcessor.process(bytes, true);


            bucket.create(filename, bytes, "image/jpeg", Bucket.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));
            Thread.sleep(2); // to another value in name
        }

        CACHE.remove(prefix);
    }

    public static String getPassword() {
        return getEnvVars().get("PASS");
    }

    public static String getMailjetKey() {
        return getEnvVars().get("MJ_KEY");
    }

    public static String getMailjetValue() {
        return getEnvVars().get("MJ_VALUE");
    }

    public static String getMailjetFrom() {
        return getEnvVars().get("MJ_FROM");
    }

    public static String getMailjetTo() {
        return getEnvVars().get("MJ_TO");
    }
}
