# Phase 1 Identity Module Design

## 1. Mục tiêu

Thiết kế lát cắt triển khai đầu tiên của Phase 1 cho module `identity` để hệ thống messaging có nguồn user chuẩn nội bộ, làm nền cho các module `conversation`, `message`, `integration` và các bước mapping security về sau.

Scope của lát cắt này là tạo được năng lực tối thiểu nhưng dùng được thật:
- tạo user nội bộ,
- cập nhật user nội bộ,
- lấy chi tiết user theo `UUID`,
- tra cứu user theo `username`.

Thiết kế này bám theo roadmap đã cập nhật trong `docs/roadmap-message-service-implementation.md` và giữ mục tiêu MVP gọn, không kéo thêm media, auth thật hoặc integration sync vào cùng một lát cắt.

## 2. Vai trò của identity trong Phase 1

Module `identity` là **core business module** trong giai đoạn đầu của Phase 1.

Trách nhiệm của module này:
- quản lý user nội bộ của hệ thống messaging,
- cung cấp dữ liệu user chuẩn để các module khác tham chiếu,
- thiết lập quy tắc identity ổn định ngay từ đầu để tránh phải thay đổi khóa nghiệp vụ khi các module sau đã phụ thuộc.

Ngoài scope của lát cắt này:
- chưa triển khai JWT auth thật,
- chưa triển khai partner sync,
- chưa liên kết avatar với media module,
- chưa xử lý đồng bộ user từ hệ thống ngoài,
- chưa mở rộng sang tìm kiếm user nâng cao hoặc danh sách phân trang.

## 3. Quyết định thiết kế chính

| Chủ đề | Quyết định |
|---|---|
| Vai trò module | `identity` là nguồn user chuẩn của hệ thống messaging |
| Primary key | `app_user.id` dùng `UUID` nội bộ |
| Username | unique, bắt buộc, không cho đổi sau khi tạo |
| Avatar | chỉ lưu `avatar_url` dạng string |
| Status MVP | `ACTIVE`, `INACTIVE`, `BLOCKED` |
| Concurrency | thêm trường `version` nội bộ để dùng optimistic locking |
| API scope | create user, update user, get by id, get by username |
| API exposure | phục vụ nội bộ trong service trước, chưa thiết kế public API cho đối tác |

## 4. Data model

### 4.1 Bảng `app_user`

Bảng `app_user` là nguồn lưu trữ chính cho user nội bộ.

Các trường cần có:

| Cột | Ý nghĩa |
|---|---|
| `id` | định danh nội bộ dạng UUID |
| `username` | định danh nghiệp vụ ổn định, duy nhất |
| `display_name` | tên hiển thị cho messaging |
| `avatar_url` | đường dẫn avatar dạng string |
| `status` | trạng thái user |
| `version` | version nội bộ cho optimistic locking |
| `created_at` | thời điểm tạo bản ghi |
| `updated_at` | thời điểm cập nhật gần nhất |

Ràng buộc dữ liệu:
- `id` là primary key.
- `username` là `not null` và `unique`.
- `status` là `not null`.
- `version` là `not null`.
- `created_at` và `updated_at` là `not null`.

### 4.2 Quy tắc dữ liệu

- `username` được thiết kế là định danh nghiệp vụ ổn định nên không cho đổi sau khi tạo.
- `display_name`, `avatar_url`, `status` được phép cập nhật.
- `avatar_url` không cần phụ thuộc `media` module trong Phase 1.
- `version` chỉ phục vụ persistence/concurrency control, chưa cần lộ ra API.

## 5. Kiến trúc module và boundary

Module `identity` tiếp tục theo cấu trúc đã chốt của dự án:
- `api`
- `application`
- `domain`
- `infrastructure`

Phân vai:

| Layer | Trách nhiệm |
|---|---|
| `api` | nhận request, validate boundary, map request/response |
| `application` | điều phối use case create/update/get |
| `domain` | chứa khái niệm user và các rule nghiệp vụ cốt lõi |
| `infrastructure` | JPA entity, repository implementation, mapping persistence |

Nguyên tắc boundary:
- controller không chứa business logic;
- rule `username` unique và immutable không nằm trong controller;
- logic tải/lưu user nằm ở application + repository boundary;
- optimistic locking là concern của persistence layer, không kéo chi tiết JPA vào API contract ở MVP.

## 6. API capability của lát cắt đầu tiên

### 6.1 Create user

Mục tiêu: tạo mới user nội bộ với identity ổn định ngay từ đầu.

Input tối thiểu:
- `username`
- `displayName`
- `avatarUrl` (optional hoặc nullable tùy implementation)
- `status`

