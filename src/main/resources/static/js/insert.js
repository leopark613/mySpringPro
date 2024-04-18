//insert.js

document.addEventListener("DOMContentLoaded", function() {
  const createForm = document.getElementById('createPostForm');
  const jwtToken = localStorage.getItem('jwtToken');

  if (!jwtToken) {
    console.error('JWT 토큰이 없습니다.');
    alert("로그인을 해주세요");
    return window.location.href = '/view/login.html';
  }

  // JWT 토큰에서 사용자 ID 추출
  const base64Url = jwtToken.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const payload = JSON.parse(window.atob(base64));
  const userId = payload.userId;

  createForm.addEventListener('submit', function(event) {
    event.preventDefault();

    const formData = new FormData();
    // formData.append('id', userId); // 사용자 ID 추가
    // formData.append('title', document.getElementById('title').value);
    // console.log('title : ' + document.getElementById('title').value);
    // formData.append('content', document.getElementById('content').value);

    const postData = { // 직접 JSON 객체를 생성합니다.
      id: userId,
      title: document.getElementById('title').value,
      content: document.getElementById('content').value
    };

    const imageFile = document.getElementById('image').files[0];
   /*
    if (imageFile) {
      // 이미지가 존재하는 경우
      const imageFormData = new FormData();
      imageFormData.append('image', imageFile);

      // 먼저 이미지를 업로드하고, 게시글을 생성하는 요청을 보냅니다.
      fetch('/board/upload', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${jwtToken}`
        },
        body: imageFormData
      })
      .then(response => {
        // JWT 토큰 만료나 다른 HTTP 에러 처리
        if (!response.ok) {
          if (response.status === 500) {
            alert("세션이 만료되었습니다. 다시 로그인 해주세요.");
            window.location.href = '/view/login.html'; // 로그인 페이지로 리다이렉트
          } else {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
        }
        return response.json();
      })
      .then(data => {
        // 이미지 업로드 성공 시, imagePath를 formData에 추가합니다.
        formData.append('imagePath', data.path);
        return fetch('/board/create', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${jwtToken}`
          },
          body: formData
        });
      })
      .then(response => {
        // 게시글 생성 요청의 결과를 처리합니다.
        if (response.ok) {
          window.location.href = '/view/board.html';
        } else {
          throw new Error('Network response was not ok.');
        }
      })
      .catch((error) => {
        console.error('Error:', error);
      });
    */
    //} else {


    //console.log('Bearer : ' , ${jwtToken});

      // 이미지가 없는 경우, 바로 게시글을 생성하는 요청을 보냅니다.
      fetch('/board/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${jwtToken}`
        },
        //body: formData
        body: JSON.stringify(postData)
      }
      )
      .then(response => {
        if (response.ok) {
          window.location.href = '/view/board.html';
        }else if (response.status === 500) {
          alert("세션이 만료되었습니다. 다시 로그인 해주세요.");
          window.location.href = '/view/login.html'; // 로그인 페이지로 리다이렉트
        } else {
          throw new Error('Network response was not ok.');
        }
      })
      .catch((error) => {
        console.error('Error:', error);
      });
    //}
  });


});
