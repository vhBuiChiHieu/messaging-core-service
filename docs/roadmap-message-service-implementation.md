# Roadmap triển khai BE Message Service

## 1. Mục tiêu tài liệu

Tài liệu này mô tả roadmap triển khai chi tiết để phát triển hệ thống **BE Message Service** từ base project hiện tại theo định hướng **Spring Boot Modular Monolith** đã chốt trong `kien-truc-he-thong-messaging-modular-monolith.md`.

Mục tiêu của roadmap:
- dùng được ngay cho triển khai thực tế,
- bám sát hiện trạng base project hiện tại,
- chia rõ theo phase, epic và task list,
- chỉ ra dependency và tiêu chí hoàn thành cho từng phase,
- giúp team có thứ tự triển khai hợp lý, tránh làm sai nền tảng từ đầu.

---

## 2. Đánh giá điểm xuất phát từ base project hiện tại

### 2.1 Những gì đã có
- Base project đã dùng **Java 21 + Spring Boot 3.5.x**.
- Đã có cấu trúc **modular monolith skeleton** với các vùng `common`, `infrastructure`, `modules`.
- Đã có module mẫu theo flow chuẩn gồm `api`, `application`, `domain`, `infrastructure`.
- Đã có **Flyway migration**.
- Đã có **Swagger/OpenAPI** theo profile local/dev.
- Đã có **test cơ bản** và quality gate (`test`, `spotless`, `checkstyle`, `spotbugs`).
- Đã có một số package marker cho các nhóm module và infrastructure.

### 2.2 Những gì chưa có nhưng bắt buộc phải bổ sung
- Chưa có persistence thực tế theo bài toán production với **MySQL + JPA**.
- Chưa có mô hình domain cho messaging: user, integration app, conversation, message, media.
- Chưa có **JWT security** đúng bài toán người dùng cuối.
- Chưa có **partner authentication** bằng API key hoặc integration token.
- Chưa có **Redis** cho presence, cache, rate limit, realtime support.
- Chưa có **WebSocket/STOMP** cho realtime messaging.
- Chưa có **MinIO** cho media storage.
- Chưa có **application event flow** cho messaging domain.
- Chưa có monitoring, audit, rate limiting theo mức đáp ứng production tối thiểu.

### 2.3 Kết luận về điểm xuất phát
Base hiện tại **đủ tốt để bắt đầu triển khai**, nhưng chỉ ở mức **starter skeleton**. Vì vậy roadmap cần bắt đầu bằng việc dựng lại nền tảng kỹ thuật cho đúng domain message-service trước khi đi vào feature business.

---

## 3. Nguyên tắc triển khai

1. **Ưu tiên nền tảng đúng trước, feature sau.** Không triển khai messaging feature trên hạ tầng in-memory hoặc auth giả lập.
2. **Đi theo Phase 0 -> Phase 1 -> Phase 2 -> Phase 3 -> Phase 4.** Không nhảy thẳng sang phase sau khi phase trước chưa đạt exit criteria.
3. **Mỗi module giữ boundary rõ ràng.** Không để logic của `conversation`, `message`, `integration`, `realtime`, `notification` trộn lẫn.
4. **Database là source of truth.** Realtime chỉ phát sau khi transaction chính commit thành công.
5. **REST API là lớp ổn định cho CRUD và truy vết.** WebSocket chỉ phục vụ realtime.
6. **Làm MVP đủ chặt để mở rộng tiếp.** Tránh over-engineering nhưng cũng không giữ sample code quá lâu.
7. **Cross-cutting concern phải được đưa vào sớm**: exception handling, response model, validation, migration, logging, test convention, profile config.

---

## 4. Lộ trình tổng thể

| Phase | Tên phase | Mục tiêu chính |
|---|---|---|
| Phase 0 | Foundation Hardening | Biến base skeleton thành nền kỹ thuật sẵn sàng cho messaging domain |
| Phase 1 | Core MVP | Hoàn thành các năng lực cốt lõi của hệ thống messaging |
| Phase 2 | Stabilization & Operations | Tăng độ ổn định, bảo mật, quan sát và khả năng vận hành |
| Phase 3 | Async Expansion | Bổ sung xử lý bất đồng bộ có retry và fan-out khi cần |
| Phase 4 | Product Expansion | Mở rộng tính năng sản phẩm sau khi core đã ổn định |

