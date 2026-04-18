# Phase 0 Foundation Hardening Design

## 1. Mục tiêu

Triển khai Phase 0 theo hướng **hard reset nền tảng từng lớp** để biến base skeleton hiện tại thành nền kỹ thuật sẵn sàng cho BE message-service.

Kết quả cần đạt sau Phase 0:
- project được rebrand hoàn toàn từ `base` sang `message-service`,
- package root được đổi sang `me.bchieu.messaging`,
- xóa hoàn toàn `modules/sample` và `modules/auth`,
- ứng dụng chạy được với MySQL thật ở local,
- Flyway migrate thành công trên schema sạch,
- có skeleton module chính thức cho domain messaging,
- có security foundation đủ để đi tiếp sang Phase 1.

## 2. Hiện trạng đã xác nhận

Từ codebase hiện tại:
- `pom.xml` vẫn đang mang định danh `base`.
- `application.yml` và các profile config vẫn dùng `spring.application.name` là `base`, `base-local`, `base-dev`, `base-prod`.
- package root hiện tại là `me.bchieu.base`.
- `modules/auth` chỉ là skeleton login in-memory, chưa phải security production foundation.
- `modules/sample` chỉ là flow mẫu tham chiếu.
- project chưa có Spring Data JPA, MySQL driver hay datasource thật.
- `compose.yaml` hiện chỉ chạy service ứng dụng.

Các quyết định đã chốt với người dùng:
- đổi package root ngay trong Phase 0,
- package root chính thức là `me.bchieu.messaging`,
- xóa hẳn `modules/sample`,
- xóa hẳn `modules/auth`,
- local environment ở Phase 0 chỉ cần bổ sung MySQL vào `compose.yaml`.

## 3. Phạm vi Phase 0

### In scope
- Rebrand toàn bộ project sang message-service.
- Đổi package root từ `me.bchieu.base` sang `me.bchieu.messaging`.
- Xóa legacy sample/auth module và test liên quan.
- Thêm technical foundation cho MySQL + JPA + Flyway, bao gồm module hỗ trợ MySQL cho Flyway.
- Chuẩn hóa cấu hình `local/dev/prod` cho datasource.
- Tạo skeleton module chính thức cho domain messaging.
- Dựng security foundation mới ở mức framework/boundary.
- Giữ hệ thống luôn ở trạng thái boot được sau từng lát cắt chính.
- Cập nhật README để phản ánh đúng mục tiêu repository.
- Bổ sung baseline verification cho startup, migration và docs exposure.

### Out of scope
- Chưa triển khai business feature của identity, conversation, message, media.
- Chưa triển khai JWT flow hoàn chỉnh hay partner authentication hoàn chỉnh.
- Chưa thêm Redis, MinIO, WebSocket/STOMP, RabbitMQ.
- Chưa thêm MySQL MCP.
- Chưa thiết kế chi tiết schema business Phase 1 ngoài phần nền cần thiết để mở đường.

## 4. Cấu trúc đích sau Phase 0

Project tiếp tục giữ kiến trúc modular monolith với 3 vùng chính:
- `common`: cross-cutting concern dùng chung.
- `infrastructure`: technical adapters dùng chung.
- `modules`: business modules.

Các module đích cần có skeleton chính thức:
- `identity`
- `conversation`
- `message`
- `media`
- `integration`
- `realtime`
- `notification`

Mỗi module mới cần có tối thiểu các package:
- `api`
- `application`
- `domain`
- `infrastructure`

Mục tiêu của skeleton là khóa boundary và dependency direction sớm, không phải đưa business logic của Phase 1 vào trước hạn.

## 5. Chiến lược triển khai theo lát cắt

### Lát cắt 1 — Rebrand và làm sạch base
Thực hiện các thay đổi định danh trước để toàn bộ phần còn lại được xây trên naming chính thức:
- đổi `artifactId`, `name`, `description` trong `pom.xml`,
- đổi `spring.application.name` trong `application.yml` và các profile config,
- đổi main application class và toàn bộ package root sang `me.bchieu.messaging`,
- cập nhật README theo mục tiêu message-service,
- xóa `modules/sample`, `modules/auth` và test liên quan.

Kết thúc lát cắt này, codebase không còn mang danh nghĩa base template và không còn flow demo gây nhiễu.

