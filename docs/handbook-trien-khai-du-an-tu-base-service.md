# Handbook triển khai dự án từ base-service

## 1. Mục đích tài liệu
Tài liệu này là handbook kỹ thuật bằng tiếng Việt dành cho người mới bắt đầu và cho team kỹ thuật khi dùng `base-service` để triển khai một dự án thật.

Handbook này giúp người đọc:
- hiểu base này phù hợp với loại dự án nào
- biết cách khởi tạo một service mới từ base
- biết cách đặt code đúng vị trí theo kiến trúc hiện tại
- biết cách thêm module, API, migration, test theo đúng convention
- biết các lỗi thiết kế phổ biến cần tránh
- có checklist thực tế trước khi commit và merge

Tài liệu này bám theo **trạng thái code hiện tại** của repository, không mô tả các tính năng chưa tồn tại như thể đã sẵn sàng production.

---

## 2. Base này là gì
`base-service` là một base backend dùng Java 21, Maven và Spring Boot 3.5.x, được tổ chức theo hướng modular monolith.

Điểm chính của base hiện tại:
- chạy như **một Spring Boot service duy nhất**
- chia source code theo business module thay vì dồn toàn bộ vào technical layer ở root
- có sẵn một số convention nền cho API, migration, error handling và test baseline
- có CI mỏng với GitHub Actions và quality checks chạy qua `mvn verify`
- có `modules/sample` làm module create-flow mẫu để tham chiếu cách tổ chức code, migration, API và test
- không có auth production-ready mặc định trong base

Các vùng chính hiện có:
- `common/`: thành phần dùng chung như response envelope, exception handling, utility, marker package
- `infrastructure/`: marker và nhóm kỹ thuật dùng chung như cache, persistence, messaging, storage, logging, mail, scheduler, external
- `modules/`: business module

Hiện trạng module nghiệp vụ:
- `modules/sample`: module create-flow mẫu dùng để tham chiếu cách tổ chức code, API, migration và test
- `modules/auth`: sample flow login đơn giản, không phải auth production-ready
- `modules/user`, `modules/role`, `modules/file`: mới là placeholder

Lưu ý quan trọng:
- `sample` hiện tại là **module mẫu/reference**, có thể giữ lại làm chuẩn tham chiếu hoặc xóa khi khởi tạo dự án mới
- `auth` chỉ là sample flow đơn giản để minh họa structure, không phải authentication module production-ready
- token đang được sinh bởi `SimpleTokenIssuer` và session được lưu bởi `InMemoryAuthSessionRepository`

---

## 3. Khi nào nên dùng
Nên dùng base này khi:
- bạn muốn khởi đầu nhanh một backend Java nhưng vẫn giữ cấu trúc rõ ràng
- team muốn chuẩn hóa flow `api -> application -> domain` ngay từ đầu
- dự án có định hướng tăng trưởng dần nhiều business module nhưng vẫn phù hợp chạy một deployable
- muốn có sẵn convention nền cho API versioning, migration và error handling validate

> **Nên làm**
> - Dùng base làm nền để xây business logic mới.
> - Chốt sớm rule tổ chức code cho cả team theo structure hiện tại.
>
> **Không nên làm**
> - Không coi base này là sản phẩm hoàn chỉnh đã có đủ auth, permission, security, observability cho production.
>
> **Ví dụ thực tế**
> - Dự án quản lý nội bộ có thể bắt đầu với `user`, `role`, `file`, sau đó mở rộng `project`, `task` theo đúng module boundary.

---

## 4. Khi nào không nên dùng
Không nên dùng base này khi:
- service quá nhỏ và chỉ cần vài API CRUD ngắn hạn
- bạn đang làm prototype tạm thời, ưu tiên tốc độ hơn convention
- bài toán yêu cầu kiến trúc đặc thù hoặc microservices ngay từ đầu
- hệ thống cần IAM/auth production-grade hoàn chỉnh ngay pha đầu

Dấu hiệu nên chọn nền tảng khác:
- cần OAuth2/OIDC/JWT chuẩn, refresh token, revocation, audit, MFA ngay từ đầu
- cần tích hợp persistence/queue/search production-grade có sẵn thay vì tự bổ sung dần
- team không muốn duy trì boundary module rõ ràng

