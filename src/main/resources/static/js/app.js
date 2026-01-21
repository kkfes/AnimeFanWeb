// AnimeFan JavaScript

document.addEventListener('DOMContentLoaded', function() {
    console.log('AnimeFan loaded');

    // Initialize theme
    initTheme();

    // Initialize tooltips
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(el => new bootstrap.Tooltip(el));

    // Initialize popovers
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    popoverTriggerList.forEach(el => new bootstrap.Popover(el));

    // Theme toggle event listener
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', toggleTheme);
    }

    // Keyboard shortcut for theme toggle (Ctrl+Shift+T)
    document.addEventListener('keydown', function(e) {
        if (e.ctrlKey && e.shiftKey && e.key === 'T') {
            e.preventDefault();
            toggleTheme();
        }
    });
});

// =====================================================
// Theme System
// =====================================================

function initTheme() {
    // Check for saved theme preference
    const savedTheme = localStorage.getItem('animefan-theme');

    if (savedTheme) {
        setTheme(savedTheme);
    } else {
        // Check system preference
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        setTheme(prefersDark ? 'dark' : 'light');
    }

    // Listen for system theme changes
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
        if (!localStorage.getItem('animefan-theme')) {
            setTheme(e.matches ? 'dark' : 'light');
        }
    });
}

function setTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('animefan-theme', theme);
    updateThemeIcon(theme);
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';

    // Add transition class for smooth animation
    document.body.style.transition = 'background-color 0.4s ease, color 0.4s ease';

    setTheme(newTheme);

    // Show toast notification (optional)
    showThemeToast(newTheme);
}

function updateThemeIcon(theme) {
    const themeToggle = document.getElementById('themeToggle');
    if (!themeToggle) return;

    const sunIcon = themeToggle.querySelector('.bi-sun');
    const moonIcon = themeToggle.querySelector('.bi-moon');
    const themeText = themeToggle.querySelector('.theme-text');

    if (theme === 'dark') {
        if (sunIcon) sunIcon.style.display = 'inline-block';
        if (moonIcon) moonIcon.style.display = 'none';
        if (themeText) themeText.textContent = 'Светлая';
    } else {
        if (sunIcon) sunIcon.style.display = 'none';
        if (moonIcon) moonIcon.style.display = 'inline-block';
        if (themeText) themeText.textContent = 'Тёмная';
    }
}

function showThemeToast(theme) {
    // Create toast element
    const toastHtml = `
        <div class="position-fixed bottom-0 end-0 p-3" style="z-index: 1100;">
            <div class="toast show" role="alert" style="background: var(--card-bg); border: 1px solid var(--border-color);">
                <div class="toast-body d-flex align-items-center gap-2" style="color: var(--text-primary);">
                    <i class="bi ${theme === 'dark' ? 'bi-moon-stars' : 'bi-sun'}" style="color: var(--accent);"></i>
                    ${theme === 'dark' ? 'Тёмная тема включена' : 'Светлая тема включена'}
                </div>
            </div>
        </div>
    `;

    // Remove existing toast
    const existingToast = document.querySelector('.position-fixed.bottom-0.end-0');
    if (existingToast) existingToast.remove();

    // Add new toast
    document.body.insertAdjacentHTML('beforeend', toastHtml);

    // Auto remove after 2 seconds
    setTimeout(() => {
        const toast = document.querySelector('.position-fixed.bottom-0.end-0');
        if (toast) {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }
    }, 2000);
}

// =====================================================
// API Helper
// =====================================================
const API = {
    baseUrl: '/api/v1',

    async request(endpoint, options = {}) {
        const url = this.baseUrl + endpoint;
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include'
        };

        try {
            const response = await fetch(url, { ...defaultOptions, ...options });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Request failed');
            }

            if (response.status === 204) {
                return null;
            }

            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },

    get(endpoint) {
        return this.request(endpoint);
    },

    post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    delete(endpoint) {
        return this.request(endpoint, {
            method: 'DELETE'
        });
    }
};

// Toast Notifications
function showToast(message, type = 'success') {
    const toastContainer = document.querySelector('.toast-container') || createToastContainer();

    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;

    toastContainer.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();

    toast.addEventListener('hidden.bs.toast', () => toast.remove());
}

function createToastContainer() {
    const container = document.createElement('div');
    container.className = 'toast-container position-fixed top-0 end-0 p-3';
    container.style.zIndex = '1055';
    document.body.appendChild(container);
    return container;
}

