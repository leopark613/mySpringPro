//modify.js

document.addEventListener("DOMContentLoaded", function() {
  //const postId = new URLSearchParams(window.location.search).get('id');
  let queryParams = new URLSearchParams(window.location.search);
  let postId = queryParams.get('id'); // 'id','seq' 값을 추출 (URL에 표시됨)
  let postSeq = queryParams.get('seq'); // 'seq' 값을 추출

  console.log("URL Query Parameters:", window.location.search);
  console.log("Post ID:", postId);
  console.log("Post Seq:", postSeq);



  if (postId && postSeq && !isNaN(parseInt(postSeq, 10))) {
    //*******서버로 가기 전, 로그인 없이 URL로만 진입을 하려고 하는 경우 검증***********
    const jwtToken = localStorage.getItem('jwtToken');
    if (!jwtToken) {
      console.error('JWT 토큰이 없습니다.');
      alert('로그인을 해주세요');
      return window.location.href = '/view/login.html';
    }

    // 서버로부터 데이터를 가져와서 화면에 표시 - 수정하기 화면
    fetch(`/board/seq/${postSeq}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${jwtToken}`,
      },
    }).then(response => {
      // JWT 토큰 만료나 다른 HTTP 에러 처리
      if (!response.ok) {
        if (response.status === 500) {
          alert('세션이 만료되었습니다. 다시 로그인 해주세요.');
          window.location.href = '/view/login.html'; // 로그인 페이지로 리다이렉트
        } else {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
      }
      return response.json();
    }).then(data => {
      document.getElementById('id').textContent = data.id;
      document.getElementById('seq').textContent = data.seq;
      document.getElementById('content').value = data.content; // textarea에 값 설정
      document.getElementById('createDate').textContent = new Date(data.createDate).toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: 'numeric',
        minute: '2-digit',
        hour12: true,
      });
      document.getElementById('count').textContent = data.count;
      document.getElementById('title').value = data.title;

      // hidden input에 값 설정 - 저장을 위한 set
      document.querySelector('input[name="id"]').value = postId;
      document.querySelector('input[name="seq"]').value = postSeq;

    }).catch(error => {
      console.error('Error:', error);
    });


    //수정버튼 클릭시 서비스에서 토큰 검증을 하기 위해 api 호출
    document.getElementById('saveButton').addEventListener('click', function(event) {
      event.preventDefault(); // 폼 기본 제출 막기

      let updateData = {
        title: document.getElementById('title').value,
        content: document.getElementById('content').value,
        id: postId,
        seq: postSeq
      };

      fetch('/board/update', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(updateData)
      }).then(response => {
        if (response.ok) {
          alert('게시글이 성공적으로 수정되었습니다.');
          window.location.href = `/view/detail.html?id=${postId}&seq=${postSeq}`;
        } else {
          throw new Error('게시글 수정에 실패했습니다.');
        }
      }).catch(error => {
        console.error('Error during update:', error);
        alert(error.message);
      });
    });
  }

});

function cancelEdit() {
  let queryString = window.location.search;
  if (queryString) {
    window.location.href = `/view/detail.html?id=${queryString}`; // 사용자를 상세 페이지로 리디렉션
  }
}

function backList() {
  window.location.href = "/view/board.html"; // 게시판 목록 페이지로 이동
}
