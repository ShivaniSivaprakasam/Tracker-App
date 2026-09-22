document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('input[type="password"]').forEach(function (input) {
        const toggle = document.createElement('button');
        toggle.type = 'button';
        toggle.textContent = 'Show';
        toggle.className = 'password-toggle-btn';
        toggle.setAttribute('aria-label', 'Show password');

        input.insertAdjacentElement('afterend', toggle);

        toggle.addEventListener('click', function () {
            const isHidden = input.type === 'password';
            input.type = isHidden ? 'text' : 'password';
            toggle.textContent = isHidden ? 'Hide' : 'Show';
        });
    });
});