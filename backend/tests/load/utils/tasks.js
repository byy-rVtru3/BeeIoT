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

export function calculateQueen(api, data, errors) {
    return runTask(() => api.calcQueen(data), 'Calc Queen', errors);
}

export function deleteHive(api, token, name, errors) {
    return runTask(() => api.deleteHive(token, name), 'Delete Hive', errors);
}

export function deleteUser(api, token, email, errors) {
    return runTask(() => api.deleteUser(token), 'Delete User', errors);
}

export function changePassword(api, email, newPassword, metrics = {}) {
    const getCounter = (key) => metrics.add ? metrics : metrics[key];
    const errors = getCounter('passwordChangeErrors');

    const req = runTask(() => api.requestPasswordChange(email), 'Request Pwd Change', errors);
    if (!req.success) return { success: false };

    sleep(1);

    const conf = runTask(() => api.confirmPasswordChange(email, MOCK_CONFIRMATION_CODE, newPassword), 'Confirm Pwd Change', errors);
    return { success: conf.success };
}