/**
 * ReWear — Scroll Reveal & Micro-Animations
 * Dijalankan saat halaman dimuat pertama kali
 */
(function () {
    'use strict';

    // ----------------------------------------------------------------
    // 1. SCROLL REVEAL — kartu & section muncul saat di-scroll ke
    // ----------------------------------------------------------------
    function initScrollReveal() {
        const targets = document.querySelectorAll(
            '.product-card, .category-card, .cta-banner, .hero-inner-container'
        );

        if (!targets.length) return;

        const observer = new IntersectionObserver(
            function (entries) {
                entries.forEach(function (entry) {
                    if (entry.isIntersecting) {
                        entry.target.classList.add('rw-revealed');
                        observer.unobserve(entry.target);
                    }
                });
            },
            { threshold: 0.12, rootMargin: '0px 0px -40px 0px' }
        );

        targets.forEach(function (el) {
            el.classList.add('rw-hidden');
            observer.observe(el);
        });
    }

    // ----------------------------------------------------------------
    // 2. STAGGERED ANIMATION — kartu muncul bertahap satu-satu
    // ----------------------------------------------------------------
    function initStagger() {
        const grids = document.querySelectorAll('.products-grid-container');
        grids.forEach(function (grid) {
            const cards = grid.querySelectorAll('.product-card');
            cards.forEach(function (card, i) {
                card.style.animationDelay = (i * 80) + 'ms';
            });
        });
    }

    // ----------------------------------------------------------------
    // 3. SMOOTH SCROLL HELPER (dipanggil dari Vaadin executeJs)
    // ----------------------------------------------------------------
    window.rwScrollTo = function (sectionId) {
        var el = document.getElementById(sectionId);
        if (el) {
            el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    };

    // ----------------------------------------------------------------
    // 4. CATEGORY CARD ACTIVE STATE — klik highlight
    // ----------------------------------------------------------------
    function initCategoryHighlight() {
        document.addEventListener('click', function (e) {
            var card = e.target.closest('.category-card');
            if (!card) return;
            document.querySelectorAll('.category-card').forEach(function (c) {
                c.classList.remove('rw-cat-active');
            });
            card.classList.add('rw-cat-active');
        });
    }

    // ----------------------------------------------------------------
    // 5. HERO COUNTER ANIMATION — angka muncul naik saat masuk viewport
    // ----------------------------------------------------------------
    function initCounters() {
        var counters = document.querySelectorAll('[data-counter]');
        if (!counters.length) return;

        var obs = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (!entry.isIntersecting) return;
                var el = entry.target;
                var target = parseInt(el.getAttribute('data-counter'), 10);
                var duration = 1200;
                var start = null;
                function step(timestamp) {
                    if (!start) start = timestamp;
                    var progress = Math.min((timestamp - start) / duration, 1);
                    el.textContent = Math.floor(progress * target).toLocaleString('id-ID');
                    if (progress < 1) requestAnimationFrame(step);
                }
                requestAnimationFrame(step);
                obs.unobserve(el);
            });
        }, { threshold: 0.5 });

        counters.forEach(function (el) { obs.observe(el); });
    }

    // ----------------------------------------------------------------
    // INIT — tunggu DOM siap
    // ----------------------------------------------------------------
    function init() {
        initScrollReveal();
        initStagger();
        initCategoryHighlight();
        initCounters();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        // Vaadin biasanya sudah render saat ini, delay kecil agar DOM lengkap
        setTimeout(init, 200);
    }

    // Re-init setiap Vaadin navigate (SPA navigation)
    document.addEventListener('vaadin-router-location-changed', function () {
        setTimeout(init, 300);
    });
})();
