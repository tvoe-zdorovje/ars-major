package by.tvoe_zdorovje;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;

public class ImageProcessor {

    /**
     * For manual processing of existing images. Saves processed images to '../processed/*' .
     *
     * @param args [0] - path to the folder with images;
     * args [1] - watermark;
     **/
    public static void main(String[] args) throws IOException {
        ImageProcessor processor = new ImageProcessor(args[1]);
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
                    byte[] img = processor.process(inputStream, false, true);
                    outputStream.write(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return FileVisitResult.CONTINUE;
            }
        };

        Files.walkFileTree(inputDir, visitor);
    }


    private static final int MAX_WIDTH = 1152; // ____ max width in gallery 1140px
    private static final int MAX_HEIGHT = 864; // _/
    private static final float DEFAULT_COMPRESSION_QUALITY = 0.7f;

    private final BufferedImage WATERMARK;
    private float compressionQuality = DEFAULT_COMPRESSION_QUALITY;


    public ImageProcessor(String path) throws IOException {
        WATERMARK = ImageIO.read(new FileInputStream(path));
    }

    // prod
    public ImageProcessor() throws IOException {
        String path = ImageProcessor.class.getResource("").getPath();
        path = path.substring(0, path.indexOf("WEB-INF")) + "resources/internal/images/watermark.png";
        WATERMARK = ImageIO.read(new FileInputStream(path));
    }

    /**
     * Resizes images, compresses and adds a watermark
     *
     * @param imgInputStream  - byte array of image;
     * @param watermark - is a watermark required;
     * @param manually  - manual image processing uses an algorithm that produces a smoother, smaller image, but requires much more RAM.
     * @return a {@code byte[]} of processed image.
     **/

    public byte[] process(InputStream imgInputStream, boolean watermark, boolean manually) throws IOException {
        if (imgInputStream.available()==0) throw new IllegalStateException("Input Stream is empty.");
        BufferedImage scaled;
        if (manually) {
            BufferedImage original = ImageIO.read(imgInputStream);
            int height = original.getHeight(), width = original.getWidth();
            scaled = scale(original, height, width);
            evaluateQuality(Math.max(height, width));

            original.flush();
            original = null;

            Runtime.getRuntime().gc();
        } else {
            scaled = scale(imgInputStream);
        }

        if (watermark) {
            addWatermark(scaled);
            Runtime.getRuntime().gc();
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No writers found");
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
            ImageWriter writer = writers.next();
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(compressionQuality);

            writer.write(null, new IIOImage(scaled, null, null), param);

            return outputStream.toByteArray();
        }
    }

    // https://stackoverflow.com/questions/3294388/make-a-bufferedimage-use-less-ram
    public BufferedImage scale(InputStream imgInputStream) throws IOException {
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(imgInputStream)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);

            if (!readers.hasNext()) {
                throw new IOException("No reader available for supplied image stream.");
            }

            ImageReader reader = readers.next();

            ImageReadParam imageReaderParams = reader.getDefaultReadParam();
            reader.setInput(inputStream);

            Dimension d1 = new Dimension(reader.getWidth(0), reader.getHeight(0));
            Dimension d2 = new Dimension(MAX_WIDTH, MAX_HEIGHT);

            int subsampling = 1;

            if (d1.getWidth() > d2.getWidth()) {
                subsampling = (int) Math.round(d1.getWidth() / d2.getWidth());
            } else if (d1.getHeight() > d2.getHeight()) {
                subsampling = (int) Math.round(d1.getHeight() / d2.getHeight());
            }

            imageReaderParams.setSourceSubsampling(subsampling, subsampling, 0, 0);
            BufferedImage scaled = reader.read(0, imageReaderParams);

            evaluateQuality(Math.max(reader.getHeight(0), reader.getWidth(0)));

            return scaled;
        }
    }

    private BufferedImage scale(BufferedImage original, int height, int width) {
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

    private void evaluateQuality(int resolution) {
        compressionQuality = DEFAULT_COMPRESSION_QUALITY;
        if (resolution >= 1280) compressionQuality += 0f;
        else if (resolution >= 1024) compressionQuality += 0.05f;
        else if (resolution >= 960) compressionQuality += 0.1f;
        else compressionQuality += 0.2f;
    }

    private void addWatermark(BufferedImage image) throws IOException {
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
}