// Anime List Management
const AnimeList = {
    async add(animeId, status) {
        try {
            await API.post('/lists', { animeId, status });
            showToast('Добавлено в список!');
            return true;
        } catch (error) {
            showToast('Ошибка: ' + error.message, 'danger');
            return false;
        }
    },

    async updateStatus(relationId, status) {
        try {
            await API.put(`/lists/${relationId}`, { status });
            showToast('Статус обновлен!');
            return true;
        } catch (error) {
            showToast('Ошибка: ' + error.message, 'danger');
            return false;
        }
    },

    async toggleFavorite(animeId) {
        try {
            await API.post(`/lists/anime/${animeId}/favorite`);
            showToast('Избранное обновлено!');
            return true;
        } catch (error) {
            showToast('Ошибка: ' + error.message, 'danger');
            return false;
        }
    },

    async remove(relationId) {
        try {
            await API.delete(`/lists/${relationId}`);
            showToast('Удалено из списка!');
            return true;
        } catch (error) {
            showToast('Ошибка: ' + error.message, 'danger');
            return false;
        }
    }
};

// Reviews Management
const Reviews = {
    async create(animeId, data) {
        try {
            await API.post('/reviews', { animeId, ...data });
            showToast('Отзыв добавлен!');
            return true;
        } catch (error) {
            showToast('Ошибка: ' + error.message, 'danger');
            return false;
        }
    },

    async markHelpful(reviewId) {
        try {
            await API.post(`/reviews/${reviewId}/helpful`);
            return true;
        } catch (error) {
            console.error(error);
            return false;
        }
    },

    async markUnhelpful(reviewId) {
        try {
            await API.post(`/reviews/${reviewId}/unhelpful`);
            return true;
        } catch (error) {
            console.error(error);
            return false;
        }
    }
};

// Search functionality
function initSearch() {
    const searchInput = document.querySelector('input[name="q"]');
    if (searchInput) {
        let timeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                // Auto-submit after typing stops
                // this.form.submit();
            }, 500);
        });
    }
}

// Lazy loading images
function initLazyLoading() {
    const images = document.querySelectorAll('img[data-src]');

    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.removeAttribute('data-src');
                observer.unobserve(img);
            }
        });
    });

    images.forEach(img => imageObserver.observe(img));
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    initSearch();
    initLazyLoading();
    initAnimeListActions();
    initRatingSlider();
});

