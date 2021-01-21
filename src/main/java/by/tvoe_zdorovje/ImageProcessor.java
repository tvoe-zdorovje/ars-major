package by.tvoe_zdorovje;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;

public class ImageProcessor {
    private static final int MAX_WIDTH = 1152; // ____ max width in gallery 1140px
    private static final int MAX_HEIGHT = 864; // _/
    private static BufferedImage WATERMARK;

    // for manual processing of existing images
    // args[0] - path to the folder with images; args[1] - watermark
    public static void main(String[] args) throws IOException {
        Path inputDir = Paths.get(args[0]);
        FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            private final String outputDir = inputDir.getParent() + "/processed/";

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                super.visitFile(file, attrs);
                System.out.println(file.toString());
                Path outputPath = Paths.get(outputDir + inputDir.relativize(file).toString());
                Files.createDirectories(outputPath.getParent());

                try (FileInputStream inputStream = new FileInputStream(file.toFile());
                     FileOutputStream outputStream = new FileOutputStream(outputPath.toFile())) {
                    byte[] img = inputStream.readAllBytes();
                    img = process(img, false);
                    outputStream.write(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return FileVisitResult.CONTINUE;
            }
        };

        Files.walkFileTree(inputDir, visitor);
    }

    public static byte[] process(byte[] image, boolean watermark) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(image));
        BufferedImage scaled = scale(original);

        if (watermark) {
            addWatermark(scaled);
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No writers found");
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
            ImageWriter writer = writers.next();
            writer.setOutput(ios);

            float quality = 0.75f;
            int resolution = Math.max(original.getHeight(), original.getWidth());

            if (resolution >= 1280) quality+=0f;
            else if (resolution >= 1024) quality+= 0.05f;
            else if (resolution >= 960) quality+= 0.01f;
            else quality+= 0.02f;

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);

            writer.write(null, new IIOImage(scaled, null, null), param);

            return outputStream.toByteArray();
        }
    }

    private static BufferedImage scale(BufferedImage original) {
        int height = original.getHeight(), width = original.getWidth();
        float scale;
        if (height <= MAX_HEIGHT && width <= MAX_WIDTH) {
            scale = 1f;
        } else if (height > width) {
            scale = (float) height / MAX_HEIGHT;
        } else {
            scale = (float) width / MAX_WIDTH;
        }

        height = Math.round(height / scale);
        width = Math.round(width / scale);

        Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics graphics = result.getGraphics();

        boolean b = graphics.drawImage(scaled, 0, 0, width, height, null);
        assert (b);
        graphics.dispose();
        return result;
    }


    private static void addWatermark(BufferedImage image) throws IOException {
        initWatermark();

        int imgWidth = image.getWidth(), imgHeight = image.getHeight();
        float watermarkScaleFactor = 7f;
        int watermarkWidth = Math.round(imgWidth / watermarkScaleFactor);
        int watermarkHeight = Math.round(watermarkWidth / (WATERMARK.getWidth() / (float) WATERMARK.getHeight()));

        Image scaledWatermark = WATERMARK.getScaledInstance(watermarkWidth, watermarkHeight, Image.SCALE_SMOOTH);

        Graphics2D g2d = (Graphics2D) image.getGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(scaledWatermark,
                imgWidth - watermarkWidth,
                imgHeight - watermarkHeight,
                null);

        g2d.dispose();
    }

    private static synchronized void initWatermark() throws IOException {
        if (WATERMARK == null) {
            String path = ImageProcessor.class.getResource("").getPath();
            path = path.substring(0, path.indexOf("WEB-INF")) + "resources/internal/images/watermark.png";
            WATERMARK = ImageIO.read(new FileInputStream(path));
        }
    }
}
