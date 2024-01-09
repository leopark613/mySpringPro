//detail.js

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
      document.getElementById('content').textContent = data.content;
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
      document.getElementById('title').textContent = data.title;

      // 수정 로직 *************** 수정하기 버튼 클릭시 id 값만 가지고 수정 화면으로 이동******************
      const hiddenInput = document.querySelector('.d-inline input[type="hidden"]');
      if (hiddenInput) {
        hiddenInput.value = postId;
      }


      //삭제로직 ************** 상태값 'Y' -> 'N' 으로 변경후 게시판 board.html로 이동 *******************************
      const deleteForm = document.getElementById('deleteForm');
      if (deleteForm) {
        // postId 값을 사용하여 폼의 action 속성을 설정.
        deleteForm.action = `/board/deactivate/${postId}`;
      }

      const deleteButton = document.getElementById('deleteButton');
      if (deleteButton && deleteForm) {
        deleteButton.addEventListener('click', function() {
          // fetch 요청 전에 폼의 action 속성이 올바르게 설정되었는지 확인합니다.
          fetch(deleteForm.action, { method: 'POST' })
          .then(response => {
            if (response.ok) {
              // 동적으로 폼을 생성하여 리디렉션 수행
              var redirectForm = document.createElement('form');
              document.body.appendChild(redirectForm);
              redirectForm.method = 'get';
              redirectForm.action = '/view/board.html';
              redirectForm.submit();
            } else {
              console.error('Failed to deactivate the board');
            }
          })
          .catch(error => console.error('Error:', error));
        });
      }
    })
    .catch(error => {
      console.error('Error:', error);
    });
  }
});
