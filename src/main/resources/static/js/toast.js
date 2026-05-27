(function () {
    "use strict";

    const TOAST_CONFIG = {
        position: "top-end",
        showConfirmButton: false,
        timerProgressBar: true
    };

    function isValidMessage(message) {
        if (message === null || message === undefined) return false;

        const text = String(message).trim();

        return text !== "" &&
            text.toLowerCase() !== "null" &&
            text.toLowerCase() !== "undefined";
    }

    function safeGetAttribute(element, attributeName) {
        if (!element) return null;

        const value = element.getAttribute(attributeName);
        return isValidMessage(value) ? value.trim() : null;
    }

    function fireToast(options) {
        if (!options || !isValidMessage(options.text)) return;

        const icon = options.icon || "info";
        const title = options.title || "";
        const text = String(options.text).trim();
        const timer = options.timer || 3000;
        const position = options.position || TOAST_CONFIG.position;

        if (typeof Swal === "undefined" || typeof Swal.fire !== "function") {
            console.log(`[Toast ${icon}] ${title ? title + ": " : ""}${text}`);
            return;
        }

        Swal.fire({
            icon: icon,
            title: title,
            text: text,
            toast: true,
            position: position,
            showConfirmButton: TOAST_CONFIG.showConfirmButton,
            timer: timer,
            timerProgressBar: TOAST_CONFIG.timerProgressBar,
            didOpen: function (toast) {
                toast.addEventListener("mouseenter", Swal.stopTimer);
                toast.addEventListener("mouseleave", Swal.resumeTimer);
            }
        });
    }

    function showSuccessToast(message) {
        fireToast({
            icon: "success",
            title: "Thành công",
            text: message,
            timer: 3000
        });
    }

    function showErrorToast(message) {
        fireToast({
            icon: "error",
            title: "Lỗi",
            text: message,
            timer: 4000
        });
    }

    function showInfoToast(message) {
        fireToast({
            icon: "info",
            title: "Thông tin",
            text: message,
            timer: 3000
        });
    }

    function showWarningToast(message) {
        fireToast({
            icon: "warning",
            title: "Cảnh báo",
            text: message,
            timer: 3500
        });
    }

    function showCustomToast(message, type = "info") {
        const allowedTypes = ["success", "error", "info", "warning", "question"];
        const icon = allowedTypes.includes(type) ? type : "info";

        fireToast({
            icon: icon,
            title: "",
            text: message,
            timer: 3000
        });
    }

    function showToastFromBodyDataset() {
        const body = document.body;
        if (!body) return;

        const successMessage = safeGetAttribute(body, "data-success-message");
        const errorMessage = safeGetAttribute(body, "data-error-message");
        const infoMessage = safeGetAttribute(body, "data-info-message");
        const warningMessage = safeGetAttribute(body, "data-warning-message");

        if (successMessage) {
            showSuccessToast(successMessage);
        }

        if (errorMessage) {
            showErrorToast(errorMessage);
        }

        if (infoMessage) {
            showInfoToast(infoMessage);
        }

        if (warningMessage) {
            showWarningToast(warningMessage);
        }
    }

    function showToastFromAuthMarkers() {
        const loginError = document.getElementById("toastLoginError");
        const logoutSuccess = document.getElementById("toastLogoutSuccess");
        const registerSuccess = document.getElementById("toastRegisterSuccess");
        const resetSuccess = document.getElementById("toastResetSuccess");

        if (loginError) {
            showErrorToast("Tên đăng nhập hoặc mật khẩu không đúng.");
        }

        if (logoutSuccess) {
            showSuccessToast("Bạn đã đăng xuất thành công.");
        }

        if (registerSuccess) {
            showSuccessToast("Đăng ký thành công. Vui lòng đăng nhập.");
        }

        if (resetSuccess) {
            showSuccessToast("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.");
        }
    }

    function initToastMessages() {
        showToastFromAuthMarkers();
        showToastFromBodyDataset();
    }

    window.fireToast = fireToast;
    window.showSuccessToast = showSuccessToast;
    window.showErrorToast = showErrorToast;
    window.showInfoToast = showInfoToast;
    window.showWarningToast = showWarningToast;
    window.showCustomToast = showCustomToast;

    document.addEventListener("DOMContentLoaded", initToastMessages);
})();