### Lát cắt 2 — Persistence foundation
Thiết lập persistence thật theo đúng roadmap:
- thêm `spring-boot-starter-data-jpa`,
- thêm MySQL driver,
- thêm `flyway-mysql` để Flyway hỗ trợ MySQL đúng với baseline Spring Boot 3.5 / Flyway 10,
- cấu hình datasource theo profile `local/dev/prod`,
- cấu hình các tham số HikariCP cơ bản theo profile, tối thiểu gồm `maximum-pool-size`, `minimum-idle`, `connection-timeout`, `idle-timeout`, `max-lifetime`,
- giữ Flyway là đường duy nhất cho migration schema,
- tạo migration nền đầu tiên theo convention schema/table/index/key.

Nguyên tắc là database trở thành source of truth ngay từ Phase 0, tránh tiếp tục sống với repository in-memory. Đồng thời, cấu hình pool không nên để hoàn toàn mặc định vì message-service có đặc thù kết nối hoạt động liên tục và cần chủ động baseline cho `dev`/`prod`.

### Lát cắt 3 — Module skeleton chính thức
Sau khi dọn code cũ và có persistence foundation, tạo các module chính thức cho message-service.

Mỗi module cần skeleton package rõ ràng và package marker hoặc test convention phù hợp để bảo vệ boundary. Ở giai đoạn này chỉ nên thêm phần khung cần thiết để compile và định vị trách nhiệm từng module.

### Lát cắt 4 — Security foundation
Dựng security foundation mới từ đầu thay cho auth demo cũ:
- thiết kế security filter chain theo loại endpoint,
- phân tách endpoint public, authenticated user, integration endpoint,
- chuẩn bị abstraction cho current principal/current integration app,
- chuẩn hóa error mapping cho unauthorized/forbidden/validation.

Phase 0 không cần full business auth, nhưng phải để lại khung đúng để Phase 1 có thể gắn JWT end-user và integration token mà không phải phá nền lần nữa.

### Lát cắt 5 — Delivery baseline
Hoàn thiện các concern nền để đội triển khai Phase 1 an toàn hơn:
- logging config cho `local/dev/prod`,
- actuator config và health endpoint cơ bản,
- rà soát `ApiResponse` và `GlobalExceptionHandler`, giữ lại nếu còn phù hợp sau khi rename package,
- bổ sung integration test tối thiểu cho startup + migration + docs exposure, với test profile tách riêng để không phụ thuộc MySQL local đang chạy,
- chuẩn hóa test fixture hoặc convention cho module test về sau.

## 6. Quyết định kỹ thuật chính

| Chủ đề | Quyết định |
|---|---|
| Package root | Đổi ngay sang `me.bchieu.messaging` |
| Application identity | Rebrand toàn bộ sang `message-service` trong Phase 0 |
| Persistence | Dùng Spring Data JPA + MySQL + Flyway, kèm `flyway-mysql` |
| Connection pool | Chốt baseline HikariCP riêng cho `dev` và `prod`, không phụ thuộc hoàn toàn vào mặc định |
| Source of truth | MySQL là nguồn dữ liệu chuẩn |
| DB charset/timezone | Chốt MySQL dùng `utf8mb4` với collation phù hợp cho emoji và chuẩn UTC cho app/DB |
| Legacy modules | Xóa hoàn toàn `modules/sample` và `modules/auth` |
| Security | Dựng security foundation mới, không tái sử dụng auth demo |
| Module policy | Chỉ tạo skeleton đúng boundary, chưa làm business feature |
| Profiles | Duy trì `local/dev/prod` |
| Local infra | Chỉ bổ sung MySQL vào `compose.yaml` ở Phase 0 |
| Extra infra | Chưa thêm Redis/MinIO/WebSocket/RabbitMQ/MySQL MCP |

## 7. Thiết kế local environment

Phase 0 sẽ mở rộng `compose.yaml` hiện có bằng cách bổ sung service MySQL phục vụ local development.

Phạm vi local compose:
- có service MySQL riêng cho local,
- có volume để dữ liệu không mất sau mỗi lần restart,
- có healthcheck để app phụ thuộc vào trạng thái DB rõ ràng hơn,
- có biến môi trường đủ để map với `application-local.yml`,
- chốt charset/collation ở mức tạo database theo hướng tương thích emoji, ưu tiên `utf8mb4`.

Không mở rộng local compose sang Redis, MinIO hay các hạ tầng khác ở giai đoạn này để giữ Phase 0 tập trung.

Ngoài local compose, application và database cần được chốt timezone chuẩn UTC ngay từ Phase 0 để tránh sai lệch thứ tự timeline và dữ liệu thời gian giữa các môi trường.

