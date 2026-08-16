# MyFlix Media3 Test

APK Android tối giản dùng Media3 để kiểm tra link video/HLS `.m3u8`.

## Build trên GitHub
1. Upload toàn bộ project lên repository.
2. Vào **Actions**.
3. Chọn **Build APK**.
4. Bấm **Run workflow** hoặc push commit mới.
5. Sau khi build xong, tải artifact `Media3-Test-debug`.

## Cấu hình
- Package: `com.myflix.tv`
- compileSdk: 35
- targetSdk: 35
- minSdk: 23
- AGP: 8.7.3
- Media3: 1.5.1
- Java: 17
- Gradle trên GitHub Actions: 8.9

Bản này cố ý dùng Media3 1.5.1 để tránh lỗi Media3 mới yêu cầu compileSdk 36.
