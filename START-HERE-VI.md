# BEYOND90 — Hướng dẫn bắt đầu (Tiếng Việt)

Đây là **bộ tài liệu hướng dẫn kỹ thuật + machine-readable contracts**, chưa phải source code của ứng dụng chạy được.

## Mục tiêu
Một sản phẩm **3D football intelligence** với Java 21 + Spring Boot cho backend, Next.js/React Three Fiber cho frontend, Microsoft Foundry AI để giải thích tactical moments, và Multiverse simulator Java. Mọi dữ liệu chính là synthetic/fictional và dẫn xuất phải có chứng cứ.

## Cho Claude Code / Copilot agent
1. Giải nén bộ file vào folder repository mới `beyond90`.
2. Mở agent trong thư mục đó; paste **Prompt A** trong `docs/17-AI-AGENT-PROMPTS.md` để agent kiểm tra kiến trúc, version, điều kiện môi trường trước.
3. Paste **Prompt B**, agent chỉ thực hiện `P0-01` rồi chạy test thật, tiếp tục task tiếp theo theo `docs/11-DELIVERY-PLAN.md`.
4. Không đưa lệnh 'build toàn bộ dự án một lèo' khi chưa đạt các milestone; làm vậy tăng rủi ro code slop.
5. Kiểm tra Azure Foundry access càng sớm càng tốt, dù P0-16 là ngày chính thức tích hợp. Nếu chưa có subscription/model/quota thì cần xử lý trước.
6. Tối ngày 25/10 dừng bổ sung tính năng, ưu tiên deploy + kiểm thử + video.

## Phân biệt hai cam kết
- **Production-oriented**: kiến trúc, hợp đồng, bảo mật và các tiêu chí kiểm thử được thiết kế để nâng cấp lên môi trường sản xuất.
- **Production-ready**: chỉ được gọi như vậy sau khi có bằng chứng thực tế về hiệu năng, an ninh, khả năng phục hồi, vận hành và quyền sử dụng dữ liệu (theo `docs/18-PRODUCTION-READINESS-SCORECARD.md`). Không thể chỉ nhờ plan mà có trạng thái này.

## Ba tài liệu đọc đầu tiên
`AGENTS.md` (luật engineering cho coding agent), `docs/11-DELIVERY-PLAN.md` (task, kiểm thử, deadline), `docs/02-ARCHITECTURE.md` (Java backend & tích hợp frontend/AI).

## Một cảnh báo về agent framework
Microsoft Agent Framework không có SDK Java chính thức theo tài liệu hiện tại. Dự án giữ Java backend và triển khai orchestration có kiểm chứng bằng Spring AI/Java cùng Microsoft Foundry; không bịa dependency để cho đẹp pitch.

## Hạn cuối cần nhớ
Hạn submission chính thức: **27/10/2026, 11:59 PM Pacific** (**28/10, 2:59 AM Toronto**). Bản demo miễn phí cần truy cập được trong thời gian judging tới 10/11/2026. Kiểm tra registration trước hạn 20/10/2026 nếu chưa hoàn tất.
