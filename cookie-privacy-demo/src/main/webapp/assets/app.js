const banner = document.querySelector('#banner');
const shade = document.querySelector('#shade');
const analytics = document.querySelector('#analytics');
const status = document.querySelector('#status');
const loginForm = document.querySelector('#loginForm');
const loginStatus = document.querySelector('#loginStatus');
const logoutButton = document.querySelector('#logoutButton');

const showBanner = open => {
  banner.classList.toggle('hidden', !open);
  shade.classList.toggle('hidden', !open);
};

async function loadConsent() {
  const response = await fetch('consent', { credentials: 'same-origin' });
  const value = (await response.text()).trim();
  const chosen = value !== 'none';
  analytics.checked = value.includes('analytics%3Dtrue');
  status.textContent = chosen
    ? `Келісім сақталған. Аналитика: ${analytics.checked ? 'рұқсат етілді' : 'өшірілген'}.`
    : 'Әзірге таңдау жасалмаған.';
  showBanner(!chosen);
}

async function saveConsent(action) {
  const body = new URLSearchParams({ action, analytics: String(analytics.checked) });
  const response = await fetch('consent', {
    method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body, credentials: 'same-origin'
  });
  if (!response.ok) { alert('Таңдауды сақтау мүмкін болмады.'); return; }
  await loadConsent();
}

async function loadLogin() {
  const response = await fetch('login', { credentials: 'same-origin', cache: 'no-store' });
  const value = (await response.text()).trim();
  const signedIn = value.startsWith('authenticated:');
  loginStatus.textContent = signedIn ? `Жүйеге кірдіңіз: ${value.slice(14)}` : 'Жүйеге кірмегенсіз.';
  loginForm.classList.toggle('hidden', signedIn);
  logoutButton.classList.toggle('hidden', !signedIn);
}

loginForm.addEventListener('submit', async event => {
  event.preventDefault();
  const body = new URLSearchParams(new FormData(loginForm));
  const response = await fetch('login', {
    method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body, credentials: 'same-origin'
  });
  if (!response.ok) { loginStatus.textContent = 'Логин немесе пароль қате.'; return; }
  loginForm.reset();
  await loadLogin();
});

logoutButton.addEventListener('click', async () => {
  const response = await fetch('login', {
    method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ action: 'logout' }), credentials: 'same-origin'
  });
  if (response.ok) await loadLogin();
});

document.querySelector('#accept').addEventListener('click', () => { analytics.checked = true; saveConsent('accept'); });
document.querySelector('#reject').addEventListener('click', () => { analytics.checked = false; saveConsent('reject'); });
document.querySelector('#save').addEventListener('click', () => saveConsent('save'));
document.querySelector('#manage').addEventListener('click', () => showBanner(true));
loadConsent().catch(() => { status.textContent = 'Сервермен байланыс жоқ. Tomcat ішінде ашыңыз.'; showBanner(true); });
loadLogin().catch(() => { loginStatus.textContent = 'Кіру күйін алу мүмкін болмады.'; });

