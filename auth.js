/**
 * OMA ACADEMY — Единая система авторизации и SSO с oma.by
 */
(function () {
  'use strict';

  var C = window.OMA_CONFIG || {};
  var API_URL = (C.API_URL || 'http://localhost:8080').replace(/\/+$/, '');
  var K = 'oma_token';
  var U = 'oma_user';

  function ls(k, v) {
    try {
      if (v === undefined) return localStorage.getItem(k);
      v === null ? localStorage.removeItem(k) : localStorage.setItem(k, v);
    } catch (e) {}
    return null;
  }

  function cached() {
    try { return JSON.parse(ls(U)); } catch (e) { return null; }
  }

  var user = ls(K) ? cached() : null;
  var mode = 'login';

  async function api(path, opt) {
    opt = opt || {};
    var h = { 'Content-Type': 'application/json' };
    var t = ls(K);
    if (t) h.Authorization = 'Bearer ' + t;
    var res;
    try {
      res = await fetch(API_URL + path, {
        method: opt.method || 'GET',
        headers: h,
        body: opt.body ? JSON.stringify(opt.body) : undefined
      });
    } catch (e) {
      throw new Error('Сервер бэкенда недоступен. Используется локальный демо-режим.');
    }
    var data = null;
    try { data = await res.json(); } catch (e) {}
    if (!res.ok) {
      var err = new Error((data && data.message) || 'Ошибка запроса (' + res.status + ')');
      err.status = res.status;
      throw err;
    }
    return data;
  }

  // Локальный мок-обработчик авторизации (если бэкенд оффлайн)
  var mock = {
    list: function () {
      try { return JSON.parse(ls('oma_mock_users')) || []; } catch (e) { return []; }
    },
    out: function (u) {
      return {
        token: 'mock.jwt.' + u.id,
        user: { id: u.id, name: u.name, email: u.email, audience: u.audience }
      };
    },
    register: function (b) {
      var list = this.list();
      if (list.some(function (u) { return u.email === b.email; })) {
        throw new Error('Этот email уже зарегистрирован в системе ОМА');
      }
      var u = {
        id: list.length + 1,
        name: b.name,
        email: b.email,
        audience: b.audience || 'student',
        password: b.password
      };
      list.push(u);
      ls('oma_mock_users', JSON.stringify(list));
      return this.out(u);
    },
    login: function (b) {
      var u = this.list().find(function (x) { return x.email === b.email; });
      if (!u) {
        // Создаем демо-пользователя на лету для удобства тестирования
        u = { id: 101, name: b.email.split('@')[0], email: b.email, audience: 'pro', password: b.password };
      }
      return this.out(u);
    },
    sso: function (b) {
      var u = { id: 202, name: b.name || 'Мастер ОМА', email: b.email || 'master@oma.by', audience: 'pro' };
      return this.out(u);
    }
  };

  async function callAuth(m, body) {
    try {
      return await api('/api/auth/' + m, { method: 'POST', body: body });
    } catch (e) {
      if (mock[m]) return mock[m](body);
      throw e;
    }
  }

  function setSession(token, usr) {
    ls(K, token);
    ls(U, usr ? JSON.stringify(usr) : null);
    user = usr;
    if (window.OmaApp && window.OmaApp.ui) window.OmaApp.ui.initHeader();
  }

  function logout() {
    setSession(null, null);
    location.reload();
  }

  // Модальное окно авторизации в дизайне Практикума
  var dlg = document.createElement('div');
  dlg.id = 'auth-dialog-wrap';
  dlg.className = 'modal-overlay';
  dlg.innerHTML =
    '<div class="modal-card" style="max-width:440px">' +
      '<button class="modal-close-btn" id="close-auth-modal">×</button>' +
      '<div style="text-align:center;margin-bottom:20px">' +
        '<span class="brand-badge" style="margin-bottom:8px;display:inline-block">OMA.BY SSO</span>' +
        '<h2 id="auth-title" style="font-size:22px;font-weight:800;margin-bottom:6px">Вход в OMA ACADEMY</h2>' +
        '<p style="font-size:13px;color:var(--text-muted)">Единая авторизация с гипермаркетом oma.by и синхронизация бонусных баллов</p>' +
      '</div>' +
      '<div style="display:flex;border-bottom:1px solid var(--border);margin-bottom:20px">' +
        '<button type="button" id="tab-login" style="flex:1;padding:10px;border:none;background:none;font-weight:700;font-size:14px;cursor:pointer;border-bottom:2px solid var(--oma-orange);color:var(--oma-orange)">Вход</button>' +
        '<button type="button" id="tab-reg" style="flex:1;padding:10px;border:none;background:none;font-weight:600;font-size:14px;cursor:pointer;border-bottom:2px solid transparent;color:var(--text-muted)">Регистрация</button>' +
      '</div>' +
      '<form id="auth-form" novalidate>' +
        '<div id="field-name" style="margin-bottom:14px;display:none">' +
          '<label style="display:block;font-size:12px;font-weight:600;margin-bottom:4px">Имя и фамилия</label>' +
          '<input id="inp-name" type="text" placeholder="Александр Смирнов" style="width:100%;padding:10px 14px;border:1px solid var(--border);border-radius:8px">' +
        '</div>' +
        '<div style="margin-bottom:14px">' +
          '<label style="display:block;font-size:12px;font-weight:600;margin-bottom:4px">Электронная почта (oma.by ID)</label>' +
          '<input id="inp-email" type="email" placeholder="example@mail.by" style="width:100%;padding:10px 14px;border:1px solid var(--border);border-radius:8px" required>' +
        '</div>' +
        '<div style="margin-bottom:14px">' +
          '<label style="display:block;font-size:12px;font-weight:600;margin-bottom:4px">Пароль</label>' +
          '<input id="inp-pass" type="password" placeholder="••••••••" style="width:100%;padding:10px 14px;border:1px solid var(--border);border-radius:8px" required>' +
        '</div>' +
        '<div id="field-aud" style="margin-bottom:14px;display:none">' +
          '<label style="display:block;font-size:12px;font-weight:600;margin-bottom:4px">Категория аккаунта</label>' +
          '<select id="inp-aud" style="width:100%;padding:10px 14px;border:1px solid var(--border);border-radius:8px;background:#fff">' +
            '<option value="teen">B2C Покупатель (DIY мастер-классы)</option>' +
            '<option value="student">Студент (Специальное образование)</option>' +
            '<option value="pro">Профессионал / Мастер (B2B)</option>' +
            '<option value="team">Строительная бригада / Компания</option>' +
          '</select>' +
        '</div>' +
        '<p id="auth-err-msg" style="color:#ef4444;font-size:13px;margin-bottom:12px"></p>' +
        '<button type="submit" id="auth-submit-btn" class="btn-primary" style="width:100%;justify-content:center;margin-bottom:12px">Войти</button>' +
        '<button type="button" id="auth-sso-btn" class="btn-secondary" style="width:100%;justify-content:center;color:var(--text-main);background:#f1f5f9;border-color:var(--border)">' +
          'Войти через учетную запись oma.by' +
        '</button>' +
      '</form>' +
    '</div>';
  document.body.appendChild(dlg);

  var form = document.getElementById('auth-form');
  var tabLogin = document.getElementById('tab-login');
  var tabReg = document.getElementById('tab-reg');
  var fieldName = document.getElementById('field-name');
  var fieldAud = document.getElementById('field-aud');
  var authTitle = document.getElementById('auth-title');
  var authSubmitBtn = document.getElementById('auth-submit-btn');
  var authErrMsg = document.getElementById('auth-err-msg');
  var closeBtn = document.getElementById('close-auth-modal');
  var ssoBtn = document.getElementById('auth-sso-btn');

  function setMode(m) {
    mode = m;
    authErrMsg.textContent = '';
    if (m === 'login') {
      tabLogin.style.borderBottomColor = 'var(--oma-orange)';
      tabLogin.style.color = 'var(--oma-orange)';
      tabReg.style.borderBottomColor = 'transparent';
      tabReg.style.color = 'var(--text-muted)';
      fieldName.style.display = 'none';
      fieldAud.style.display = 'none';
      authTitle.textContent = 'Вход в OMA ACADEMY';
      authSubmitBtn.textContent = 'Войти в личный кабинет';
    } else {
      tabReg.style.borderBottomColor = 'var(--oma-orange)';
      tabReg.style.color = 'var(--oma-orange)';
      tabLogin.style.borderBottomColor = 'transparent';
      tabLogin.style.color = 'var(--text-muted)';
      fieldName.style.display = 'block';
      fieldAud.style.display = 'block';
      authTitle.textContent = 'Регистрация в Академии';
      authSubmitBtn.textContent = 'Зарегистрироваться';
    }
  }

  tabLogin.onclick = function () { setMode('login'); };
  tabReg.onclick = function () { setMode('register'); };
  closeBtn.onclick = function () { dlg.classList.remove('open'); };
  dlg.onclick = function (e) { if (e.target === dlg) dlg.classList.remove('open'); };

  ssoBtn.onclick = async function () {
    authErrMsg.textContent = '';
    try {
      var r = await callAuth('sso', {
        email: 'profi.master@oma.by',
        name: 'Виктор Строитель (oma.by SSO)',
        omaBonusCard: '7789-4412-9901',
        audience: 'pro'
      });
      setSession(r.token, r.user);
      dlg.classList.remove('open');
      location.reload();
    } catch (err) {
      authErrMsg.textContent = err.message;
    }
  };

  form.onsubmit = async function (e) {
    e.preventDefault();
    authErrMsg.textContent = '';
    var email = document.getElementById('inp-email').value.trim();
    var pass = document.getElementById('inp-pass').value;

    if (!email) { authErrMsg.textContent = 'Введите email'; return; }
    if (!pass) { authErrMsg.textContent = 'Введите пароль'; return; }

    var payload = { email: email, password: pass };
    if (mode === 'register') {
      var name = document.getElementById('inp-name').value.trim();
      var aud = document.getElementById('inp-aud').value;
      if (!name) { authErrMsg.textContent = 'Укажите ваше имя'; return; }
      payload.name = name;
      payload.audience = aud;
    }

    authSubmitBtn.disabled = true;
    try {
      var res = await callAuth(mode, payload);
      setSession(res.token, res.user);
      dlg.classList.remove('open');
      location.reload();
    } catch (err) {
      authErrMsg.textContent = err.message;
    }
    authSubmitBtn.disabled = false;
  };

  function open(m) {
    setMode(m || 'login');
    dlg.classList.add('open');
  }

  window.OmaAuth = {
    open: open,
    logout: logout,
    user: function () { return user; }
  };
})();
