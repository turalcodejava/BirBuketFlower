-- Köhnə vahid plant_diagnosis cədvəlindən keçid (bir dəfə, əl ilə).
-- Yeni Hibernate cədvəlləri plant_home_visit və plant_consultation avtomatik yarana bilər;
-- sonra məlumatı bu skript vasitəsilə köçürün və köhnə cədvəli DROP edin.

-- HOME_VISIT sətirləri
INSERT INTO plant_home_visit (
  id, plant_type, email, symptoms, image_url, image_path, agronomist_response,
  address_id, phone_number, full_address_line, special_note, plant_count_range,
  visit_date, visit_time_slot, distance_km, base_visit_fee, plant_count_fee,
  transport_fee, total_fee, customer_latitude, customer_longitude,
  agronomist_id, reserved_at, completed_at, status, created_at, updated_at
)
SELECT
  id, plant_type, email, symptoms, image_url, image_path, agronomist_response,
  address_id, phone_number, full_address_line, special_note, plant_count_range,
  visit_date, visit_time_slot, distance_km, base_visit_fee, plant_count_fee,
  transport_fee, total_fee, customer_latitude, customer_longitude,
  agronomist_id, reserved_at, completed_at, status, created_at, updated_at
FROM plant_diagnosis
WHERE kind = 'HOME_VISIT';

-- CONSULTATION sətirləri
INSERT INTO plant_consultation (
  id, plant_type, email, symptoms, image_url, image_path, agronomist_response,
  special_note, agronomist_id, reserved_at, completed_at, status, created_at, updated_at
)
SELECT
  id, plant_type, email, symptoms, image_url, image_path, agronomist_response,
  special_note, agronomist_id, reserved_at, completed_at, status, created_at, updated_at
FROM plant_diagnosis
WHERE kind = 'CONSULTATION';

-- SEQ sinxronu (PostgreSQL identity üçün tabl sıfırdan yaradılıbsa, MAX(id)+1 seçin).
-- ALTER SEQUENCE ... üçün hər bir cədvəlin identity sequence adını öz mühitinizdə yoxlayın.

-- DROP TABLE plant_diagnosis;  -- sığınaraq edin backup-dan sonra
