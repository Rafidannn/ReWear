/**
 * ReWear — Utility Helpers
 */
(function () {
    'use strict';

    // SMOOTH SCROLL HELPER
    window.rwScrollTo = function (sectionId) {
        var el = document.getElementById(sectionId);
        if (el) {
            el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    };
})();