---

## 5. Phase 0 - Foundation Hardening

### 5.1 Objective
Dựng nền tảng kỹ thuật đúng để toàn bộ module business về sau có thể triển khai ổn định, không phải phá đi làm lại.

### 5.2 Deliverables
- Base project đổi từ template chung sang định danh `message-service`.
- Có datasource thật cho MySQL.
- Có cấu hình môi trường local/dev/prod rõ ràng.
- Có nền JPA/Flyway hoạt động ổn định.
- Có khung security và convention để phát triển tiếp.
- Loại bỏ hoặc cô lập sample code không còn phù hợp.

### 5.3 Dependencies
- Base project hiện tại.
- Tài liệu kiến trúc đã chốt.
- Quy ước database, API, error code hiện có trong `docs/`.

### 5.4 Epic và task list

#### Epic 0.1 - Rebrand và làm sạch base project
- [x] Đổi `artifactId`, `name`, `description` trong `pom.xml` sang tên service thực tế.
- [x] Đổi `spring.application.name` trong các profile config.
- [x] Rà soát package root `me.bchieu.base` và đổi sang package domain chính thức `me.bchieu.messaging`.
- [x] Cập nhật `README.md` để mô tả đúng mục tiêu của message-service.
- [x] Xóa `modules/sample` khỏi codebase hiện tại.
- [x] Loại bỏ `modules/auth` sample để chuẩn bị thay bằng auth thật ở phase sau.

> **Trạng thái thực tế:** Epic này đã hoàn thành trên `master`.

#### Epic 0.2 - Thiết lập persistence thật với MySQL
- [x] Thêm dependency cần thiết cho MySQL và Spring Data JPA.
- [x] Cấu hình datasource cho local/dev/prod.
- [x] Cấu hình connection pool.
- [x] Chuẩn hóa strategy migration với Flyway.
- [ ] Xây dựng convention cho naming schema, table, index, foreign key.
- [ ] Thay các repository in-memory bằng persistence pattern thật cho module mới.
- [x] Chuẩn bị local compose hoặc hướng dẫn local environment để chạy MySQL ổn định.

> **Trạng thái thực tế:** Persistence foundation đã có, nhưng convention schema chi tiết và repository thật cho domain module mới vẫn là việc phase sau.

#### Epic 0.3 - Chuẩn hóa cấu trúc module cho domain messaging
- [x] Tạo skeleton module: `identity`, `conversation`, `message`, `media`, `integration`, `realtime`, `notification`.
- [x] Xác định rõ package `api`, `application`, `domain`, `infrastructure` cho từng module.
- [ ] Xác định module nào là core business và module nào là supporting.
- [x] Viết package marker hoặc convention test để bảo vệ boundary module.
- [x] Rà soát `user`, `role`, `file` package marker hiện có và loại bỏ phần `me.bchieu.base` còn sót.

> **Trạng thái thực tế:** Skeleton và smoke-test boundary đã có; phần phân loại core/supporting nên chốt rõ hơn trước khi vào Phase 1.

#### Epic 0.4 - Security foundation
- [ ] Chốt cơ chế JWT cho end-user.
- [ ] Chốt cơ chế API key hoặc integration token cho partner.
- [ ] Thiết kế security filter chain theo loại request.
- [x] Thiết kế rule phân tách endpoint public, authenticated user, integration endpoint.
- [ ] Chuẩn hóa mã lỗi auth/forbidden/validation.
- [x] Chuẩn bị util hoặc abstraction cho current principal/current integration app.

> **Trạng thái thực tế:** Security seam và endpoint classification đã sẵn sàng; cơ chế auth thật vẫn chưa được triển khai ở Phase 0.