## 8. Data và migration strategy

Phase 0 chưa cần mô hình đầy đủ cho toàn domain messaging, nhưng phải đặt ra convention nền cho các migration tiếp theo.

Đề xuất:
- dùng naming convention nhất quán cho schema, bảng, cột, index, foreign key,
- chốt database dùng `utf8mb4` và collation tương thích emoji; application và database cùng dùng timezone UTC,
- migration đầu nên tập trung vào phần nền và các quyết định kiến trúc bền vững,
- mọi thay đổi schema đi qua Flyway, không để Hibernate tự tạo schema,
- cấu hình JPA theo hướng validate hoặc tương đương để tránh lệch schema và code,
- chốt naming convention Flyway theo timestamp, ví dụ `V20260418_1030__init_schema.sql`, để giảm khả năng đụng độ khi nhiều người làm song song.

Điều này giúp Phase 1 có thể thêm bảng `app_user`, `integration_app`, `conversation`, `message`, `media_file` mà không phải sửa lại nguyên tắc từ đầu.

Lý do chốt sớm charset/timezone và naming convention là vì đây là các quyết định nền khó sửa về sau, đặc biệt với hệ thống message có emoji, timeline và nhiều migration chạy song song giữa các nhánh.

## 9. Error handling và API baseline

Project hiện đã có `ApiResponse` và `GlobalExceptionHandler`. Trong Phase 0, hai thành phần này nên được đánh giá theo tiêu chí:
- còn phù hợp với message-service không,
- có đáp ứng được error code convention trong `docs/error-code.md` không,
- có đủ rõ để làm nền cho auth, validation, business errors của Phase 1 không.

Nếu phù hợp thì giữ lại sau khi đổi package root và naming. Nếu chưa phù hợp thì chỉnh lại ngay ở Phase 0 để tránh lan rộng convention sai.

## 10. Testing và verification strategy

Phase 0 chỉ được coi là hoàn tất khi hệ thống đạt các mốc xác nhận sau:
- ứng dụng boot được với MySQL thật ở local,
- Flyway migrate thành công trên schema sạch,
- OpenAPI docs exposure hoạt động đúng theo profile mong muốn,
- các integration test nền cho startup/migration/docs chạy ổn,
- `mvn verify` chạy xanh sau toàn bộ thay đổi nền tảng.

Nguyên tắc test trong Phase 0:
- ưu tiên test chứng minh nền tảng hoạt động thật,
- loại bỏ test đang gắn với `sample` và `auth` demo,
- thêm test mới bám theo application startup, migration, docs exposure, module structure và configuration contract.

## 11. Rủi ro và cách kiểm soát

| Rủi ro | Cách kiểm soát |
|---|---|
| Diff lớn do đổi package root | Tách triển khai theo lát cắt, giữ app compile/boot được sau mỗi mốc |
| Xóa sample/auth làm mất tham chiếu cũ | Roadmap và architecture hiện đã đủ định hướng, module mới sẽ thay bằng skeleton chính thức |
| Cấu hình datasource lệch giữa local/dev/prod | Chuẩn hóa property naming ngay từ đầu và kiểm chứng bằng startup test/profile test |
| Flyway/JPA xung đột | Chốt Flyway là đường duy nhất cho schema và cấu hình JPA không tự generate |
| Phase 0 phình sang feature work | Giới hạn chặt phạm vi: chỉ foundation, không triển khai business flow Phase 1 |

## 12. Exit criteria

Phase 0 được xem là đạt khi tất cả điều kiện sau đúng:
- project mang định danh `message-service` thay vì `base`,
- package root đã đổi hoàn toàn sang `me.bchieu.messaging`,
- `modules/sample` và `modules/auth` đã bị loại bỏ hoàn toàn,
- app chạy được với MySQL local từ compose,
- Flyway migrate thành công trên schema sạch,
- skeleton module messaging đã hiện diện đầy đủ,
- security foundation mới đã sẵn sàng để Phase 1 cắm cơ chế auth thật,
- `mvn verify` chạy ổn định.

## 13. Khuyến nghị thực thi tiếp theo

Sau khi spec này được duyệt, bước kế tiếp là viết implementation plan chi tiết cho Phase 0 theo đúng thứ tự lát cắt:
1. rebrand và package rename,
2. xóa legacy modules,
3. dựng persistence foundation,
4. tạo module skeleton chính thức,
5. dựng security foundation,
6. hoàn thiện delivery baseline và verification.
