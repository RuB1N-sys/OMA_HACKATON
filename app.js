/**
 * OMA ACADEMY — Клиентский движок платформы
 * Архитектура: Интеграция с Spring Boot REST API + интеллектуальный fallback
 */

(function () {
  'use strict';

  var C = window.OMA_CONFIG || {};
  var API_URL = (C.API_URL || 'http://localhost:8080').replace(/\/+$/, '');

  // Хранилище сессии
  var TOKEN_KEY = 'oma_token';
  var USER_KEY = 'oma_user';
  var ONBOARDING_KEY = 'oma_onboarding_done';

  function getToken() {
    try { return localStorage.getItem(TOKEN_KEY); } catch (e) { return null; }
  }

  function getUser() {
    try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch (e) { return null; }
  }

  // ==========================================================================
  // ВСТРОЕННАЯ БАЗА ДАННЫХ ДЛЯ ОФФЛАЙН/ДЕМО РЕЖИМА
  // ==========================================================================
  var MOCK_DB = {
    projects: [
      {
        id: 1,
        title: 'Стильная настенная полка в стиле Лофт своими руками',
        description: 'Пошаговый мастер-класс по сборке и тонированию деревянной полки на металлическом каркасе. Проект идеально подходит для семейного досуга и начинающих мастеров.',
        audience: 'teen',
        category: 'masterclass',
        difficulty: 'Легко',
        durationMin: 45,
        pointsReward: 100,
        price: 49.90,
        imageUrl: 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80',
        stepsCount: 3,
        steps: [
          {
            id: 1,
            stepNumber: 1,
            title: 'Подготовка и шлифовка древесины сосны',
            description: 'Шлифуем деревянные щиты наждачной бумагой P120 вдоль волокон для удаления ворса и выравнивания кромок. Затем финишируем бумагой P240 для идеальной шелковистости перед нанесением масла.',
            videoUrl: 'https://www.youtube.com/embed/5NwtC4qEw38',
            sortOrder: 1,
            materials: [
              { id: 1, name: 'Мебельный щит сосна 800x200x18мм', sku: 'OMA-108234', quantity: 2, unit: 'шт', priceRetail: 18.50, priceWholesale: 14.80, omaProductUrl: 'https://oma.by/product/108234' },
              { id: 2, name: 'Шлифовальная бумага рулон P180 5м', sku: 'OMA-339481', quantity: 1, unit: 'шт', priceRetail: 6.20, priceWholesale: 4.90, omaProductUrl: 'https://oma.by/product/339481' }
            ],
            tools: [
              { id: 1, name: 'Шлифмашина эксцентриковая Bosch PEX 220', sku: 'OMA-TOOL-001', omaProductUrl: 'https://oma.by/product/tool-001' }
            ]
          },
          {
            id: 2,
            stepNumber: 2,
            title: 'Тонирование и защита декоративным маслом V33',
            description: 'Тщательно перемешайте масло. Наносите плоской кистью вдоль волокон равномерным слоем. Через 15 минут удалите излишки безворсовой салфеткой. Нанесите второй защитный слой.',
            videoUrl: 'https://www.youtube.com/embed/PzbG3W7_5wY',
            sortOrder: 2,
            materials: [
              { id: 3, name: 'Масло для дерева V33 Белый Дуб 0.5л', sku: 'OMA-449102', quantity: 1, unit: 'шт', priceRetail: 25.00, priceWholesale: 20.00, omaProductUrl: 'https://oma.by/product/449102' }
            ],
            tools: [
              { id: 2, name: 'Кисть плоская натуральная щетина 50мм', sku: 'OMA-TOOL-002', omaProductUrl: 'https://oma.by/product/tool-002' }
            ]
          },
          {
            id: 3,
            stepNumber: 3,
            title: 'Сборка каркаса и скрытый монтаж на стену',
            description: 'Разметьте отверстия на нижней части полок с помощью угольника. Зафиксируйте металлические кронштейны саморезами 4x16. Закрепите готовую полку на стене дюбелями Fischer.',
            videoUrl: 'https://www.youtube.com/embed/tB5W4lI4wWk',
            sortOrder: 3,
            materials: [
              { id: 4, name: 'Кронштейн лофт металлический черный 200мм', sku: 'OMA-778192', quantity: 2, unit: 'шт', priceRetail: 12.00, priceWholesale: 9.60, omaProductUrl: 'https://oma.by/product/778192' },
              { id: 5, name: 'Комплект саморезов и дюбелей Fischer 6x40', sku: 'OMA-551029', quantity: 1, unit: 'уп', priceRetail: 7.50, priceWholesale: 6.00, omaProductUrl: 'https://oma.by/product/551029' }
            ],
            tools: [
              { id: 3, name: 'Дрель-шуруповерт аккумуляторный Makita 12V', sku: 'OMA-TOOL-003', omaProductUrl: 'https://oma.by/product/tool-003' }
            ]
          }
        ]
      },
      {
        id: 2,
        title: 'Кашпо для домашних растений из бетона и дерева',
        description: 'Увлекательный DIY-проект для дома: создание фактурного кашпо из строительного цемента с деревянной подставкой.',
        audience: 'teen',
        category: 'masterclass',
        difficulty: 'Легко',
        durationMin: 60,
        pointsReward: 120,
        price: 35.00,
        imageUrl: 'https://images.unsplash.com/photo-1485955900006-10f4d324d411?auto=format&fit=crop&w=800&q=80',
        stepsCount: 2,
        steps: []
      },
      {
        id: 3,
        title: 'Базовый электромонтаж квартиры: от щитка до розеток',
        description: 'Фундаментальный образовательный курс для начинающих мастеров и студентов профильных колледжей на базе кабельной продукции и автоматики ОМА.',
        audience: 'student',
        category: 'course',
        difficulty: 'Средне',
        durationMin: 180,
        pointsReward: 300,
        price: 150.00,
        imageUrl: 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&w=800&q=80',
        stepsCount: 4,
        steps: []
      },
      {
        id: 4,
        title: 'Современный дизайн интерьера: раскладка плитки и зонирование',
        description: 'Практический курс по укладке керамогранита, подбору текстур и колористике отделочных материалов из ассортимента гипермаркетов ОМА.',
        audience: 'student',
        category: 'course',
        difficulty: 'Средне',
        durationMin: 240,
        pointsReward: 400,
        price: 199.00,
        imageUrl: 'https://images.unsplash.com/photo-1502005229762-ee1b2da97a06?auto=format&fit=crop&w=800&q=80',
        stepsCount: 3,
        steps: []
      },
      {
        id: 5,
        title: 'Монтаж крупноформатного керамогранита с Ceresit & Litokol',
        description: 'Профессиональная сертификация мастеров-плиточников совместно с брендом Ceresit. Работа с эластичными смесями CM 17 и системами выравнивания СВП.',
        audience: 'pro',
        category: 'b2b',
        difficulty: 'Сложно',
        durationMin: 360,
        pointsReward: 600,
        price: 350.00,
        imageUrl: 'https://images.unsplash.com/photo-1581094794329-c8112a89af12?auto=format&fit=crop&w=800&q=80',
        stepsCount: 3,
        steps: [
          {
            id: 4,
            stepNumber: 1,
            title: 'Подготовка основания и обеспыливание Ceresit CT 17',
            description: 'Контроль прочности и влажности стяжки прибором CM. Нанесение грунтовки глубокого проникновения в два прохода кистью-макловицей.',
            videoUrl: 'https://www.youtube.com/embed/G1l2W5Qj3vQ',
            sortOrder: 1,
            materials: [
              { id: 6, name: 'Грунтовка Ceresit CT 17 10л', sku: 'OMA-CER-01', quantity: 2, unit: 'шт', priceRetail: 32.00, priceWholesale: 24.00, omaProductUrl: 'https://oma.by/product/cer-01' }
            ],
            tools: []
          },
          {
            id: 5,
            stepNumber: 2,
            title: 'Приготовление и нанесение эластичного клея Ceresit CM 17',
            description: 'Двухсторонний метод нанесения клеевой смеси для плит размером 120х60 и 120х120см гребенчатым шпателем 10мм.',
            videoUrl: 'https://www.youtube.com/embed/N6jU18Y_EGE',
            sortOrder: 2,
            materials: [
              { id: 7, name: 'Клей высокоэластичный Ceresit CM 17 25кг', sku: 'OMA-CER-02', quantity: 6, unit: 'меш', priceRetail: 45.00, priceWholesale: 33.50, omaProductUrl: 'https://oma.by/product/cer-02' }
            ],
            tools: []
          },
          {
            id: 6,
            stepNumber: 3,
            title: 'Монтаж СВП и виброукладка плиты',
            description: 'Установка зажимов СВП с зазором 1.5мм, притягивание клиньями специальными клещами и удаление пустот аккумуляторной присоской.',
            videoUrl: 'https://www.youtube.com/embed/3z5e5c7s9dI',
            sortOrder: 3,
            materials: [
              { id: 8, name: 'Система выравнивания плитки DLS зажим 1.5мм (500 шт)', sku: 'OMA-SVP-01', quantity: 1, unit: 'уп', priceRetail: 38.00, priceWholesale: 29.00, omaProductUrl: 'https://oma.by/product/svp-01' }
            ],
            tools: []
          }
        ]
      },
      {
        id: 6,
        title: 'Энергоэффективные системы фасадов с Knauf & Ceresit',
        description: 'Обучение монтажных бригад стандартам утепления минеральной ватой и тонкослойным штукатуркам с выдачей сертификата бригады.',
        audience: 'team',
        category: 'b2b',
        difficulty: 'Сложно',
        durationMin: 480,
        pointsReward: 800,
        price: 500.00,
        imageUrl: 'https://images.unsplash.com/photo-1504307651254-35680f356dfd?auto=format&fit=crop&w=800&q=80',
        stepsCount: 4,
        steps: []
      }
    ],
    gallery: [
      {
        id: 1,
        title: 'Укладка крупноформатного керамогранита в санузле',
        userName: 'Алексей Смирнов (Прораб)',
        projectTitle: 'Монтаж керамогранита с Ceresit & Litokol',
        beforeUrl: 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=600&q=80',
        afterUrl: 'https://images.unsplash.com/photo-1552321554-5fefe8c9ef14?auto=format&fit=crop&w=600&q=80',
        description: 'Использовали клей Ceresit CM 17 и СВП 1.5мм из ОМА. Геометрия швов идеальная, заказчик в восторге!',
        publishedAt: '2026-10-25'
      },
      {
        id: 2,
        title: 'Зонирование прихожей: деревянная полка Лофт',
        userName: 'Елена Кузнецова',
        projectTitle: 'Стильная настенная полка в стиле Лофт',
        beforeUrl: 'https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=600&q=80',
        afterUrl: 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=600&q=80',
        description: 'Собрали с мужем за вечер! Все материалы заказали в 1 клик через BOM-корзину ОМА с доставкой.',
        publishedAt: '2026-10-26'
      }
    ],
    achievements: [
      { id: 1, code: 'first_project', name: 'Первый шаг', description: 'Успешно завершен первый проект в Академии', iconUrl: '🏆', earned: true },
      { id: 2, code: 'five_projects', name: 'Мастер на все руки', description: 'Завершено 5 обучающих программ ОМА', iconUrl: '⭐', earned: false },
      { id: 3, code: 'certified', name: 'Сертифицированный специалист', description: 'Получен официальный диплом ОМА', iconUrl: '🎓', earned: true },
      { id: 4, code: 'b2b_pro', name: 'B2B Эксперт', description: 'Пройден специализированный курс повышения квалификации', iconUrl: '💼', earned: false },
      { id: 5, code: 'social_star', name: 'Вдохновитель сообщества', description: 'Опубликован проект в галерее До/После', iconUrl: '📸', earned: true }
    ],
    companies: [
      {
        id: 1,
        name: 'ООО «СтройМастер Минск»',
        ownerName: 'Дмитрий Ковалев (Бригадир)',
        membersCount: 4,
        members: [
          { id: 1, userId: 1, userName: 'Дмитрий Ковалев', userEmail: 'kovalev@stroymaster.by', role: 'owner' },
          { id: 2, userId: 2, userName: 'Иван Петров', userEmail: 'petrov@stroymaster.by', role: 'member' },
          { id: 3, userId: 3, userName: 'Сергей Морозов', userEmail: 'morozov@stroymaster.by', role: 'member' }
        ],
        assignments: [
          { id: 1, userName: 'Иван Петров', projectTitle: 'Монтаж крупноформатного керамогранита с Ceresit', status: 'completed', completedSteps: 3, totalSteps: 3, deadline: '2026-11-15' },
          { id: 2, userName: 'Сергей Морозов', projectTitle: 'Энергоэффективные фасады Knauf', status: 'in_progress', completedSteps: 2, totalSteps: 4, deadline: '2026-11-30' }
        ]
      }
    ]
  };

  // ==========================================================================
  // API КЛИЕНТ
  // ==========================================================================
  async function apiRequest(endpoint, options) {
    options = options || {};
    var headers = { 'Content-Type': 'application/json' };
    var token = getToken();
    if (token) headers.Authorization = 'Bearer ' + token;

    try {
      var res = await fetch(API_URL + endpoint, {
        method: options.method || 'GET',
        headers: headers,
        body: options.body ? JSON.stringify(options.body) : undefined
      });
      if (!res.ok) {
        var errJson = await res.json().catch(function () { return {}; });
        throw new Error(errJson.message || 'Ошибка сервера (' + res.status + ')');
      }
      return await res.json();
    } catch (err) {
      // При недоступности бэкенда переключаемся на mock
      return null;
    }
  }

  // ==========================================================================
  // СЕРВИСЫ ДАННЫХ С FALLBACK
  // ==========================================================================
  var AppData = {
    // Получение списка проектов
    getProjects: async function (audience, category) {
      var query = [];
      if (audience && audience !== 'all') query.push('audience=' + encodeURIComponent(audience));
      if (category && category !== 'all') query.push('category=' + encodeURIComponent(category));
      var qs = query.length ? '?' + query.join('&') : '';

      var data = await apiRequest('/api/projects' + qs);
      if (data && Array.isArray(data) && data.length) return data;

      // Fallback к mock
      return MOCK_DB.projects.filter(function (p) {
        if (audience && audience !== 'all' && p.audience !== audience) return false;
        if (category && category !== 'all' && p.category !== category) return false;
        return true;
      });
    },

    // Получение одного проекта
    getProject: async function (id) {
      var data = await apiRequest('/api/projects/' + id);
      if (data) return data;

      var p = MOCK_DB.projects.find(function (x) { return x.id == id; }) || MOCK_DB.projects[0];
      return p;
    },

    // BOM-смета на 1 клик
    getBom: async function (projectId, forceWholesale) {
      var qs = '?projectId=' + projectId + (forceWholesale ? '&wholesale=true' : '');
      var data = await apiRequest('/api/bom' + qs);
      if (data) return data;

      // Fallback
      var p = await this.getProject(projectId);
      var items = [];
      var total = 0;
      var savings = 0;
      var cartParts = [];

      (p.steps || []).forEach(function (s) {
        (s.materials || []).forEach(function (m) {
          var price = forceWholesale ? (m.priceWholesale || m.priceRetail) : m.priceRetail;
          var subtotal = price * m.quantity;
          var retSub = m.priceRetail * m.quantity;
          total += subtotal;
          savings += (retSub - subtotal);
          items.push({
            name: m.name,
            sku: m.sku,
            quantity: m.quantity,
            unit: m.unit,
            unitPrice: price,
            totalPrice: subtotal,
            priceType: forceWholesale ? 'оптовая' : 'розничная',
            omaProductUrl: m.omaProductUrl
          });
          cartParts.push(m.sku + ':' + m.quantity);
        });
      });

      return {
        projectId: projectId,
        projectTitle: p.title,
        items: items,
        totalPrice: total.toFixed(2),
        totalSavings: savings.toFixed(2),
        wholesaleDiscountApplied: !!forceWholesale,
        cartUrl: 'https://oma.by/cart?items=' + encodeURIComponent(cartParts.join(','))
      };
    },

    // Завершение шага
    completeStep: async function (projectId, stepId) {
      var res = await apiRequest('/api/enrollments/' + projectId + '/steps/' + stepId + '/complete', { method: 'POST' });
      if (res) return res;

      // Mock
      var enrolled = JSON.parse(localStorage.getItem('oma_completed_steps_' + projectId) || '[]');
      if (!enrolled.includes(stepId)) enrolled.push(stepId);
      localStorage.setItem('oma_completed_steps_' + projectId, JSON.stringify(enrolled));
      return { status: 'in_progress', completedSteps: enrolled.length };
    },

    // Баллы и лояльность
    getPoints: async function () {
      var data = await apiRequest('/api/points');
      if (data) return data;

      var savedCard = localStorage.getItem('oma_card') || '7789-4412-9901';
      var savedPts = parseInt(localStorage.getItem('oma_points') || '450', 10);
      return {
        totalPoints: savedPts,
        omaBonusCard: savedCard,
        transactions: [
          { id: 1, amount: 100, reason: 'Завершение мастер-класса: Полка Лофт', createdAt: '2026-10-26' },
          { id: 2, amount: 50, reason: 'Бонус за привязку дисконтной карты ОМА', createdAt: '2026-10-24' },
          { id: 3, amount: 300, reason: 'Успешная сертификация по электромонтажу', createdAt: '2026-10-20' }
        ]
      };
    },

    // Привязка карты ОМА
    syncBonusCard: async function (cardNumber) {
      var res = await apiRequest('/api/points/sync-card', { method: 'POST', body: { omaBonusCard: cardNumber } });
      if (res) return res;

      localStorage.setItem('oma_card', cardNumber);
      var pts = parseInt(localStorage.getItem('oma_points') || '400', 10) + 50;
      localStorage.setItem('oma_points', pts.toString());
      return { totalPoints: pts, omaBonusCard: cardNumber };
    },

    // Галерея До/После
    getGallery: async function () {
      var data = await apiRequest('/api/gallery');
      if (data && Array.isArray(data) && data.length) return data;
      return MOCK_DB.gallery;
    },

    // Ачивки
    getAchievements: async function () {
      var data = await apiRequest('/api/achievements');
      if (data && Array.isArray(data) && data.length) return data;
      return MOCK_DB.achievements;
    },

    // B2B данные
    getCompanies: async function () {
      var data = await apiRequest('/api/b2b/companies');
      if (data && Array.isArray(data) && data.length) return data;
      return MOCK_DB.companies;
    }
  };

  // ==========================================================================
  // ГЛОБАЛЬНЫЙ UI И МОДАЛЬНЫЕ ОКНА
  // ==========================================================================
  var UI = {
    // Инициализация общих элементов шапки
    initHeader: function () {
      var user = getUser();
      var authContainer = document.getElementById('header-auth-slot');
      var pointsContainer = document.getElementById('header-points-slot');

      if (pointsContainer) {
        AppData.getPoints().then(function (pts) {
          pointsContainer.innerHTML = '<span class="points-pill" title="Бонусные баллы ОМА (1 балл = 1 руб скидки)" onclick="location.href=\'cabinet.html?tab=points\'">⭐ <b>' + (pts.totalPoints || 0) + '</b> баллов</span>';
        });
      }

      if (authContainer) {
        if (user) {
          authContainer.innerHTML =
            '<div class="user-profile-badge" onclick="location.href=\'cabinet.html\'">' +
              '<div class="avatar-circle">' + (user.name ? user.name[0].toUpperCase() : 'U') + '</div>' +
              '<div class="user-info-text">' +
                '<div class="user-name">' + (user.name || 'Пользователь') + '</div>' +
                '<div class="user-role-label">' + (user.audience === 'pro' || user.audience === 'team' ? 'B2B Бригада' : 'Студент') + '</div>' +
              '</div>' +
            '</div>';
        } else {
          authContainer.innerHTML =
            '<button class="btn-header-auth" id="open-auth-btn">' +
              '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>' +
              'Войти / Регистрация' +
            '</button>';
          var btn = document.getElementById('open-auth-btn');
          if (btn && window.OmaAuth) {
            btn.onclick = function () { window.OmaAuth.open('login'); };
          }
        }
      }
    },

    // Модальное окно покупки BOM в 1 клик
    openBomModal: async function (projectId, forceWholesale) {
      var overlay = document.getElementById('bom-modal-overlay');
      if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'bom-modal-overlay';
        overlay.className = 'modal-overlay';
        document.body.appendChild(overlay);
      }

      overlay.innerHTML =
        '<div class="modal-card">' +
          '<button class="modal-close-btn" onclick="document.getElementById(\'bom-modal-overlay\').classList.remove(\'open\')">×</button>' +
          '<div style="display:flex;align-items:center;gap:10px;margin-bottom:12px">' +
            '<span style="background:var(--oma-orange);color:#fff;padding:4px 8px;border-radius:6px;font-size:12px;font-weight:700">BOM АДАПТЕР</span>' +
            '<h2 style="font-size:20px;font-weight:800;margin:0">Купить комплект материалов в 1 клик</h2>' +
          '</div>' +
          '<p style="color:var(--text-muted);font-size:13px;margin-bottom:20px">Все материалы для мастер-класса подобраны из ассортимента гипермаркета ОМА. Смета пересчитана с учетом вашей роли.</p>' +
          '<div style="background:#f8fafc;border:1px solid var(--border);border-radius:12px;padding:12px;margin-bottom:18px;display:flex;justify-content:space-between;align-items:center">' +
            '<span style="font-size:13px;font-weight:600">Тарифный план:</span>' +
            '<div style="display:flex;gap:8px">' +
              '<button id="bom-toggle-retail" class="btn" style="padding:6px 14px;font-size:12px;' + (!forceWholesale ? 'background:var(--oma-orange);color:#fff' : 'background:#e2e8f0;color:#0f172a') + '">B2C Розница</button>' +
              '<button id="bom-toggle-wholesale" class="btn" style="padding:6px 14px;font-size:12px;' + (forceWholesale ? 'background:var(--oma-orange);color:#fff' : 'background:#e2e8f0;color:#0f172a') + '">B2B Оптовая скидка</button>' +
            '</div>' +
          '</div>' +
          '<div id="bom-items-list" style="margin-bottom:20px">Загрузка состава сметы...</div>' +
          '<div style="border-top:2px solid var(--border);padding-top:16px;display:flex;justify-content:space-between;align-items:center;margin-bottom:24px">' +
            '<div>' +
              '<div style="font-size:12px;color:var(--text-muted)">Итоговая стоимость сметы:</div>' +
              '<div id="bom-total-price" style="font-size:26px;font-weight:800;color:var(--text-main)">...</div>' +
              '<div id="bom-savings-note" style="font-size:12px;color:var(--oma-green);font-weight:600"></div>' +
            '</div>' +
            '<a id="bom-checkout-btn" href="#" target="_blank" class="btn-primary" style="padding:12px 24px">' +
              '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path></svg>' +
              'Добавить все в корзину ОМА' +
            '</a>' +
          '</div>' +
        '</div>';

      overlay.classList.add('open');

      var bom = await AppData.getBom(projectId, forceWholesale);

      var listEl = document.getElementById('bom-items-list');
      var totalEl = document.getElementById('bom-total-price');
      var savingsEl = document.getElementById('bom-savings-note');
      var checkoutBtn = document.getElementById('bom-checkout-btn');

      if (listEl) {
        var html = '<table class="materials-table"><thead><tr><th>Материал</th><th>Артикул</th><th>Кол-во</th><th>Сумма</th></tr></thead><tbody>';
        bom.items.forEach(function (it) {
          html += '<tr>' +
            '<td><b>' + it.name + '</b></td>' +
            '<td><span class="mat-sku">' + (it.sku || 'OMA-BY') + '</span></td>' +
            '<td>' + it.quantity + ' ' + (it.unit || 'шт') + '</td>' +
            '<td><b>' + it.totalPrice.toFixed(2) + ' BYN</b></td>' +
          '</tr>';
        });
        html += '</tbody></table>';
        listEl.innerHTML = html;
      }

      if (totalEl) totalEl.textContent = bom.totalPrice + ' BYN';
      if (savingsEl && parseFloat(bom.totalSavings) > 0) {
        savingsEl.textContent = '✓ Экономия по B2B тарифу: ' + bom.totalSavings + ' BYN';
      }
      if (checkoutBtn) checkoutBtn.href = bom.cartUrl;

      // Переключатели тарифа
      var retBtn = document.getElementById('bom-toggle-retail');
      var wsBtn = document.getElementById('bom-toggle-wholesale');
      if (retBtn) retBtn.onclick = function () { UI.openBomModal(projectId, false); };
      if (wsBtn) wsBtn.onclick = function () { UI.openBomModal(projectId, true); };
    },

    // Интерактивный опрос онбординга (Яндекс Практикум стайл)
    openOnboardingModal: function () {
      var overlay = document.getElementById('onboard-modal-overlay');
      if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'onboard-modal-overlay';
        overlay.className = 'modal-overlay';
        document.body.appendChild(overlay);
      }

      overlay.innerHTML =
        '<div class="modal-card">' +
          '<button class="modal-close-btn" onclick="document.getElementById(\'onboard-modal-overlay\').classList.remove(\'open\')">×</button>' +
          '<div style="display:flex;align-items:center;gap:10px;margin-bottom:12px">' +
            '<span class="brand-badge">ПЕРСОНАЛИЗАЦИЯ</span>' +
            '<span style="font-size:12px;color:var(--text-muted);font-weight:600">Шаг 1 из 3</span>' +
          '</div>' +
          '<h2 style="font-size:22px;font-weight:800;margin-bottom:10px">Подберем идеальную программу обучения</h2>' +
          '<p style="color:var(--text-secondary);font-size:14px;margin-bottom:20px">Ответьте на 3 коротких вопроса, чтобы система адаптировала курсы и показала персональные скидки на материалы ОМА.</p>' +
          '<div style="font-weight:700;font-size:15px;margin-bottom:12px">Какая ваша главная цель?</div>' +
          '<div class="onboarding-options" style="margin-bottom:24px">' +
            '<button class="onboard-btn" data-goal="b2c"><span>🏡 Обустроить дом своими руками (DIY)</span><span>→</span></button>' +
            '<button class="onboard-btn" data-goal="student"><span>🎓 Получить профессию (электрика, дизайн, ремонт)</span><span>→</span></button>' +
            '<button class="onboard-btn" data-goal="b2b"><span>💼 Повысить квалификацию бригады и получить оптовые скидки</span><span>→</span></button>' +
          '</div>' +
          '<button class="btn-primary" style="width:100%;justify-content:center" onclick="localStorage.setItem(\'oma_onboarding_done\',\'true\');document.getElementById(\'onboard-modal-overlay\').classList.remove(\'open\');location.href=\'courses.html\'">' +
            'Применить и перейти к программам' +
          '</button>' +
        '</div>';

      overlay.classList.add('open');

      overlay.querySelectorAll('.onboard-btn').forEach(function (btn) {
        btn.onclick = function () {
          overlay.querySelectorAll('.onboard-btn').forEach(function (b) { b.classList.remove('selected'); });
          btn.classList.add('selected');
        };
      });
    }
  };

  // Экспорт в глобальное пространство
  window.OmaApp = {
    data: AppData,
    ui: UI
  };

  // Автозапуск при готовности DOM
  document.addEventListener('DOMContentLoaded', function () {
    UI.initHeader();
  });

})();
