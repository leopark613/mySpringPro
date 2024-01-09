//modify.js

document.addEventListener("DOMContentLoaded", function() {
  const postId = new URLSearchParams(window.location.search).get('id');

  if (postId) {

    //*******서버로 가기 전, 로그인 없이 URL로만 진입을 하려고 하는 경우 검증***********
    const jwtToken = localStorage.getItem('jwtToken');
    if (!jwtToken) {
      console.error('JWT 토큰이 없습니다.');
      alert("로그인을 해주세요");
      return window.location.href = `/view/login.html`;
    }


    fetch(`/board/${postId}`)
    .then(response => response.json())
    .then(data => {
      document.getElementById('id').textContent = data.id;
      document.getElementById('content').value = data.content; // textarea에 값 설정
      //document.getElementById('createDate').textContent = new Date(data.createDate).toLocaleString();
      document.getElementById('createDate').textContent = new Date(data.createDate).toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
      });
      document.getElementById('count').textContent = data.count;
      document.getElementById('title').value = data.title;
      document.querySelector('input[name="id"]').value = postId; // hidden input에 값 설정
    })
    .catch(error => {
      console.error('Error:', error);
    });
  }
});

function cancelEdit() {
  const postId = document.querySelector('input[name="id"]').value;
  if (postId) {
    window.location.href = `/view/detail.html?id=${postId}`; // 사용자를 상세 페이지로 리디렉션
  }
}

function backList() {
  window.location.href = "/view/board.html"; // 게시판 목록 페이지로 이동
}