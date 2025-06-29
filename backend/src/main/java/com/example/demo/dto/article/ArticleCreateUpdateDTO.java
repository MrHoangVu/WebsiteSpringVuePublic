// src/main/java/com/example/demo/dto/ArticleCreateUpdateDTO.java
package com.example.demo.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArticleCreateUpdateDTO {

    @NotBlank(message = "Tiêu đề là bắt buộc")
    @Size(min = 5, max = 250, message = "Tiêu đề phải từ 5 đến 250 ký tự")
    private String title;

    @NotBlank(message = "Nội dung là bắt buộc")
    @Size(min = 50, message = "Nội dung phải có ít nhất 50 ký tự")
    private String content;

    @Size(max = 500, message = "Tóm tắt không quá 500 ký tự")
    private String excerpt; // Tóm tắt (tùy chọn)

    @Size(max = 512, message = "URL ảnh không quá 512 ký tự")
    // Có thể thêm @URL nếu muốn validate định dạng URL chặt chẽ
    private String featuredImageUrl; // URL ảnh (tùy chọn)

    // isPublished sẽ được xử lý riêng qua API publish/unpublish
}