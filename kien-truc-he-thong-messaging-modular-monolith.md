# Tài liệu mô tả kiến trúc hệ thống BE Messaging Platform
## Phiên bản đề xuất ban đầu để trình duyệt nội bộ

**Mục tiêu:** Xây dựng một hệ thống Backend dùng chung để cung cấp chức năng nhắn tin cơ bản tương tự các ứng dụng chat/message, có khả năng tích hợp vào nhiều ứng dụng khác nhau, đồng thời hỗ trợ API đồng bộ danh sách người dùng từ các hệ thống đối tác.  
**Định hướng công nghệ đã chốt:** Spring Boot Modular Monolith, Java 21, MySQL, Redis, WebSocket/STOMP, MinIO, Spring Application Events, RabbitMQ nếu cần mở rộng bất đồng bộ.

---

# 1. Tổng quan bài toán

Hệ thống cần đóng vai trò là một nền tảng nhắn tin dùng chung để nhiều ứng dụng có thể tích hợp vào, thay vì mỗi ứng dụng tự xây một module chat riêng.

Các yêu cầu cốt lõi:

- Hỗ trợ nhắn tin cơ bản giữa người dùng.
- Hỗ trợ hội thoại 1-1 và nhóm.
- Hỗ trợ lưu lịch sử tin nhắn.
- Hỗ trợ realtime để tin nhắn nhận ngay trên client.
- Hỗ trợ đồng bộ người dùng từ hệ thống bên ngoài.
- Hỗ trợ upload file/hình ảnh phục vụ chat.
- Có thể mở rộng dần mà chưa cần tách microservice từ đầu.
- Phù hợp với tải khởi điểm khoảng vài nghìn người dùng, khoảng 3.000 user là mức mục tiêu ban đầu.

Kiến trúc được đề xuất theo hướng **Modular Monolith** để cân bằng giữa:
- tốc độ phát triển,
- độ đơn giản khi vận hành,
- khả năng mở rộng về sau,
- phù hợp năng lực triển khai của team backend nhỏ hoặc trung bình.

---

# 2. Quyết định kiến trúc đã chốt

## 2.1 Kiến trúc tổng thể

Hệ thống sẽ được triển khai dưới dạng:

- **Một repo duy nhất trong giai đoạn đầu**
- **Một ứng dụng Spring Boot duy nhất để build và deploy**
- Bên trong chia thành các **module logic độc lập**
- Các module giao tiếp với nhau qua:
  - gọi service nội bộ theo ranh giới module
  - event nội bộ bằng Spring Application Events
- Khi cần xử lý bất đồng bộ mạnh hơn sẽ bổ sung **RabbitMQ**

Điều này phù hợp với bối cảnh triển khai ban đầu của dự án cá nhân:
- giảm chi phí setup và vận hành,
- dễ debug và test end-to-end,
- chưa cần gánh thêm độ phức tạp của nhiều repo hoặc nhiều service,
- vẫn giữ được khả năng tách service về sau nếu boundary module được giữ chặt.

## 2.2 Công nghệ chính

- **Ngôn ngữ:** Java 21
- **Framework:** Spring Boot 3.x
- **Kiến trúc:** Modular Monolith
- **Database chính:** MySQL
- **Cache / Presence / Hỗ trợ realtime:** Redis
- **Realtime communication:** Spring WebSocket + STOMP
- **Lưu trữ file:** MinIO
- **Event nội bộ:** Spring Application Events
- **Xử lý async nâng cao (tùy chọn):** RabbitMQ

## 2.3 Vì sao không chọn Microservices ngay từ đầu

Không đề xuất đi thẳng lên microservice ở giai đoạn đầu vì:

- phạm vi chức năng hiện tại chưa đủ lớn để tách dịch vụ độc lập,
- chi phí vận hành tăng mạnh khi có nhiều service,
- phức tạp hơn ở CI/CD, monitoring, tracing, deployment,
- khó hơn với team còn nhỏ hoặc giai đoạn đầu dự án,
- bài toán hiện tại vẫn có thể đáp ứng tốt bằng một monolith chia module rõ ràng.