#### Epic 0.5 - Technical baseline for delivery
- [ ] Thiết lập config logging phù hợp cho local/dev/prod.
- [ ] Bổ sung actuator config cần thiết.
- [ ] Rà soát health endpoint.
- [x] Chuẩn hóa API response envelope nếu cần.
- [x] Chuẩn hóa global exception mapping theo domain error.
- [x] Bổ sung integration test tối thiểu cho startup + migration + docs exposure.
- [ ] Thiết lập test fixture/convention cho module test về sau.

> **Trạng thái thực tế:** Verification baseline đã hoàn tất với `mvn verify` xanh; logging/actuator/test fixture vẫn còn có thể làm sâu hơn.

### 5.5 Exit criteria
- [x] Ứng dụng chạy được với MySQL thật ở local.
- [x] Flyway migrate thành công trên schema sạch.
- [x] Đã có skeleton module messaging và không còn phụ thuộc vào sample flow để triển khai domain thật.
- [x] Có security foundation tối thiểu đủ để bắt đầu Phase 1.
- [x] `mvn verify` chạy ổn định sau các thay đổi nền tảng.

> **Đánh giá hiện tại:** Phase 0 về mặt implementation nền tảng đã hoàn thành. Các mục còn mở chủ yếu là quyết định chi tiết/convention để làm rõ thêm trước hoặc trong đầu Phase 1.

### 5.6 Risk / note
- Nếu bỏ qua Phase 0, toàn bộ feature Phase 1 có nguy cơ phải refactor lớn.
- Đây là phase dễ bị xem nhẹ, nhưng là phase quyết định tốc độ phát triển về sau.

---

## 6. Phase 1 - Core MVP

### 6.1 Objective
Hoàn thành bộ năng lực tối thiểu để hệ thống hoạt động như một messaging platform MVP tích hợp được với app client và partner system.

### 6.2 Deliverables
- Identity/user sync hoạt động.
- Direct conversation và group conversation cơ bản hoạt động.
- Gửi/nhận message text hoạt động.
- Lấy lịch sử message hoạt động.
- Realtime message delivery cơ bản hoạt động.
- Upload file cơ bản hoạt động với MinIO.

### 6.3 Dependencies
- Phase 0 hoàn tất.
- MySQL, security, migration foundation ổn định.

### 6.4 Epic và task list

#### Epic 1.1 - Identity module
- [x] Thiết kế entity/bảng `app_user` mở rộng đúng bài toán messaging, dùng `UUID` làm khóa chính nội bộ.
- [x] Bổ sung các trường tối thiểu: `username`, `display_name`, `avatar_url`, `status`, `version`, `created_at`, `updated_at`.
- [x] Quy định `username` là unique, bắt buộc và không cho đổi sau khi tạo.
- [x] Chốt `status` MVP gồm `ACTIVE`, `INACTIVE`, `BLOCKED`.
- [x] Giai đoạn đầu chỉ lưu `avatar_url` dạng string, chưa liên kết chặt với media module.
- [x] Xây dựng domain model và repository cho user.
- [x] Xây dựng API create/update nội bộ cho user.
- [x] Xây dựng API hoặc application service cho tra cứu user nội bộ theo `id` và `username`.
- [x] Xây dựng use case tạo/cập nhật user nội bộ.
- [x] Thêm `version` nội bộ để sẵn sàng dùng optimistic locking annotation ở persistence layer.
- [ ] Chuẩn hóa mapping giữa security principal và user nội bộ.
- Thực tế hiện tại: đã có migration `app_user`, JPA persistence, service create/update/get, API create/update/get-by-id/get-by-username, và error coverage cho duplicate username / missing id.
- Trạng thái verify: các test identity và test suite hiện pass, nhưng `mvn verify` toàn repo vẫn còn bị chặn bởi checkstyle Javadoc pre-existing ngoài scope identity.

