document.addEventListener('DOMContentLoaded', function () {
    const quickViewButtons = document.querySelectorAll('.js-room-quick-view');

    const titleEl = document.getElementById('quickRoomTitle');
    const categoryEl = document.getElementById('quickRoomCategory');
    const priceEl = document.getElementById('quickRoomPrice');
    const statusEl = document.getElementById('quickRoomStatus');
    const capacityEl = document.getElementById('quickRoomCapacity');
    const bedTypeEl = document.getElementById('quickRoomBedType');
    const floorEl = document.getElementById('quickRoomFloor');
    const descriptionEl = document.getElementById('quickRoomDescription');
    const amenitiesEl = document.getElementById('quickRoomAmenities');
    const imageEl = document.getElementById('quickRoomImage');
    const fallbackEl = document.getElementById('quickRoomFallback');
    const detailLinkEl = document.getElementById('quickRoomDetailLink');
    const bookingLinkEl = document.getElementById('quickRoomBookingLink');

    if (!quickViewButtons.length) {
        return;
    }

    quickViewButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            const roomNumber = button.dataset.roomNumber || '---';
            const categoryName = button.dataset.categoryName || 'Loại phòng';
            const rawPrice = button.dataset.price || '0';
            const capacity = button.dataset.capacity || '---';
            const bedType = button.dataset.bedType || 'Đang cập nhật';
            const floor = button.dataset.floor || '---';
            const status = button.dataset.status || '';
            const description = button.dataset.description || 'Chưa có mô tả cho phòng này.';
            const amenities = button.dataset.amenities || '';
            const imageUrl = button.dataset.imageUrl || '';
            const detailUrl = button.dataset.detailUrl || '#';
            const bookingUrl = button.dataset.bookingUrl || '#';

            titleEl.textContent = 'Phòng ' + roomNumber;
            categoryEl.textContent = categoryName;
            priceEl.textContent = formatVnd(rawPrice) + ' đ';
            capacityEl.textContent = capacity;
            bedTypeEl.textContent = bedType;
            floorEl.textContent = floor;
            descriptionEl.textContent = description;

            updateStatus(statusEl, status);
            renderAmenities(amenitiesEl, amenities);
            updateImage(imageEl, fallbackEl, imageUrl, categoryName);
            updateLinks(detailLinkEl, bookingLinkEl, detailUrl, bookingUrl, status);
        });
    });
});

function formatVnd(value) {
    const numberValue = Number(value);

    if (Number.isNaN(numberValue)) {
        return value;
    }

    return new Intl.NumberFormat('vi-VN').format(numberValue);
}

function updateStatus(statusEl, status) {
    statusEl.classList.remove('status-available', 'status-maintenance', 'status-unavailable');

    if (status === 'AVAILABLE') {
        statusEl.textContent = 'Còn phòng';
        statusEl.classList.add('status-available');
        return;
    }

    if (status === 'MAINTENANCE') {
        statusEl.textContent = 'Bảo trì';
        statusEl.classList.add('status-maintenance');
        return;
    }

    statusEl.textContent = 'Không khả dụng';
    statusEl.classList.add('status-unavailable');
}

function renderAmenities(amenitiesEl, amenities) {
    amenitiesEl.innerHTML = '';

    const amenityList = amenities
        .split(',')
        .map(function (item) {
            return item.trim();
        })
        .filter(function (item) {
            return item.length > 0;
        });

    if (!amenityList.length) {
        const emptyChip = document.createElement('span');
        emptyChip.className = 'amenity-chip';
        emptyChip.textContent = 'Đang cập nhật tiện nghi';
        amenitiesEl.appendChild(emptyChip);
        return;
    }

    amenityList.forEach(function (amenity) {
        const chip = document.createElement('span');
        chip.className = 'amenity-chip';
        chip.textContent = amenity;
        amenitiesEl.appendChild(chip);
    });
}

function updateImage(imageEl, fallbackEl, imageUrl, categoryName) {
    const normalizedImageUrl = normalizeImageUrl(imageUrl);

    if (!normalizedImageUrl) {
        imageEl.classList.add('d-none');
        imageEl.removeAttribute('src');
        fallbackEl.classList.remove('d-none');
        return;
    }

    imageEl.src = normalizedImageUrl;
    imageEl.alt = 'Ảnh ' + categoryName;
    imageEl.classList.remove('d-none');
    fallbackEl.classList.add('d-none');

    imageEl.onerror = function () {
        imageEl.classList.add('d-none');
        imageEl.removeAttribute('src');
        fallbackEl.classList.remove('d-none');
    };
}

function normalizeImageUrl(imageUrl) {
    if (!imageUrl || imageUrl === 'null' || imageUrl === 'undefined') {
        return '';
    }

    if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://') || imageUrl.startsWith('/')) {
        return imageUrl;
    }

    return '/' + imageUrl;
}

function updateLinks(detailLinkEl, bookingLinkEl, detailUrl, bookingUrl, status) {
    detailLinkEl.href = detailUrl || '#';

    bookingLinkEl.classList.remove('is-disabled');

    if (status !== 'AVAILABLE') {
        bookingLinkEl.href = '#';
        bookingLinkEl.textContent = 'Không thể đặt';
        bookingLinkEl.classList.add('is-disabled');
        return;
    }

    bookingLinkEl.href = bookingUrl || '#';
    bookingLinkEl.textContent = 'Đặt phòng';
}