Modular Monolith là bước đệm phù hợp vì:
- code vẫn có ranh giới module rõ ràng,
- dễ maintain hơn kiểu layered monolith truyền thống,
- sau này có thể tách riêng module thành service nếu tăng tải hoặc tăng nghiệp vụ.

---

# 3. Mục tiêu phi chức năng

## 3.1 Mục tiêu ban đầu

- Đảm bảo triển khai nhanh và rõ ràng.
- Hỗ trợ đồng thời khoảng 3.000 user ở mức ban đầu.
- Dễ tích hợp với Mobile App, Web App và hệ thống đối tác.
- Dễ mở rộng thêm notification, attachment, webhook, audit, analytics sau này.

## 3.2 Nguyên tắc thiết kế

- **Database là nguồn dữ liệu chính (source of truth).**
- **Redis chỉ đóng vai trò hỗ trợ cache, realtime, presence, rate limiting.**
- **WebSocket chỉ dùng cho realtime, không thay thế hoàn toàn REST API.**
- **REST API vẫn là lớp ổn định cho CRUD, lịch sử, đồng bộ, quản trị.**
- **Không lưu message chính ở Redis.**
- **Không đẩy business logic cốt lõi sang WebSocket hoặc Notification layer.**
- **Không đẩy độ phức tạp async quá sớm nếu chưa cần.**
- **Ưu tiên giữ core business trong các module Identity, Conversation, Message, Integration.**

---

# 4. Các thành phần hệ thống

## 4.1 Client / Partner Systems

Các nguồn gọi vào hệ thống có thể gồm:

- Mobile App
- Web App
- 3rd Party App
- Hệ thống tích hợp đối tác là nguồn đồng bộ người dùng

Các client này có thể dùng hệ thống qua:
- REST API
- WebSocket/STOMP
- API tích hợp riêng cho partner

## 4.2 API Layer / Security Layer

Tầng vào của hệ thống bao gồm:

- Spring Security
- xác thực JWT cho người dùng cuối
- xác thực API key hoặc integration token cho đối tác tích hợp
- CORS
- rate limiting
- request logging
- kiểm tra phân quyền theo conversation / resource

Lưu ý: trong giai đoạn đầu, không cần tách một API Gateway độc lập như Spring Cloud Gateway. Có thể triển khai:

- Nginx reverse proxy
- một ứng dụng Spring Boot xử lý toàn bộ REST + WebSocket + Security

## 4.3 REST API Layer

REST API chịu trách nhiệm cho các luồng ổn định, có thể truy vết và phù hợp với CRUD:

- đồng bộ người dùng từ đối tác
- tạo hội thoại
- lấy danh sách hội thoại
- lấy lịch sử tin nhắn
- gửi tin nhắn qua HTTP nếu cần
- đánh dấu đã đọc
- quản lý thành viên nhóm
- upload / gắn file

## 4.4 WebSocket/STOMP Layer

WebSocket được dùng cho:

- nhận tin nhắn realtime
- phát sự kiện đã đọc
- typing indicator nếu cần
- presence / online status ở mức realtime

Nguyên tắc:
- Client kết nối WebSocket sau khi đăng nhập.
- Token cần được xác thực khi connect.
- Khi subscribe vào một conversation, backend phải kiểm tra user có thuộc conversation đó hay không.

---

# 5. Kiến trúc module bên trong Modular Monolith

Đề xuất chia thành các module sau.

Có thể chia thành 2 nhóm chính:
- **Core business modules:** Identity, Conversation, Message, Media, Integration
- **Supporting modules:** Realtime, Notification, Shared/Common

Việc phân nhóm này giúp giữ rõ đâu là nơi chứa nghiệp vụ cốt lõi và đâu là lớp hỗ trợ, tránh để các module hỗ trợ dần dần nắm quá nhiều business logic.

