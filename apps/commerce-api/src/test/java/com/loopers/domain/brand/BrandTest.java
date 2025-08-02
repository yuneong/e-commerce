package com.loopers.domain.brand;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BrandTest {

    @DisplayName("브랜드 생성 시,")
    @Nested
    class CreateBrand {

        @DisplayName("브랜드명은 한글 또는 영어, 숫자만 허용되며, 공백이나 특수문자가 포함되면 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "brand#@#",
                "1  23",
                "!#$%^&*()",
        })
        void throwsIllegalArgumentException_whenNameIsInvalid(String name) {
            assertThrows(IllegalArgumentException.class, () -> {
                Brand.create(
                        name,
                        "브랜드 설명",
                        "https://example.com/image.jpg"
                );
            });
        }

        @DisplayName("imageUrl이 빈 문자열이거나 형식이 올바르지 않으면 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "invalid-url",
                "https://example.com/image.txt",
                "https://example.com/image"
        })
        void throwsIllegalArgumentException_whenUrlIsInvalid(String url) {
            assertThrows(IllegalArgumentException.class, () -> {
                Brand.create(
                        "브랜드명",
                        "브랜드 설명",
                        url
                );
            });
        }

        @DisplayName("정상적인 값이면 Brand가 생성된다.")
        @Test
        void success_whenAllValueAreValid() {
            Brand brand = Brand.create(
                    "브랜드명",
                    "브랜드 설명",
                    "https://example.com/image.jpg"
            );

            assertNotNull(brand);
            assertEquals("브랜드명", brand.getName());
            assertEquals("https://example.com/image.jpg", brand.getImageUrl());
        }
    }

}
