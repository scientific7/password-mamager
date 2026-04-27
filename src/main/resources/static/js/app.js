document.addEventListener("DOMContentLoaded", function () {
    setupThemeToggle();
    setupPasswordToggles();
    setupPasswordGenerators();
    setupPasswordStrengthMeters();
    setupCopyButtons();
    setupCapsLockWarnings();
    autoDismissAlerts();
});

function setupThemeToggle() {
    const savedTheme = localStorage.getItem("pm-theme");
    if (savedTheme) {
        document.documentElement.setAttribute("data-theme", savedTheme);
    }

    document.querySelectorAll("[data-theme-toggle]").forEach(button => {
        button.addEventListener("click", function () {
            const current = document.documentElement.getAttribute("data-theme") === "dark" ? "light" : "dark";
            document.documentElement.setAttribute("data-theme", current);
            localStorage.setItem("pm-theme", current);
        });
    });
}

function setupPasswordToggles() {
    document.querySelectorAll("[data-toggle-password]").forEach(button => {
        button.addEventListener("click", function () {
            const input = document.getElementById(button.getAttribute("data-toggle-password"));
            if (!input) {
                return;
            }

            const showing = input.type === "text";
            input.type = showing ? "password" : "text";
            button.textContent = showing ? "Show" : "Hide";
        });
    });
}

function setupPasswordGenerators() {
    document.querySelectorAll("[data-generate-password]").forEach(button => {
        button.addEventListener("click", function () {
            const target = document.getElementById(button.getAttribute("data-generate-password"));
            if (!target) {
                return;
            }

            target.value = generateStrongPassword(getGeneratorOptions(button));
            target.dispatchEvent(new Event("input", { bubbles: true }));
        });
    });
}

function setupPasswordStrengthMeters() {
    document.querySelectorAll("[data-password-strength]").forEach(input => {
        const update = () => renderPasswordStrength(input);
        input.addEventListener("input", update);
        update();
    });
}

function setupCopyButtons() {
    document.querySelectorAll("[data-copy-target], [data-copy-value]").forEach(button => {
        button.addEventListener("click", async function () {
            const explicitValue = button.getAttribute("data-copy-value");
            const targetId = button.getAttribute("data-copy-target");
            const target = targetId ? document.getElementById(targetId) : null;
            const textToCopy = explicitValue || (target ? target.value || target.textContent || "" : "");

            const success = await copyText(textToCopy);
            showCopyFeedback(button, success);
        });
    });
}

function setupCapsLockWarnings() {
    document.querySelectorAll("input[type='password']").forEach(input => {
        ["keyup", "keydown"].forEach(eventName => {
            input.addEventListener(eventName, event => {
                const container = input.closest(".mb-3, .col-md-8, .card-body") || document;
                const warning = container.querySelector("[data-caps-warning]");
                if (warning) {
                    warning.style.display = event.getModifierState && event.getModifierState("CapsLock") ? "block" : "none";
                }
            });
        });
    });
}

function getGeneratorOptions(button) {
    if (!button.hasAttribute("data-generator-options")) {
        return {
            length: 18,
            uppercase: true,
            lowercase: true,
            numbers: true,
            symbols: true
        };
    }

    return {
        length: Number(document.getElementById("length")?.value || 18),
        uppercase: Boolean(document.getElementById("includeUppercase")?.checked),
        lowercase: Boolean(document.getElementById("includeLowercase")?.checked),
        numbers: Boolean(document.getElementById("includeNumbers")?.checked),
        symbols: Boolean(document.getElementById("includeSpecialCharacters")?.checked)
    };
}

