
-- 브랜드 생성
LOAD DATA LOCAL INFILE '/Users/yunyeong/study/loopers/apps/commerce-api/src/test/java/com/loopers/dummyData/csv/brands_10.csv'
INTO TABLE brands
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(id, name, description, image_url, created_at);


select * from brands;



-- 상품 생성
LOAD DATA LOCAL INFILE '/Users/yunyeong/study/loopers/apps/commerce-api/src/test/java/com/loopers/dummyData/csv/products_100000.csv'
INTO TABLE products
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(id, brand_id, name, description, image_url, price, stock, status, like_count, created_at);

-- ALTER TABLE products ADD COLUMN like_count BIGINT NOT NULL DEFAULT 0;

select * from products;


-- DELETE FROM brands where 1=1;
-- DELETE FROM products where 1=1;
