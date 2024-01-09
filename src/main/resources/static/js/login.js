//login.js

let isIdValid = false; // 유효성 검사 플래그 초기화
let isEmailValid = false; // 이메일 유효성 검사 플래그 초기화
let isPassWordValid = false; // 비밀번호 유효성 검사 플래그 초기화
let isAddressValid = false; // 주소 유효성 검사 플래그 초기화//
let isNameValid = false; // 이름 유효성 검사 플래그 초기화

let isIdChecked = false; // 아이디 중복확인 완료 여부
let isEmailChecked = false; // 이메일 중복확인 완료 여부


document.addEventListener("DOMContentLoaded", function() {
  const loginForm = document.getElementById('loginForm');


  //로그인에 대한 이벤트 리스너 등록
  loginForm.addEventListener('submit', function(event) {
    event.preventDefault();
    // 입력된 사용자 정보 가져오기
    const id = document.getElementById('uid').value;
    const password = document.getElementById('upw').value;

    fetch('/api/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        id,
        password
      })
    })
    .then(response => {
      if (response.ok) {
        return response.json();
      } else {
        return response.text().then(text => {
          // 서버에서 검증한 예외 메시지를 모달에 표시
          document.getElementById('loginErrorMessage').textContent = text;
          // 순수 JavaScript로 모달 활성화
          let loginErrorModal = new bootstrap.Modal(document.getElementById('loginErrorModal'), {});
          loginErrorModal.show();
          throw new Error(text);
        });
      }
    })
    .then(data => {
      // 로그인 성공 시 JWT 토큰을 로컬 스토리지에 저장
      if (data.token) {
        localStorage.setItem('jwtToken', data.token);

        // 동적으로 폼 생성 및 제출하여 페이지 리디렉션
        const form = document.createElement('form');
        form.method = 'GET';
        form.action = '/view/board.html';
        document.body.appendChild(form);
        form.submit();
      } else {
        alert('로그인 실패: ' + data.message);
      }
    })
    .catch(error => {
      console.error('Error:', error);
      //alert(error.message);
    });
  });


//***************************************** 회원가입 ******************************************************

  // 회원가입 버튼 클릭 시 모달 띄우기
  document.getElementById('joinModal').addEventListener('click', function() {
    joinModal.show();
  });

  // 회원가입 모달화면에서 esc클릭 혹은 모달을 벗어난 구역에 마우스 클릭시 모달팝업 닫히지않게하기
  let joinModal = new bootstrap.Modal(document.getElementById('joinModal'), {
    backdrop: false, // 모달 외부 클릭 시 닫히지 않음
    keyboard: false  // ESC 키로 닫히지 않음
  });

  //***************************** 이벤트 리스너 모음 ******************************************


  //blur 이벤트: HTML 요소가 포커스를 잃었을 때 발생하는 이벤트
  // 비밀번호 입력란 필드에 대한 blur 이벤트 리스너 추가
  document.getElementById('joinPassword').addEventListener('blur', validatePassword);
  // 이름 입력란에 대한 'blur' 이벤트 리스너: 입력란에서 벗어났을 때 유효성 검사를 수행
  document.getElementById('joinName').addEventListener('blur', validateName);

  // 주소 입력란에 'input' 이벤트 리스너 추가 - 주소 input에 다시 커서를 놓으면 '유효성' 안내 문구 사라지게함
  document.getElementById('joinAddress').addEventListener('input', validateKoreanAddress);

  // 이름 입력란에 대한 'input' 이벤트 리스너: 입력 중에는 유효성 검사 메시지를 숨깁
  document.getElementById('joinName').addEventListener('input', function() {
    const nameError = document.getElementById('nameError');
    nameError.style.display = 'none';
  });


  // 모달 숨김 이벤트 리스너 추가 - 유효성 검사 도중 화면을 [닫기] 후 다시 [회원가입] 버튼을 클릭했을때 경고 텍스트 초기화 하는 목적
  document.getElementById('joinModal').addEventListener('hidden.bs.modal', function () {
    // 유효성 검사 메시지 숨기기
    document.getElementById('idError').style.display = 'none';
    document.getElementById('emailError').style.display = 'none';
    document.getElementById('passwordError').style.display = 'none';
    document.getElementById('addressError').style.display = 'none';
    document.getElementById('nameError').style.display = 'none';
    // 입력 필드 초기화
    document.getElementById('joinId').value = '';
    document.getElementById('joinPassword').value = '';
    document.getElementById('joinEmail').value = '';
    document.getElementById('joinPhone').value = '';
    document.getElementById('joinAddress').value = '';
    document.getElementById('joinDetailAddress').value = '';
    document.getElementById('joinName').value = '';
  });



  // [닫기] 버튼 클릭 이벤트 리스너 등록 모음
  //회원가입 모달 닫기 버튼
  document.getElementById('closeJoinButton').addEventListener('click', closeJoinModal);
  //아이디 중복확인 버튼 ->  '이미 사용된 아이디입니다' 모달팝업에서의 [닫기] 버튼
  document.getElementById('closeJoinButton2').addEventListener('click', alertModal);

  //전화번호 입력시 자동으로 하이픈 들어가지게 하기
  document.getElementById('joinPhone').addEventListener('input', function(e) {
    let x = e.target.value.replace(/\D/g, '').match(/(\d{0,3})(\d{0,4})(\d{0,4})/);
    e.target.value = !x[2] ? x[1] : `${x[1]}-${x[2]}` + (x[3] ? `-${x[3]}` : '');
  });
  //****************************************************************************************

  // [가입하기] 버튼 클릭하여 서버로 폼 제출
  document.getElementById('joinForm').addEventListener('submit', function(event) {
    event.preventDefault();
    closeJoinModal();
    submitJoinForm();
  });


