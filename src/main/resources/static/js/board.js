//board.js
document.addEventListener("DOMContentLoaded", function() {
  const ITEMS_PER_PAGE = 20; // 한 페이지에 표시할 항목 수
  const MAX_PAGES = 5; // 한 번에 표시할 최대 페이지 수
  let currentPage = 1; // 현재 페이지
  let totalPages = 0; // 총 페이지 수

  const searchButton = document.getElementById('searchButton');
  const searchInput = document.getElementById('searchInput');
  const boardContent = document.getElementById('boardContent');

  // 검색 버튼 클릭 이벤트 리스너
  searchButton.addEventListener('click', function() {
    const searchType = document.getElementById('searchType').value;
    const searchTerm = searchInput.value.trim();
    fetchAndDisplayPage(1, searchType, searchTerm);
  });

  // 검색 입력창에서 엔터키 이벤트 리스너
  searchInput.addEventListener('keypress', function(event) {
    if (event.key === 'Enter') {
      event.preventDefault();
      searchButton.click();
    }
  });

  // 로그아웃 버튼 클릭 이벤트 리스너
  document.getElementById('logoutButton').addEventListener('click', function() {
    localStorage.removeItem('jwtToken'); // 토큰 제거

    // 동적으로 로그인 페이지로 이동하는 폼 생성 및 제출
    var form = document.createElement('form');
    document.body.appendChild(form);
    form.method = 'get';
    form.action = '/view/login.html';
    form.submit();
  });

  //글쓰기 버튼 클릭시, 토큰값 검사 후 글쓰기 화면으로 이동
  const createPostButton = document.getElementById('createPost');
  createPostButton.addEventListener('click', function(event) {
    const jwtToken = localStorage.getItem('jwtToken');
    if (!jwtToken) {
      alert("로그인을 해주세요");
      window.location.href = '/view/login.html';
      event.preventDefault(); // 폼 제출 막기
    } else {
      // 서버에 토큰 유효성 검증 요청을 보낼 수 있음
      fetch('/board/writeBoard', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${jwtToken}`
        }
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('세션이 만료되었습니다. 다시 로그인 해주세요.');
        }
        return response.json();
      })
      .then(data => {
        if (data.isValid) {
          window.location.href = '/view/insert.html'; // 유효한 토큰일 경우 글쓰기 페이지로 이동
        } else {
          throw new Error('세션이 만료되었습니다. 다시 로그인 해주세요.');
        }
      })
      .catch(error => {
        alert(error.message);
        window.location.href = '/view/login.html'; // 로그인 페이지로 리다이렉트
      });
    }
  });

  // 페이지를 가져오고 표시하는 함수
  function fetchAndDisplayPage(page, searchType, searchTerm) {
    let url = '/board/active';
    if (searchType && searchTerm) {
      url += `?type=${searchType}&term=${encodeURIComponent(searchTerm)}`;
    }
    //*******서버로 가기 전, 로그인 없이 URL로만 진입을 하려고 하는 경우 검증***********
    const jwtToken = localStorage.getItem('jwtToken');
    //로그인없이(토큰없이) 입장 하려 할 경우
    if (!jwtToken) {
      console.error('JWT 토큰이 없습니다.');
      alert("로그인을 해주세요");
      return window.location.href = `/view/login.html`;
    }
    fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${jwtToken}` // 토큰을 헤더에 포함시킵니다.
      }
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Server responded with ' + response.status);
      }
      if (response.status === 204) { // 204 No Content 응답 처리
        return []; // 빈 배열 반환
      }
      if (response.status === 500) {
        alert("세션이 만료되었습니다. 다시 로그인 해주세요.");
        return window.location.href = '/view/login.html'; // 로그인 페이지로 리다이렉트
      }
      return response.json();
    })
    .then(activeBoards => {

      //************************************************************
      // 로그인한사용자 권한 확인
      const userAuth = getUserAuthFromJwtToken();
      // 사용자 권한에 따른 게시글 필터링
      let boardsToDisplay;
      if (userAuth === '1') {
        // 관리자는 모든 게시글 보임
        boardsToDisplay = activeBoards;
      } else {
        // 일반 사용자는 권한이 '0'인 게시글만 보임
        boardsToDisplay = activeBoards.filter(board => board.auth === '0');
      }
      //************************************************************

      const boardContent = document.getElementById('boardContent');
      boardContent.innerHTML = '';

      //검색한 키워드에 대한 결과값이 없을 경우
      if (boardsToDisplay.length === 0) {
        const noDataMessage = document.createElement('div');
        noDataMessage.textContent = '조회할 데이터가 없습니다';
        noDataMessage.style.textAlign = 'center';
        noDataMessage.style.margin = '20px';
        noDataMessage.style.fontSize = '20px';
        boardContent.appendChild(noDataMessage);
        return;
      }


      totalPages = Math.ceil(boardsToDisplay.length / ITEMS_PER_PAGE);

      const startIndex = (page - 1) * ITEMS_PER_PAGE;
      const endIndex = Math.min(startIndex + ITEMS_PER_PAGE, boardsToDisplay.length);
      const boards = activeBoards.slice(startIndex, endIndex);

      //boards.forEach(board => {
      boardsToDisplay.slice(startIndex, endIndex).forEach(board => {
        const template = document.getElementById('boardRowTemplate').content.cloneNode(true);
        template.querySelector('.seq-cell').textContent = board.seq;
        //template.querySelector('.title-cell').textContent = board.title;
        template.querySelector('.title-button').textContent = board.title;
        template.querySelector('.id-cell').textContent = board.id;
        template.querySelector('.count-cell').textContent = board.count;
        template.querySelector('.create-date-cell').textContent = new Date(board.createDate).toLocaleString('ko-KR', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: 'numeric',
          minute: '2-digit',
          hour12: true
        });
        template.querySelector('.update-date-cell').textContent = new Date(board.updateDate).toLocaleString('ko-KR', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: 'numeric',
          minute: '2-digit',
          hour12: true
        });

        // hidden input 값 설정
        template.querySelector('.title-cell input[name="id"]').value = board.id;
        template.querySelector('.title-cell input[name="seq"]').value = board.seq;
        //detail.html(상세페이지)로 넘어가기 위한 쿼리스트링
        template.querySelector('.title-cell').action = `/view/detail.html?id=${board.id}&seq=${board.seq}`;


        boardContent.appendChild(document.importNode(template, true));
      });

      updatePagination();
    })
    .catch(error => {
      console.error('Fetch error:', error);
    });
  }

  // 페이지네이션 업데이트 함수
  function updatePagination() {
    const paginationDiv = document.getElementById('pagination');
    paginationDiv.innerHTML = '';

    // 시작 페이지와 끝 페이지 계산
    let startPage, endPage;
    if (totalPages <= MAX_PAGES) {
      // 전체 페이지 수가 MAX_PAGES 이하인 경우
      startPage = 1;
      endPage = totalPages;
    } else {
      // 현재 페이지가 3보다 작거나 같은 경우
      if (currentPage <= 3) {
        startPage = 1;
        endPage = 5;
      } else if (currentPage + 2 >= totalPages) {
        // 현재 페이지가 전체 페이지 수 - 2보다 큰 경우
        startPage = totalPages - 4;
        endPage = totalPages;
      } else {
        // 그 외 경우, 현재 페이지를 중심으로 표시
        startPage = currentPage - 2;
        endPage = currentPage + 2;
      }
    }

    // 이전 페이지 화살표 추가
    const prevPageItem = document.createElement('li');
    prevPageItem.classList.add('page-item');
    const prevPageLink = document.createElement('a');
    prevPageLink.classList.add('page-link');
    prevPageLink.href = '#';
    prevPageLink.setAttribute('aria-label', 'Previous');
    prevPageLink.innerHTML = '&laquo;';
    prevPageLink.addEventListener('click', (e) => {
      e.preventDefault();
      if (currentPage > 1) {
        currentPage--;
        fetchAndDisplayPage(currentPage);
      }
    });
    prevPageItem.appendChild(prevPageLink);
    paginationDiv.appendChild(prevPageItem);

    // 페이지 번호 버튼 생성 및 추가
    for (let i = startPage; i <= endPage; i++) {
      const pageItem = document.createElement('li');
      pageItem.classList.add('page-item');
      if (i === currentPage) {
        pageItem.classList.add('active');
      }

      const pageLink = document.createElement('a');
      pageLink.classList.add('page-link');
      pageLink.textContent = i;
      pageLink.href = '#';
      pageLink.addEventListener('click', (e) => {
        e.preventDefault();
        currentPage = i;
        fetchAndDisplayPage(i);
      });

      pageItem.appendChild(pageLink);
      paginationDiv.appendChild(pageItem);
    }

    // 다음 페이지 화살표 추가
    const nextPageItem = document.createElement('li');
    nextPageItem.classList.add('page-item');
    const nextPageLink = document.createElement('a');
    nextPageLink.classList.add('page-link');
    nextPageLink.href = '#';
    nextPageLink.setAttribute('aria-label', 'Next');
    nextPageLink.innerHTML = '&raquo;';
    nextPageLink.addEventListener('click', (e) => {
      e.preventDefault();
      if (currentPage < totalPages) {
        currentPage++;
        fetchAndDisplayPage(currentPage);
      }
    });
    nextPageItem.appendChild(nextPageLink);
    paginationDiv.appendChild(nextPageItem);
  }

  fetchAndDisplayPage(1); // 초기 페이지 로드
});



// JWT 토큰에서 사용자 권한 추출 함수 - auth 추출
function getUserAuthFromJwtToken() {
  const jwtToken = localStorage.getItem('jwtToken');
  if (!jwtToken) {
    return null;
  }
  const base64Url = jwtToken.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const payload = JSON.parse(window.atob(base64));
  return payload.auth; // 'auth' 클레임 추출
}