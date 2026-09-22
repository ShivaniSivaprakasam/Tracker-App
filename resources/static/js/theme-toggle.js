// Apply saved theme immediately, before paint, to avoid a flash of the
// wrong theme.
(function () {
    var saved = localStorage.getItem('theme') || 'dark';
    document.documentElement.setAttribute('data-theme', saved);
})();

// Event delegation on document — works regardless of when the
// .theme-toggle button appears in the DOM.
document.addEventListener('click', function (event) {
    var btn = event.target.closest('.theme-toggle');
    if (!btn) return;

    var current = document.documentElement.getAttribute('data-theme');
    var next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('theme', next);
    refreshAllToggleLabels();
});

document.addEventListener('DOMContentLoaded', refreshAllToggleLabels);

function refreshAllToggleLabels() {
    var current = document.documentElement.getAttribute('data-theme');
    document.querySelectorAll('.theme-toggle').forEach(function (btn) {
        btn.innerHTML = current === 'dark'
            ? '<i class="bi bi-sun"></i> Light Mode'
            : '<i class="bi bi-moon-stars"></i> Dark Mode';
    });
}