//**************************************** 유효성 검사 및 검증 *******************************************************


  //************** 아이디, 이메일 [중복확인] 버튼 클릭시 로직 *************************
  // 1) ID input
    // 아이디 입력 필드에 대한 유효성 검사 이벤트 리스너 추가
    document.getElementById('joinId').addEventListener('blur', function() {
      validateId();
    });

    // 아이디 [중복확인] 버튼 이벤트 리스너
    document.getElementById('checkDuplicateIdButton').addEventListener('click', function() {
      const modal = new bootstrap.Modal(document.getElementById('alertModal'));
      const modalBody = document.querySelector('#alertModal .modal-body');
      const userId = document.getElementById('joinId').value;
   
      //ID 입력을 안하고 [중복확인] 버튼을 눌렀을 경우
      if(userId === null || userId === undefined || userId.trim() === '')
      {
        //alert("아이디를 입력해주세요");
        modalBody.textContent = '아이디를 입력해주세요';
        modal.show();
        return;
      }

      //유효성을 통과하지 못할경우 ID [중복확인] 버튼클릭 로직을 타지 않고 리턴시킨다
      if (!isIdValid) {
        //alert('유효한 아이디를 입력해주세요.');
        return;
      }

      // 서버로 중복 확인 요청
      fetch(`/api/check-duplicate-id?id=${encodeURIComponent(userId)}`)
      .then(response => response.json())
      .then(isDuplicate => {
        if (isDuplicate === true) {
          modalBody.textContent = '이미 사용 중인 아이디입니다.';
          document.getElementById('joinId').focus();
        } else if (isDuplicate === false) {
          modalBody.textContent = '사용 가능한 아이디입니다.';
        } else {
          console.error('Unexpected server response:', isDuplicate);
        }
        modal.show();
      })
      .catch(error => {
        console.error('Error:', error);
        alert('중복 확인 중 오류가 발생했습니다.');
      });

      isIdChecked = true; // 아이디 중복확인 완료

    });



  // 2) 이메일 input
    // 이메일 입력 필드에 대한 유효성 검사 이벤트 리스너 추가
    document.getElementById('joinEmail').addEventListener('blur', validateEmail);

    // 이메일 [중복확인] 버튼 이벤트 리스너
    document.getElementById('checkDuplicateEmailButton').addEventListener('click', function() {
      const modal = new bootstrap.Modal(document.getElementById('alertModal'));
      const modalBody = document.querySelector('#alertModal .modal-body');
      const userEmail = document.getElementById('joinEmail').value;

      //이메일 입력을 안하고 [중복확인] 버튼을 눌렀을 경우
      if(userEmail === null || userEmail === undefined || userEmail.trim() === '')
      {
        modalBody.textContent = '이메일을 입력해주세요';
        modal.show();
        return;
      }

      //유효성을 통과하지 못할경우 이메일 [중복확인] 버튼클릭 로직을 타지 않고 리턴시킨다
      if (!isEmailValid) {
        //alert('유효한 이메일을 입력해주세요.');
        return;
      }

      // 서버로 중복 확인 요청
      fetch(`/api/check-duplicate-email?email=${encodeURIComponent(userEmail)}`)
      .then(response => response.json())
      .then(isDuplicate => {
        if (isDuplicate === true) {
          modalBody.textContent = '이미 사용 중인 이메일입니다.';
          document.getElementById('joinEmail').focus();
        } else if (isDuplicate === false) {
          modalBody.textContent = '사용 가능한 이메일입니다.';
        } else {
          console.error('Unexpected server response:', isDuplicate);
        }
        modal.show();
      })
      .catch(error => {
        console.error('Error:', error);
        alert('중복 확인 중 오류가 발생했습니다.');
      });

      isEmailChecked = true; // 이메일 중복확인 완료

    });

  // 주소 찾기 버튼 이벤트 리스너
    document.getElementById('addressSearch').addEventListener('click', function() {
      new daum.Postcode({
        oncomplete: function(data) {
          // 주소 선택 시 실행되는 콜백 함수
          // 예: 사용자가 주소를 선택하면, 해당 주소를 입력 필드에 자동으로 채워넣음
          document.getElementById('joinAddress').value = data.address;
        }
      }).open();
    });
});



