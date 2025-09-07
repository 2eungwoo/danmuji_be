import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
  vus: 100,
  duration: '30s',
  thresholds: {
    http_req_duration: ['avg<500', 'p(95)<1000'],  // 평균 500ms 이하, 95%가 1초 이내
    http_req_failed: ['rate<0.01'],               // 오류율 1% 이하
  },
};

// const HISTORY_SEARCH_URL = 'http://localhost:8080/api/histories/search?page=0&size=10';
const POST_SEARCH_URL = 'http://localhost:8080/api/posts/search?projectId=3&page=0&size=10';

const TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJteWRldmVsb3BlciIsImlkIjoyMywicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTc1MTg2NDA1NiwiZXhwIjoxNzUxODY3NjU2fQ.GiiPsuoD2Z9qX_l_Hx3uc6lWRX0weGoQIhsYi2Yet78";

export default function () {
  const params = {
    headers: {
      Authorization: TOKEN
    },
  };

  const res = http.get(POST_SEARCH_URL, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response contains data': (r) => r.body && r.body.includes('data'),
  });

  sleep(1);
}

// export function handleSummary(data) {
//   return {
//     stdout: textSummary(data, { indent: ' ', enableColors: true }),
//     'summary.txt': textSummary(data, { indent: ' ', enableColors: true }),
//     'summary.json': JSON.stringify(data, null, 2),
//   };
// }