## 5.1 Identity / User Module

Chịu trách nhiệm:

- thông tin user nội bộ
- profile cơ bản
- mapping user nội bộ với user từ hệ thống bên ngoài
- tra cứu danh tính người dùng
- API user sync cơ bản

Nghiệp vụ chính:
- tạo user nội bộ
- cập nhật profile
- upsert user khi partner sync dữ liệu
- quản lý external user mapping

## 5.2 Conversation Module

Chịu trách nhiệm:

- tạo hội thoại 1-1
- tạo hội thoại nhóm
- quản lý thành viên
- quyền trong nhóm
- setting cơ bản của hội thoại

Nghiệp vụ chính:
- tạo direct conversation
- tạo group conversation
- thêm / xóa thành viên
- đổi tên nhóm
- mute conversation
- kiểm tra membership khi đọc/gửi message

## 5.3 Message Module

Chịu trách nhiệm:

- gửi tin nhắn
- lưu lịch sử tin nhắn
- chỉnh sửa / xóa mềm tin nhắn nếu cần
- trạng thái gửi / đọc
- reaction cơ bản nếu triển khai

Nghiệp vụ chính:
- lưu message text
- lưu tham chiếu attachment
- phân trang lịch sử tin nhắn
- đánh dấu đã đọc
- phát sinh event khi gửi thành công

## 5.4 Notification Module

Đây là **supporting module**, không phải nơi xử lý nghiệp vụ gửi message cốt lõi.

Chịu trách nhiệm:

- in-app notification
- push notification trong tương lai
- FCM nếu sau này tích hợp mobile push
- phân luồng thông báo cho user offline

Giai đoạn đầu có thể chỉ cần:
- chuẩn bị interface
- xử lý tối thiểu bằng event nội bộ
- phản ứng sau khi nghiệp vụ chính đã hoàn tất thành công

Notification Module nên tiêu thụ event từ module khác thay vì giữ logic nghiệp vụ trung tâm.

## 5.5 Media Module

Chịu trách nhiệm:

- upload file
- upload ảnh
- quản lý metadata file
- liên kết file với message

File sẽ được lưu ở MinIO. Database chỉ nên lưu metadata và đường dẫn/key object.

## 5.6 Integration Module

Chịu trách nhiệm:

- quản lý ứng dụng tích hợp / partner systems
- quản lý API key / token tích hợp
- user sync API
- webhook callback nếu có
- xác thực hệ thống đối tác

Đây là module rất quan trọng vì hệ thống không chỉ phục vụ end-user app mà còn phải mở API cho hệ thống khác đẩy danh sách user sang.

## 5.7 Shared / Common Module

Chứa các phần dùng chung:

- exception handling
- response model
- common config
- security utils
- audit base classes
- constants
- utils
- logging helpers

Lưu ý:
- shared chỉ nên chứa phần dùng chung thực sự, tránh biến shared thành “sọt rác” cho mọi thứ,
- không đưa business logic vào shared chỉ vì muốn tái sử dụng nhanh,
- nếu một đoạn code chỉ phục vụ một module nghiệp vụ thì nên để lại trong module đó.

## 5.8 Realtime Module

Đây là **supporting module**, chịu trách nhiệm đẩy sự kiện realtime tới client sau khi nghiệp vụ chính đã được xử lý và commit thành công.

Chịu trách nhiệm:

- cấu hình WebSocket/STOMP
- authenticate khi connect
- authorize khi subscribe theo conversation
- đẩy message mới, read receipt, presence event tới client

Realtime Module không nên trở thành nơi xử lý nghiệp vụ gửi tin nhắn chính. Business flow vẫn nên đi qua REST/WebSocket entrypoint -> application service -> database -> event -> realtime push.

---

# 6. Mô hình tương tác giữa các module

Các module nên giao tiếp theo nguyên tắc:

