import { check } from "k6";

export const DEFAULT_PASSWORD = 'Test1234567890';
export const MOCK_CONFIRMATION_CODE = '123456';

export function generateEmail(prefix = 'loadtest') {
    return `${prefix}${__VU}_${__ITER}_${Date.now()}@example.com`;
}

export function generateHiveName(prefix = 'Hive') {
    return `${prefix}-${__VU}-${__ITER}`;
}

export function extractToken(response) {
    try {
        return response.json().data.token;
    } catch (e) {
        return null;
    }
}

export function checkResponse(response, name, maxDuration = 2000, customChecks = {}) {
    const checks = {
        [`${name}: status 2xx`]: (r) => r.status >= 200 && r.status < 300,
        [`${name}: time < ${maxDuration}ms`]: (r) => r.timings.duration < maxDuration,
        ...customChecks
    };
    return check(response, checks);
}