#### Epic 1.2 - Integration module
- [ ] Thiết kế bảng `integration_app`.
- [ ] Thiết kế bảng `external_user_mapping`.
- [ ] Thiết kế cơ chế hash và kiểm tra API key/token.
- [ ] Xây dựng endpoint sync user đơn lẻ.
- [ ] Xây dựng endpoint bulk sync user.
- [ ] Thiết kế cơ chế idempotent cơ bản cho sync API.
- [ ] Viết validation cho payload sync user.
- [ ] Ghi log/audit cho hoạt động sync.

#### Epic 1.3 - Conversation module
- [ ] Thiết kế bảng `conversation`.
- [ ] Thiết kế bảng `conversation_member`.
- [ ] Xây dựng use case tạo direct conversation.
- [ ] Xây dựng rule chống tạo trùng direct conversation.
- [ ] Xây dựng use case tạo group conversation.
- [ ] Xây dựng use case thêm member vào group.
- [ ] Xây dựng use case xóa member khỏi group.
- [ ] Xây dựng use case đổi tên group.
- [ ] Xây dựng API lấy danh sách conversation của user.
- [ ] Xây dựng API lấy chi tiết conversation.
- [ ] Xây dựng rule authorization theo membership.

#### Epic 1.4 - Message module
- [ ] Thiết kế bảng `message`.
- [ ] Quyết định có cần bảng `message_receipt` trong MVP hay defer sang Phase 2.
- [ ] Xây dựng use case gửi text message qua REST.
- [ ] Xây dựng validation gửi message.
- [ ] Kiểm tra membership trước khi gửi.
- [ ] Lưu message vào MySQL trước khi phát event.
- [ ] Xây dựng API lấy lịch sử message.
- [ ] Thiết kế cursor-based pagination cho lịch sử.
- [ ] Xây dựng API edit/delete message nếu đưa vào MVP; nếu không thì defer rõ ràng.
- [ ] Xây dựng event `MessageSentEvent`.

#### Epic 1.5 - Realtime module
- [ ] Thêm dependency và cấu hình WebSocket/STOMP.
- [ ] Thiết kế cơ chế authenticate khi connect.
- [ ] Thiết kế authorize khi subscribe conversation.
- [ ] Định nghĩa destination cho user queue và conversation topic.
- [ ] Xây dựng listener đẩy message mới sau commit.
- [ ] Xây dựng luồng push read event nếu đã hỗ trợ trong MVP.
- [ ] Test golden flow: gửi message -> commit DB -> push realtime.

#### Epic 1.6 - Media module
- [ ] Thêm dependency/cấu hình MinIO.
- [ ] Thiết kế bảng `media_file`.
- [ ] Xây dựng API upload file cơ bản.
- [ ] Validate content type và file size.
- [ ] Lưu metadata vào MySQL và object binary vào MinIO.
- [ ] Xây dựng rule liên kết media với message.
- [ ] Xây dựng API lấy metadata file nếu cần cho MVP.

#### Epic 1.7 - Core event flow
- [ ] Chuẩn hóa internal event cho `UserSyncedEvent`, `ConversationCreatedEvent`, `MessageSentEvent`, `MediaUploadedEvent`.
- [ ] Thiết kế nguyên tắc publish event sau transaction commit.
- [ ] Tách listener cho realtime và notification khỏi business core.
- [ ] Kiểm tra không đưa business rule cốt lõi vào listener.

#### Epic 1.8 - API contract và tài liệu hóa MVP
- [ ] Chốt endpoint path cho integration, conversation, message, media.
- [ ] Chuẩn hóa request/response schema.
- [ ] Chuẩn hóa mã lỗi cho các flow cốt lõi.
- [ ] Kiểm tra OpenAPI docs hiển thị đầy đủ trên local/dev.
- [ ] Bổ sung example payload cho endpoint quan trọng.

### 6.5 Exit criteria
- Partner sync user được vào hệ thống thành công.
- Tạo direct conversation và group conversation được.
- Gửi message text lưu DB thành công và nhận realtime được.
- Lấy lịch sử message theo pagination được.
- Upload file cơ bản thành công qua MinIO.
- Boundary giữa các module core và supporting vẫn rõ ràng.
- Có test cho các luồng chính của MVP.

