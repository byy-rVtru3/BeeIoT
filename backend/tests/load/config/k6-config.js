export const standardOptions = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.01'],
    },
};

export const smokeOptions = {
    vus: 1,
    duration: '10s',
    thresholds: {
        http_req_failed: ['rate==0.00'],
    },
};

export const stressOptions = {
    stages: [
        { duration: '1m', target: 200 },
        { duration: '2m', target: 200 },
        { duration: '1m', target: 500 },
        { duration: '2m', target: 500 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<5000'],
        http_req_failed: ['rate<0.05'],
    },
};
