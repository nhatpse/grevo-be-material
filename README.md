

## Authentication API (`/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | Đăng nhập |
| POST | `/auth/register` | Đăng ký |
| POST | `/auth/google-login` | Đăng nhập Google |
| POST | `/auth/logout` | Đăng xuất |

## User Profile API (`/users`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users/profile` | Lấy thông tin profile |
| PUT | `/users/profile` | Cập nhật profile |
| PUT | `/users/password` | Đổi mật khẩu |
| POST | `/users/avatar` | Upload avatar |
| DELETE | `/users/avatar` | Xóa avatar |
| DELETE | `/users/account` | Xóa tài khoản |

## Enterprise API (`/enterprises`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/enterprises/me` | Lấy thông tin enterprise |
| PUT | `/enterprises/me` | Cập nhật enterprise |
| GET | `/enterprises/me/scope` | Lấy danh sách vùng hoạt động |
| POST | `/enterprises/me/scope` | Thêm vùng hoạt động |
| DELETE | `/enterprises/me/scope/{id}` | Xóa vùng hoạt động |
| GET | `/enterprises/me/collectors?status=` | Lấy collectors (PENDING/APPROVED) |
| POST | `/enterprises/me/collectors/{id}/approve` | Duyệt collector |
| POST | `/enterprises/me/collectors/{id}/reject` | Từ chối collector |

## Collector API (`/collector`)
| Method | Endpoint | Description |
|--------|----------|-------------|s
| GET | `/collector/enterprise/status` | Trạng thái enterprise của collector |
| GET | `/collector/enterprise/search?query=` | Tìm kiếm enterprise |
| POST | `/collector/enterprise/join` | Xin gia nhập enterprise |
| POST | `/collector/enterprise/leave` | Rời enterprise |
| GET | `/collector/profile` | Lấy thông tin collector |
| PUT | `/collector/profile` | Cập nhật thông tin collector |

## Admin API (`/admin`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/users` | Lấy danh sách users (paginated) |
| PUT | `/admin/users/{id}` | Cập nhật user |
| DELETE | `/admin/users/{id}` | Xóa user |
| POST | `/admin/users/{id}/reset-password` | Reset mật khẩu |
| GET | `/admin/areas` | Lấy system areas |
| POST | `/admin/areas` | Thêm area |
| DELETE | `/admin/areas/{id}` | Xóa area |
| GET | `/admin/enterprises` | Lấy tất cả enterprises |
| PUT | `/admin/enterprises/{id}/status` | Cập nhật trạng thái enterprise |
| GET | `/admin/logs` | Lấy system logs |

## Reports API (`/reports`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/reports` | Tạo báo cáo rác (multipart) |

## Location API (`/api/location`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/location/static-map` | Lấy static map |
| GET | `/api/location/geocode` | Forward geocoding |

---
**Tổng cộng: 32 endpoints**