package com.photogenic.service;

import com.photogenic.model.pgModel;
import com.photogenic.model.userModel;
import com.photogenic.repository.pgRepository;
import com.photogenic.utility.jwtUtility;
import net.coobird.thumbnailator.Thumbnails;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import javax.imageio.ImageIO;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.properties.HorizontalAlignment;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.filters.Caption;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service public class pgService {

    @Autowired
    private pgRepository imageRepository;

    @Autowired
    private userService userService;

    @Autowired
    private jwtUtility jwtUtil;

    public String uploadImage(@NotNull MultipartFile file) throws IOException {
        String username=jwtUtil.getUsername();
        userModel user = userService.getUserByUsername(username);
        pgModel image = new pgModel();
        image.setName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setImageData(file.getBytes());
        pgModel savedImage = imageRepository.save(image);
        user.getPgModel().add(savedImage);
        userService.saveUser(user);
        return savedImage.getId();
    }

    public List<pgModel> getAllImages() {
        String userName = jwtUtil.getUsername();
        userModel user = userService.getUserByUsername(userName);
        List<pgModel> all= user.getPgModel();
        if( all != null && !all.isEmpty()){
            return all;
        }
        return null;

    }

    public byte[] getImageById(String id) {
        Optional<pgModel> imageOptional = imageRepository.findById(id);
        if (imageOptional.isPresent()) {
            return imageOptional.get().getImageData();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }
    }

    public pgModel getImageMetadataById(String id) {
        return imageRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));
    }

    public void deleteImage(String id) {
        String username=jwtUtil.getUsername();
        userModel user = userService.getUserByUsername(username);
        user.getPgModel().removeIf(x->x.getId().equals(id));
        userService.saveUser(user);
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
            System.out.println("Image deleted Successfully!!!");
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found!!!");
        }
    }

    public pgModel updateImageData(String id, String newName, String newContentType) {
        Optional<pgModel> imageEntityOptional = imageRepository.findById(id);
        if (imageEntityOptional.isPresent()) {
            pgModel image = imageEntityOptional.get();
            image.setName(newName);
            image.setContentType(newContentType);
            return imageRepository.save(image);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not Found!!!");
        }
    }

    public boolean updateImageFile(String id, MultipartFile file) throws IOException {
        pgModel existingImage = imageRepository.findById(id).orElse(null);
        if (existingImage != null) {
            existingImage.setName(file.getOriginalFilename());
            existingImage.setContentType(file.getContentType());
            existingImage.setImageData(file.getBytes());
            imageRepository.save(existingImage);
            return true;
        }
        return false;
    }

    public ResponseEntity<byte[]> resizeImage(MultipartFile file, int width, int height) throws IOException {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive integers.");
        }

        String outputFormat = "jpeg";
        MediaType outputMediaType = MediaType.IMAGE_JPEG;

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (file.getContentType() != null && file.getContentType().equalsIgnoreCase("image/png")) {
            outputFormat = "png";
            outputMediaType = MediaType.IMAGE_PNG;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(originalImage)
                .size(width, height)
                .outputQuality(0.75f)
                .outputFormat(outputFormat)
                .toOutputStream(outputStream);

        System.out.println("Resized width: " + width + " , Resized height: " + height);

        byte[] resizedImageBytes = outputStream.toByteArray();

        return ResponseEntity.ok()
                .contentType(outputMediaType)
                .body(resizedImageBytes);
    }


    public ResponseEntity<byte[]> rotateImage(MultipartFile file, double angle) throws IOException {

        String outputFormat = "jpeg";
        MediaType outputMediaType = MediaType.IMAGE_JPEG;

        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        if (file.getContentType() != null && file.getContentType().equalsIgnoreCase("image/png")) {
            outputFormat = "png";
            outputMediaType = MediaType.IMAGE_PNG;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(originalImage)
                .scale(1.0)
                .rotate(angle)
                .outputQuality(0.75f)
                .outputFormat(outputFormat)
                .toOutputStream(outputStream);

        System.out.println("Image rotated by: " + angle + " degrees");

        byte[] rotatedImageBytes = outputStream.toByteArray();

        return ResponseEntity.ok()
                .contentType(outputMediaType)
                .body(rotatedImageBytes);
    }


    public ResponseEntity<byte[]> cropImage(MultipartFile file, int x, int y,int height, int width ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Input file cannot be null or empty.");
        }
        if (x < 0 || y < 0 || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Crop coordinates and dimensions must be positive or zero for x, y.");
        }
        String outputFormat = "jpeg";
        MediaType outputMediaType = MediaType.IMAGE_JPEG;
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (file.getContentType() != null && file.getContentType().equalsIgnoreCase("image/png")) {
            outputFormat = "png";
            outputMediaType = MediaType.IMAGE_PNG;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .sourceRegion(x, y, width, height)
                .size(width, height)
                .outputQuality(0.75f)
                .outputFormat(outputFormat)
                .toOutputStream(outputStream);

        System.out.println("Cropped Image Size: " + width + "x" + height);

        byte[] croppedImageBytes = outputStream.toByteArray();

        return ResponseEntity.ok()
                .contentType(outputMediaType)
                .body(croppedImageBytes);
    }


    public ResponseEntity<byte[]> addWaterMark(MultipartFile file, String title) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Input file cannot be null or empty.");
            }
            if (title == null || title.isEmpty()) {
                throw new IllegalArgumentException("Watermark title cannot be null or empty.");
            }
            String outputFormat = "jpeg"; // Default to JPG
            MediaType outputMediaType = MediaType.IMAGE_JPEG;
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (file.getContentType() != null && file.getContentType().equalsIgnoreCase("image/png")) {
                outputFormat = "png";
                outputMediaType = MediaType.IMAGE_PNG;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Font font = new Font("Arial", Font.BOLD, 26);
            Color color = new Color(255, 255, 255, 150);
            Positions position = Positions.BOTTOM_RIGHT;
            int insets = 20;
            Thumbnails.of(originalImage)
                    .scale(1.0)
                    .addFilter(new Caption(title, font, color, position, insets))
                    .outputQuality(0.75f)
                    .outputFormat(outputFormat)
                    .toOutputStream(outputStream);
            System.out.println("Watermark added: " + title);
            byte[] watermarkedImageBytes = outputStream.toByteArray();
            return ResponseEntity.ok()
                    .contentType(outputMediaType)
                    .body(watermarkedImageBytes);

        } catch (IOException e) {
            System.err.println("Error processing image for watermarking: " + e.getMessage());
            throw new RuntimeException("Failed to add watermark to the image", e);
        }
    }

    public byte[] convertToJPG(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
//          String originalExtension = (originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".") + 1) : "unknown");

            BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            ImageIO.write(inputImage, "jpg", outputStream);
            System.out.println("Converted file extension: jpg");

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert image to JPG", e);
        }
    }


    public ResponseEntity<byte[]> convertImageToPDF(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Input file cannot be null or empty.");
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(byteArrayOutputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            ImageData imageData = ImageDataFactory.create(file.getBytes());
            Image pdfImage = new Image(imageData);

            pdfImage.setAutoScale(true);
            pdfImage.setHorizontalAlignment(HorizontalAlignment.CENTER);

//             float documentWidth = document.getPageEffectiveArea().getWidth();
//             pdfImage.setWidth(documentWidth);
//             pdfImage.setAutoScaleHeight(true);

            document.add(pdfImage);
            document.close();
            pdf.close();

            byte[] pdfBytes = byteArrayOutputStream.toByteArray();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (IOException e) {
            System.err.println("Error converting image to PDF: " + e.getMessage());
            throw new RuntimeException("Failed to convert image to PDF", e);
        }
    }


    public byte[] mirrorImage(MultipartFile file, String direction) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        BufferedImage mirroredImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        if ("horizontal".equalsIgnoreCase(direction)) {
            for (int x = 0; x < originalImage.getWidth(); x++) {
                for (int y = 0; y < originalImage.getHeight(); y++) {
                    mirroredImage.setRGB(originalImage.getWidth() - 1 - x, y, originalImage.getRGB(x, y));
                }
            }
        } else if ("vertical".equalsIgnoreCase(direction)) {
            for (int x = 0; x < originalImage.getWidth(); x++) {
                for (int y = 0; y < originalImage.getHeight(); y++) {
                    mirroredImage.setRGB(x, originalImage.getHeight() - 1 - y, originalImage.getRGB(x, y));
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid direction. Use 'horizontal' or 'vertical'.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(mirroredImage, "jpg", baos);
        baos.flush();
        return baos.toByteArray();
    }

    public ResponseEntity<byte[]> compressImage(MultipartFile file, float quality) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Input file cannot be null or empty.");
        }

        if (quality < 0.0f || quality > 1.0f) {
            throw new IllegalArgumentException("Quality must be between 0.0 and 1.0.");
        }

        String outputFormat = "jpeg";
        MediaType outputMediaType = MediaType.IMAGE_JPEG;

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (file.getContentType() != null && file.getContentType().equalsIgnoreCase("image/png")) {
            outputFormat = "png";
            outputMediaType = MediaType.IMAGE_PNG;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .scale(1.0)
                .outputQuality(quality)
                .outputFormat(outputFormat)
                .toOutputStream(outputStream);

        System.out.println("Image compressed with quality: " + quality);

        byte[] compressedImageBytes = outputStream.toByteArray();

        return ResponseEntity.ok()
                .contentType(outputMediaType)
                .body(compressedImageBytes);
    }

}
