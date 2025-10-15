-- CSV 파일을 통한 대량 데이터 적재 전체 스크립트
-- MySQL 8.0 이상 기준

-- 1️⃣ LOCAL INFILE 허용 (파일 업로드 가능하도록)
SET GLOBAL local_infile = 1;



/* ------------------------------------------------------------
   [PART 1] DUR 마스터 데이터 적재
------------------------------------------------------------ */
DROP TEMPORARY TABLE IF EXISTS tmp_drug;

-- 1. 임시 테이블 생성
CREATE TEMPORARY TABLE tmp_drug (
  item_seq VARCHAR(50),
  item_name VARCHAR(500),
  entp_name VARCHAR(200),
  etc_otc_code VARCHAR(50),
  class_no VARCHAR(100),
  chart VARCHAR(1000),
  material_name TEXT,
  valid_term VARCHAR(200)
);

-- 2. CSV 로드 (drug 기본 정보)
LOAD DATA LOCAL INFILE 'C:/csv/drug_500.csv'
INTO TABLE tmp_drug
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(
  @dummy,            -- No
  item_seq,          -- 품목기준코드
  item_name,         -- 품목명
  entp_name,         -- 업체명
  @permit_date,
  etc_otc_code,      -- 전문/일반 구분
  class_no,          -- 분류번호
  chart,             -- 제형
  @bar_code,
  material_name,     -- 성분정보
  @ee_doc,
  @ud_doc,
  @nb_doc,
  @insert_file,
  @storage_method,
  valid_term,        -- 사용기한
  @reexam_target,
  @reexam_date,
  @pack_unit,
  @edi_code,
  @cancel_date,
  @cancel_name,
  @type_code,
  @type_name,
  @change_date,
  @bizrno,
  @rownum
);

-- 3. drug 테이블에 INSERT or UPDATE
INSERT INTO drug (
  item_seq,
  item_name,
  entp_name,
  etc_otc_code,
  class_no,
  chart,
  material_name,
  valid_term
)
SELECT 
  item_seq,
  item_name,
  entp_name,
  etc_otc_code,
  class_no,
  chart,
  material_name,
  valid_term
FROM tmp_drug
ON DUPLICATE KEY UPDATE
  item_name = VALUES(item_name),
  entp_name = VALUES(entp_name),
  etc_otc_code = VALUES(etc_otc_code),
  class_no = VALUES(class_no),
  chart = VALUES(chart),
  material_name = VALUES(material_name),
  valid_term = VALUES(valid_term);




/* ------------------------------------------------------------
   [PART 2] e약은요 상세정보 업데이트
------------------------------------------------------------ */
DROP TEMPORARY TABLE IF EXISTS tmp_easy_drug;

-- 1. 임시 테이블 생성
CREATE TEMPORARY TABLE tmp_easy_drug (
  entp_name VARCHAR(200),
  item_name VARCHAR(500),
  item_seq VARCHAR(50),
  efcy_qesitm TEXT,
  use_method_qesitm TEXT,
  atpn_warn_qesitm TEXT,
  atpn_qesitm TEXT,
  intrc_qesitm TEXT,
  se_qesitm TEXT,
  deposit_method_qesitm VARCHAR(1000)
);

-- 2. CSV 로드 (e약은요 상세)
LOAD DATA LOCAL INFILE 'C:/csv/eyak_50.csv'
INTO TABLE tmp_easy_drug
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(
  @no,
  entp_name,
  item_name,
  item_seq,
  efcy_qesitm,
  use_method_qesitm,
  atpn_warn_qesitm,
  atpn_qesitm,
  intrc_qesitm,
  se_qesitm,
  deposit_method_qesitm,
  @open_de,
  @update_de,
  @item_image,
  @bizrno,
  @rownum
)
SET
  item_seq = TRIM(item_seq),
  efcy_qesitm = TRIM(efcy_qesitm),
  use_method_qesitm = TRIM(use_method_qesitm),
  atpn_warn_qesitm = TRIM(atpn_warn_qesitm),
  atpn_qesitm = TRIM(atpn_qesitm),
  intrc_qesitm = TRIM(intrc_qesitm),
  se_qesitm = TRIM(se_qesitm),
  deposit_method_qesitm = TRIM(deposit_method_qesitm);

