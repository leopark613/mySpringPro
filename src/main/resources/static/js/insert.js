//insert.js

document.addEventListener("DOMContentLoaded", function() {
  const createForm = document.getElementById('createPostForm');

  //*******서버로 가기 전, 로그인 없이 URL로만 진입을 하려고 하는 경우 검증***********
  const jwtToken = localStorage.getItem('jwtToken');
  if (!jwtToken) {
    console.error('JWT 토큰이 없습니다.');
    alert("로그인을 해주세요");
    return window.location.href = `/view/login.html`;
  }


  // JWT 토큰에서 사용자 ID 추출 - 저장버튼 클릭시 사용자 ID가 저장되기 위함
  const base64Url = jwtToken.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const payload = JSON.parse(window.atob(base64));
  const userId = payload.userId;

  createForm.addEventListener('submit', function(event) {
    //jwt토큰 추출(auth도 함께 보내기 위함)
    const jwtToken = localStorage.getItem('jwtToken');
    event.preventDefault();
    const formData = {
      id: userId,
      title: document.getElementById('title').value,
      content: document.getElementById('content').value
    };

    fetch('/board/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${jwtToken}` // JWT 토큰을 헤더에 포함
      },
      body: JSON.stringify(formData)
    })
    .then(response => {
      if (response.ok) {
        // 동적으로 폼 생성 및 제출하여 페이지 리디렉션
        const form = document.createElement('form');
        form.method = 'GET';
        form.action = '/view/board.html';
        document.body.appendChild(form);
        form.submit();
      } else {
        throw new Error('Network response was not ok.');
      }
    })
    .catch((error) => {
      console.error('Error:', error);
    });
  });
});
