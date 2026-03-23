import { sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { BeeIoTAPI } from '../utils/api-client.js';
import { generateEmail, generateHiveName, generateQueenName, DEFAULT_PASSWORD } from '../utils/test-data.js';
import {
    fullUserRegistration,
    createHive,
    listHives,
    deleteHive,
    createQueen,
    deleteQueen,
    deleteUser,
} from '../utils/tasks.js';

// All counters must be declared in init context
const scenarioErrors = new Counter('write_ops_errors');
const setupErrors = new Counter('setup_errors');
const teardownErrors = new Counter('teardown_errors');

export function writeOperation() {
    const api = new BeeIoTAPI();
    const email = generateEmail('write');
    const password = DEFAULT_PASSWORD;

    const metrics = {
        registrationErrors: setupErrors,
        confirmationErrors: setupErrors,
        loginErrors: setupErrors,

        hiveCreateErrors: scenarioErrors,
        hiveListErrors: scenarioErrors,
        hiveDeleteErrors: scenarioErrors,
        queenCreateErrors: scenarioErrors,
        queenDeleteErrors: scenarioErrors,

        userDeleteErrors: teardownErrors,
    };

    const regResult = fullUserRegistration(api, email, password, metrics);
    if (!regResult.success) return;
    const token = regResult.token;

    // Loop создания и удаления ульёв (5 итераций)
    for (let i = 0; i < 5; i++) {
        const hiveName = generateHiveName(`Write-${i}`);

        if (createHive(api, token, hiveName, metrics.hiveCreateErrors).success) {
            listHives(api, token, metrics.hiveListErrors);
            sleep(0.5);
            deleteHive(api, token, hiveName, metrics.hiveDeleteErrors);
        }
        sleep(0.3);
    }

    // Loop создания и удаления маток (3 итерации)
    for (let i = 0; i < 3; i++) {
        const queenName = generateQueenName(`WQ-${i}`);

        if (createQueen(api, token, queenName, '2026-01-01', metrics.queenCreateErrors).success) {
            sleep(0.3);
            deleteQueen(api, token, queenName, metrics.queenDeleteErrors);
        }
        sleep(0.3);
    }

    // Teardown
    deleteUser(api, token, email, metrics.userDeleteErrors);
}