### 6.6 Risk / note
- Rủi ro lớn nhất của Phase 1 là để business logic bị đẩy sang controller, WebSocket handler hoặc listener.
- Cần khóa chặt nguyên tắc: save DB trước, rồi mới phát realtime/event.

---

## 7. Phase 2 - Stabilization & Operations

### 7.1 Objective
Tăng độ tin cậy, bảo mật, khả năng quan sát và khả năng vận hành production cho core MVP.

### 7.2 Deliverables
- Read receipt cơ bản.
- Presence/online status với Redis.
- Rate limiting.
- Audit log.
- Metrics, health check, monitoring cơ bản.
- Tối ưu index và truy vấn.

### 7.3 Dependencies
- Phase 1 đã ổn định chức năng.
- Có số liệu ban đầu để biết đâu là điểm nghẽn thật.

### 7.4 Epic và task list

#### Epic 2.1 - Read receipt
- [ ] Chốt mô hình `last_read_message_id` hay `message_receipt` kết hợp.
- [ ] Xây dựng API mark-as-read.
- [ ] Cập nhật state đọc theo conversation member hoặc receipt.
- [ ] Phát event read receipt.
- [ ] Push realtime read update nếu cần.
- [ ] Kiểm tra hiệu năng khi conversation có nhiều message.

#### Epic 2.2 - Presence với Redis
- [ ] Thêm dependency/cấu hình Redis.
- [ ] Thiết kế key strategy cho online/offline status.
- [ ] Thiết kế cập nhật last active.
- [ ] Tích hợp presence vào connect/disconnect flow.
- [ ] Cân nhắc pub/sub nếu chạy nhiều instance.
- [ ] Xây dựng API hoặc realtime event để client nhận trạng thái online/offline.

#### Epic 2.3 - Rate limiting và hardening bảo mật
- [ ] Áp rate limit cho integration API.
- [ ] Áp rate limit cho auth hoặc connect endpoint nếu có.
- [ ] Giới hạn upload file.
- [ ] Rà soát validation payload nhạy cảm.
- [ ] Bổ sung audit/security log cho action nhạy cảm.
- [ ] Rà soát lại phân quyền theo conversation/resource.

#### Epic 2.4 - Audit và logging
- [ ] Thiết kế audit event cho sync user, add/remove member, rename group, delete message, rotate key.
- [ ] Chuẩn hóa correlation id/request id nếu cần.
- [ ] Phân tách request log, error log, security log, audit log.
- [ ] Thiết kế format log thuận tiện cho truy vết production.

#### Epic 2.5 - Monitoring và vận hành
- [ ] Bổ sung actuator endpoint cần thiết.
- [ ] Bổ sung metrics cho HTTP request, websocket connection, message throughput, error rate.
- [ ] Chuẩn hóa health indicator cho DB, Redis, MinIO.
- [ ] Chuẩn bị dashboard/metric naming convention.
- [ ] Chuẩn bị alert baseline cho production.

#### Epic 2.6 - Hiệu năng dữ liệu và truy vấn
- [ ] Rà soát index đúng theo kiến trúc đã đề xuất.
- [ ] Tối ưu truy vấn lấy conversation list.
- [ ] Tối ưu truy vấn lịch sử message.
- [ ] Tối ưu truy vấn membership check.
- [ ] Kiểm tra phân trang trong dữ liệu lớn.
- [ ] Chuẩn bị kịch bản test cho mốc tải ban đầu khoảng 3.000 user.

### 7.5 Exit criteria
- Hệ thống có read receipt cơ bản và presence hoạt động.
- Các endpoint nhạy cảm đã có rate limit và audit hợp lý.
- Đã có metrics/health/logging đủ để vận hành production mức đầu.
- Hiệu năng truy vấn chính đáp ứng mục tiêu tải ban đầu.

### 7.6 Risk / note
- Không nên thêm Redis chỉ để “cho có”. Redis phải phục vụ đúng presence, cache, rate limit hoặc pub/sub.
- Monitoring phải bám vào luồng quan trọng, không chỉ bật actuator mặc định.