Hành vi:
1. validate request ở boundary;
2. kiểm tra `username` chưa tồn tại;
3. sinh `UUID` cho `id`;
4. khởi tạo user với `version` ban đầu;
5. lưu vào `app_user`;
6. trả response chuẩn hóa.

### 6.2 Update user

Mục tiêu: cập nhật thông tin mutable của user.

Input:
- `userId`
- `displayName`
- `avatarUrl`
- `status`

Hành vi:
1. tải user theo `id`;
2. nếu không tồn tại thì trả lỗi not found;
3. cập nhật các field cho phép;
4. không cho phép đổi `username`;
5. lưu lại bản ghi đã cập nhật;
6. trả response chuẩn hóa.

### 6.3 Get user by id

Mục tiêu: cung cấp lookup chuẩn cho các module nội bộ khác.

Hành vi:
- tìm user theo `UUID`;
- nếu không tồn tại thì trả lỗi not found;
- nếu tồn tại thì trả chi tiết user.

### 6.4 Get user by username

Mục tiêu: hỗ trợ tra cứu theo định danh nghiệp vụ ổn định.

Hành vi:
- tìm user theo `username`;
- nếu không tồn tại thì trả lỗi not found;
- nếu tồn tại thì trả chi tiết user.

## 7. Data flow

### 7.1 Create flow

1. Request đi vào controller của module `identity`.
2. Controller map request DTO và chuyển vào application service.
3. Application service kiểm tra trùng `username`.
4. Nếu hợp lệ, application service tạo aggregate/model user mới.
5. Repository lưu user xuống MySQL.
6. API trả `ApiResponse` chứa dữ liệu user đã tạo.

### 7.2 Update flow

1. Request update đi vào controller.
2. Application service tải user hiện có theo `id`.
3. Nếu không có dữ liệu, ném lỗi domain phù hợp.
4. Application service cập nhật các field mutable.
5. Repository lưu lại user.
6. API trả `ApiResponse` với dữ liệu mới nhất.

### 7.3 Query flow

1. Request lookup theo `id` hoặc `username` đi vào controller.
2. Application service gọi repository tương ứng.
3. Nếu không tìm thấy, ném lỗi not found.
4. Nếu tìm thấy, map sang response DTO và trả về.

## 8. Error handling

Các lỗi business cần xử lý ở lát cắt này:

| Tình huống | Hướng xử lý |
|---|---|
| `username` trùng | trả lỗi business/conflict rõ nghĩa |
| user không tồn tại | trả lỗi not found rõ nghĩa |
| payload không hợp lệ | trả lỗi validation ở boundary |
| optimistic locking conflict | để persistence layer phát hiện, mapping qua global exception nếu phát sinh |

Quy ước xử lý:
- tiếp tục dùng `GlobalExceptionHandler` hiện có;
- bổ sung domain exception rõ nghĩa cho identity nếu cần;
- tiếp tục dùng `ApiResponse` cho shape response thống nhất.

## 9. Testing strategy

Lát cắt này ưu tiên test theo hướng integration để xác nhận đầy đủ migration, persistence, API mapping và rule nghiệp vụ cơ bản.

Golden flows cần có:
- tạo user thành công;
- tạo user với `username` trùng bị reject;
- cập nhật user thành công;
- lấy user theo `id` thành công;
- lấy user theo `username` thành công;
- lấy user không tồn tại trả lỗi phù hợp.

Ngoài ra cần kiểm tra:
- migration tạo được bảng `app_user` với unique index trên `username`;
- field `version` hoạt động đúng cho persistence mapping ở mức baseline.

## 10. Thứ tự triển khai đề xuất

1. cập nhật migration để tạo bảng `app_user`;
2. thêm persistence model và repository cho user;
3. thêm domain/application service cho create/update/query;
4. thêm request/response DTO và controller cho identity API;
5. thêm integration test cho các luồng chính;
6. rà soát lại boundary để chắc chắn controller không giữ business rule.

## 11. Các điểm cố ý defer

Để giữ MVP gọn và đúng mục tiêu, các nội dung sau chưa đưa vào lát cắt này:
- JWT authentication thật;
- partner/integration sync;
- liên kết avatar với media upload;
- search user nâng cao;
- danh sách user phân trang;
- expose `version` ra API để làm concurrency contract với client;
- event `UserSyncedEvent` hoặc các integration event khác.

## 12. Kết luận

Lát cắt `identity` này được thiết kế để tạo ra một nền user nội bộ đủ chặt cho messaging domain mà không kéo theo các concern của auth thật, integration hoặc media. Nếu triển khai đúng boundary, đây sẽ là điểm bám ổn định cho các bước tiếp theo của Phase 1 như integration sync, conversation membership và principal mapping.