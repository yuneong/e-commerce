package com.loopers.dummyData.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class GenerateBrandAndProductCSVTest {

    static final Random random = new Random(42); // 재현성 위해 고정 시드
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");

    public static void main(String[] args) throws IOException {
        String projectRoot = System.getProperty("user.dir");
        String outputDir = projectRoot + "/apps/commerce-api/src/test/java/com/loopers/dummyData/csv";
        new java.io.File(outputDir).mkdirs();

        int brandCount   = 10;        // ← 필요 시 변경
        int productCount = 100_000;   // ← 필요 시 변경
        double s = 1.1;               // ← Zipf 지수 (1.0~1.3 권장, 클수록 상위 집중)

        String brandFile = outputDir + "/brands_" + brandCount + ".csv";
        String productFile = outputDir + "/products_" + productCount + ".csv";

        generateBrands(brandFile, brandCount);

        // Zipf 분포로 브랜드별 상품 개수 산정
        int[] perBrandCounts = zipfAllocate(productCount, brandCount, s);

        // 실제 상품 CSV 생성
        generateProductsSkewed(productFile, perBrandCounts);

        System.out.println("✅ CSV 생성 완료!");
        System.out.println("브랜드: " + brandFile);
        System.out.println("상품: " + productFile);
    }

    private static void generateBrands(String filePath, int count) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(filePath))) {
            w.write("id,name,description,image_url,created_at");
            w.newLine();
            String today = LocalDate.now().format(formatter);
            for (int i = 1; i <= count; i++) {
                w.write(String.format(
                        "%d,Brand%d,Description for brand %d,https://picsum.photos/seed/brand%d.png,%s",
                        i, i, i, i, today
                ));
                w.newLine();
            }
        }
    }

    /**
     * Zipf 분포로 productCount를 brandCount개 구간에 배분
     * rank=1이 가장 큰 몫을 가짐. (1/rank^s)
     * - 총합이 정확히 productCount가 되도록 마지막에 보정.
     */
    private static int[] zipfAllocate(int productCount, int brandCount, double s) {
        double[] weights = new double[brandCount];
        double sum = 0.0;
        for (int r = 1; r <= brandCount; r++) {
            double w = 1.0 / Math.pow(r, s); // rank r
            weights[r - 1] = w;
            sum += w;
        }

        // 초기 배분(반올림)
        int[] counts = new int[brandCount];
        int allocated = 0;
        for (int i = 0; i < brandCount; i++) {
            int c = (int) Math.round(weights[i] / sum * productCount);
            counts[i] = c;
            allocated += c;
        }

        // 합계 보정: 초과/미달을 상위 브랜드부터 조정
        int diff = productCount - allocated;
        int idx = 0;
        while (diff != 0) {
            int step = diff > 0 ? 1 : -1;
            counts[idx] = Math.max(0, counts[idx] + step);
            diff -= step;
            idx = (idx + 1) % brandCount;
        }
        return counts;
    }

    /**
     * 분포에 따라 상품 생성:
     * - 상위 몇 개 브랜드에 수천 개, 하위는 몇 개 혹은 0개도 가능 (편차 큼)
     * - created_at: 최근 365일 랜덤
     * - status: ACTIVE 92%, INACTIVE 5%, OUT_OF_STOCK 3% (대충 현실감)
     * - price: 1,000 ~ 200,000 (100단위)
     * - stock: Zipf 상위 브랜드일수록 평균 재고도 조금 높게 가중 (옵션)
     */
    private static void generateProductsSkewed(String filePath, int[] perBrandCounts) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(filePath))) {
            w.write("id,brand_id,name,description,image_url,price,stock,status,like_count,created_at");
            w.newLine();

            int idSeq = 1;
            int brandCount = perBrandCounts.length;

            // 브랜드 순회: rank 1 → N
            for (int brandId = 1; brandId <= brandCount; brandId++) {
                int count = perBrandCounts[brandId - 1];
                if (count <= 0) continue;

                // 상위 브랜드일수록 평균 재고 상향 (완전 랜덤보다 미묘한 편향)
                double rankFactor = Math.max(0.6, 1.2 - (brandId - 1) / (double) brandCount); // 1.2 → 0.6 선형 감소
                for (int j = 0; j < count; j++) {
                    String name = "Product" + idSeq;
                    String desc = "Description for product " + idSeq;
                    String img = "https://picsum.photos/seed/product" + idSeq + ".png";
                    int price = (random.nextInt(2000) + 10) * 100; // 1,000 ~ 200,000

                    // stock: 0 확률 조금, 평균은 rankFactor 반영
                    int base = (int) Math.round(500 * rankFactor);
                    int stock = random.nextInt(Math.max(1, base + 300)); // 0~(base+300-1)
                    if (random.nextInt(100) < 8) stock = 0; // 8% 품절

                    String status = pickStatus();
                    Long likeCount = sampleLikeCount(rankFactor);
                    String createdAt = LocalDate.now().minusDays(random.nextInt(365)).format(formatter);

                    w.write(String.format(
                            "%d,%d,%s,%s,%s,%d,%d,%s,%d,%s",
                            idSeq, brandId, name, desc, img, price, stock, status, likeCount, createdAt
                    ));
                    w.newLine();
                    idSeq++;
                }
            }
        }
    }

    private static String pickStatus() {
        int p = random.nextInt(100);
        if (p < 92) return "ACTIVE";
        if (p < 97) return "INACTIVE";
        return "OUT_OF_STOCK";
    }

    /**
     * Heavy‑tailed like_count generator.
     * brandFactor: 0.6 ~ 1.2 (higher for top brands) to bias likes toward top brands.
     */
    private static long sampleLikeCount(double brandFactor) {
        int p = random.nextInt(100); // percentile bucket
        long likes;
        if (p < 1) {
            // Top 1% viral items
            likes = 20000 + random.nextInt(30000); // 20k–50k
        } else if (p < 10) {
            // Next 9%
            likes = 2000 + random.nextInt(8000);   // 2k–10k
        } else if (p < 30) {
            // Next 20%
            likes = 200 + random.nextInt(1800);    // 200–2k
        } else {
            // Remaining 70%
            likes = random.nextInt(200);           // 0–199
        }
        likes = Math.round(likes * brandFactor);
        return Math.max(0L, likes);
    }


}
