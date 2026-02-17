# 🐝 BeeIoT — PAC Repository

> Единый репозиторий для **PAC**: разработка мобильного приложения и сервера.

# BeeIoT

BeeIoT — это система для удаленного мониторинга состояния пчелиных ульев. Проект позволяет пчеловодам отслеживать важные показатели (температуру, уровень шума) в реальном времени, получать уведомления и анализировать состояние пчелиных семей.

## Структура проекта

Проект состоит из трех основных компонентов:
1.  **Backend** (`backend/`): Серверная часть на Go. Обрабатывает данные от датчиков, предоставляет API для мобильного приложения и управляет базой данных.
2.  **Firmware** (`firmware/`): Прошивка для микроконтроллеров, устанавливаемых в ульи. Написана на MicroPython.
3.  **Mobile App** (`frontend/Mobile/`): Мобильное приложение для Android, позволяющее пользователю просматривать данные и управлять устройствами.

---

## Запуск и настройка

### 1. Сервер (Backend)

Серверная часть использует Docker для запуска.

**Требования:**
*   Docker & Docker Compose

**Шаги для запуска:**

1.  Перейдите в директорию сборки:
    ```bash
    cd backend/build
    ```

2.  Создайте файл `.env`, задав нужные переменные окружения из docker-compose.yml

3.  Запустите сервер:
    ```bash
    docker-compose up -d
    ```

### 2. Датчики (Firmware)

Код прошивки находится в папке `firmware/`. Устройства собирают данные о температуре и уровне шума и отправляют их на сервер через MQTT.

**Настройка:**
1.  Задайте нужные параметры в `config.py`.
2.  Загрузите код из папки `firmware/` на микроконтроллер.
3.  Включите микроконтроллер

### 3. Мобильное приложение (Mobile App)

Приложение написано на Kotlin с использованием Jetpack Compose.

**Требования:**
*   JDK 11+

**Сборка и запуск:**
1.  Откройте проект `frontend/Mobile` в Android Studio.
2.  Дождитесь синхронизации Gradle.
3.  Настройте адрес API сервера в конфигурации приложения.
4.  Запустите приложение на эмуляторе или реальном устройстве.

## Функциональность

*   **Регистрация и авторизация пользователей**: Безопасный вход в систему.
*   **Мониторинг в реальном времени**: Отображение температуры и уровня шума в улье.
*   **Уведомления**: Оповещение пользователя о критических изменениях.
*   **Управление устройствами**: Добавление и настройка датчиков.









[![Status](https://img.shields.io/badge/status-in_progress-yellow)](#)
[![Figma Board](https://img.shields.io/badge/Figma-Board-0ACF83?logo=figma&logoColor=white)](https://www.figma.com/board/7augfCx7bs5eItu9bLrrKX/BeeIoT?node-id=0-1&p=f&t=EtxzAufpNuN2hxLF-0)
[![Figma Design](https://img.shields.io/badge/Figma-Design-0ACF83?logo=figma&logoColor=white)](https://www.figma.com/design/XBaquza75M7RAq5aR2GChz/BeeIoT?node-id=0-1&p=f&t=Cz75UibSZ7Ut0IbE-0)

---

## 🔗 Быстрые ссылки
- 📌 **Product Board** (задачи):  
  https://www.figma.com/board/7augfCx7bs5eItu9bLrrKX/BeeIoT?node-id=0-1&p=f&t=EtxzAufpNuN2hxLF-0
- 🎨 **UI/UX Design** (макеты экранов, компоненты):  
  https://www.figma.com/design/XBaquza75M7RAq5aR2GChz/BeeIoT?node-id=0-1&p=f&t=Cz75UibSZ7Ut0IbE-0
- 🗂️ **Gantt chart**:
  https://miro.com/app/board/uXjVJsWVmWY=/

## 📁 Документы проекта
- 🗓️ **Project schedule** (график проекта):  
  https://docs.google.com/document/d/1yGd2L4erWQtEZQJV0UCVBrdDIWhEG6kKhHBEaxwgGOo/edit?usp=sharing
- 📄 **SRS** (спецификация требований программного обеспечения):  
  https://docs.google.com/document/d/1QIodiUMq_Z5epvHOnq9zZvcVxb3OX3ylgJBSufI42yw/edit?usp=sharing
- 🎯 **Vision** (видение проекта):
  https://docs.google.com/document/d/1E6JWQt-eEOeX0CoDFyc3pHSL-0Htkao_hHzbLtlq5SY/edit?usp=sharing
- 📊 **Presentation** (презентация):
  https://docs.google.com/presentation/d/1yrYslXZLUU_pNW3oEP3yG8VLHuOIWepEDGlEcQZQyHI/edit?usp=sharing
- ⚠️ **Технические риски проекта**:  
  https://docs.google.com/document/d/1JEZ4LTdOlg_LpHXy_LAKT2Z9eAqTm-iWaqW3QQUOzyc/edit?usp=sharing
- 📄 **SAD** (Системная архитектурная документация):  
  https://docs.google.com/document/d/1KFIc90MHfPPeP4XmMzmz8R3hljP8pGWwcN34mUYic4I/edit?tab=t.0
---
