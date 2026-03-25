package hsf.project_gr1.controller.api;

import hsf.project_gr1.model.entity.Product;
import hsf.project_gr1.model.entity.ProductAttachment;
import hsf.project_gr1.repository.ProductAttachmentRepository;
import hsf.project_gr1.security.CustomUserDetails;
import hsf.project_gr1.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductService productService;
    private final ProductAttachmentRepository attachmentRepository;

    @Value("${app.upload.dir:uploads/product-images}")
    private String uploadDir;

    @PostMapping
    public ResponseEntity<?> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!product.getSeller().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body("Bạn không có quyền upload ảnh cho sản phẩm này");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("Chỉ chấp nhận file ảnh");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("Kích thước ảnh không được vượt quá 5MB");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            String fileUrl = "/product-images/" + fileName;

            ProductAttachment attachment = ProductAttachment.builder()
                    .product(product)
                    .fileName(originalName != null ? originalName : fileName)
                    .fileUrl(fileUrl)
                    .fileType(contentType)
                    .fileSize(file.getSize())
                    .isHidden(false)
                    .build();

            return ResponseEntity.ok(attachmentRepository.save(attachment));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lưu file: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductAttachment>> getImages(@PathVariable Long productId) {
        return ResponseEntity.ok(attachmentRepository.findByProductIdAndIsHidden(productId, false));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!product.getSeller().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body("Bạn không có quyền xóa ảnh này");
        }

        ProductAttachment attachment = attachmentRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        // Delete physical file
        try {
            String fileName = attachment.getFileUrl().replace("/product-images/", "");
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }

        attachmentRepository.delete(attachment);
        return ResponseEntity.ok().build();
    }
}
