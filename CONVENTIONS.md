# Project Conventions

Tài liệu này tổng hợp các convention và lưu ý đang được áp dụng trong repo để tiện tra cứu nhanh. Đây là bản tóm tắt từ codebase và các tài liệu trong `docs/`, không thay thế tài liệu chi tiết.

## 1. Kiến trúc tổng thể
- Dự án là **modular monolith** chạy dưới dạng một Spring Boot service duy nhất.
- Mã nguồn được chia thành 3 vùng chính:
  - `modules/`: business modules.
  - `common/`: shared cross-cutting code.
  - `infrastructure/`: shared technical adapters.

## 2. Cấu trúc module
Mỗi business module trong `modules/<module>` nên tách rõ các package:
- `api/`
- `application/`
- `domain/`
- `infrastructure/`

Mục tiêu là giữ boundary rõ ràng giữa HTTP/API, use case, business rule và technical adapter.

## 3. Quy tắc phụ thuộc
- Luồng layer trong một module: `api -> application -> domain`.
- Shared infrastructure chỉ nên phụ thuộc vào `domain` hoặc `application`.
- `common` không được phụ thuộc vào package bên trong `modules`.
- Một module không được phụ thuộc trực tiếp vào `infrastructure` của module khác.

## 4. API convention
- Public HTTP endpoints dùng prefix version: `/api/v1`.
- Request DTO đặt tại `modules/<module>/api/request`.
- Response DTO đặt tại `modules/<module>/api/response`.
- Controller nên trả về `common/response/ApiResponse` để thống nhất response envelope.

## 5. Coding note
- Business logic nên nằm ở `domain` và `application`, không nhồi vào controller.
- Chỉ chia sẻ abstraction ổn định qua `common` hoặc shared `infrastructure`.
- Ưu tiên bám theo pattern hiện có của module thay vì tự mở rộng scope refactor.
- Comment ở các chỗ quan trọng, nhất là nơi có ràng buộc hoặc ý đồ không hiển nhiên.

## 6. Verify local
Các lệnh thường dùng:

```bash
mvn spring-boot:run
mvn verify
```

## 7. Tài liệu gốc nên đọc khi cần chi tiết
- `README.md`
- `docs/architecture.md`
- `docs/api-convention.md`
- `docs/database-convention.md`
- `docs/error-code.md`
