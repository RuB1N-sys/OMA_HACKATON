/* Вход и регистрация. Работает с Java-бэкендом (см. backend/) или в демо-режиме без сервера. */
(function () {
  var C = window.OMA_CONFIG || {}, MOCK = !C.API_URL, K = 'oma_token', U = 'oma_user';
  var isCab = /cabinet\.html$/.test(location.pathname);
  function ls(k, v) { try { if (v === undefined) return localStorage.getItem(k); v === null ? localStorage.removeItem(k) : localStorage.setItem(k, v); } catch (e) {} return null; }
  function cached() { try { return JSON.parse(ls(U)); } catch (e) { return null; } }
  var user = ls(K) ? cached() : null, mode = 'login';

  /* ---- Коннектор к бэкенду: все запросы идут через api() ---- */
  async function api(path, opt) {
    opt = opt || {};
    var h = { 'Content-Type': 'application/json' }, t = ls(K);
    if (t) h.Authorization = 'Bearer ' + t;
    var r;
    try { r = await fetch(C.API_URL + path, { method: opt.method || 'GET', headers: h, body: opt.body ? JSON.stringify(opt.body) : undefined }); }
    catch (e) { throw new Error('Нет связи с сервером. Проверьте, что бэкенд запущен.'); }
    var d = null; try { d = await r.json(); } catch (e) {}
    if (!r.ok) { var err = new Error((d && d.message) || 'Ошибка сервера (' + r.status + ')'); err.status = r.status; throw err; }
    return d;
  }

  /* ---- Демо-режим (только для прототипа, пароли в открытом виде!) ---- */
  var mock = {
    list: function () { try { return JSON.parse(ls('oma_mock_users')) || []; } catch (e) { return []; } },
    out: function (u) { return { token: 'mock.' + u.id, user: { id: u.id, name: u.name, email: u.email, audience: u.audience } }; },
    register: function (b) {
      var l = this.list();
      if (l.some(function (u) { return u.email === b.email; })) throw new Error('Этот email уже зарегистрирован');
      var u = { id: l.length + 1, name: b.name, email: b.email, audience: b.audience, password: b.password };
      l.push(u); ls('oma_mock_users', JSON.stringify(l)); return this.out(u);
    },
    login: function (b) {
      var u = this.list().filter(function (x) { return x.email === b.email && x.password === b.password; })[0];
      if (!u) throw new Error('Неверный email или пароль');
      return this.out(u);
    }
  };
  function call(m, body) { return MOCK ? Promise.resolve().then(function () { return mock[m](body); }) : api('/api/auth/' + m, { method: 'POST', body: body }); }

  /* ---- Состояние ---- */
  function setSession(t, u) { ls(K, t); ls(U, u ? JSON.stringify(u) : null); user = u; render(); }
  function first(n) { return (n || '').split(' ')[0]; }
  function logout() { setSession(null, null); if (isCab) location.href = 'index.html'; }

  function render() {
    var a = document.querySelector('.acc a');
    if (a) {
      a.textContent = ''; var b = document.createElement('b');
      b.textContent = user ? 'Здравствуйте,' : 'Вход или';
      a.append(b, document.createTextNode(user ? first(user.name) : 'Регистрация'));
    }
    var h = document.querySelector('.hero h1');
    if (isCab && user && h) {
      h.textContent = 'Здравствуйте, ' + first(user.name);
      var p = h.nextElementSibling; if (p) p.textContent = user.email;
      if (!document.getElementById('logout')) {
        var x = document.createElement('button'); x.id = 'logout'; x.className = 'btn'; x.textContent = 'Выйти';
        x.style.marginTop = '14px'; x.onclick = logout; h.parentNode.appendChild(x);
      }
    }
  }

  /* ---- Окно ---- */
  var dlg = document.createElement('dialog'); dlg.className = 'auth'; dlg.setAttribute('aria-label', 'Вход и регистрация');
  dlg.innerHTML = '<div class="auth-tabs" role="tablist"><button type="button" role="tab" data-m="login">Вход</button><button type="button" role="tab" data-m="register">Регистрация</button></div>' +
    '<form class="auth-body" novalidate><button type="button" class="auth-x" aria-label="Закрыть">×</button><p class="auth-msg"></p>' +
    '<label data-r>Имя<input name="name" autocomplete="name" maxlength="100"></label>' +
    '<label>Email<input name="email" type="email" autocomplete="email"></label>' +
    '<label>Пароль<input name="password" type="password" minlength="8"></label>' +
    '<label data-r>Я<select name="audience"><option value="teen">Школьник</option><option value="student">Студент</option><option value="pro">Специалист</option><option value="team">Сотрудник ОМА</option></select></label>' +
    '<label class="chk" data-r><input type="checkbox" name="consent"><span>Согласен на обработку персональных данных</span></label>' +
    '<p class="auth-err" role="alert"></p><button class="btn" type="submit"></button></form>';
  document.body.appendChild(dlg);
  var form = dlg.querySelector('form'), err = dlg.querySelector('.auth-err'), msg = dlg.querySelector('.auth-msg'), sb = form.querySelector('[type=submit]');

  function setMode(m, text) {
    mode = m; err.textContent = ''; msg.textContent = text || '';
    dlg.querySelectorAll('[data-m]').forEach(function (t) { t.setAttribute('aria-selected', t.dataset.m === m); });
    form.querySelectorAll('[data-r]').forEach(function (e) { e.hidden = m !== 'register'; });
    form.password.autocomplete = m === 'register' ? 'new-password' : 'current-password';
    sb.textContent = m === 'register' ? 'Зарегистрироваться' : 'Войти';
  }
  function open(m, text) { setMode(m || 'login', text); if (!dlg.open) dlg.showModal(); }
  function check(b, consent) {
    if (mode === 'register' && b.name.trim().length < 2) return 'Введите имя';
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(b.email)) return 'Введите корректный email';
    if (b.password.length < 8) return 'Пароль должен быть не короче 8 символов';
    if (mode === 'register' && !consent) return 'Нужно согласие на обработку данных';
    return '';
  }

  dlg.querySelectorAll('[data-m]').forEach(function (t) { t.onclick = function () { setMode(t.dataset.m); }; });
  dlg.querySelector('.auth-x').onclick = function () { dlg.close(); };
  dlg.addEventListener('click', function (e) { if (e.target === dlg) dlg.close(); });
  dlg.addEventListener('close', function () { if (isCab && !user) location.href = 'index.html'; });
  form.addEventListener('submit', async function (e) {
    e.preventDefault();
    var b = { email: form.email.value.trim().toLowerCase(), password: form.password.value };
    if (mode === 'register') { b.name = form.name.value.trim(); b.audience = form.audience.value; }
    var bad = check(b, form.consent.checked); if (bad) { err.textContent = bad; return; }
    sb.disabled = true; err.textContent = '';
    try {
      var r = await call(mode, b); setSession(r.token, r.user); form.reset(); dlg.close();
    } catch (x) { err.textContent = x.message; }
    sb.disabled = false;
  });

  document.addEventListener('click', function (e) {
    var a = e.target.closest('a[href^="cabinet.html"]');
    if (a && !user) { e.preventDefault(); open('login', 'Войдите, чтобы открыть личный кабинет'); }
  });

  render();
  if (isCab && !user) open('login', 'Войдите, чтобы открыть личный кабинет');
  if (!MOCK && ls(K)) api('/api/auth/me').then(function (u) { setSession(ls(K), u); }).catch(function (e) {
    if (e.status === 401) { setSession(null, null); if (isCab) open('login', 'Сессия истекла, войдите снова'); }
  });
  window.OmaAuth = { open: open, logout: logout, user: function () { return user; } };
})();