-- 3. drug 테이블 업데이트 (item_seq 기준)
UPDATE drug d
JOIN tmp_easy_drug e ON d.item_seq = e.item_seq
SET
  d.detail_source = 'EASYDRUG',
  d.efcy_qesitm = e.efcy_qesitm,
  d.use_method_qesitm = e.use_method_qesitm,
  d.atpn_warn_qesitm = e.atpn_warn_qesitm,
  d.atpn_qesitm = e.atpn_qesitm,
  d.intrc_qesitm = e.intrc_qesitm,
  d.se_qesitm = e.se_qesitm,
  d.deposit_method_qesitm = e.deposit_method_qesitm;




/* ------------------------------------------------------------
   [PART 3] DUR 주의사항 분리 및 적재
------------------------------------------------------------ */
DROP TEMPORARY TABLE IF EXISTS tmp_drug_type_raw;

-- 1. 원본 주의사항 로드
CREATE TEMPORARY TABLE tmp_drug_type_raw (
  item_seq VARCHAR(50),
  type_code TEXT,
  type_name TEXT
);

LOAD DATA LOCAL INFILE 'C:/csv/drug_500.csv'
INTO TABLE tmp_drug_type_raw
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' 
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(
  @no,
  @item_seq,
  @item_name,
  @entp_name,
  @permit_date,
  @etc_otc_code,
  @class_no,
  @chart,
  @bar_code,
  @material_name,
  @ee_doc,
  @ud_doc,
  @nb_doc,
  @insert_file,
  @storage_method,
  @valid_term,
  @reexam_target,
  @reexam_date,
  @pack_unit,
  @edi_code,
  @cancel_date,
  @cancel_name,
  @type_code,   -- DUR 유형 코드
  @type_name,   -- DUR 유형명
  @change_date,
  @bizrno,
  @rownum
)
SET
  item_seq = TRIM(@item_seq),
  type_code = TRIM(@type_code),
  type_name = TRIM(@type_name);

-- 2. 콤마(,)로 분리된 주의사항 분할 저장
DROP TEMPORARY TABLE IF EXISTS tmp_drug_type_split;
CREATE TEMPORARY TABLE tmp_drug_type_split (
  item_seq VARCHAR(50),
  type_code CHAR(1),
  type_name VARCHAR(100)
);

INSERT INTO tmp_drug_type_split (item_seq, type_code, type_name)
SELECT 
  item_seq,
  TRIM(JSON_UNQUOTE(JSON_EXTRACT(json_each.value, '$[0]'))) AS type_code,
  TRIM(JSON_UNQUOTE(JSON_EXTRACT(json_each.value, '$[1]'))) AS type_name
FROM (
  SELECT 
    item_seq,
    JSON_ARRAYAGG(JSON_ARRAY(
      TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(type_code, ',', n.n), ',', -1)),
      TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(type_name, ',', n.n), ',', -1))
    )) AS pairs
  FROM tmp_drug_type_raw
  JOIN (
    SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
  ) n
  ON CHAR_LENGTH(type_code) - CHAR_LENGTH(REPLACE(type_code, ',', '')) >= n.n - 1
  GROUP BY item_seq
) t,
JSON_TABLE(t.pairs, "$[*]" COLUMNS(value JSON PATH "$")) AS json_each;

-- 3. drug_type 테이블에 삽입 (중복 시 UPDATE)
INSERT INTO drug_type (item_seq, type_code, type_name)
SELECT DISTINCT
  s.item_seq,
  s.type_code,
  s.type_name
FROM tmp_drug_type_split s
JOIN drug d ON d.item_seq = s.item_seq
WHERE s.type_code IS NOT NULL AND s.type_code != ''
ON DUPLICATE KEY UPDATE
  type_name = VALUES(type_name);
