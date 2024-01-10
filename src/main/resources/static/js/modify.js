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
      alert("로그인을 해주세요");
      return window.location.href = '/view/login.html';
    }


    // 서버로부터 데이터를 가져와서 화면에 표시 - 수정하기 화면
    fetch(`/board/seq/${postSeq}`)
    .then(response => response.json())
    .then(data => {
      document.getElementById('id').textContent = data.id;
      document.getElementById('seq').textContent = data.seq;
      document.getElementById('content').value = data.content; // textarea에 값 설정
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

      // hidden input에 값 설정 - 저장을 위한 set
      document.querySelector('input[name="id"]').value = postId;
      document.querySelector('input[name="seq"]').value = postSeq;

    })
    .catch(error => {
      console.error('Error:', error);
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
