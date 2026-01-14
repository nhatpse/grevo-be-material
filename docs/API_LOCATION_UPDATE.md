# API Location Update - Frontend Integration Guide

## Endpoint Changed

**POST `/api/location`**

---

## Request (Không đổi)

```json
{
  "lat": 10.8231,
  "lng": 106.6297,
  "accuracy": 50.0  // optional
}
```

---

## Response (ĐÃ THAY ĐỔI)

### Trước đây:
```json
{
  "location": "Số 123, Đường ABC, Phường XYZ, Quận 1, TP.HCM, Việt Nam"
}
```

### Bây giờ:
```json
{
  "addressDetails": "Số 123, Đường ABC",
  "ward": "Phường XYZ",
  "city": "Thành phố Hồ Chí Minh"
}
```

---

## Field Mapping

| Field | Mô tả | Ví dụ |
|-------|-------|-------|
| `addressDetails` | Địa chỉ chi tiết (tên địa điểm, số nhà, đường) | `"Min Hair Salon Q9"`, `"123 Đường Nguyễn Huệ"` |
| `ward` | Phường / Xã / Thị trấn | `"Phường Bến Nghé"`, `"Xã Long Thạnh Mỹ"` |
| `city` | Tỉnh / Thành phố | `"Thành phố Hồ Chí Minh"`, `"Tỉnh Bình Dương"` |

---

## Frontend Code Update

### JavaScript/TypeScript

```javascript
// Trước
const response = await fetch('/api/location', {
  method: 'POST',
  body: JSON.stringify({ lat, lng })
});
const data = await response.json();
console.log(data.location); // "Số 123, Đường ABC, Phường XYZ..."

// Sau
const response = await fetch('/api/location', {
  method: 'POST',
  body: JSON.stringify({ lat, lng })
});
const data = await response.json();
console.log(data.addressDetails); // "Số 123, Đường ABC"
console.log(data.ward);           // "Phường XYZ"
console.log(data.city);           // "Thành phố Hồ Chí Minh"
```

### React Example

```jsx
const [location, setLocation] = useState({
  addressDetails: '',
  ward: '',
  city: ''
});

const fetchLocation = async (lat, lng) => {
  const res = await locationService.reverseGeocode(lat, lng);
  setLocation({
    addressDetails: res.addressDetails || '',
    ward: res.ward || '',
    city: res.city || ''
  });
};

// Trong form
<input value={location.addressDetails} placeholder="Address Details" />
<input value={location.ward} placeholder="Ward / Commune" />
<input value={location.city} placeholder="City / Province" />
```

---

## Lưu ý

1. **Null handling**: Các field có thể trống (`""`) nếu không có dữ liệu. Frontend nên xử lý trường hợp này.

2. **Backward compatibility**: Field `location` cũ đã bị xóa. Cần cập nhật code nếu đang dùng field này.

3. **Các endpoint khác không đổi**:
   - `GET /api/location/autocomplete`
   - `GET /api/location/details`
   - `GET /api/location/static-map`
