# BirBuketFlower

Bu layihə **mikroservis** arxitekturası ilə qurulmuş onlayn gül mağazası backend hissəsidir.  
Hər servis ayrıca modul kimi işləyir və API Gateway üzərindən yönləndirilir.

## Bu nə layihəsidir? (qısa)

`BirBuketFlower` bir **gül/e-commerce backend** layihəsidir.
- İstifadəçi girişi və qeydiyyatı (`auth-service` + Keycloak)
- Məhsul, kateqoriya və variant idarəetməsi (`product-service`)
- Sifariş axını üçün ayrıca modul (`order-service`)

## Layihədə olan servislər (qısa)

- `gateway` - bütün request-ləri qəbul edir və uyğun servisə yönləndirir. (`8081`)
- `auth-service` - login/register və Keycloak ilə autentifikasiya. (`8082`)
- `product-service` - məhsul, kateqoriya, variant və şəkil yükləmə funksiyaları. (`8083`)
- `order-service` - sifariş modulu (hazırda inkişaf mərhələsindədir, port ayrıca verilməyib).
- `common` - ortaq DTO, exception və utility hissələri.

## Tələblər

- Java `21`
- Docker (PostgreSQL üçün)
- Keycloak (lokalda işləməlidir, default: `http://localhost:8080`)

## Layihəni run etmək

### 1) PostgreSQL-i qaldır

Repo kökündə:

```powershell
docker compose up -d
```

Bu əmrlə `postgres` `5432` portunda açılır.

### 2) Servisləri build et (istəyə bağlı, tövsiyə olunur)

```powershell
.\gradlew.bat clean build
```

### 3) Servisləri ayrı-ayrı işə sal

Hər servisi ayrıca terminalda aç:

```powershell
.\gradlew.bat :gateway:bootRun
.\gradlew.bat :auth-service:bootRun
.\gradlew.bat :product-service:bootRun
```

`order-service` üçün:

```powershell
.\gradlew.bat :order-service:bootRun
```

Qeyd: `order-service` üçün `application.yml`-də `server.port` verilmədiyi üçün default olaraq `8080`-da açıla bilər.

## Gateway route-ları

Gateway (`8081`) üzərindən əsas path-lar:

- `/api/auth/**` -> `auth-service`
- `/api/product/**` -> `product-service`
- `/api/category/**` -> `product-service`
- `/api/variant/**` -> `product-service`
- `/uploads/**` -> `product-service`

## Faydalı qeydlər

- `auth-service` Keycloak realm və client konfiqurasiyası tələb edir.
- Database URL/username/password dəyərləri `application.yml` içində env var ilə override edilə bilər.
- Lokal inkişafda əvvəlcə `gateway`, `auth-service`, `product-service` işlətmək kifayətdir.