function generateStrongPassword(options) {
    const sets = [];
    if (options.uppercase) {
        sets.push("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }
    if (options.lowercase) {
        sets.push("abcdefghijklmnopqrstuvwxyz");
    }
    if (options.numbers) {
        sets.push("0123456789");
    }
    if (options.symbols) {
        sets.push("!@#$%^&*()_+-=[]{}");
    }
    if (sets.length === 0) {
        sets.push("abcdefghijklmnopqrstuvwxyz");
    }

    const length = Math.max(12, Math.min(64, options.length || 18));
    const allChars = sets.join("");
    const password = sets.map(set => randomChar(set));

    while (password.length < length) {
        password.push(randomChar(allChars));
    }

    return shuffle(password).join("");
}

function randomChar(chars) {
    const values = new Uint32Array(1);
    crypto.getRandomValues(values);
    return chars[values[0] % chars.length];
}

function shuffle(values) {
    for (let i = values.length - 1; i > 0; i--) {
        const random = new Uint32Array(1);
        crypto.getRandomValues(random);
        const j = random[0] % (i + 1);
        [values[i], values[j]] = [values[j], values[i]];
    }
    return values;
}

function renderPasswordStrength(input) {
    const meter = document.getElementById(input.getAttribute("data-password-strength"));
    const hints = document.getElementById(input.getAttribute("data-strength-hints"));
    if (!meter) {
        return;
    }

    const result = evaluatePassword(input.value);
    const bar = meter.querySelector(".password-strength-bar");
    meter.setAttribute("data-strength", result.label.toLowerCase());
    if (bar) {
        bar.style.width = `${result.score}%`;
    }
    if (hints) {
        hints.textContent = `${result.label} (${result.score}%). ${result.hints.join(" ")}`;
    }
}

function evaluatePassword(password) {
    if (!password) {
        return { score: 0, label: "Weak", hints: ["Start with 12+ characters."] };
    }

    let score = 0;
    const hints = [];

    if (password.length >= 16) {
        score += 40;
    } else if (password.length >= 12) {
        score += 30;
    } else if (password.length >= 8) {
        score += 18;
        hints.push("Make it at least 12 characters.");
    } else {
        score += 5;
        hints.push("Make it much longer.");
    }

    score += /[a-z]/.test(password) ? 12 : 0;
    score += /[A-Z]/.test(password) ? 12 : 0;
    score += /\d/.test(password) ? 12 : 0;
    score += /[^A-Za-z0-9]/.test(password) ? 16 : 0;

    if (!/[A-Z]/.test(password)) {
        hints.push("Add uppercase.");
    }
    if (!/\d/.test(password)) {
        hints.push("Add a number.");
    }
    if (!/[^A-Za-z0-9]/.test(password)) {
        hints.push("Add a symbol.");
    }
    if (/(.)\1\1/.test(password)) {
        score -= 15;
        hints.push("Avoid repeated characters.");
    }
    if (/password|admin|qwerty|123456|welcome/i.test(password)) {
        score -= 25;
        hints.push("Avoid common words.");
    }

    score = Math.max(0, Math.min(100, score));
    const label = score >= 75 ? "Strong" : score >= 50 ? "Medium" : "Weak";
    if (hints.length === 0) {
        hints.push("Looks healthy.");
    }
    return { score, label, hints };
}

async function copyText(text) {
    if (!text) {
        return false;
    }

    if (navigator.clipboard && window.isSecureContext) {
        try {
            await navigator.clipboard.writeText(text);
            return true;
        } catch (error) {
            console.error("Clipboard API failed:", error);
        }
    }

    return fallbackCopyText(text);
}

function fallbackCopyText(text) {
    const textArea = document.createElement("textarea");
    textArea.value = text;
    textArea.style.position = "fixed";
    textArea.style.left = "-9999px";
    textArea.style.top = "0";
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    let success = false;
    try {
        success = document.execCommand("copy");
    } catch (error) {
        console.error("Fallback copy failed:", error);
    }

    document.body.removeChild(textArea);
    return success;
}

function showCopyFeedback(button, success) {
    const originalText = button.dataset.originalText || button.textContent;
    if (!button.dataset.originalText) {
        button.dataset.originalText = originalText;
    }

    button.textContent = success ? "Copied" : "Copy failed";
    showToast(success ? "Copied to clipboard" : "Copy failed");

    setTimeout(() => {
        button.textContent = originalText;
    }, 1400);
}

function showToast(message) {
    let toast = document.querySelector("[data-toast-copy]");
    if (!toast) {
        toast = document.createElement("div");
        toast.className = "toast-copy";
        toast.setAttribute("data-toast-copy", "");
        document.body.appendChild(toast);
    }
    toast.textContent = message;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 1600);
}

function autoDismissAlerts() {
    document.querySelectorAll(".alert-dismissible").forEach(alert => {
        setTimeout(() => {
            if (typeof bootstrap !== "undefined") {
                bootstrap.Alert.getOrCreateInstance(alert).close();
            }
        }, 3500);
    });
}
