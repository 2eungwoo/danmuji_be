import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
  vus: 100,
  duration: '30s',
  thresholds: {
    http_req_duration: ['avg<500', 'p(95)<1000'],
    http_req_failed: ['rate<0.01'],
  },
};

const TOKEN = 'Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJteWRldmVsb3BlciIsImlkIjoyMywicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTc1MTQ1NzI4NiwiZXhwIjoxNzUxNDYwODg2fQ.XPmgGIIXzxcVqNhyIWTaOXt22EuUovRTNefjAbz1RCc';

const BASE_URL = 'http://localhost:8080/api/users';
const PROJECT_ID = 1;

export default function () {
  const params = {
    headers: {
      Authorization: TOKEN,
    },
  };

  // 프로젝트 기반 유저 조회
  const projectRes = http.get(`${BASE_URL}/project/${PROJECT_ID}`, params);
  check(projectRes, {
    'project - status is 200': (r) => r.status === 200,
    'project - response has data': (r) => r.body.includes('data'),
  });

  sleep(1); // 부하 분산을 위한 대기
}