- module này không truy cập bừa vào toàn bộ chi tiết bên trong module khác,
- chỉ gọi qua service công khai hoặc event,
- giữ ranh giới nghiệp vụ rõ ràng,
- hạn chế phụ thuộc trực tiếp giữa nhiều module core nếu không thật sự cần,
- không đẩy business rule vào realtime, notification hoặc shared chỉ vì tiện triển khai.

Với giai đoạn đầu dùng một repo và một deployable service, boundary ở cấp module là hàng rào quan trọng nhất. Nếu giữ ranh giới này tốt từ đầu thì sau này việc tách service sẽ dễ hơn nhiều.

Ví dụ luồng gửi tin nhắn:

1. Request đi vào Message API
2. Message Module gọi Conversation Module để xác nhận membership
3. Message Module lưu message vào MySQL
4. Message Module phát `MessageSentEvent`
5. Realtime / Notification lắng nghe event để xử lý tiếp sau khi transaction chính đã commit thành công

Điều này giúp:
- giảm coupling
- dễ test
- dễ chuyển một module ra service riêng trong tương lai nếu cần

---

# 7. Thiết kế dữ liệu mức khái niệm

## 7.1 Các thực thể chính

### User
Thông tin người dùng nội bộ của nền tảng messaging.

Ví dụ trường:
- id
- username
- display_name
- avatar_url
- status
- created_at
- updated_at

### IntegrationApp
Đại diện cho một hệ thống bên ngoài được phép tích hợp.

Ví dụ trường:
- id
- code
- name
- api_key_hash hoặc token metadata
- status
- created_at

### ExternalUserMapping
Bảng mapping giữa user ngoài hệ thống và user nội bộ.

Ví dụ trường:
- id
- integration_app_id
- external_user_id
- user_id
- username_snapshot
- display_name_snapshot
- last_synced_at

**Ràng buộc quan trọng:**
- unique `(integration_app_id, external_user_id)`

Điều này tránh lỗi trùng ID giữa nhiều hệ thống đối tác.

### Conversation
Đại diện cho hội thoại.

Ví dụ trường:
- id
- type (DIRECT / GROUP)
- title
- created_by
- created_at
- updated_at

### ConversationMember
Đại diện cho thành viên thuộc hội thoại.

Ví dụ trường:
- id
- conversation_id
- user_id
- role
- joined_at
- last_read_message_id
- muted

### Message
Đại diện cho tin nhắn.

Ví dụ trường:
- id
- conversation_id
- sender_id
- message_type
- content
- metadata_json
- status
- created_at
- updated_at

### MessageReceipt
Theo dõi trạng thái delivered / read của từng user cho từng message nếu cần mở rộng.

Ví dụ trường:
- id
- message_id
- user_id
- status
- read_at

### MediaFile
Thông tin metadata của file upload.

Ví dụ trường:
- id
- object_key
- bucket
- file_name
- content_type
- size
- uploaded_by
- created_at

## 7.2 Index quan trọng

Các index nên có từ đầu:

- index trên `(conversation_id, created_at)` của bảng message
- unique trên `(conversation_id, user_id)` của conversation_member
- unique trên `(integration_app_id, external_user_id)` của external_user_mapping
- index trên `user_id` của conversation_member
- index trên `sender_id`, `created_at` nếu cần thống kê

---

# 8. Luồng nghiệp vụ chính

## 8.1 Luồng đồng bộ user từ hệ thống tích hợp

1. Partner gọi API sync user.
2. Integration Module xác thực integration token / API key.
3. Dữ liệu user được chuyển qua User Module để upsert.
4. Hệ thống lưu hoặc cập nhật `ExternalUserMapping`.
5. Nếu cần, phát event `UserSyncedEvent`.

Ghi chú:
- nên hỗ trợ sync từng user và bulk sync,
- nên có cơ chế idempotent cơ bản để tránh tạo trùng khi partner gọi lại.

## 8.2 Luồng tạo hội thoại 1-1

