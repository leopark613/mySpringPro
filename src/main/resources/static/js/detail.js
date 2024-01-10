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



       //이게 없어도될까? 이미 html에서 action 이 존재하기때문...

      //삭제로직 ************** 상태값 'Y' -> 'N' 으로 변경후 게시판 board.html로 이동 *******************************
       const deleteForm = document.getElementById('deleteForm');
       if (deleteForm) {
         // postSeq 값을 사용하여 폼의 action 속성을 설정.
         deleteForm.action = `/board/deactivate/${postSeq}`;
       }

      // const deleteButton = document.getElementById('deleteButton');
      // if (deleteButton && deleteForm) {
      //   deleteButton.addEventListener('click', function() {
      //     // fetch 요청 전에 폼의 action 속성이 올바르게 설정되었는지 확인합니다.
      //     fetch(deleteForm.action, { method: 'POST' })
      //     .then(response => {
      //       if (response.ok) {
      //         // 동적으로 폼을 생성하여 리디렉션 수행
      //         let redirectForm = document.createElement('form');
      //         document.body.appendChild(redirectForm);
      //         redirectForm.method = 'get';
      //         redirectForm.action = '/view/board.html';
      //         redirectForm.submit();
      //       } else {
      //         console.error('Failed to deactivate the board');
      //       }
      //     })
      //     .catch(error => console.error('Error:', error));
      //   });
      // }

    })
    .catch(error => {
      console.error('Error:', error);
    });
  }
});
