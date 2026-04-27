# Backend Analiz Tapıntıları

## 1) Backend-də çatışmayan və ya konfliktli hissələr

- `order-service/src/main/java/com/birbuket/orderservice/controller/OrderController.java` faylı boşdur, endpoint qatında implementasiya yoxdur.
- `order-service/src/main/java/com/birbuket/orderservice/repository/OrderItemRepository.java` faylı boşdur, repository kontraktı yoxdur.
- `order-service/src/main/java/com/birbuket/orderservice/models/Order.java` yalnız boş class-dır, domain modeli tamamlanmayıb.
- `product-service/src/main/java/com/birbuket/productservice/controller/ProductController.java` controller `ProductServiceImpl`-dən birbaşa asılıdır; interface (`ProductService`) üzərindən inyeksiya edilməlidir.
- `product-service/src/main/java/com/birbuket/productservice/controller/ProductCategoryController.java` içində `updateCategory` metodunda `@Valid` yoxdur, `UpdateCategoryRequest` validasiyası işləməyə bilər.
- `product-service/src/main/java/com/birbuket/productservice/service/impl/ProductServiceImpl.java` içində `IllegalArgumentException` atılır, amma `common/src/main/java/com/birbuket/common/exception/GlobalExceptionHandler.java` yalnız `BaseException` tutur; bu tip xətalar standart API error formatına düşmür.
- `gateway/src/main/java/com/birbuket/gateway/config/SecurityConfig.java` içində `"/api/**"` tam `permitAll` verilib; gateway səviyyəsində bütün API-lər açıq qalır.
- `product-service/src/main/java/com/birbuket/productservice/config/SecurityConfig.java` içində `POST/DELETE/PATCH /api/product/**` hamısı `permitAll`-dır; mutasiya endpoint-ləri auth olmadan əlçatandır.
- `product-service/src/main/java/com/birbuket/productservice/service/impl/ProductServiceImpl.java` içində fayl yüklənməsi (`uploadMultipartFiles`) DB `save` əməliyyatından əvvəl edilir; DB save fail olsa yüklənən fayllar orphan qala bilər (transaction ilə atomik deyil).
- `product-service/src/main/java/com/birbuket/productservice/repository/ProductRepository.java` içində `aj.org.objectweb.asm.commons.Remapper` importu artıq/əlaqəsiz görünür.

## 2) Dinamik olmalı, amma statik qalan hissələr

- `auth-service/src/main/resources/application.yml` və `product-service/src/main/resources/application.yml` daxilində DB host/port/user/password birbaşa hardcode olunub (`localhost`, `arxideya464` və s.); env/profil əsaslı idarə olunmalıdır.
- `gateway/src/main/resources/application.yaml` daxilində route `uri` dəyərləri (`http://localhost:8082`, `http://localhost:8083`) statikdir; mühitə görə dəyişən konfiqurasiya olmalıdır.
- `gateway/src/main/resources/application.yaml` içində OAuth `client-secret` placeholder kimi qalıb (`<secret>`); secret manager və ya env ilə idarə olunmalıdır.
- `gateway/src/main/java/com/birbuket/gateway/config/SecurityConfig.java` içində CORS origin yalnız `http://localhost:3000` olaraq sabitdir; mühitə görə konfiqurasiya edilməlidir.
- `product-service/src/main/java/com/birbuket/productservice/service/impl/ProductServiceImpl.java` içində upload qovluq adları (`"product_image"`) kodda sabitdir; konfiqurasiya/property üzərindən gəlməlidir.
- `auth-service/src/main/java/com/birbuket/authservice/service/impl/AuthServiceImpl.java` içində register zamanı rol və status sabit verilir (`Role.USER`, `UserStatus.ACTIVE`); business qaydası/konfiqurasiya ilə idarə oluna bilər.
- `auth-service/src/main/java/com/birbuket/authservice/service/impl/AuthServiceImpl.java` içində yaş limiti sabitdir (`< 18`); policy kimi xaricdən idarə olunan parametr olmalıdır.

## 3) Dərin analiz (əlavə backend riskləri)

- **High**: `auth-service/src/main/java/com/birbuket/authservice/dto/UserLoginRequest.java` üçün `username/password` sahələrində bean validation yoxdur; boş/null login payload-ları service qatına keçir.
- **High**: `auth-service/src/main/java/com/birbuket/authservice/service/impl/AuthServiceImpl.java` içində login axınında token əvvəl alınır, sonra local DB rol/status yoxlanır və tapılmayanda `Role.USER` fallback verilir; authn və authz arasında drift riski yaradır.
- **High**: `product-service/src/main/java/com/birbuket/productservice/service/impl/ProductServiceImpl.java` içində `createProduct` request-də gələn `discountPercentage`, `active`, `isSingle`, `rating`, `reviewCount` sahələri entity-yə map olunmur; API kontraktında qəbul edilən data səssiz ignor edilir.
- **High**: `product-service/src/main/java/com/birbuket/productservice/service/impl/ProductServiceImpl.java` içində PATCH update-də nullable field-lər birbaşa set edilir (`productName` və s.); partial payload null göndərərsə DB constraint xətasına 500 ilə düşə bilər.
- **High**: `gateway/src/main/resources/application.yaml` yalnız `/api/auth/**` və `/api/product/**` route edir; `product-service`-də mövcud `/api/category/**` və `/api/variant/**` endpoint-ləri gateway üzərindən əlçatmaz qalır.
- **High**: `settings.gradle` içində `order-service` include edilməyib; modul build pipeline-dan kənarda qalır.
- **High**: `order-service/build.gradle` Spring Boot versiyası `4.0.5`-dir, digər modullar 3.x xəttindədir; inteqrasiya zamanı major uyğunsuzluq riski var.
- **Medium**: `product-service/src/main/java/com/birbuket/productservice/controller/ProductController.java` içində `size/page` üçün hədd və minimum validasiya yoxdur; mənfi və ya həddən böyük dəyərlər `PageRequest` üzərində runtime problem yarada bilər.
- **Medium**: `product-service/src/main/java/com/birbuket/productservice/service/impl/ProductCategoryServiceImpl.java` içində `existsByTitle` + `save` check-then-insert pattern-i var; paralel request-lərdə race condition mümkündür.
- **Medium**: `product-service/src/main/java/com/birbuket/productservice/service/impl/FileUploadService.java` MIME yoxlaması `application/octet-stream` qəbul edir; fayl tipinin zəif doğrulanması təhlükəsizlik riskini artırır.
- **Low**: `auth-service/src/main/java/com/birbuket/authservice/controller/AuthController.java` və məhsul controller-lərində create/delete üçün əsasən `200 OK` qaytarılır; HTTP semantikası baxımından `201/204` daha düzgün olardı.

## 4) Dərin analiz (əlavə statik qalan, dinamik olmalı hissələr)

- **Medium**: `auth-service/src/main/java/com/birbuket/authservice/config/KeycloakProperties.java` içində `serverUrl`, `realm`, `clientId` default olaraq kodda sabitdir; mühit/profil ilə idarə olunması daha təhlükəsizdir.
- **Medium**: `auth-service/src/main/resources/application.yml` və `product-service/src/main/resources/application.yml` içində `spring.jpa.hibernate.ddl-auto: update` və `show-sql: true` əsas konfiqdə statik aktivdir; profile görə ayrılmalıdır.
- **Medium**: `gateway/src/main/java/com/birbuket/gateway/config/SwaggerConfig.java` içində Swagger description lokal host/port-lara sabitlənib (`localhost:8082/8083`); deployment mühitində yalnış metadata verə bilər.
