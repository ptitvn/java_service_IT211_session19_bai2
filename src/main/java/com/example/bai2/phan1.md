Phần 1 – Phân tích logic
Đoạn code InsecureTokenService có hai lỗ hổng bảo mật nghiêm trọng:

Lỗ hổng Mô tả Hậu quả

| Lỗ hổng                                   | Mô tả                                                                                                                                                                  | Hậu quả                                                                                                                                                                                                                       |
|-------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| HARDCODED_SECRET_KEY**              | Khóa bí mật được viết trực tiếp trong mã nguồn (``private ``static ``final ``String ``HARDCODED_SECRET_KEY ``= ``"ThisIsAVerySecretKeyButItsHardcodedAndTooShort";``). | - Dễ bị lộ khi mã nguồn bị rò rỉ hoặc bị reverse-engineering.<br>- Kẻ tấn công có thể tạo token giả mạo hợp lệ nếu biết khóa này.<br>- Token giả mạo có thể mang quyền admin, gây rủi ro nghiêm trọng cho hệ thống giao dịch. |
| ACCESS_TOKEN_EXPIRATION_DAYS = 30** | Token có thời gian sống quá dài (30 ngày).                                                                                                                             | - Nếu token bị đánh cắp, kẻ tấn công có thể sử dụng nó trong suốt 30 ngày.<br>- Tăng “khung thời gian khai thác” và giảm khả năng phát hiện sớm hành vi xâm nhập.                                                             |

Kịch bản tấn công thực tế:

Hacker truy cập được mã nguồn hoặc đoán được secret key.

Tạo token giả mạo với quyền “admin”.

Gửi request đến API — hệ thống chấp nhận vì token hợp lệ.

Trong 30 ngày, hacker có thể thực hiện giao dịch giả, xem dữ liệu người dùng, hoặc thao túng thị trường.