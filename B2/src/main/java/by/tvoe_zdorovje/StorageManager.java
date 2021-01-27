package by.tvoe_zdorovje;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class StorageManager {
    private static final Logger LOGGER = Logger.getLogger("StorageManager");

    private static final String PROJECT_ID = "ars-major";
    private static final String BUCKET_NAME = "ars-major.appspot.com"; // host

    private static Bucket bucket;

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
        LOGGER.info("Load ENV_VARs metadata.");
        return bucket.get("ENV_VARs").getMetadata();
    }

    public static int uploadResources(String theme, byte[] img) throws InterruptedException, IOException {
        LOGGER.info("Upload image...");

        StringBuilder filenameBuilder = new StringBuilder("resources/images/")
                .append(theme)
                .append("/");

        String prefix = filenameBuilder.toString();

        Thread.sleep(2); // for another value in the name
        LocalDate date = LocalDate.now(ZoneOffset.ofHours(3)); // UTC+3
        filenameBuilder
                .append(theme)
                .append("-")
                .append(date)
                .append("-")
                .append(System.currentTimeMillis() % 10000000);

        String filename = filenameBuilder.toString();
        bucket.create(filename, img, "image/jpeg", Bucket.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));

        filenameBuilder.delete(prefix.length(), filenameBuilder.length());

        LOGGER.info("Image uploaded: " + filename);
        return 1;
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