//******************* 함수 ******************************


// 로그인 실패시 팝업 - 아이디나 비밀번호 틀려서 띄우는 모달팝업에서 [x] 버튼, [닫기] 버튼 클릭시 팝업 끄게하기
function closeLoginModal() {
  let loginErrorModal = bootstrap.Modal.getInstance(document.getElementById('loginErrorModal'));
  loginErrorModal.hide();
}

// 회원가입 모달 팝업 기능 구현
function submitJoinForm() {

  //주소와 상세주소 빈값 검증하기 위한 식별자
  let addressVal = document.getElementById('joinAddress').value;
  let detailAddress = document.getElementById('joinDetailAddress').value;
  
  // 검증 모달 팝업
  let modal = new bootstrap.Modal(document.getElementById('alertModal'));
  let modalBody = document.querySelector('#alertModal .modal-body');




  // 유효성 검사
  if (!isIdChecked) {
    //alert('ID 중복확인을 해주세요');
    modalBody.textContent = 'ID 중복확인을 해주세요';
    modal.show();
    document.getElementById('joinId').focus();
    return;
  }

  if (!validateName()) {
    document.getElementById('joinAddress').focus();
    return;
  }

  if (!validatePassword()) {
    //alert('비밀번호 형식을 확인해주세요');
    modalBody.textContent = '비밀번호 형식을 확인해주세요';
    modal.show();
    document.getElementById('joinPassword').focus();
    return;
  }

  if (!isEmailChecked) {
    //alert('이메일 중복확인을 해주세요');
    modalBody.textContent = '이메일 중복확인을 해주세요';
    modal.show();
    document.getElementById('joinEmail').focus();
    return;
  }
  // 주소 입력 검증
  if (!validateKoreanAddress()) {
    //alert('주소를 입력해주세요');
    modalBody.textContent = '주소를 입력해주세요';
    modal.show();
    document.getElementById('joinAddress').focus();
    return;
  }

  if (!detailAddress) {
    //alert('상세 주소를 입력해주세요');
    modalBody.textContent = '상세 주소를 입력해주세요';
    modal.show();
    document.getElementById('joinDetailAddress').focus();
    return;
  }


  // 폼 데이터 수집
  let id = document.getElementById('joinId').value;
  let password = document.getElementById('joinPassword').value;
  let email = document.getElementById('joinEmail').value;
  let phone = document.getElementById('joinPhone').value;
  let address = document.getElementById('joinAddress').value;
  let name = document.getElementById('joinName').value;

  // API 요청 (예시 URL 및 메소드)
  fetch('/api/creatuser', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      id: id,
      password: password,
      email: email,
      phone: phone,
      address: address,
      auth : '0',
      name: name
    })
  })
  .then(response => {
    if (response.ok) {
      return response.json();
    } else {
      throw new Error('회원가입 실패');
    }
  })
  .then(data => {
    console.log("회원가입성공! data : " + data);
    // 회원가입 성공 처리
    //alert('회원가입 성공!');
    modalBody.textContent = '회원가입 성공!';
    modal.show();
    // 회원가입 모달 닫기
    closeJoinModal();
  })
  .catch(error => {
    alert(error.message);
  });
}


