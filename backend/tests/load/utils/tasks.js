import { sleep } from 'k6';
import { checkResponse, MOCK_CONFIRMATION_CODE, extractToken } from './test-data.js';


function runTask(apiCall, taskName, errorCounter, checks = {}) {
    const response = apiCall();

    const success = checkResponse(response, taskName, 2000, checks);

    if (!success) {
        if (errorCounter) errorCounter.add(1);
        console.error(`Error in ${taskName}: ${response.status} ${response.body ? response.body.slice(0, 100) : ''}`);
    }

    return { success, response };
}

export function registerUser(api, email, password, errors) {
    return runTask(() => api.register(email, password), 'Registration', errors, {
        'Body exists': (r) => r.body && r.body.length > 0
    });
}

export function confirmUserRegistration(api, email, password, errors) {
    return runTask(() => api.confirmRegistration(email, MOCK_CONFIRMATION_CODE, password), 'Confirm Registration', errors);
}

export function loginUser(api, email, password, errors) {
    const result = runTask(() => api.login(email, password), 'Login', errors, {
        'Token exists': (r) => extractToken(r) !== null
    });

    return {
        success: result.success,
        token: result.success ? extractToken(result.response) : null
    };
}

export function fullUserRegistration(api, email, password, metrics = {}) {
    const getCounter = (key) => metrics.add ? metrics : metrics[key];

    if (!registerUser(api, email, password, getCounter('registrationErrors')).success) return { success: false };
    sleep(1);
    if (!confirmUserRegistration(api, email, password, getCounter('confirmationErrors')).success) return { success: false };
    sleep(1);
    return loginUser(api, email, password, getCounter('loginErrors'));
}

// Hive tasks
export function createHive(api, token, name, errors) {
    return runTask(() => api.createHive(token, name), 'Create Hive', errors);
}

export function listHives(api, token, errors) {
    return runTask(() => api.listHives(token), 'List Hives', errors, {
        'Is Array': (r) => { try { return Array.isArray(r.json().data); } catch { return false; } }
    });
}

export function getHive(api, token, name, errors) {
    return runTask(() => api.getHive(token, name), 'Get Hive', errors);
}

export function updateHive(api, token, data, errors) {
    return runTask(() => api.updateHive(token, data), 'Update Hive', errors);
}

export function deleteHive(api, token, name, errors) {
    return runTask(() => api.deleteHive(token, name), 'Delete Hive', errors);
}

export function linkHubToHive(api, token, hiveName, hubId, errors) {
    return runTask(() => api.linkHubToHive(token, hiveName, hubId), 'Link Hub To Hive', errors);
}

export function linkQueenToHive(api, token, hiveName, queenName, errors) {
    return runTask(() => api.linkQueenToHive(token, hiveName, queenName), 'Link Queen To Hive', errors);
}

// Hub tasks
export function createHub(api, token, id, name, errors) {
    return runTask(() => api.createHub(token, id, name), 'Create Hub', errors);
}

export function listHubs(api, token, errors) {
    return runTask(() => api.listHubs(token), 'List Hubs', errors, {
        'Is Array': (r) => { try { return Array.isArray(r.json().data); } catch { return false; } }
    });
}

export function getHub(api, token, id, errors) {
    return runTask(() => api.getHub(token, id), 'Get Hub', errors);
}

export function updateHub(api, token, data, errors) {
    return runTask(() => api.updateHub(token, data), 'Update Hub', errors);
}

// Queen tasks
export function createQueen(api, token, name, startDate, errors) {
    return runTask(() => api.createQueen(token, name, startDate), 'Create Queen', errors, {
        'Has calendar': (r) => { try { return r.json().data.calendar !== undefined; } catch { return false; } }
    });
}

export function listQueens(api, token, errors) {
    return runTask(() => api.listQueens(token), 'List Queens', errors, {
        'Is Array': (r) => { try { return Array.isArray(r.json().data); } catch { return false; } }
    });
}

export function getQueen(api, token, name, errors) {
    return runTask(() => api.getQueen(token, name), 'Get Queen', errors, {
        'Has calendar': (r) => { try { return r.json().data.calendar !== undefined; } catch { return false; } }
    });
}

export function updateQueen(api, token, data, errors) {
    return runTask(() => api.updateQueen(token, data), 'Update Queen', errors);
}

export function deleteQueen(api, token, name, errors) {
    return runTask(() => api.deleteQueen(token, name), 'Delete Queen', errors);
}

// Telemetry tasks
export function setHiveWeight(api, token, data, errors) {
    return runTask(() => api.setHiveWeight(token, data), 'Set Hive Weight', errors);
}

export function getWeightSince(api, token, hubId, since, errors) {
    return runTask(() => api.getWeightSince(token, hubId, since), 'Get Weight Since', errors, {
        'Is Array': (r) => { try { return Array.isArray(r.json().data); } catch { return false; } }
    });
}

export function getNoiseSince(api, token, hubId, since, errors) {
    return runTask(() => api.getNoiseSince(token, hubId, since), 'Get Noise Since', errors, {
        'Is Array': (r) => { try { return Array.isArray(r.json().data); } catch { return false; } }
    });
}

export function getTemperatureSince(api, token, hubId, since, errors) {
    return runTask(() => api.getTemperatureSince(token, hubId, since), 'Get Temperature Since', errors, {
        'Is Array': (r) => { try { return Array.isArray(r.json().data); } catch { return false; } }
    });
}

// Auth tasks
export function deleteUser(api, token, email, errors) {
    return runTask(() => api.deleteUser(token), 'Delete User', errors);
}

export function changePassword(api, email, newPassword, metrics = {}) {
    const getCounter = (key) => metrics.add ? metrics : metrics[key];
    const errors = getCounter('passwordChangeErrors');

    const req = runTask(() => api.requestPasswordChange(email, newPassword), 'Request Pwd Change', errors);
    if (!req.success) return { success: false };

    sleep(1);

    const conf = runTask(() => api.confirmPasswordChange(email, MOCK_CONFIRMATION_CODE, newPassword), 'Confirm Pwd Change', errors);
    return { success: conf.success };
}
