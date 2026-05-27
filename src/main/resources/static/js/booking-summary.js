document.addEventListener('DOMContentLoaded', function () {
    const summaryBox = document.getElementById('bookingSummary');

    if (!summaryBox) {
        return;
    }

    const checkInInput = document.getElementById('checkIn');
    const checkOutInput = document.getElementById('checkOut');
    const roomQuantityInput = document.getElementById('roomQuantity');

    const summaryCheckIn = document.getElementById('summaryCheckIn');
    const summaryCheckOut = document.getElementById('summaryCheckOut');
    const summaryNights = document.getElementById('summaryNights');
    const summaryRoomQuantity = document.getElementById('summaryRoomQuantity');
    const summarySubtotal = document.getElementById('summarySubtotal');
    const summaryValidationMessage = document.getElementById('summaryValidationMessage');

    const roomPrice = Number(summaryBox.dataset.roomPrice || 0);
    const availableRooms = Number(summaryBox.dataset.availableRooms || 1);

    setMinDates(checkInInput, checkOutInput);
    syncRoomQuantityLimit(roomQuantityInput, availableRooms);

    updateBookingSummary();

    [checkInInput, checkOutInput, roomQuantityInput].forEach(function (input) {
        if (!input) {
            return;
        }

        input.addEventListener('input', updateBookingSummary);
        input.addEventListener('change', updateBookingSummary);
    });

    function updateBookingSummary() {
        const checkInValue = checkInInput ? checkInInput.value : '';
        const checkOutValue = checkOutInput ? checkOutInput.value : '';
        const roomQuantity = getRoomQuantity(roomQuantityInput);

        updateText(summaryCheckIn, checkInValue || 'Chưa chọn');
        updateText(summaryCheckOut, checkOutValue || 'Chưa chọn');
        updateText(summaryRoomQuantity, roomQuantity);

        const validation = validateDates(checkInValue, checkOutValue);

        if (!validation.valid) {
            updateText(summaryNights, '0');
            updateText(summarySubtotal, formatVnd(0));
            showMessage(validation.message, 'warning');
            return;
        }

        const nights = calculateNights(checkInValue, checkOutValue);
        const subtotal = roomPrice * nights * roomQuantity;

        updateText(summaryNights, nights);
        updateText(summarySubtotal, formatVnd(subtotal));

        if (nights > 0) {
            showMessage(
                'Tạm tính dựa trên ' + nights + ' đêm và ' + roomQuantity + ' phòng. Mã giảm giá sẽ được áp dụng khi xác nhận đặt phòng.',
                'success'
            );
        }
    }

    function validateDates(checkInValue, checkOutValue) {
        if (!checkInValue || !checkOutValue) {
            return {
                valid: false,
                message: 'Vui lòng chọn ngày nhận và ngày trả phòng để xem tổng tiền tạm tính.'
            };
        }

        const checkInDate = parseDate(checkInValue);
        const checkOutDate = parseDate(checkOutValue);

        if (!checkInDate || !checkOutDate) {
            return {
                valid: false,
                message: 'Ngày lưu trú không hợp lệ. Vui lòng kiểm tra lại.'
            };
        }

        if (checkOutDate <= checkInDate) {
            return {
                valid: false,
                message: 'Ngày trả phòng phải sau ngày nhận phòng.'
            };
        }

        return {
            valid: true,
            message: ''
        };
    }

    function calculateNights(checkInValue, checkOutValue) {
        const checkInDate = parseDate(checkInValue);
        const checkOutDate = parseDate(checkOutValue);

        if (!checkInDate || !checkOutDate || checkOutDate <= checkInDate) {
            return 0;
        }

        const millisecondsPerDay = 24 * 60 * 60 * 1000;
        return Math.round((checkOutDate - checkInDate) / millisecondsPerDay);
    }

    function parseDate(value) {
        if (!value) {
            return null;
        }

        const parts = value.split('-');

        if (parts.length !== 3) {
            return null;
        }

        const year = Number(parts[0]);
        const month = Number(parts[1]) - 1;
        const day = Number(parts[2]);

        const date = new Date(year, month, day);

        if (
            date.getFullYear() !== year ||
            date.getMonth() !== month ||
            date.getDate() !== day
        ) {
            return null;
        }

        return date;
    }

    function getRoomQuantity(input) {
        if (!input) {
            return 1;
        }

        let value = Number(input.value || 1);

        if (Number.isNaN(value) || value < 1) {
            value = 1;
        }

        if (availableRooms > 0 && value > availableRooms) {
            value = availableRooms;
            input.value = availableRooms;
        }

        return value;
    }

    function syncRoomQuantityLimit(input, max) {
        if (!input || !max || max < 1) {
            return;
        }

        input.setAttribute('max', max);

        if (!input.value || Number(input.value) < 1) {
            input.value = 1;
        }

        if (Number(input.value) > max) {
            input.value = max;
        }
    }

    function setMinDates(checkInInputEl, checkOutInputEl) {
        const today = new Date();
        const todayValue = toInputDate(today);

        if (checkInInputEl) {
            checkInInputEl.setAttribute('min', todayValue);
        }

        if (checkOutInputEl) {
            checkOutInputEl.setAttribute('min', todayValue);
        }

        if (checkInInputEl && checkOutInputEl) {
            checkInInputEl.addEventListener('change', function () {
                if (!checkInInputEl.value) {
                    checkOutInputEl.setAttribute('min', todayValue);
                    return;
                }

                const checkInDate = parseDate(checkInInputEl.value);

                if (!checkInDate) {
                    checkOutInputEl.setAttribute('min', todayValue);
                    return;
                }

                const nextDay = new Date(checkInDate);
                nextDay.setDate(nextDay.getDate() + 1);

                checkOutInputEl.setAttribute('min', toInputDate(nextDay));

                if (checkOutInputEl.value) {
                    const checkOutDate = parseDate(checkOutInputEl.value);

                    if (checkOutDate && checkOutDate <= checkInDate) {
                        checkOutInputEl.value = toInputDate(nextDay);
                    }
                }

                updateBookingSummary();
            });
        }
    }

    function toInputDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        return year + '-' + month + '-' + day;
    }

    function formatVnd(value) {
        const numberValue = Number(value || 0);

        if (Number.isNaN(numberValue)) {
            return '0 đ';
        }

        return new Intl.NumberFormat('vi-VN').format(numberValue) + ' đ';
    }

    function updateText(element, value) {
        if (!element) {
            return;
        }

        element.textContent = value;
    }

    function showMessage(message, type) {
        if (!summaryValidationMessage) {
            return;
        }

        summaryValidationMessage.textContent = message;
        summaryValidationMessage.classList.remove(
            'is-success',
            'is-warning',
            'is-error'
        );

        if (type === 'success') {
            summaryValidationMessage.classList.add('is-success');
        } else if (type === 'error') {
            summaryValidationMessage.classList.add('is-error');
        } else {
            summaryValidationMessage.classList.add('is-warning');
        }
    }
});