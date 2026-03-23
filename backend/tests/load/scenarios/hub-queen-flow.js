import { sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { BeeIoTAPI } from '../utils/api-client.js';
import {
    generateEmail, generateHiveName, generateQueenName, generateHubId, DEFAULT_PASSWORD
} from '../utils/test-data.js';
import {
    fullUserRegistration,
    createHive,
    getHive,
    deleteHive,
    linkHubToHive,
    linkQueenToHive,
    createHub,
    listHubs,
    getHub,
    updateHub,
    createQueen,
    listQueens,
    getQueen,
    updateQueen,
    deleteQueen,
    setHiveWeight,
    getWeightSince,
    getNoiseSince,
    getTemperatureSince,
    deleteUser,
} from '../utils/tasks.js';


const scenarioErrors = new Counter('hub_queen_flow_errors');

export function hubQueenFlow() {
    const api = new BeeIoTAPI();
    const email = generateEmail('hqf');
    const password = DEFAULT_PASSWORD;
    const hiveName = generateHiveName('HQF');
    const hubId = generateHubId('hub');
    const hubName = `HubName-${__VU}-${__ITER}`;
    const queenName = generateQueenName('HQF');
    const since = Math.floor(Date.now() / 1000) - 86400; // Unix timestamp 24h назад

    const metrics = {
        registrationErrors: scenarioErrors,
        confirmationErrors: scenarioErrors,
        loginErrors: scenarioErrors,
        hiveCreateErrors: scenarioErrors,
        hubCreateErrors: scenarioErrors,
        hubListErrors: scenarioErrors,
        hubGetErrors: scenarioErrors,
        hubUpdateErrors: scenarioErrors,
        queenCreateErrors: scenarioErrors,
        queenListErrors: scenarioErrors,
        queenGetErrors: scenarioErrors,
        queenUpdateErrors: scenarioErrors,
        queenDeleteErrors: scenarioErrors,
        linkHubErrors: scenarioErrors,
        linkQueenErrors: scenarioErrors,
        hiveGetErrors: scenarioErrors,
        weightSetErrors: scenarioErrors,
        weightGetErrors: scenarioErrors,
        noiseGetErrors: scenarioErrors,
        tempGetErrors: scenarioErrors,
        hiveDeleteErrors: scenarioErrors,
        userDeleteErrors: scenarioErrors,
    };

    // 1. Регистрация и вход
    const regResult = fullUserRegistration(api, email, password, metrics);
    if (!regResult.success) return;
    const token = regResult.token;
    sleep(1);

    // 2. Создание хаба
    const hubResult = createHub(api, token, hubId, hubName, metrics.hubCreateErrors);
    sleep(0.5);

    // 3. Создание улья
    const hiveResult = createHive(api, token, hiveName, metrics.hiveCreateErrors);
    if (!hiveResult.success) {
        deleteUser(api, token, email, metrics.userDeleteErrors);
        return;
    }
    sleep(0.5);

    // 4. Создание матки — ответ содержит рассчитанный календарь
    const queenResult = createQueen(api, token, queenName, '2026-01-01', metrics.queenCreateErrors);
    sleep(0.5);

    // 5. Привязка хаба и матки к улью
    if (hubResult.success) {
        linkHubToHive(api, token, hiveName, hubId, metrics.linkHubErrors);
    }
    if (queenResult.success) {
        linkQueenToHive(api, token, hiveName, queenName, metrics.linkQueenErrors);
    }
    sleep(0.5);

    // 6. Получение улья — проверяем что хаб и матка привязаны
    getHive(api, token, hiveName, metrics.hiveGetErrors);
    sleep(0.5);

    // 7. Список и получение хабов
    listHubs(api, token, metrics.hubListErrors);
    if (hubResult.success) {
        getHub(api, token, hubId, metrics.hubGetErrors);
        sleep(0.3);

        // 8. Обновление имени хаба
        const newHubName = `${hubName}-v2`;
        updateHub(api, token, { id: hubId, name: newHubName }, metrics.hubUpdateErrors);
        sleep(0.3);
    }

    // 9. Список и получение маток
    listQueens(api, token, metrics.queenListErrors);
    if (queenResult.success) {
        // 10. Получение матки с календарём фаз
        getQueen(api, token, queenName, metrics.queenGetErrors);
        sleep(0.3);

        // 11. Обновление матки
        const newQueenName = `${queenName}-v2`;
        updateQueen(api, token, { old_name: queenName, new_name: newQueenName }, metrics.queenUpdateErrors);
        sleep(0.3);

        // 12. Отвязка матки от улья и удаление
        linkQueenToHive(api, token, hiveName, '', metrics.linkQueenErrors);
        deleteQueen(api, token, newQueenName, metrics.queenDeleteErrors);
    }
    sleep(0.5);

    // 13. Телеметрия: запись веса
    if (hubResult.success) {
        const weightTime = new Date().toISOString();
        const weightData = { weight: 25.5, time: weightTime, hub: hubId };
        setHiveWeight(api, token, weightData, metrics.weightSetErrors);
        sleep(0.3);

        // 14. Получение данных за последние 24ч
        getWeightSince(api, token, hubId, since, metrics.weightGetErrors);
        getNoiseSince(api, token, hubId, since, metrics.noiseGetErrors);
        getTemperatureSince(api, token, hubId, since, metrics.tempGetErrors);
        sleep(0.3);
    }

    // 15. Удаление улья
    deleteHive(api, token, hiveName, metrics.hiveDeleteErrors);
    sleep(0.5);

    // 16. Удаление пользователя
    deleteUser(api, token, email, metrics.userDeleteErrors);
}
