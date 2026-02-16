import { sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { BeeIoTAPI } from '../utils/api-client.js';
import { generateEmail, generateHiveName, DEFAULT_PASSWORD } from '../utils/test-data.js';
import {
    fullUserRegistration,
    createHive,
    listHives,
    getHive,
    updateHive,
    calculateQueen,
    changePassword,
    loginUser,
    deleteHive,
    deleteUser,
} from '../utils/tasks.js';


const scenarioErrors = new Counter('full_cycle_errors');

export function fullCycle() {
    const api = new BeeIoTAPI();
    const email = generateEmail('full');
    const password = DEFAULT_PASSWORD;
    const newPassword = "NewPassword123";
    const hiveName = generateHiveName('Full');

    const metrics = {
        registrationErrors: scenarioErrors,
        confirmationErrors: scenarioErrors,
        loginErrors: scenarioErrors,
        hiveCreateErrors: scenarioErrors,
        hiveListErrors: scenarioErrors,
        hiveGetErrors: scenarioErrors,
        hiveUpdateErrors: scenarioErrors,
        calcErrors: scenarioErrors,
        passwordChangeErrors: scenarioErrors,
        hiveDeleteErrors: scenarioErrors,
        userDeleteErrors: scenarioErrors,
    };

    // 1-3. Регистрация
    const regResult = fullUserRegistration(api, email, password, metrics);
    if (!regResult.success) return;

    let token = regResult.token;
    sleep(1);

    // 4. Создание улья
    if (!createHive(api, token, hiveName, metrics.hiveCreateErrors).success) return;
    sleep(1);

    // 5-6. Список и получение
    listHives(api, token, metrics.hiveListErrors);
    getHive(api, token, hiveName, metrics.hiveGetErrors);
    sleep(1);

    // 7. Обновление
    const updateData = { old_name: hiveName, new_name: `${hiveName}-updated` };
    if (!updateHive(api, token, updateData, metrics.hiveUpdateErrors).success) return;
    const updatedHiveName = updateData.new_name;
    sleep(1);

    // 8. Калькулятор матки
    calculateQueen(api, { start_date: "2026-02-11" }, metrics.calcErrors);
    sleep(1);

    // 11-12. Смена пароля
    if (!changePassword(api, email, newPassword, metrics).success) return;
    sleep(1);

    // 14. Логин с новым паролем
    const loginResult = loginUser(api, email, newPassword, metrics.loginErrors);
    if (!loginResult.success) return;
    token = loginResult.token;
    sleep(1);

    // 15. Удаление улья
    deleteHive(api, token, updatedHiveName, metrics.hiveDeleteErrors);
    sleep(1);

    // 16. Удаление пользователя
    deleteUser(api, token, email, metrics.userDeleteErrors);
}