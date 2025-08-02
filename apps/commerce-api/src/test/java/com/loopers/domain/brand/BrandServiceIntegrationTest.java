package com.loopers.domain.brand;

import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BrandServiceIntegrationTest {

    @Autowired
    private BrandService brandService;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private Brand testBrand;

    @BeforeEach
    void setupTestData() {
        testBrand = brandRepository.save(Brand.create(
                "브랜드명",
                "브랜드설명",
                "https://example.com/brand-image.jpg"
        ));
    }

    @AfterEach
    void cleanDatabase() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("브랜드 정보 조회 시,")
    @Nested
    class getBrandDetail {

        @DisplayName("존재하는 브랜드 ID로 조회 시, 정상적으로 브랜드 정보를 반환한다.")
        @Test
        void successToGetBrandDetail_whenBrandExists() {
            // given
            Long brandId = testBrand.getId();

            // when
            Brand brand = brandService.getBrandDetail(brandId);

            // then
            assertAll(
                () -> assertThat(brand).isNotNull(),
                () -> assertThat(brand.getId()).isEqualTo(brandId),
                () -> assertThat(brand.getName()).isNotBlank()
            );
        }

        @DisplayName("존재하지 않는 브랜드 ID로 조회 시, 예외가 발생한다.")
        @Test
        void failToGetBrandDetail_whenBrandDoesNotExist() {
            // given
            Long nonExistentBrandId = 999L;

            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                    brandService.getBrandDetail(nonExistentBrandId)
            );
        }

    }

}