1. User A chọn user B.
2. Hệ thống kiểm tra đã có direct conversation giữa 2 user chưa.
3. Nếu chưa có thì tạo conversation loại DIRECT.
4. Tạo 2 bản ghi conversation member.
5. Trả về conversation info.

## 8.3 Luồng gửi tin nhắn

1. User gửi request qua REST hoặc WebSocket.
2. Security xác thực token.
3. Message Module kiểm tra user có là member của conversation hay không.
4. Lưu message vào MySQL.
5. Sau khi transaction thành công, phát event `MessageSentEvent`.
6. Realtime layer đẩy message tới các client đang subscribe.
7. Nếu người nhận offline, Notification Module có thể xử lý thêm push/in-app sau này.

**Nguyên tắc rất quan trọng:** phải lưu DB thành công trước rồi mới phát realtime.

## 8.4 Luồng đọc lịch sử tin nhắn

1. User gọi API lấy lịch sử.
2. Kiểm tra membership.
3. Trả về danh sách message theo pagination.

Khuyến nghị dùng cursor-based pagination thay vì offset pagination khi dữ liệu lớn.

Ví dụ:
- lấy 30 tin nhắn trước message id X
- hoặc trước thời điểm Y

## 8.5 Luồng đánh dấu đã đọc

1. User mở conversation.
2. Frontend gửi request mark-as-read.
3. Hệ thống cập nhật last read marker hoặc message receipt.
4. Phát event read-receipt nếu có.

---

# 9. Realtime và Presence

## 9.1 Vai trò của WebSocket/STOMP

WebSocket/STOMP được dùng cho:
- nhận tin nhắn mới ngay lập tức
- cập nhật trạng thái đọc
- typing indicator
- thay đổi online status

## 9.2 Vai trò của Redis

Redis được dùng để hỗ trợ:
- lưu online/offline status tạm thời
- lưu last active
- cache dữ liệu nóng
- rate limit
- hỗ trợ pub/sub hoặc session mapping khi nhiều instance

Redis **không** phải nơi lưu trữ message chính.

## 9.3 Channel gợi ý

Ví dụ các destination:
- `/user/queue/messages`
- `/topic/conversations/{conversationId}`

Lưu ý bảo mật:
- không được cho subscribe tùy ý vào mọi conversation
- phải kiểm tra quyền truy cập conversation trước khi cho nhận dữ liệu

---

# 10. Quản lý file với MinIO

## 10.1 Vai trò của MinIO

MinIO được dùng để:
- lưu ảnh
- lưu file đính kèm
- phục vụ object storage kiểu S3-compatible

## 10.2 Nguyên tắc lưu file

- File binary lưu trên MinIO
- MySQL chỉ lưu metadata
- Message chỉ lưu tham chiếu tới file hoặc media object

Ví dụ:
- message chứa `media_file_id`
- hoặc `metadata_json` chứa danh sách attachment refs

## 10.3 Lợi ích

- tránh lưu file lớn trong database
- dễ mở rộng dung lượng
- dễ thay MinIO bằng S3 thật nếu cần trong tương lai

---

# 11. Event nội bộ với Spring Application Events

## 11.1 Mục tiêu

Dùng Spring Application Events để:
- tách bớt coupling trực tiếp giữa các module
- xử lý các hành vi phát sinh sau khi nghiệp vụ chính hoàn thành
- hỗ trợ mở rộng dần mà chưa cần queue ngoài ngay

## 11.2 Ví dụ event

- `MessageSentEvent`
- `ConversationCreatedEvent`
- `UserSyncedEvent`
- `MessageReadEvent`
- `MediaUploadedEvent`

## 11.3 Ứng dụng thực tế

Ví dụ khi gửi tin:
- Message Module chỉ cần lưu message và bắn `MessageSentEvent`
- Notification Module nhận event để chuẩn bị gửi thông báo
- Realtime handler nhận event để push tới websocket
- Audit module nhận event để ghi log nghiệp vụ nếu cần