// =====================================================
// Anime List Actions (Add to list, Favorites, Reviews)
// =====================================================
function initAnimeListActions() {
    // Get CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    console.log('Initializing anime list actions...');
    console.log('CSRF:', csrfToken ? 'found' : 'NOT FOUND');

    // Helper function for fetch with auth
    function fetchWithAuth(url, options = {}) {
        const headers = {
            'Content-Type': 'application/json'
        };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }
        return fetch(url, {
            ...options,
            headers,
            credentials: 'include'
        });
    }

    // Toast notification
    function showToast(message, type = 'success') {
        const existing = document.querySelector('.toast-container-custom');
        if (existing) existing.remove();

        const container = document.createElement('div');
        container.className = 'toast-container-custom position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = '9999';
        container.innerHTML = `
            <div class="toast show align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'}" role="alert">
                <div class="d-flex">
                    <div class="toast-body">${message}</div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        `;
        document.body.appendChild(container);
        setTimeout(() => container.remove(), 3000);
    }

    // Add to list buttons
    const addToListBtns = document.querySelectorAll('.add-to-list');
    console.log('Add to list buttons found:', addToListBtns.length);

    addToListBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const animeId = this.getAttribute('data-anime-id');
            const status = this.getAttribute('data-status');
            const label = this.getAttribute('data-label') || status;

            console.log('Adding to list:', { animeId, status, label });

            fetchWithAuth('/api/v1/lists', {
                method: 'POST',
                body: JSON.stringify({ animeId: animeId, status: status })
            })
            .then(response => {
                console.log('Response:', response.status);
                if (response.ok) {
                    showToast('Добавлено: ' + label, 'success');
                    setTimeout(() => location.reload(), 500);
                } else {
                    response.text().then(text => {
                        console.error('Error:', text);
                        showToast('Ошибка: ' + response.status, 'error');
                    });
                }
            })
            .catch(err => {
                console.error('Fetch error:', err);
                showToast('Ошибка сети', 'error');
            });
        });
    });

    // Toggle favorite
    const favoriteBtns = document.querySelectorAll('.toggle-favorite');
    console.log('Favorite buttons found:', favoriteBtns.length);

    favoriteBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();

            const animeId = this.getAttribute('data-anime-id');
            console.log('Toggle favorite:', animeId);

            fetchWithAuth('/api/v1/lists/anime/' + animeId + '/favorite', {
                method: 'POST'
            })
            .then(response => {
                console.log('Favorite response:', response.status);
                if (response.ok) {
                    showToast('Избранное обновлено', 'success');
                    setTimeout(() => location.reload(), 500);
                } else {
                    showToast('Ошибка: ' + response.status, 'error');
                }
            })
            .catch(err => {
                console.error('Fetch error:', err);
                showToast('Ошибка сети', 'error');
            });
        });
    });

    // Remove from list
    const removeBtns = document.querySelectorAll('.remove-from-list');
    removeBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const animeId = this.getAttribute('data-anime-id');

            fetchWithAuth('/api/v1/lists/anime/' + animeId, {
                method: 'DELETE'
            })
            .then(response => {
                if (response.ok) {
                    showToast('Удалено из списка', 'success');
                    setTimeout(() => location.reload(), 500);
                } else {
                    showToast('Ошибка при удалении', 'error');
                }
            });
        });
    });

    // Review form
    const reviewForm = document.getElementById('reviewForm');
    if (reviewForm) {
        const animeId = reviewForm.getAttribute('data-anime-id');

        reviewForm.addEventListener('submit', function(e) {
            e.preventDefault();
            console.log('Submitting review for:', animeId);

            const formData = new FormData(this);

            fetchWithAuth('/api/v1/reviews', {
                method: 'POST',
                body: JSON.stringify({
                    animeId: animeId,
                    rating: parseInt(formData.get('rating')),
                    title: formData.get('title'),
                    text: formData.get('text'),
                    spoiler: formData.get('spoiler') === 'on'
                })
            })
            .then(response => {
                if (response.ok) {
                    showToast('Отзыв отправлен', 'success');
                    setTimeout(() => location.reload(), 500);
                } else {
                    showToast('Ошибка при отправке', 'error');
                }
            });
        });
    }

    // Helpful/Unhelpful
    document.querySelectorAll('.review-helpful').forEach(btn => {
        btn.addEventListener('click', function() {
            const reviewId = this.getAttribute('data-review-id');
            fetchWithAuth('/api/v1/reviews/' + reviewId + '/helpful', { method: 'POST' })
                .then(() => location.reload());
        });
    });

    document.querySelectorAll('.review-unhelpful').forEach(btn => {
        btn.addEventListener('click', function() {
            const reviewId = this.getAttribute('data-review-id');
            fetchWithAuth('/api/v1/reviews/' + reviewId + '/unhelpful', { method: 'POST' })
                .then(() => location.reload());
        });
    });
}

// =====================================================
// Rating Slider
// =====================================================
function initRatingSlider() {
    const ratingInput = document.getElementById('ratingInput');
    const ratingValue = document.getElementById('ratingValue');
    const ratingLabel = document.getElementById('ratingLabel');

    if (!ratingInput) return;

    const ratingLabels = {
        1: { text: 'Ужасно', color: '#dc3545', emoji: '💀' },
        2: { text: 'Очень плохо', color: '#dc3545', emoji: '😫' },
        3: { text: 'Плохо', color: '#fd7e14', emoji: '😕' },
        4: { text: 'Ниже среднего', color: '#fd7e14', emoji: '😐' },
        5: { text: 'Средне', color: '#ffc107', emoji: '😶' },
        6: { text: 'Неплохо', color: '#ffc107', emoji: '🙂' },
        7: { text: 'Хорошо', color: '#20c997', emoji: '😊' },
        8: { text: 'Отлично', color: '#20c997', emoji: '😄' },
        9: { text: 'Великолепно', color: '#28a745', emoji: '🤩' },
        10: { text: 'Шедевр', color: '#28a745', emoji: '🏆' }
    };

    function updateRating(value) {
        const rating = ratingLabels[value];
        if (ratingValue) {
            ratingValue.textContent = value;
            ratingValue.style.backgroundColor = rating.color;
        }
        if (ratingLabel) {
            ratingLabel.textContent = rating.emoji + ' ' + rating.text;
            ratingLabel.style.color = rating.color;
        }
    }

    ratingInput.addEventListener('input', function() {
        updateRating(this.value);
    });

    // Initial update
    updateRating(ratingInput.value);
    console.log('Rating slider initialized');
}

// Confirm delete actions
document.querySelectorAll('[data-confirm]').forEach(el => {
    el.addEventListener('click', function(e) {
        if (!confirm(this.dataset.confirm || 'Вы уверены?')) {
            e.preventDefault();
        }
    });
});
