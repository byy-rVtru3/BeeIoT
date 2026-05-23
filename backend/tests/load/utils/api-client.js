import http from "k6/http"

const BASE_URL = __ENV.BASE_URL || 'http://localhost/api';

export class BeeIoTAPI {
    constructor(baseUrl = BASE_URL) {
        this.baseUrl = baseUrl;
    }

    _request(method, url, headers, body = null, tags = {}) {
        const fullUrl = (this.baseUrl + url).replace(/([^:]\/)\/+/g, "$1");
        const params = { headers, tags };
        const payload = body ? JSON.stringify(body) : null;

        return http.request(method, fullUrl, payload, params);
    }

    get(name, url, token = null) {
        return this._request('GET', url, this._headers(token), null, { name });
    }

    post(name, url, body, token = null) {
        return this._request('POST', url, this._headers(token), body, { name });
    }

    put(name, url, body, token = null) {
        return this._request('PUT', url, this._headers(token), body, { name });
    }

    del(name, url, body = null, token = null) {
        return this._request('DELETE', url, this._headers(token), body, { name });
    }

    _headers(token) {
        const headers = { 'Content-Type': 'application/json' };
        if (token) headers['Authorization'] = `Bearer ${token}`;
        return headers;
    }

    // Auth
    register(email, password) {
        return this.post('Registration', '/auth/registration', { email, password });
    }

    confirmRegistration(email, code, password) {
        return this.post('ConfirmRegistration', '/auth/confirm/registration', { email, code, password });
    }

    login(email, password) {
        return this.post('Login', '/auth/login', { email, password });
    }

    changeName(token, name) {
        return this.post('ChangeName', '/auth/change/name', { name }, token);
    }

    requestPasswordChange(email, newPassword) {
        return this.post('RequestPasswordChange', '/auth/change', { email, password: newPassword });
    }

    confirmPasswordChange(email, code, password) {
        return this.post('ConfirmPasswordChange', '/auth/confirm/password', { email, code, password });
    }

    logout(token) {
        return this.del('Logout', '/auth/logout', null, token);
    }

    deleteUser(token) {
        return this.del('DeleteUser', '/auth/delete/user', null, token);
    }

    // Hive
    createHive(token, name) {
        return this.post('CreateHive', '/hive/create', { name }, token);
    }

    listHives(token) {
        return this.get('ListHives', '/hive/list', token);
    }

    getHive(token, name) {
        return this.get('GetHive', `/hive?name=${encodeURIComponent(name)}`, token);
    }

    updateHive(token, hiveData) {
        return this.put('UpdateHive', '/hive/update', hiveData, token);
    }

    deleteHive(token, name) {
        return this.del('DeleteHive', '/hive/delete', { name }, token);
    }

    linkHubToHive(token, hiveName, targetName) {
        return this.post('LinkHubToHive', '/hive/link/hub', { hive_name: hiveName, target_name: targetName }, token);
    }

    linkQueenToHive(token, hiveName, targetName) {
        return this.post('LinkQueenToHive', '/hive/link/queen', { hive_name: hiveName, target_name: targetName }, token);
    }

    // Hub
    createHub(token, id, name) {
        return this.post('CreateHub', '/hub/create', { id, name }, token);
    }

    listHubs(token) {
        return this.get('ListHubs', '/hub/list', token);
    }

    getHub(token, id) {
        return this.get('GetHub', `/hub?id=${encodeURIComponent(id)}`, token);
    }

    updateHub(token, data) {
        return this.put('UpdateHub', '/hub/update', data, token);
    }

    // Queen
    createQueen(token, name, startDate) {
        return this.post('CreateQueen', '/queen/create', { name, start_date: startDate }, token);
    }

    listQueens(token) {
        return this.get('ListQueens', '/queen/list', token);
    }

    getQueen(token, name) {
        return this.get('GetQueen', `/queen?name=${encodeURIComponent(name)}`, token);
    }

    updateQueen(token, data) {
        return this.put('UpdateQueen', '/queen/update', data, token);
    }

    deleteQueen(token, name) {
        return this.del('DeleteQueen', '/queen/delete', { name }, token);
    }

    // Telemetry
    setHiveWeight(token, data) {
        return this.post('SetHiveWeight', '/telemetry/weight/set', data, token);
    }

    deleteHiveWeight(token, data) {
        return this.del('DeleteHiveWeight', '/telemetry/weight/delete', data, token);
    }

    getWeightSince(token, hubId, since) {
        return this.get('GetWeightSince', `/telemetry/weight/get?hub=${encodeURIComponent(hubId)}&since=${since}`, token);
    }

    getNoiseSince(token, hubId, since) {
        return this.get('GetNoiseSince', `/telemetry/noise/get?hub=${encodeURIComponent(hubId)}&since=${since}`, token);
    }

    getTemperatureSince(token, hubId, since) {
        return this.get('GetTemperatureSince', `/telemetry/temperature/get?hub=${encodeURIComponent(hubId)}&since=${since}`, token);
    }
}
