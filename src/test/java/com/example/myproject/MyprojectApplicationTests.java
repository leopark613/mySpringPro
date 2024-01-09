package com.example.myproject;

import com.example.myproject.entity.Board;
import com.example.myproject.repository.BoardRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MyprojectApplicationTests {

	@Test
	void contextLoads() {
	}


	@Autowired
	BoardRepository boardRepository;

	@Test
	void findAllMember() {
		//memberRepository.findAll();
		List<Board> board = boardRepository.findAll();


		// members를 활용하여 테스트 검증을 수행하거나 로깅할 수 있음
		Assertions.assertNotNull(board); // 예시로 members가 null이 아닌지 검증하는 코드 추가
	}
	@Test
	void testSearchActiveBoards() {
		// 'title'로 검색
		List<Board> searchResultsTitle = boardRepository.findByTitleContainingAndUseYn("하이", "Y");
		Assertions.assertNotNull(searchResultsTitle);
		Assertions.assertFalse(searchResultsTitle.isEmpty());

		// 'content'로 검색
		List<Board> searchResultsContent = boardRepository.findByContentContainingAndUseYn("검색어", "Y");
		Assertions.assertNotNull(searchResultsContent);
		Assertions.assertFalse(searchResultsContent.isEmpty());

		// 'id'로 검색
		List<Board> searchResultsId = boardRepository.findByIdContainingAndUseYn("검색어", "Y");
		Assertions.assertNotNull(searchResultsId);
		Assertions.assertFalse(searchResultsId.isEmpty());
	}
}
