import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
  vus: 1,
  duration: '3s',
  thresholds: {
    http_req_duration: ['avg<500', 'p(95)<1000'],
    http_req_failed: ['rate<0.01'],
  },
};

const CREATE_POSTS_URL = 'http://localhost:8080/api/posts';

const TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJteWRldmVsb3BlciIsImlkIjoyMywicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTc1MjA0NTI5MywiZXhwIjoxNzUyMDQ4ODkzfQ.b8Go5oTol7pVlOweUulpGHchUBUL1PGERvcogCPtZDg";

export default function () {
  const boundary = '----WebKitFormBoundary7MA4YWxkTrZu0gW';

  const jsonPart = JSON.stringify({
    parentId: null,
    title: `테스트 제목 ${Math.random()}`,
    content: `테스트 내용 ${Math.random()}`,
    type: 'GENERAL',
    priority: 'HIGH',
    projectId: 1,
    stepId: 1,
    newLinks: [],
  });

  const body =
      `--${boundary}\r\n` +
      `Content-Disposition: form-data; name="data"\r\n` +
      `Content-Type: application/json\r\n\r\n` +
      `${jsonPart}\r\n` +
      `--${boundary}--\r\n`;

  const headers = {
    'Content-Type': `multipart/form-data; boundary=${boundary}`,
    Authorization: TOKEN,
  };

  const res = http.post(CREATE_POSTS_URL, body, { headers });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response contains data': (r) => r.body && r.body.includes('data'),
  });

  sleep(1);
}