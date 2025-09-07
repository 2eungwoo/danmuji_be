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

const CREATE_COMMENT_URL = 'http://localhost:8080/api/comments';

const TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJteWRldmVsb3BlciIsImlkIjoyMywicm9sZSI6IlJPTEVfVVNFUiIsImlhdCI6MTc1MTk3MTQ1NiwiZXhwIjoxNzUxOTc1MDU2fQ.25C92082qIVFdCnUzPQKCEQxtTwIKWH-EoKctfSuyog";

export default function () {
  const payload = JSON.stringify({
    postId: 2000,
    parentId: null,
    content: `테스트 댓글 ${Math.floor(Math.random() * 10000)}`
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: TOKEN,
    },
  };

  const res = http.post(CREATE_COMMENT_URL, payload, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response contains data': (r) => r.body && r.body.includes('data'),
  });

  sleep(1);
}