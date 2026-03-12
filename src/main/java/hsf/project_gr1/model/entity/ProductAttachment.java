package hsf.project_gr1.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"attachments", "transactions", "seller"})
    private Product product;

    @Column(name = "file_name",nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;
    @Column(name = "file_type")
    private String fileType;
    @Column(name = "file_size")
    private Long fileSize;
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = true;        // true = chỉ buyer đã mua mới tải được

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}