// 회원가입 모달을 닫는 함수
function closeJoinModal() {
  let joinModal = bootstrap.Modal.getInstance(document.getElementById('joinModal'));

    joinModal.hide();

}

//ID [중복확인] 버튼 -> ['이미사용중인 아이디입니다'] 팝업 -> 닫기 버튼 함수
function alertModal(){
  let alertModal = bootstrap.Modal.getInstance(document.getElementById('alertModal'));
  alertModal.hide();
}

//[회원가입] - ID 유효성검사
function validateId() {
  let id = document.getElementById('joinId').value;

  if (!isIdChecked) {
    if (id.length === 0) {
      // 아이디 필드가 비어있으면 경고 메시지를 표시하지 않음
      document.getElementById('idError').style.display = 'none';
    } else if (!/^[A-Za-z][A-Za-z0-9_-]{3,11}$/.test(id)) {
      // 유효하지 않은 아이디 형식이면 경고 메시지 표시
      document.getElementById('idError').style.display = 'inline';
      isIdValid = false;
    } else {
      // 유효한 형식이면 경고 메시지 숨김
      document.getElementById('idError').style.display = 'none';
      isIdValid = true;
    }
  }
}


//[회원가입] - 이메일 유효성검사
function validateEmail() {
  const email = document.getElementById('joinEmail').value;
  const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

  if (email.length === 0) {
    document.getElementById('emailError').style.display = 'none';
  } else if (!emailRegex.test(email)) {
    document.getElementById('emailError').style.display = 'inline';
    isEmailValid = false;
  } else {
    document.getElementById('emailError').style.display = 'none';
    isEmailValid = true;
  }
}

// [회원가입] - 비밀번호 유효성 검사
function validatePassword() {
  const password = document.getElementById('joinPassword').value;
  const passwordError = document.getElementById('passwordError');

  if (password.length === 0) {
    passwordError.style.display = 'none';
  } else if (password.length < 8 || !/[a-zA-Z]/.test(password) || !/\d/.test(password)) {
    passwordError.style.display = 'inline';
    isPassWordValid = false;
  } else {
    passwordError.style.display = 'none';
    isPassWordValid = true;
  }

  return isPassWordValid;
}

// [회원가입] - 주소 유효성 검사
function validateKoreanAddress() {
  const address = document.getElementById('joinAddress').value;
  const addressError = document.getElementById('addressError');
  // 대한민국 주소 형식에 맞는 정규 표현식
  const addressRegex = /^[가-힣0-9\s-]+$/;

  if (address.length === 0) {
    // 주소 입력이 없는 경우, 메시지를 표시하지 않음
    addressError.style.display = 'none';
  } else if (!addressRegex.test(address)) {
    // 주소 형식이 올바르지 않은 경우
    addressError.style.display = 'inline';
    isAddressValid = false;
  } else {
    // 주소 형식이 올바른 경우
    addressError.style.display = 'none';
    isAddressValid = true;
  }
  return isAddressValid;
}

// [회원가입] - 이름 유효성 검사
function validateName() {
  const name = document.getElementById('joinName').value;
  const nameError = document.getElementById('nameError');
  const nameRegex = /^[가-힣a-zA-Z]+$/;

  if (!nameRegex.test(name) && name.length > 0) {
    // 이름이 정규식에 맞지 않는 경우
    nameError.style.display = 'inline';
    isNameValid = false;
  } else {
    // 이름이 정규식에 맞는 경우
    nameError.style.display = 'none';
    isNameValid = true;
  }
  return isNameValid;
}