## 11.4 Lưu ý

- event nội bộ chỉ phù hợp khi còn chạy trong một ứng dụng
- nếu sau này cần reliability cao, retry rõ ràng, tách process riêng, nên chuyển một phần sang RabbitMQ

---

# 12. Khi nào cần RabbitMQ

RabbitMQ chưa bắt buộc ngay từ ngày đầu. Chỉ nên thêm khi xuất hiện một hoặc nhiều nhu cầu sau:

- cần xử lý bất đồng bộ rõ ràng với retry
- cần gửi push notification hàng loạt
- cần webhook callback có retry
- cần tách tiến trình xử lý nền ra khỏi luồng request chính
- cần giảm thời gian response cho request
- có tác vụ nền tương đối nặng hoặc dễ lỗi tạm thời

## 12.1 Các use case phù hợp cho RabbitMQ

- gửi push notification
- gửi email
- webhook ra đối tác
- xử lý upload hậu kỳ
- đồng bộ dữ liệu số lượng lớn
- fan-out event ra nhiều consumer

## 12.2 Không nên dùng RabbitMQ cho phần nào ở giai đoạn đầu

Không nên phụ thuộc queue để xử lý logic gửi message cốt lõi ngay từ đầu nếu chưa thật sự cần, vì sẽ làm tăng độ phức tạp của:
- consistency
- tracing
- debugging
- vận hành

Luồng gửi message lõi vẫn nên là:
- validate
- save DB
- commit
- đẩy realtime / phát event

---

# 13. Bảo mật

## 13.1 Xác thực

Nên có hai lớp xác thực riêng:

### Đối với user cuối
- JWT access token

### Đối với hệ thống tích hợp
- API key
- hoặc integration token riêng

Không nên dùng chung một cơ chế token cho cả end-user và partner integration nếu nghiệp vụ khác nhau rõ ràng.

## 13.2 Phân quyền

Các điểm cần kiểm tra chặt:
- user có thuộc conversation không
- user có quyền thêm / xóa member không
- app tích hợp có quyền sync user không
- user có quyền xem file của conversation không

## 13.3 Các biện pháp khác

- rate limit cho REST API
- rate limit cho WebSocket connect nếu cần
- validate dữ liệu đầu vào
- kiểm tra kích thước file upload
- log audit các hành động nhạy cảm
- hash API key, không lưu plain text

---

# 14. Logging, Audit, Monitoring

Đây là phần nên có ngay từ đầu dù MVP.

## 14.1 Logging

Nên có:
- request log
- error log
- security log
- audit log mức nghiệp vụ

Ví dụ cần log:
- sync user từ partner
- tạo conversation
- gửi tin nhắn lỗi
- upload file lỗi
- lỗi websocket auth

## 14.2 Audit

Các hành động nên audit:
- partner sync user
- thêm / xóa thành viên group
- xóa tin nhắn
- đổi thông tin hội thoại
- rotate API key nếu có

## 14.3 Monitoring

Khuyến nghị:
- Spring Boot Actuator
- health check
- metrics cơ bản
- thống kê số websocket connection
- thống kê message throughput
- thống kê error rate

---

# 15. Định hướng API mức cao

## 15.1 Integration API

Ví dụ:
- `POST /api/integrations/users/sync`
- `POST /api/integrations/users/bulk-sync`
- `GET /api/integrations/users/{externalUserId}`

## 15.2 Conversation API

Ví dụ:
- `POST /api/conversations/direct`
- `POST /api/conversations/group`
- `GET /api/conversations`
- `GET /api/conversations/{id}`
- `POST /api/conversations/{id}/members`
- `DELETE /api/conversations/{id}/members/{userId}`

## 15.3 Message API

Ví dụ:
- `POST /api/conversations/{conversationId}/messages`
- `GET /api/conversations/{conversationId}/messages`
- `PUT /api/messages/{messageId}`
- `DELETE /api/messages/{messageId}`
- `POST /api/messages/{messageId}/read`