---

## 8. Phase 3 - Async Expansion

### 8.1 Objective
Bổ sung các khả năng bất đồng bộ và retry rõ ràng cho các tác vụ không nên nằm trên request path chính.

### 8.2 Deliverables
- RabbitMQ được thêm vào đúng use case.
- Notification/webhook/retry flow có thể chạy nền.
- Fan-out event linh hoạt hơn.

### 8.3 Dependencies
- Phase 2 hoàn tất.
- Đã có nhu cầu thực tế cần retry hoặc tác vụ nền tách khỏi request path.

### 8.4 Epic và task list

#### Epic 3.1 - Quyết định phạm vi async
- [ ] Xác định use case thật sự cần queue: push notification, webhook, retry integration, media hậu kỳ.
- [ ] Chốt use case nào vẫn nên giữ synchronous.
- [ ] Xác định SLA cho từng tác vụ async.

#### Epic 3.2 - RabbitMQ foundation
- [ ] Thêm dependency/cấu hình RabbitMQ.
- [ ] Thiết kế exchange, queue, routing key ở mức tối thiểu cần thiết.
- [ ] Thiết kế retry/dead-letter strategy.
- [ ] Chuẩn hóa serialization cho async payload.

#### Epic 3.3 - Notification async
- [ ] Tách notification processing ra consumer riêng trong cùng monolith nếu cần.
- [ ] Xây dựng push notification pipeline cơ bản.
- [ ] Xây dựng retry cho notification thất bại tạm thời.
- [ ] Đảm bảo notification không làm hỏng transaction gửi message cốt lõi.

#### Epic 3.4 - Webhook / integration async
- [ ] Xây dựng outbound webhook event nếu có nhu cầu.
- [ ] Thiết kế retry khi webhook lỗi tạm thời.
- [ ] Bổ sung audit và monitoring cho webhook delivery.

#### Epic 3.5 - Async observability
- [ ] Bổ sung metrics cho queue depth, retry count, failure rate.
- [ ] Bổ sung log correlation giữa request gốc và background job.
- [ ] Thiết kế dashboard và alert cho consumer lỗi.

### 8.5 Exit criteria
- RabbitMQ chỉ được dùng cho đúng loại tác vụ phù hợp.
- Notification hoặc webhook có retry rõ ràng.
- Luồng gửi message lõi vẫn không phụ thuộc queue để hoàn tất business transaction chính.

### 8.6 Risk / note
- Đây là phase rất dễ bị over-engineering nếu thêm queue quá sớm.
- Chỉ triển khai khi nhu cầu thực tế đủ rõ.

---

## 9. Phase 4 - Product Expansion

### 9.1 Objective
Mở rộng tính năng sản phẩm sau khi nền tảng messaging core đã đủ vững.

### 9.2 Deliverables
- Reaction, typing indicator, mute/pin, edit/delete nâng cao, analytics hoặc báo cáo tùy nhu cầu.

### 9.3 Dependencies
- Core message service đã ổn định vận hành.
- Có phản hồi thật từ client/product/business.

### 9.4 Epic và task list

#### Epic 4.1 - Message interaction features
- [ ] Reaction cho message.
- [ ] Reply/thread nếu được ưu tiên.
- [ ] Edit/delete message nâng cao.
- [ ] Kiểm soát quyền và audit cho các thao tác này.

#### Epic 4.2 - Conversation experience
- [ ] Typing indicator.
- [ ] Mute conversation.
- [ ] Pin conversation.
- [ ] Cải thiện conversation settings.

#### Epic 4.3 - Product insights
- [ ] Analytics cơ bản cho throughput và hành vi sử dụng.
- [ ] Report vận hành hoặc report product nếu cần.
- [ ] Rà soát nhu cầu retention policy cho media/message.

#### Epic 4.4 - Scale strategy review
- [ ] Đánh giá lại module boundary sau khi hệ thống tăng độ phức tạp.
- [ ] Xác định module nào có thể tách service trong tương lai nếu cần.
- [ ] Đánh giá độ sẵn sàng cho scaling nhiều instance.

