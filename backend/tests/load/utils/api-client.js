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



    register(email, password) {
        return this.post('Registration', '/auth/registration', { email, password });
    }

    confirmRegistration(email, code, password) {
        return this.post('ConfirmRegistration', '/auth/confirm/registration', { email, code, password });
    }

    login(email, password) {
        return this.post('Login', '/auth/login', { email, password });
    }

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

    calcQueen(data, token) {
        return this.post('CalcQueen', '/calcQueen/calc', data, token);
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

    deleteHive(token, name) {
        return this.del('DeleteHive', '/hive/delete', { name }, token);
    }

    deleteUser(token) {
        return this.del('DeleteUser', '/auth/delete/user', null, token);
    }
}