## 15.4 Media API

Ví dụ:
- `POST /api/media/upload`
- `GET /api/media/{id}`
- `DELETE /api/media/{id}`

---

# 16. Định hướng cấu trúc code

Gợi ý package/module:

```text
com.company.messaging
├── MessagingApplication
├── shared
│   ├── config
│   ├── security
│   ├── exception
│   ├── response
│   └── util
├── identity
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── conversation
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── message
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── notification
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── media
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── integration
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
└── realtime
    ├── api
    ├── application
    └── infrastructure
```

Trong đó:
- `api`: controller, request, response
- `application`: service, use case, orchestration
- `domain`: entity, aggregate, business rules
- `infrastructure`: repository, external adapter, config kỹ thuật

---

# 17. Ưu điểm của phương án đề xuất

- Dễ hiểu, dễ triển khai cho team Java/Spring Boot.
- Phù hợp với tải ban đầu khoảng 3.000 user.
- Không bị over-engineering quá sớm.
- Có ranh giới module rõ, hỗ trợ maintain tốt hơn layered monolith thường.
- Dễ mở rộng thêm notification, media, webhook, sync user.
- Có đường nâng cấp rõ ràng sang async queue hoặc tách service sau này.

---

# 18. Rủi ro và lưu ý

## 18.1 Rủi ro nếu thiết kế không chặt

- Coupling quá nhiều giữa các module.
- Không kiểm soát quyền conversation tốt.
- Đồng bộ user gây trùng dữ liệu.
- Lạm dụng Redis làm nơi lưu dữ liệu chính.
- Đẩy quá nhiều logic vào WebSocket.
- Thêm RabbitMQ quá sớm làm tăng độ phức tạp.

## 18.2 Các điểm cần chốt thêm ở giai đoạn thiết kế chi tiết

- chiến lược auth cụ thể
- định dạng integration token
- versioning API
- thiết kế read receipt chi tiết
- xóa mềm / thu hồi message
- giới hạn file upload
- retention policy của media
- quy tắc tạo direct conversation trùng
- mức độ hỗ trợ message reaction và reply thread

---

# 19. Lộ trình triển khai đề xuất

## Phase 1 - Core MVP
- user nội bộ
- external user mapping
- sync user từ partner
- direct conversation
- group conversation cơ bản
- gửi / nhận message text
- lấy lịch sử message
- WebSocket realtime cơ bản
- upload file cơ bản với MinIO

## Phase 2 - Ổn định và vận hành
- read receipt
- online/offline presence với Redis
- rate limit
- audit log
- health check / metrics
- tối ưu index và pagination

## Phase 3 - Mở rộng async
- RabbitMQ cho notification / webhook / retry
- push notification
- webhook callback
- xử lý nền nâng cao

## Phase 4 - Mở rộng sản phẩm
- reaction
- message edit/delete nâng cao
- typing indicator
- mute / pin conversation
- analytics / report
- cân nhắc tách service nếu quy mô tăng mạnh

---

# 20. Kết luận

Phương án kiến trúc được đề xuất là:

- **Spring Boot Modular Monolith**
- **MySQL**
- **Redis**
- **WebSocket/STOMP**
- **MinIO**
- **Spring Application Events**
- **RabbitMQ nếu cần mở rộng async**

Đây là một phương án cân bằng giữa:
- khả năng triển khai thực tế,
- độ phức tạp hợp lý,
- khả năng mở rộng sau này,
- phù hợp cho một hệ thống messaging platform tích hợp đa ứng dụng trong giai đoạn đầu.

Định hướng này phù hợp để:
- triển khai nhanh MVP,
- dễ trình bày cho quản lý,
- có nền tảng đủ tốt để phát triển thành hệ thống production,
- và vẫn giữ cửa mở cho việc scale tiếp trong tương lai mà không cần làm lại toàn bộ kiến trúc từ đầu.
