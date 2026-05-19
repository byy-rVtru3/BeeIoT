# BeeIoT Admin Frontend

## Установка

```bash
npm install
```

## Dev

```bash
npm run dev
```

## Env

```
# .env.local
VITE_API_URL=http://localhost:8000/api
```

## Сборка для бэка

```bash
npm run build
# или сразу с прод-URL:
VITE_API_URL=https://api.example.com npm run build
```

Артефакт: dist/ (содержимое). Залить в директорию статики бэка (public/, static/, nginx root).

## Проверка сборки

```bash
npm run preview
```
