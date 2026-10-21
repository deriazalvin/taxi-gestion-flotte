/* =========================================================
   TaxiFleet — interactions légères (100% local, sans CDN)
   ========================================================= */
(function () {
    'use strict';

    var reduced = window.matchMedia
        && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    document.documentElement.classList.add('js');

    /* ---------------------------------------------------------
       Révélation au défilement (remplace la librairie AOS)
       --------------------------------------------------------- */
    function reveal() {
        var targets = document.querySelectorAll('[data-aos], .stagger');

        if (reduced || !('IntersectionObserver' in window)) {
            targets.forEach(function (el) { el.classList.add('aos-animate'); });
            return;
        }

        var observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (!entry.isIntersecting) return;
                entry.target.classList.add('aos-animate');
                observer.unobserve(entry.target);
            });
        }, { rootMargin: '0px 0px -60px 0px', threshold: 0.05 });

        targets.forEach(function (el) {
            // Cascade décalée pour les conteneurs .stagger
            if (el.classList.contains('stagger')) {
                Array.prototype.forEach.call(el.children, function (child, i) {
                    child.style.transitionDelay = Math.min(i * 60, 600) + 'ms';
                });
            }
            observer.observe(el);
        });
    }

    /* ---------------------------------------------------------
       Compteurs animés
       --------------------------------------------------------- */
    function countUp() {
        if (reduced) return;

        document.querySelectorAll('[data-count]').forEach(function (el) {
            var raw = el.getAttribute('data-count') || '0';
            var target = parseFloat(raw.replace(',', '.')) || 0;
            // Un attribut data-decimals fixe le nombre de décimales
            var decimals = parseInt(el.getAttribute('data-decimals') || '0', 10);
            var duration = Math.min(1400, 500 + target * 30);
            var start = null;

            function render(value) {
                el.textContent = decimals > 0
                    ? value.toLocaleString('fr-FR', {
                        minimumFractionDigits: decimals,
                        maximumFractionDigits: decimals
                    })
                    : Math.round(value).toLocaleString('fr-FR');
            }

            function step(ts) {
                if (!start) start = ts;
                var progress = Math.min(1, (ts - start) / duration);
                var eased = 1 - Math.pow(1 - progress, 3);
                render(target * eased);
                if (progress < 1) requestAnimationFrame(step);
            }

            render(0);
            requestAnimationFrame(step);
        });
    }

    /* ---------------------------------------------------------
       Barres de progression
       --------------------------------------------------------- */
    function progressBars() {
        document.querySelectorAll('.progress > span[data-target]').forEach(function (bar) {
            var target = Math.max(0, Math.min(100, parseFloat(bar.getAttribute('data-target')) || 0));
            setTimeout(function () { bar.style.width = target + '%'; }, 220);
        });
    }

    /* ---------------------------------------------------------
       Ondes au clic sur les boutons principaux
       --------------------------------------------------------- */
    function ripples() {
        document.querySelectorAll('.btn-ripple').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                var rect = btn.getBoundingClientRect();
                var size = Math.max(rect.width, rect.height);
                var wave = document.createElement('span');
                wave.className = 'ripple-wave';
                wave.style.width = wave.style.height = size + 'px';
                wave.style.left = (e.clientX - rect.left - size / 2) + 'px';
                wave.style.top = (e.clientY - rect.top - size / 2) + 'px';
                btn.appendChild(wave);
                setTimeout(function () { wave.remove(); }, 700);
            });
        });
    }

    /* ---------------------------------------------------------
       Mise en évidence de l'onglet actif
       --------------------------------------------------------- */
    function activeNav() {
        var path = window.location.pathname;
        var links = document.querySelectorAll('#main-nav a[data-nav]');

        links.forEach(function (link) {
            var target = link.getAttribute('data-nav');
            var isMatch = target === '/' ? path === '/' : path.indexOf(target) === 0;
            if (!isMatch) return;

            link.classList.add('active');
            link.setAttribute('aria-current', 'page');
        });
    }

    /* ---------------------------------------------------------
       Surlignage de la ligne de tableau pointée par l'ancre
       --------------------------------------------------------- */
    function flashRow() {
        var hash = window.location.hash;
        if (!hash) return;
        var row = document.querySelector(hash);
        if (!row) return;
        row.classList.add('row-flash');
        setTimeout(function () { row.classList.remove('row-flash'); }, 1800);
    }

    /* ---------------------------------------------------------
       Filtrage instantané des tableaux (côté client)
       --------------------------------------------------------- */
    function tableFilter() {
        document.querySelectorAll('input[data-filter-target]').forEach(function (input) {
            var table = document.querySelector(input.getAttribute('data-filter-target'));
            if (!table) return;

            input.addEventListener('input', function () {
                var needle = input.value.trim().toLowerCase();
                var rows = table.querySelectorAll('tbody tr');
                var shown = 0;

                rows.forEach(function (row) {
                    var match = !needle
                        || row.textContent.toLowerCase().indexOf(needle) !== -1;
                    row.hidden = !match;
                    if (match) shown++;
                });

                var counter = document.querySelector(input.getAttribute('data-filter-target') + '-count');
                if (counter) counter.textContent = String(shown);
            });
        });
    }

    /* ---------------------------------------------------------
       Barre de filtres : soumission manuelle
       --------------------------------------------------------- */
    function filterSubmit() {
        document.querySelectorAll('form[data-auto-submit]').forEach(function (form) {
            form.querySelectorAll('select').forEach(function (select) {
                select.addEventListener('change', function () { form.submit(); });
            });
            var applyBtn = form.querySelector('[data-apply]');
            if (applyBtn) applyBtn.addEventListener('click', function () { form.submit(); });
        });
    }

    /* ---------------------------------------------------------
       Confirmation avant action destructive
       --------------------------------------------------------- */
    function confirmDestructive() {
        document.querySelectorAll('form[data-confirm]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (!window.confirm(form.getAttribute('data-confirm'))) {
                    e.preventDefault();
                }
            });
        });
    }

    function init() {
        reveal();
        progressBars();
        ripples();
        activeNav();
        tableFilter();
        filterSubmit();
        confirmDestructive();
        flashRow();

        var login = document.querySelector('.login-card');
        if (login) {
            setTimeout(function () { login.classList.add('ready'); }, 100);
        }

        // Compteurs démarrés une fois la page en place
        setTimeout(countUp, 350);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
