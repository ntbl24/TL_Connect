# TL Connect

TL Connect la backend monolith cho he thong quan ly dao tao va sinh vien tai Thang Long University. He thong cung cap REST API cho hoc vu, dang ky hoc, lich hoc, lich thi, diem danh, ket qua hoc tap, hoc phi/thanh toan, thong bao, don tu va phan hoi.

## Diem ky thuat noi bat

- Dang ky hoc phan voi kiem tra ky dang ky, tien quyet, lop da qua, so tin chi toi da, lich trung va suc chua lop.
- Xu ly tien quyet bang DAG va su dung interval tree de tim lich hoc trung nhanh.
- Cap nhat suc chua lop bang cau lenh SQL nguyen tu trong transaction, giam nguy co dang ky vuot qua capacity khi co nhieu request dong thoi.
- Tich hop VNPay, ZaloPay va MoMo theo provider/factory; ho tro tao thanh toan, callback, truy van trang thai va quy trinh hoan tien.
- Diem danh bang QR co han, HMAC-SHA256 signature, GPS geofencing va kiem tra quyen sinh vien.
- Realtime thong bao va thanh toan qua STOMP/WebSocket; thong bao dong thoi ho tro Firebase Cloud Messaging.
- Xu ly tac vu sau commit bang Spring events, async task executor va cache Redis.
- Tinh tong ket GPA/hoc ky va tao hoa don hoc phi bang cac function PL/pgSQL tren PostgreSQL.
- Import/export du lieu Excel/CSV voi Apache POI va OpenCSV.

## Cong nghe

| Phan | Cong nghe |
|---|---|
| Ngon ngu/runtime | Java 17 |
| Framework | Spring Boot 3.5, Spring MVC |
| Security | Spring Security, JWT HMAC-SHA256, Microsoft OAuth2 |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL |
| Cache/realtime support | Redis, Spring WebSocket/STOMP |
| Push notification | Firebase Admin SDK |
| Thanh toan | VNPay, ZaloPay, MoMo |
| Luu tru tep | Backblaze B2 S3, Cloudinary |
| Xu ly tep | Apache POI, OpenCSV |
| Build/test | Gradle Wrapper, JUnit 5, Spring Boot Test |
| Container | Docker, Docker Compose |

## Kien truc

Du an la Spring Boot monolith theo feature module.

```text
src/main/java/com/tl_connect/dev
├── Main.java
├── modules/
│   ├── academic_result/   # Ket qua va tong ket hoc tap
│   ├── application/       # Don tu va phan loai don
│   ├── attendance/        # Phien diem danh va QR check-in
│   ├── chatbot/           # Nguy canh hoc vu cho chatbot
│   ├── course_class/      # Lop hoc phan
│   ├── enroll/            # Dang ky hoc va rang buoc hoc vu
│   ├── exam/              # Dang ky lich thi
│   ├── feedback/          # Gop y va phan hoi
│   ├── notification/      # Thong bao
│   ├── oauth/             # Dang nhap, JWT, refresh token
│   ├── payment/           # Thanh toan va hoan tien
│   ├── realtime/          # WebSocket/Firebase gateway
│   ├── schedule/          # Thoi khoa bieu
│   ├── student/           # Sinh vien
│   ├── subject/           # Mon hoc va tien quyet
│   ├── tuition/           # Hoc phi
│   └── ...
├── shared/                # Security, config, exception, cache, utility
└── core/                  # Response va shared infrastructure
```

Moi module co cac lop controller, dto, entity, projection, repository, service va interface theo huong don gian hoa; controller mong delegate nghiep vu cho service, repository xu ly truy van va service quan ly transaction.

## Tinh nang chinh

- Quan ly sinh vien, giang vien, khoa, nganh, chuong trinh dao tao, lop sinh vien va mon hoc.
- Quan ly ky hoc, lop hoc phan, lich hoc, lich thi va so tin chi dang ky.
- Dang ky/rut lop voi validation dong bo va cache lich cua tung sinh vien.
- Ket qua hoc tap, GPA tung ky, tong tin chi dat va xep loai.
- Tao hoa don hoc phi, han thanh toan, transaction hoc phi va cap nhat qua han theo lich.
- Tao thanh toan qua cang thanh toan va gui su kien ket qua thanh cong sau khi transaction commit.
- Tao QR diem danh co han; xac minh chu ky, buoi hoc, lop dang ky va vi tri GPS.
- Gui thong bao private/topic, luu lich su doc va ho tro Firebase push notification.
- Gui feedback/gop y kem tep dinh kem; quan ly don tu va loai don.
- Import/xuat du lieu hoc tap va ket qua bang Excel/CSV.

## Yeu cau

- JDK 17
- PostgreSQL
- Redis
- Gradle (co the dung wrapper duoc kem trong repo)
- Docker/Docker Compose neu chay qua container

## Cau hinh moi truong

Dung [application.yml](src/main/resources/application.yml) lam cau hinh mac dinh va truyen secret bang bien moi truong. Khong luu credential that vao source code.

