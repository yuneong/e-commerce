-- 상품 목록 조회

-- 1. 브랜드 필터, 좋아요 수 정렬
explain analyze
select * from products
where brand_id = 10
order by like_count desc;

CREATE INDEX idx_brand_id_like_count
    ON products (brand_id, like_count DESC);

ALTER TABLE products ALTER INDEX idx_brand_id_like_count INVISIBLE;
ALTER TABLE products ALTER INDEX idx_brand_id_like_count VISIBLE;

-- 2. 브랜드 필터, 가격 정렬
explain
select * from products
where brand_id = 1
order by price asc;

CREATE INDEX idx_brand_id_price
    ON products (brand_id, price ASC);

ALTER TABLE products ALTER INDEX idx_brand_id_price INVISIBLE;
ALTER TABLE products ALTER INDEX idx_brand_id_price VISIBLE;

-- 3. 브랜드 필터, 최신순 정렬
explain
select * from products
where brand_id = 1
order by created_at desc;

CREATE INDEX idx_brand_id_created_at
    ON products (brand_id, created_at DESC);

ALTER TABLE products ALTER INDEX idx_brand_id_created_at INVISIBLE;
ALTER TABLE products ALTER INDEX idx_brand_id_created_at VISIBLE;



-- 브랜드별 제품 수 집계
SELECT
    brand_id,
    COUNT(*) AS productCount
FROM products
GROUP BY brand_id
ORDER BY productCount DESC;



