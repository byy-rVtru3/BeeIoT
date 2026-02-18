import { sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { BeeIoTAPI } from '../utils/api-client.js';
import { generateEmail, generateHiveName, DEFAULT_PASSWORD } from '../utils/test-data.js';
import {
    fullUserRegistration,
    createHive,
    listHives,
    getHive,
    calculateQueen,
    deleteUser,
} from '../utils/tasks.js';

// All counters must be declared in init context
const scenarioErrors = new Counter('read_ops_errors');
const setupErrors = new Counter('setup_errors');
const teardownErrors = new Counter('teardown_errors');

export function readOperation() {
    const api = new BeeIoTAPI();
    const email = generateEmail('read');
    const password = DEFAULT_PASSWORD;
    const hiveName = generateHiveName('Read');

    const metrics = {
        registrationErrors: setupErrors,
        confirmationErrors: setupErrors,
        loginErrors: setupErrors,
        hiveCreateErrors: setupErrors,

        hiveListErrors: scenarioErrors,
        hiveGetErrors: scenarioErrors,
        calcErrors: scenarioErrors,
        userDeleteErrors: teardownErrors,
    };

    const regResult = fullUserRegistration(api, email, password, metrics);
    if (!regResult.success) return;
    const token = regResult.token;

    if (!createHive(api, token, hiveName, metrics.hiveCreateErrors).success) {
        deleteUser(api, token, email, metrics.userDeleteErrors);
        return;
    }

    // Loop операций чтения (10 итераций)
    for (let i = 0; i < 10; i++) {
        listHives(api, token, metrics.hiveListErrors);
        getHive(api, token, hiveName, metrics.hiveGetErrors);
        calculateQueen(api, { start_date: "2026-05-01" }, metrics.calcErrors);
        sleep(0.5);
    }

    // Teardown
    deleteUser(api, token, email, metrics.userDeleteErrors);
}