### Cau hinh database va Redis

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ThangLong
SPRING_DATASOURCE_USERNAME=<postgres-username>
SPRING_DATASOURCE_PASSWORD=<postgres-password>
SPRING_DATA_REDIS_URL=redis://localhost:6379
```

### Cau hinh xac thuc va dich vu ngoai

```text
MICROSOFT_TENANT_ID=
MICROSOFT_CLIENT_ID=
MICROSOFT_CLIENT_SECRET=
JWT_SECRET=

B2_AWS_ACCESS_KEY_ID=
B2_AWS_SECRET_ACCESS_KEY=
B2_END_POINT=
B2_BUCKET_NAME=

ZALOPAY_APP_ID=
ZALOPAY_KEY1=
ZALOPAY_KEY2=
ZALOPAY_ENDPOINT_CREATE=
ZALOPAY_ENDPOINT_GET_STATUS=
ZALOPAY_ENDPOINT_PARTIAL_REFUND=
ZALOPAY_ENDPOINT_QUERY_REFUND=
ZALOPAY_REDIRECT_URL=
ZALOPAY_CALLBACK_URL=

CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

VNPAY_TMN_CODE=
VNPAY_SECRET_KEY=
VNPAY_PAY_URL=
VNPAY_REFUND_URL=
VNPAY_RETURN_URL=
VNPAY_VERSION=
VNPAY_COMMAND=
VNPAY_API_URL=

ATTENDANCE_SECRET=
ATTENDANCE_EXPIRATION=15

MOMO_PARTNER_CODE=
MOMO_ACCESS_KEY=
MOMO_SECRET_KEY=
MOMO_PUBLIC_KEY=
MOMO_REDIRECT_URL=
MOMO_IPN_URL=
MOMO_ENDPOINT=
MOMO_REFUND=
MOMO_CREATE=
```

Firebase su dung service-account JSON duoi `src/main/resources/`; file nay khong nen commit va da duoc them vao `.gitignore`.

## Khoi tao PostgreSQL

Hibernate duoc cau hinh `ddl-auto: none`, vi vay schema duoc quan ly bang cac script SQL trong thu muc `schema/`.

Thu tu khoi tao da tot:

```sql
\i schema/init.sql
\i schema/datas.sql
\i schema/indexes.sql
\i schema/trigger.sql
\i schema/generate_invoice_func.sql
\i schema/generate_semester_summaries.sql
```

Tuy thuoc database/tool ban dung, co the chay tung script trong pgAdmin hoac IDE thay vi dung lenh `psql` tren.

## Chay local

### 1. Khoi dong Redis

```bash
docker compose up redis -d
```

### 2. Khoi dong ung dung

Windows:

```bat
gradlew.bat bootRun
```

Linux/macOS:

```bash
./gradlew bootRun
```

Ung dung mac dinh chay tai `http://localhost:8080`.

## Chay bang Docker Compose

Compose hien tai co Redis va build API tu Dockerfile. Truyen/cau hinh lai bien moi truong va endpoint database ben ngoai truoc khi chay:

```bash
docker compose up --build
```

API se expose tai `http://localhost:8080`.

## Build va test

Windows:

```bat
gradlew.bat build
gradlew.bat test
```

Linux/macOS:

```bash
./gradlew build
./gradlew test
```

Dia chi cua cac service test phu thuoc vao profiling va profile local; can dam bao PostgreSQL va Redis san sang truoc khi chay integration test lien quan database/cache.

## API va realtime

- Tai lieu REST API: [docs/api-reference.md](docs/api-reference.md)
- Tai lieu WebSocket: [docs/api-websocket.md](docs/api-websocket.md)
- WebSocket endpoint: `/ws`
- Cac topic/private queue real-time: xem tai lieu WebSocket

## Scheduler va async

He thong bat scheduling va async tai [Main.java](src/main/java/com/tl_connect/dev/Main.java).

- Cap nhat hoc phi qua han bang scheduler hang ngay luc 00:05.
- Side effect dang ky/roi lop duoc xu ly bang async listener sau transaction commit.
- Firebase va cache/lich trinh co execution tren thread pool rieng.

## Bao mat

- Hau het API yeu cau bearer JWT.
- Route `/api/v1/admin/**` gioi han theo role; callback thanh toan va route OAuth/WebSocket duoc cau hinh rieng.
- Refresh token luu Redis va duoc rotate sau khi su dung.
- Callback thanh toan va QR diem danh duoc kiem tra chu ky.
- Session app la stateless.

Neu `application-local.yml`, `docker-compose.yml`, Firebase JSON hoac cac cau hinh khac da chua credential that, hay thu hoi va thay the cac secret; sau do khong commit lai gia tri cu.

## Link du an

- API docs: [docs/api-reference.md](docs/api-reference.md)
- WebSocket docs: [docs/api-websocket.md](docs/api-websocket.md)
- PostgreSQL schema: [schema/](schema/)