### 9.5 Exit criteria
- Các tính năng mở rộng không phá vỡ boundary cốt lõi.
- Hệ thống vẫn giữ được khả năng bảo trì và vận hành.
- Có dữ liệu đủ để quyết định mở rộng tiếp hay tối ưu kiến trúc.

### 9.6 Risk / note
- Không nên đưa reaction, typing, analytics vào quá sớm khi core MVP chưa ổn.
- Phase này phải bám vào nhu cầu thực, không nên xây theo suy đoán.

---

## 10. Thứ tự ưu tiên module đề xuất

| Thứ tự | Module / nhóm | Lý do |
|---|---|---|
| 1 | Persistence + Security foundation | Nếu nền này sai, toàn bộ phase sau bị kéo chậm |
| 2 | Identity + Integration | Vì user sync là đầu vào quan trọng của toàn hệ thống |
| 3 | Conversation | Là boundary nghiệp vụ cần có trước khi gửi message |
| 4 | Message | Core business chính |
| 5 | Realtime | Theo sau business commit flow |
| 6 | Media | Cần cho attachment nhưng không nên chặn text messaging |
| 7 | Notification | Nên để là supporting module, không chen vào core quá sớm |
| 8 | Presence / Redis optimization | Bổ sung sau khi core đã chạy ổn |
| 9 | Async / RabbitMQ | Chỉ thêm khi có nhu cầu rõ |

---

## 11. Cross-cutting checklist xuyên suốt mọi phase

- [ ] Mọi module mới đều theo đúng cấu trúc `api/application/domain/infrastructure`.
- [ ] Không để controller giữ business logic.
- [ ] Không để listener/event handler giữ business rule cốt lõi.
- [ ] Không để WebSocket handler trở thành nơi xử lý gửi message chính.
- [ ] Mọi thay đổi schema đều đi qua Flyway migration.
- [ ] Mọi endpoint chính đều có request validation rõ ràng.
- [ ] Mọi lỗi business chính đều có error code hoặc mapping rõ ràng.
- [ ] Mọi luồng quan trọng đều có test ở mức phù hợp.
- [ ] Mọi task thêm mới phải kiểm tra boundary module trước khi triển khai.
- [ ] Realtime chỉ phát sau khi dữ liệu đã được lưu bền vững.

---

## 12. Recommended implementation order từ base hiện tại

### Bước 1 - Chuyển base thành message-service thật
- Rebrand project.
- Xóa phụ thuộc vào sample flow.
- Chốt package naming.

### Bước 2 - Dựng technical foundation
- MySQL + JPA + Flyway chuẩn.
- Security foundation.
- Module skeleton chính thức.

### Bước 3 - Làm luồng dữ liệu đầu vào
- Identity.
- Integration app.
- User sync.

### Bước 4 - Làm core conversation và message
- Conversation + membership.
- Send message + history.
- Event sau commit.

### Bước 5 - Gắn realtime và media
- WebSocket/STOMP.
- MinIO upload.
- Realtime push message.

### Bước 6 - Ổn định production baseline
- Read receipt.
- Presence.
- Audit/logging/metrics/rate limit.

### Bước 7 - Mở rộng async và product feature
- RabbitMQ nếu thật sự cần.
- Notification/webhook/retry.
- Reaction, typing, pin, mute, analytics.

---

## 13. Kết luận

Roadmap này giả định rằng base project hiện tại được dùng như một **nền khởi đầu kỹ thuật**, không phải một message-service đã hoàn chỉnh. Vì vậy trình tự hợp lý nhất là:

1. **củng cố nền tảng**, 
2. **xây core MVP**,
3. **ổn định vận hành**,
4. **mở rộng async**,
5. **mở rộng tính năng sản phẩm**.

Nếu bám đúng thứ tự này, dự án sẽ giảm đáng kể nguy cơ phải đập đi làm lại ở các phần persistence, security, realtime và module boundary.