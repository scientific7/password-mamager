document.addEventListener('DOMContentLoaded', function () {
    const copyButtons = document.querySelectorAll('[data-copy-target]');
    copyButtons.forEach(button => {
        button.addEventListener('click', async function () {
            const targetId = this.getAttribute('data-copy-target');
            const input = document.getElementById(targetId);
            if (!input) return;

            try {
                await navigator.clipboard.writeText(input.value);
                this.textContent = 'Copied!';
                setTimeout(() => (this.textContent = 'Copy'), 1500);
            } catch (e) {
                console.error('Copy failed', e);
                this.textContent = 'Error';
            }
        });
    });
});