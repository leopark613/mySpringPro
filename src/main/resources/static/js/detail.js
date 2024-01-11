//detail.js

document.addEventListener("DOMContentLoaded", function() {
  //const postId = new URLSearchParams(window.location.search).get('id');
  let queryParams = new URLSearchParams(window.location.search);
  let postId = queryParams.get('id'); // 'id' 값을 추출 (URL에 표시됨)
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

    // 서버로부터 데이터를 가져와서 화면에 표시 - 상세글보기 화면
     fetch(`/board/seq/${postSeq}`) //이걸로 수정해야함
    .then(response => response.json())
    .then(data => {
      document.getElementById('id').textContent = data.id;
      document.getElementById('seq').textContent = data.seq;
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



      // 수정 로직 *************** 수정하기 버튼 클릭시 id,seq 값만 가지고 수정 화면으로 이동******************
       //[수정버튼] - id, seq를 가지고 쿼리스트링으로 수정화면으로 이동
       document.querySelector('.d-inline input[name="id"]').value = data.id;
       document.querySelector('.d-inline input[name="seq"]').value = data.seq;
       document.querySelector('.d-inline').action = `/view/modify.html?id=${data.id}&seq=${data.seq}`;


      //삭제로직 ************** 상태값 'Y' -> 'N' 으로 변경후 게시판 board.html로 이동 *******************************
       const deleteForm = document.getElementById('deleteForm');
       if (deleteForm) {
         // postSeq 값을 사용하여 폼의 action 속성을 설정.
         deleteForm.action = `/board/deactivate/${postSeq}`;
       }

      // 로그인한 사용자의 id일 경우에만 [수정하기],[삭제] 버튼 보이게하기
       const loggedInUserId = getUserIdFromJwtToken(); //JWT토큰에서 추출한 로그인한 id 추출한 함수
       const postAuthorId = data.id;  //상세보기 화면에서 게시글의 작성자 id 추출

       //수정하기버튼
       const editButton = document.getElementById('editPost');
       const delButton = document.getElementById('deleteButton');

       //게시글 작성자와 접속한 id가 같지 않은 경우 [수정],[삭제] 버튼 disable
       if (editButton) {
         if (loggedInUserId !== postAuthorId) {
           editButton.style.display = 'none'; // 수정 버튼 disable
           delButton.style.display = 'none'; // 삭제 버튼 disable
         }
       }



    })
    .catch(error => {
      console.error('Error:', error);
    });
  }
});



// JWT 토큰에서 사용자 ID 추출
function getUserIdFromJwtToken() {
  const jwtToken = localStorage.getItem('jwtToken');
  if (!jwtToken) {
    return null;
  }
  const base64Url = jwtToken.split('.')[1];
  const base64 = base64Url.replace('-', '+').replace('_', '/');
  return JSON.parse(window.atob(base64)).userId;
}