---

## 5. Quy trình khởi tạo một dự án mới từ base này
### 5.1. Bước 1 - Clone hoặc copy base
- clone repository base hiện tại
- tạo repository mới cho service thật
- chưa nên code tính năng ngay trước khi dọn phần nhận diện dự án

### 5.2. Bước 2 - Đổi thông tin nhận diện project
Cần rà và đổi đồng bộ:
- `groupId`
- `artifactId`
- `name`
- `description`
- package root Java
- `spring.application.name`
- nội dung `README.md`
- các docs còn nhắc tới tên service cũ

### 5.3. Bước 3 - Rà lại cấu hình môi trường
Hiện có các file:
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`
- `.env.example`

Thực hành nên áp dụng:
- giữ `application.yml` cho config chung
- tách cấu hình theo profile cho local/dev/prod
- tránh hard-code config môi trường trong code Java

### 5.4. Bước 4 - Chạy baseline verification nền
Chạy:
```bash
mvn verify
```
Hoặc tối thiểu:
```bash
mvn -Dtest=ApplicationStructureSmokeTest test
```
`mvn verify` sẽ chạy compile, test và các quality checks của base. Mục tiêu là xác nhận baseline structure, tài nguyên nền và code quality còn đúng trước khi mở rộng dự án.

### 5.5. Bước 5 - Dọn phần mẫu không dùng
Cần hiểu rõ ngay từ đầu:
- `auth` hiện tại chỉ là module tham chiếu, chưa phải auth production-ready
- `user`, `role`, `file` hiện tại chủ yếu là placeholder
- base này là khung khởi đầu, không phải sản phẩm hoàn thiện sẵn

> **Nên làm**
> - Chốt naming, DTO rule, migration rule và response contract ngay sau khi khởi tạo.
>
> **Không nên làm**
> - Viết tính năng ad-hoc trước khi xác định module boundary.
>
> **Ví dụ thực tế**
> - Sprint đầu có thể chỉ làm module `user` với create user API, migration bảng đầu tiên và test cơ bản cho service + controller.

---

## 6. Tổng quan kiến trúc
Base này được tổ chức theo mô hình modular monolith: toàn bộ hệ thống vẫn là một Spring Boot service deploy chung, nhưng code được chia theo business module.

### 6.1. Các vùng chính trong source code
- `modules/`: chứa business module
- `common/`: chứa cross-cutting concern dùng chung toàn hệ thống
- `infrastructure/`: chứa technical adapter dùng chung toàn hệ thống

### 6.2. Layer flow trong một module
Quy tắc phụ thuộc chính:
- `api -> application -> domain`

Diễn giải ngắn:
- `api`: nhận request, validate input, map response
- `application`: orchestration use case, điều phối domain và interface cần thiết
- `domain`: mô hình nghiệp vụ, rule cốt lõi, abstraction nghiệp vụ

### 6.3. Dependency direction bắt buộc
- `module infrastructure -> domain` hoặc `module infrastructure -> application`
- `common` không phụ thuộc `modules`
- module A không gọi trực tiếp `infrastructure` của module B

> **Nên làm**
> - Giữ controller mỏng, business logic nằm ở `application` và `domain`.
>
> **Không nên làm**
> - Để controller gọi trực tiếp repository hoặc tự xử lý rule nghiệp vụ.
>
> **Ví dụ thực tế**
> - `AuthController` chỉ nhận request, gọi `AuthApplicationService`, rồi bọc output bằng `ApiResponse`.

---

## 7. Quy ước thư mục và trách nhiệm từng package
### 7.1. `common/`
Dùng cho các thành phần cross-cutting thực sự dùng chung, ví dụ:
- `common/response/ApiResponse`
- `common/exception/GlobalExceptionHandler`
- config hoặc utility dùng chung

Không nên đặt vào `common/`:
- business enum của riêng một module
- business helper chỉ phục vụ một module
- domain model riêng của `user`, `auth`, `role`, `file`

### 7.2. `infrastructure/`
Dùng cho technical adapter dùng chung toàn hệ thống, ví dụ:
- persistence config
- cache
- logging
- messaging
- storage
- mail
- scheduler

Không nên đặt ở đây:
- `UserRepositoryImpl`
- `AuthTokenJpaAdapter`
- implementation chỉ phục vụ một module riêng

### 7.3. `modules/`
Mỗi module là một business capability riêng. Structure chuẩn bên trong mỗi module:
- `api/`
- `api/request`
- `api/response`
- `application/`
- `domain/`
- `infrastructure/`

### 7.4. Ví dụ placement thực tế
| Artifact/class | Nên đặt ở đâu | Vì sao |
|---|---|---|
| `CreateUserRequest` | `modules/user/api/request` | Đây là HTTP input contract |
| `CreateUserService` | `modules/user/application/service` | Đây là orchestration của use case |
| `User` | `modules/user/domain/model` | Đây là business model |
| `UserRepositoryJpaAdapter` | `modules/user/infrastructure/persistence` | Đây là implementation riêng của module user |
| `S3ClientConfig` | `infrastructure/storage` | Đây là shared technical adapter |

### 7.5. Anti-pattern cần tránh
- biến `common` thành nơi nhét tạm mọi thứ
- để business logic trong controller
- để module phụ thuộc chéo trực tiếp vào nhau
- đặt module-specific implementation vào `infrastructure` global
- để mỗi module tự trả response theo kiểu khác nhau
- tạo quá nhiều package rỗng nhưng không dùng

> **Nên làm**
> - Đặt code đúng theo responsibility của package, ưu tiên rõ boundary hơn là tiện tay.
>
> **Không nên làm**
> - Thấy tiện thì đẩy hết helper hoặc adapter vào `common` hay `infrastructure` global.
>
> **Ví dụ thực tế**
> - `CreateUserRequest` nên ở `modules/user/api/request` vì đây là HTTP contract, không phải domain model.

---

## 8. Quy trình thêm một business module mới
Chỉ tạo module mới khi đó là một business capability đủ rõ ràng. Đừng tạo module chỉ vì muốn tách theo kỹ thuật.

### Bước triển khai đề xuất
1. Tạo package `modules/<module>`.
2. Tạo các package con: `api`, `api/request`, `api/response`, `application`, `domain`, `infrastructure`.
3. Xác định use case chính trước, ví dụ `createUser`, `getUserDetail`.
4. Đưa business rule vào domain hoặc abstraction nghiệp vụ phù hợp.
5. Triển khai adapter kỹ thuật riêng của module trong `module/infrastructure`.
6. Viết controller mỏng để map request -> command -> response.

> **Nên làm**
> - Chọn module đầu tiên theo nghiệp vụ trung tâm của service mới.
> - Đặt tên class bám sát use case và domain language.
>
> **Không nên làm**
> - Không import chéo implementation giữa các module nghiệp vụ.
> - Không gom nhiều capability khác nhau vào một module chỉ vì cùng dùng một bảng.
>
> **Ví dụ thực tế**
> - Module `project` có thể gồm `ProjectController`, `ProjectApplicationService`, `Project`, `ProjectRepositoryJpaAdapter` theo đúng từng lớp.

---

## 9. Quy trình viết API mới
### 9.1. Quy ước bắt buộc cho business API hiện tại
- endpoint business API hiện tại dùng prefix `/api/v1`
- request DTO đặt ở `api/request`
- response DTO đặt ở `api/response`
- response envelope chuẩn cho business API hiện tại là `ApiResponse(success, message, data)`

### 9.2. Luồng triển khai đề xuất
1. Tạo request DTO và đặt validation annotation như `@NotBlank`, `@NotNull` nếu cần.
2. Tạo response DTO cho output API.
3. Tạo method controller nhận `@Valid @RequestBody`.
4. Gọi application service bằng command/query object phù hợp.
5. Trả dữ liệu bằng `ApiResponse.success(...)`.

### 9.3. Hành vi hiện tại của response contract
`ApiResponse<T>` hiện có 3 trường:
- `success`
- `message`
- `data`

Helper hiện có:
- `ApiResponse.success(data)` -> trả `success=true`, `message=null`, `data=<payload>`

### 9.4. Ví dụ thật trong codebase hiện tại
- endpoint mẫu: `POST /api/v1/auth/login`
- controller: `modules/auth/api/AuthController`
- input: `LoginRequest(username, password)` với `@NotBlank`
- output: `ApiResponse<LoginResponse>`

> **Nên làm**
> - Giữ controller chỉ làm transport mapping và validate input.
> - Đảm bảo response luôn thống nhất để client xử lý ổn định.
>
> **Không nên làm**
> - Trả raw object không bọc `ApiResponse`.
> - Đưa business logic vào controller.
>
> **Ví dụ thực tế**
> - `AuthController` gọi `AuthApplicationService` và trả `ApiResponse.success(new LoginResponse(token))`.

---

## 10. Quy trình làm database và migration
### 10.1. Quy ước migration
- thư mục migration: `src/main/resources/db/migration`
- format tên file: `V<version>__<description>.sql`

Ví dụ hiện có:
- `V1__init_schema.sql`
- `V2__seed_default_data.sql`

### 10.2. Nguyên tắc thay đổi schema
- một migration nên tập trung vào một nhóm thay đổi rõ ràng
- schema migration và seed data nên tách file
- không sửa file migration cũ đã dùng ở môi trường chia sẻ; hãy thêm migration mới tăng version

> **Nên làm**
> - Tăng version tuần tự và giữ description ngắn gọn nhưng rõ nghĩa.
> - Review SQL trước khi merge nếu thay đổi có thể ảnh hưởng dữ liệu đang dùng.
>
> **Không nên làm**
> - Dồn nhiều thay đổi không liên quan vào cùng một migration.
> - Trộn seed tạm và schema trong cùng file.
>
> **Ví dụ thực tế**
> - Tạo bảng `project`: `V3__create_project_table.sql`
> - Seed role mặc định: `V4__seed_project_roles.sql`

---

## 11. Quy ước error handling hiện tại
`GlobalExceptionHandler` hiện đang xử lý `MethodArgumentNotValidException`.

Hành vi thực tế:
1. Gom toàn bộ lỗi validate theo field.
2. Join thành một chuỗi message bằng dấu `; `.
3. Trả HTTP `400 BAD_REQUEST` với body `ApiResponse<Void>`:
   - `success=false`
   - `message="fieldA: ...; fieldB: ..."`
   - `data=null`

Điểm cần nhớ:
- base này **chưa có trường `code` riêng** cho business error
- nếu sau này mở rộng response contract, phải cập nhật cả implementation và docs cùng lúc

> **Nên làm**
> - Viết validation message rõ nghĩa ở DTO để chuỗi message tổng hợp vẫn dễ đọc.
> - Giữ response envelope thống nhất giữa success và error.
>
> **Không nên làm**
> - Trả lỗi validate theo format khác nhau giữa các controller.
> - Giả định rằng base hiện tại đã có business error code riêng.
>
> **Ví dụ thực tế**
> - Request login thiếu `username` và `password` có thể tạo message dạng `username: must not be blank; password: must not be blank`.

---

## 12. Test strategy tối thiểu
Mức tối thiểu nên có cho mỗi module mới:
1. một application/service test cho use case chính
2. một controller test hoặc integration test cho HTTP contract
3. verify bổ sung nếu có migration hoặc flow DB quan trọng

Hiện tại repo đã có:
- `ApplicationStructureSmokeTest` để kiểm tra resource và skeleton types
- `AuthApplicationServiceTest`
- `AuthControllerTest`

> **Nên làm**
> - Với module mới, thêm ít nhất 1 test cho service và 1 test cho API thành công + validate fail.
>
> **Không nên làm**
> - Chỉ test happy path mà bỏ qua validation/error contract.
>
> **Ví dụ thực tế**
> - Với API tạo user: test thành công khi input hợp lệ, test `400` và `ApiResponse.success=false` khi field bắt buộc bị rỗng.

---

## 13. Cấu hình môi trường
Các file profile hiện có:
- `application.yml`
- `application-local.yml`
- `application-dev.yml`
- `application-prod.yml`
- `.env.example`

Khuyến nghị vận hành:
- giữ `application.yml` cho config nền chung
- để phần khác biệt môi trường trong các file profile riêng
- không commit secret thật vào repository
- ưu tiên dùng biến môi trường hoặc secret manager cho production
- giữ local/dev/prod đủ gần nhau để giảm sai khác hành vi

---

## 14. Checklist trước commit hoặc merge
### 14.1. Kiến trúc và cấu trúc
- [ ] Không vi phạm luồng `api -> application -> domain`
- [ ] Không import chéo sai boundary giữa các module
- [ ] DTO request/response đặt đúng `api/request`, `api/response`

### 14.2. API contract
- [ ] Endpoint dùng prefix `/api/v1`
- [ ] Response bọc `ApiResponse(success, message, data)`
- [ ] Validation fail trả contract đồng nhất qua global handler

### 14.3. Database
- [ ] Migration đặt đúng `db/migration`
- [ ] Tên file đúng format `V<version>__<description>.sql`
- [ ] Seed data và schema được tách hợp lý

### 14.4. Test
- [ ] Chạy test liên quan module mới hoặc phần vừa sửa
- [ ] Smoke test baseline không vỡ

### 14.5. Dọn phần base
- [ ] Đổi tên project đầy đủ
- [ ] Package root đã đúng
- [ ] Đã dọn module mẫu không dùng
- [ ] Docs riêng của service đã được cập nhật

> **Nên làm**
> - Dùng checklist này trong PR review để cả author và reviewer cùng đối chiếu.
>
> **Không nên làm**
> - Merge khi chưa xác nhận response contract hoặc migration naming.

---

## 15. Workflow mẫu để triển khai một dự án thật từ base
### Giai đoạn A - Foundation
1. Khởi tạo repo từ base.
2. Đổi tên project, package root, application name.
3. Chạy smoke test xác nhận baseline.
4. Dọn các module mẫu không dùng.

### Giai đoạn B - Vertical slice đầu tiên
1. Chọn một use case giá trị cao, ví dụ đăng ký user.
2. Làm đầy đủ từ migration -> domain -> application -> API -> test.
3. Review lại architecture boundary trước khi nhân rộng.

### Giai đoạn C - Mở rộng module
1. Thêm module mới theo pattern đã ổn định.
2. Chỉ đưa concern đủ ổn định vào `common` hoặc shared `infrastructure`.
3. Bổ sung dần logging, security, observability theo nhu cầu thật của sản phẩm.

> **Nên làm**
> - Làm theo vertical slice để sớm kiểm chứng kiến trúc.
> - Refactor nhẹ sau mỗi 1-2 use case để giữ structure sạch.
>
> **Không nên làm**
> - Làm hàng loạt API khi chưa có test strategy và migration discipline.
>
> **Ví dụ thực tế**
> - Sprint 1: user registration + login flow mẫu.
> - Sprint 2: role & permission cơ bản.
> - Sprint 3: file metadata + upload integration.

---

## 16. FAQ
### Có thể dùng luôn module `auth` hiện tại cho production không?
Không. `auth` hiện tại chỉ là **sample/reference** để minh họa tổ chức module và API contract.

### Có bắt buộc mọi API đều trả `ApiResponse` không?
Theo convention hiện tại, các business API trong service nên trả `ApiResponse(success, message, data)` để client xử lý thống nhất. Điều này không nhằm mô tả các endpoint kỹ thuật như actuator.

### Có nên đặt request/response DTO ngoài `api/request`, `api/response` không?
Không nên. Đây là convention quan trọng để codebase dễ đọc và dễ review.

### Tôi có thể sửa migration cũ không?
Không nên nếu migration đã chạy ở môi trường chia sẻ. Hãy tạo migration mới tăng version.

### Nếu cần chia sẻ logic giữa các module thì làm sao?
Ưu tiên đưa thứ đủ ổn định vào `common` hoặc shared `infrastructure`, tránh phụ thuộc trực tiếp vào implementation nội bộ của module khác.

---

## 17. Kết luận
`base-service` phù hợp khi team cần một điểm khởi đầu rõ ràng cho backend Java theo modular monolith, với convention đủ chặt để mở rộng dần mà vẫn dễ review và dễ onboard.

Giá trị lớn nhất của base này không nằm ở số lượng code có sẵn, mà nằm ở cấu trúc, rule và cách làm thống nhất để cả team cùng đi theo.

Khi triển khai dự án thật, hãy dùng handbook này như playbook nền, rồi bổ sung dần các năng lực production còn thiếu theo nhu cầu thực tế của sản phẩm.