(function () {
    if (typeof Chart === "undefined" || !window.ADMIN_DASHBOARD_DATA) {
        return;
    }

    var data = window.ADMIN_DASHBOARD_DATA;
    var labels = Array.isArray(data.labels) ? data.labels : [];
    var revenue = Array.isArray(data.revenue) ? data.revenue : [];
    var bookings = Array.isArray(data.bookings) ? data.bookings : [];
    var customers = Array.isArray(data.customers) ? data.customers : [];

    var revenueEl = document.getElementById("revenueChart");
    if (revenueEl) {
        new Chart(revenueEl, {
            type: "line",
            data: {
                labels: labels,
                datasets: [
                    {
                        label: "Doanh thu (đ)",
                        data: revenue,
                        borderColor: "#0f766e",
                        backgroundColor: "rgba(15, 118, 110, 0.1)",
                        fill: true,
                        tension: 0.35,
                        pointRadius: 3
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: "bottom"
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function (value) {
                                return value.toLocaleString("vi-VN");
                            }
                        }
                    }
                }
            }
        });
    }

    var bookingEl = document.getElementById("bookingChart");
    if (bookingEl) {
        new Chart(bookingEl, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [
                    {
                        label: "Booking",
                        data: bookings,
                        backgroundColor: "rgba(37, 99, 235, 0.75)",
                        borderRadius: 8
                    },
                    {
                        label: "Khách mới",
                        data: customers,
                        backgroundColor: "rgba(16, 185, 129, 0.75)",
                        borderRadius: 8
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: "bottom"
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });
    }
})();

