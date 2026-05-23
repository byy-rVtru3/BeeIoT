import { standardOptions } from './config/k6-config.js';
import { basicFlow } from './scenarios/basic-flow.js';
import { fullCycle } from './scenarios/full-cycle.js';
import { readOperation } from './scenarios/read-operation.js';
import { writeOperation } from './scenarios/write-operation.js';
import { hubQueenFlow } from './scenarios/hub-queen-flow.js';

export const options = {
    scenarios: {
        basic_flow: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: standardOptions.stages,
            gracefulStop: '30s',
            exec: 'runBasicFlow',
        },
        full_cycle: {
            executor: 'per-vu-iterations',
            vus: 10,
            iterations: 1,
            maxDuration: '5m',
            exec: 'runFullCycle',
            startTime: '10s',
        },
        read_heavy: {
            executor: 'constant-vus',
            vus: 20,
            duration: '2m',
            exec: 'runReadOperation',
            startTime: '2m',
        },
        write_heavy: {
            executor: 'constant-vus',
            vus: 5,
            duration: '2m',
            exec: 'runWriteOperation',
            startTime: '2m',
        },
        hub_queen_flow: {
            executor: 'per-vu-iterations',
            vus: 5,
            iterations: 1,
            maxDuration: '5m',
            exec: 'runHubQueenFlow',
            startTime: '15s',
        },
    },
    thresholds: standardOptions.thresholds,
};

export function runBasicFlow() { basicFlow(); }
export function runFullCycle() { fullCycle(); }
export function runReadOperation() { readOperation(); }
export function runWriteOperation() { writeOperation(); }
export function runHubQueenFlow() { hubQueenFlow(); }
