# MyFlix Android TV V3 Native Player
- Media3/ExoPlayer phát link_m3u8 native.
- Remote kiểu TV: D-pad chọn mục gần nhất, animation focus rất ngắn.
- Poster focus phóng lớn + viền trắng/đỏ.
- Trong player: OK play/pause, trái/phải tua 10 giây, Back thoát.
- Hết tập tự gọi tập tiếp.
- Nếu tập không có link_m3u8 thì fallback player web.
- GitHub Actions có sẵn: push repo > Actions > tải APK artifact.


## Netflix Focus UI
- Bỏ viền trắng/đỏ hình vuông quanh mọi mục.
- Poster focus: phóng to + bóng đổ + vạch đỏ nhỏ phía dưới.
- Menu chữ: chỉ sáng lên, không có khung.
- Nút thường: nền trắng khi focus.
- Tập phim: nền đỏ khi focus.
- Ô tìm kiếm: viền sáng nhẹ.
- D-pad vẫn dùng thuật toán chọn phần tử gần nhất để giảm lag.


## V3.1 Crash Fix
- PlayerActivity được bọc try/catch.
- Media3 có onPlayerError, không còn văng app khi HLS lỗi.
- URL không hợp lệ sẽ quay về player web.
- Native player cancel/crash logic sẽ fallback về player web.
- Giữ nguyên Netflix-style focus.


## V3.3 Native click hook
- Không còn phụ thuộc vào việc ghi đè playEpisode().
- Bắt click nút tập ở capture phase trước player HTML5.
- Tìm tập tương ứng trong currentEpisodes và gửi link_m3u8 trực tiếp sang Media3.
- Chỉ fallback player web khi không có HLS hoặc native player báo lỗi.
- Giữ Netflix Focus và crash protection.


## V3.4 Direct API native player
- APK không còn lấy link_m3u8 từ currentEpisodes trong WebView.
- Khi bấm tập, WebView chỉ gửi slug phim + số tập sang Android.
- Android gọi trực tiếp `https://myflix-9n6o.onrender.com/api/movie/{slug}`.
- Android tự tìm `link_m3u8` rồi mở Media3.
- Nếu API không có HLS hoặc lỗi thì mới fallback player web.


## V3.5 Hook playEpisode
- Hook trực tiếp hàm `playEpisode(si, ei)` của MyFlix.
- Bắt được cả Xem phim -> playFirst, chọn tập, Tập trước/Tập tiếp.
- Web chỉ gửi slug + số tập sang Android.
- Android tự gọi `/api/movie/{slug}` để lấy `link_m3u8`.
- Native lỗi mới fallback về player HTML5.
