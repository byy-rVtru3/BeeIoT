import { sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { BeeIoTAPI } from '../utils/api-client.js';
import { generateEmail, generateHiveName, DEFAULT_PASSWORD } from '../utils/test-data.js';
import {
    fullUserRegistration,
    createHive,
    listHives,
    getHive,
    deleteUser,
} from '../utils/tasks.js';


const scenarioErrors = new Counter('basic_flow_errors');

export function basicFlow() {
    const api = new BeeIoTAPI();
    const email = generateEmail('basic');
    const password = DEFAULT_PASSWORD;
    const hiveName = generateHiveName('Basic');

    const metrics = {
        registrationErrors: scenarioErrors,
        confirmationErrors: scenarioErrors,
        loginErrors: scenarioErrors,
        hiveCreateErrors: scenarioErrors,
        hiveListErrors: scenarioErrors,
        hiveGetErrors: scenarioErrors,
        userDeleteErrors: scenarioErrors,
    };

    // 1-3. Регистрация и вход
    const regResult = fullUserRegistration(api, email, password, metrics);
    if (!regResult.success) return;

    const token = regResult.token;
    sleep(1);

    // 4. Создание улья
    const createResult = createHive(api, token, hiveName, metrics.hiveCreateErrors);
    if (!createResult.success) {
        deleteUser(api, token, email, metrics.userDeleteErrors);
        return;
    }
    sleep(1);

    // 5. Список ульев
    listHives(api, token, metrics.hiveListErrors);
    sleep(1);

    // 6. Получение улья
    getHive(api, token, hiveName, metrics.hiveGetErrors);
    sleep(1);

    // 16. Удаление пользователя
    deleteUser(api, token, email, metrics.userDeleteErrors);
}