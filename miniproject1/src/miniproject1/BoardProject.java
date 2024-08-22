package miniproject1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BoardProject {
	private static Scanner sc = new Scanner(System.in);
	private static Connection conn;
	private static Member member = new Member();
	private static  List<Board> boardList = new ArrayList<>();

	public BoardProject() {
		try {
			Class.forName("oracle.jdbc.OracleDriver");

			conn = DriverManager.getConnection(
					"jdbc:oracle:thin:@localhost:1521/xe", 
					"user01", 
					"1004"
					);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean mainMenu() throws Exception {
		System.out.println("---------------------");
		System.out.println("<메인 메뉴>");
		System.out.println("1. 회원가입");
		System.out.println("2. 로그인");
		System.out.println("3. 아이디 찾기");
		System.out.println("4. 비밀번호 초기화");
		System.out.println("5. 종료");
		String input = getInput("메뉴 선택");
		switch (input) {
		case "1":
			register();
			return true;
		case "2":
			loginMenu();
			return true;
		case "3":
			findID();
			return true;
		case "4":
			initPW();
			return true;
		case "5":
			System.out.println("프로그램 종료");
			System.out.close();
			conn.close();
			System.exit(0);
		default :
			System.out.println("메뉴를 잘못 입력하셨습니다");
			return true;
		}
	}

	public static void register() {
		System.out.println("---------------------");
		System.out.println("회원 가입화면");

		String id       = getUniqueId();
		String password = getInput("비밀번호");
		String name     = getInput("이름");
		String callNum  = getInput("전화번호");
		String address  = getInput("주소");
		String gender   = getInput("성별");

		while (true) {
			System.out.println("---------------------");
			System.out.println("1. 가입\n2. 다시입력\n3. 이전 화면으로");
			String input = getInput("메뉴 선택");

			switch (input) {
			case "1":
				if (insertMember(id, password, name, callNum, address, gender)) {
					System.out.println("가입을 축하합니다.");
					return;
				} else {
					System.out.println("가입에 실패했습니다. 다시 시도해주세요.");
				}
				break;
			case "2":
				register();
			case "3":
				System.out.println("이전 화면으로 돌아갑니다.");
				return;
			default:
				System.out.println("잘못된 입력입니다.");
			}
		}
	}

	private static String getUniqueId() {
		while (true) {
			String id = getInput("아이디");
			try {
				if (!checkID(id)) {
					return id;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("아이디가 이미 존재합니다.");
		}
	}

	private static String getInput(String str) {
		System.out.print(str + ": ");
		return sc.nextLine();
	}

	private static boolean insertMember(String id, String password, String name, String callNum, String address, String gender) {
		String sql = "INSERT INTO MEMBER(ID, password, name, callNum, address, gender, login, logout, signUp_date, withdrawal) " +
				"VALUES (?, ?, ?, ?, ?, ?, null, null, SYSDATE, 0)";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			pstmt.setString(2, password);
			pstmt.setString(3, name);
			pstmt.setString(4, callNum);
			pstmt.setString(5, address);
			pstmt.setString(6, gender);

			return pstmt.executeUpdate() >= 1;
		} catch (Exception e) {
			System.out.println("데이터베이스 오류");
			return false;
		}
	}

	public static boolean checkID(String ID) throws Exception {
		String sql = "SELECT ID FROM MEMBER WHERE ID = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, ID);
		ResultSet rs = pstmt.executeQuery();

		if(rs.next()) {
			rs.close();
			return true;
		} else {
			return false;
		}
	}

	public static boolean checkPW(String password) throws SQLException {
		String sql = "SELECT * FROM MEMBER WHERE ID = ? AND PASSWORD = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, member.getID());
		pstmt.setString(2, password);
		ResultSet rs = pstmt.executeQuery();

		if(rs.next()) {
			rs.close();
			return true;
		} else {
			return false;
		}
	}

	public static void loginMenu() throws Exception {

		System.out.println("---------------------");
		System.out.println("로그인 화면");
		String ID = getInput("아이디");
		String password = getInput("비밀번호");

		while(true) {
			System.out.println("---------------------");
			System.out.println("1. 로그인\n2. 다시입력\n3. 이전 화면으로");
			String input = getInput("메뉴 선택");

			switch(input) {
			case "1" : 
				login(ID, password);
				return;
			case "2" : 
				System.out.println("다시 입력해주세요.");
				loginMenu();
				break;
			case "3" : 
				System.out.println("이전 화면으로 돌아갑니다.");
				return;
			default : 
				System.out.println("잘못된 입력입니다.");
				break;
			}
		}
	}

	public static void login(String ID, String password) throws Exception {
		boolean isLogin;
		if(checkMember(ID, password) != null) {
			isLogin = true;
			logUserAction(ID, isLogin);
			System.out.println(ID + " 계정으로 로그인되었습니다.");
			while(myPage());
		} else if(checkAdmin(ID, password) != null) {
			isLogin = true;
			logUserAction(ID, isLogin);
			System.out.println(ID + " 관리자 계정으로 로그인되었습니다.");
			while(adminPage());
		} else {
			System.out.println("아이디 또는 비밀번호가 틀렸습니다.");
			loginMenu();
		}
	}

	public static Member checkMember(String ID, String password) throws Exception {
		String sql = "SELECT * FROM MEMBER WHERE ID = ? AND PASSWORD = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, ID);
		pstmt.setString(2, password);
		ResultSet rs = pstmt.executeQuery();

		if(rs.next() && rs.getInt("WITHDRAWAL") == 0) {
			member.setID(rs.getString("ID"));
			member.setPassword(rs.getString("PASSWORD"));
			member.setName(rs.getString("NAME"));
			member.setCallNum(rs.getString("CALLNUM"));
			member.setAddress(rs.getString("ADDRESS"));
			member.setGender(rs.getString("GENDER"));
			member.setUser_role("Member");
			member.setSignUp_date(rs.getString("SIGNUP_DATE"));
			return member;
		} else {
			return null;
		}
	}

	public static Member checkAdmin(String ID, String password) throws Exception{
		String sql = "SELECT * FROM ADMIN WHERE ID = ? AND PASSWORD = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, ID);
		pstmt.setString(2, password);
		ResultSet rs = pstmt.executeQuery();

		if(rs.next() && rs.getInt("WITHDRAWAL") == 0) {
			member.setID(rs.getString("ID"));
			member.setPassword(rs.getString("PASSWORD"));
			member.setName(rs.getString("NAME"));
			member.setCallNum(rs.getString("CALLNUM"));
			member.setAddress(rs.getString("ADDRESS"));
			member.setGender(rs.getString("GENDER"));
			member.setUser_role("Admin");
			member.setSignUp_date(rs.getString("SIGNUP_DATE"));
			return member;
		} else {
			return null;
		}
	}

	public static boolean logUserAction(String ID, boolean isLogin) {
		String sql = "INSERT INTO LOG (ID, USER_ROLE, LOGIN, LOGOUT) VALUES (?, ?, ?, ?)";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, ID);
			pstmt.setString(2, member.getUser_role());
			if (isLogin) {
				pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				pstmt.setNull(4, java.sql.Types.TIMESTAMP);
			} else {
				pstmt.setNull(3, java.sql.Types.TIMESTAMP);
				pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			}
			int rows = pstmt.executeUpdate();
			return rows >=1;
		} catch (SQLException e) {
			System.out.println("로그를 남기는 도중에 오류가 발생했습니다.");
			return false;
		}
	}

	public static boolean myPage() throws Exception {
		System.out.println("---------------------");
		System.out.println("1. 나의 정보확인");
		System.out.println("2. 게시물 목록");
		System.out.println("3. 로그아웃");
		System.out.println("4. 종료");
		String input = getInput("메뉴 선택");

		switch(input) {
		case "1" : 
			myInfo();
			return true;
		case "2" : 
			list();
			return true;
		case "3" : 
			boolean isLogin = false;
			logUserAction(member.getID(), isLogin);
			member = new Member();
			System.out.println("로그아웃 되었습니다.");
			return false;
		case "4" : 
			isLogin = false;
			logUserAction(member.getID(), isLogin);
			System.out.println("프로그램 종료");
			System.out.close();
			conn.close();
			System.exit(0);
		default :
			System.out.println("메뉴를 잘못 입력하셨습니다");
			return true;
		}
	}

	public static boolean adminPage() {
		System.out.println("---------------------");
		System.out.println("1. 나의 정보확인");
		System.out.println("2. 게시물 목록");
		System.out.println("3. 회원 목록");
		System.out.println("4. 로그아웃");
		System.out.println("5. 종료");
		String input = getInput("메뉴 선택");

		return false;
	}

	public static void myInfo() throws Exception {
		System.out.println("---------------------");
		System.out.println(member);
		while(myInfoMenu());
	}

	public static boolean myInfoMenu() throws Exception {
		System.out.println("---------------------");
		System.out.println("1. 회원 정보수정\n2. 회원 탈퇴\n3. 이전 화면으로");
		String input = getInput("메뉴 선택");

		switch(input) {
		case "1" :
			String password = getInput("비밀번호");
			if(checkPW(password)) {
				while(updateUserInfoMenu());
			} else {
				System.out.println("비밀번호가 틀립니다.");
			}
			return true;
		case "2" :
			deleteUserAccount();
			boolean isLogin = false;
			logUserAction(member.getID(), isLogin);
			member = new Member();
			System.out.println("회원 탈퇴가 되었습니다.");
			mainMenu();
		case "3" :
			System.out.println("이전 화면으로 돌아갑니다.");
			return false;
		default : 
			System.out.println("메뉴를 잘못 입력하셨습니다");
			return true;
		}
	}

	public static boolean updateUserInfoMenu() {
		System.out.println("---------------------");
		System.out.println("수정을 원하시는 정보를 선택해주세요.");
		System.out.println("1. 이름\n2. 비밀번호\n3. 전화번호\n4. 주소\n5. 성별\n6. 이전 화면으로");
		String input = getInput("메뉴 선택");
		String info = null;

		switch(input){
		case "1" : 
			info = "NAME";
			input = getInput("새로운 이름");
			member.setName(input);
			break;
		case "2" : 
			info = "PASSWORD";
			input = getInput("새로운 비밀번호");
			member.setPassword(input);
			break;
		case "3" : 
			info = "CALLNUM";
			input = getInput("새로운 전화번호");
			member.setCallNum(input);
			break;
		case "4" : 
			info = "ADDRESS";
			input = getInput("새로운 주소");
			member.setAddress(input);
			break;
		case "5" : 
			info = "GENDER";
			input = getInput("새로운 성별");
			member.setGender(input);
			break;
		case "6" :
			System.out.println("이전 화면으로 돌아갑니다.");
			return false;
		default :
			System.out.println("메뉴를 잘못 입력하셨습니다");
			return true;
		}
		updateInfo(info, input);
		return true;
	}

	public static void updateInfo(String info, String input) {
		String sql = "UPDATE  MEMBER SET "+info+" = ? WHERE ID = ?";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, input);
			pstmt.setString(2, member.getID());
			int rows = pstmt.executeUpdate();
			System.out.println("회원 정보수정이 완료되었습니다.");
			if(rows == 0) 
				System.out.println("회원 정보수정하는 도중에 오류가 발생했습니다.");
		} catch (SQLException e) {
			System.out.println("회원 정보수정하는 도중에 오류가 발생했습니다.");
		}
	}

	public static void deleteUserAccount() {
		String sql = "UPDATE  MEMBER SET WITHDRAWAL = ? WHERE ID = ?";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, 1);
			pstmt.setString(2, member.getID());
			int rows = pstmt.executeUpdate();
			if(rows == 0) 
				System.out.println("회원 탈퇴하는 도중에 오류가 발생했습니다.");
		} catch (SQLException e) {
			System.out.println("회원 탈퇴하는 도중에 오류가 발생했습니다.");
		}
	}

	public static void list() {
		int pageNo = 1;
		System.out.println("---------------------");
		System.out.println("게시물 목록 화면");
		listBoard(pageNo);
		while(listMenu(pageNo) != 0) {
			pageNo = listMenu(pageNo);
		}
	}

	public static int listBoard(int pageNo) {
		int pageSize = 10;
		int offset = (pageNo - 1) * pageSize;
		int rows = 0;

		if (pageNo < 1) {
			return 0; 
		}
		String sql =
				"SELECT * FROM (" +
						"   SELECT b.*, ROW_NUMBER() OVER (ORDER BY BOARD_NUM) AS rn" +
						"   FROM BOARD b" +
						") WHERE rn BETWEEN ? AND ?" ;

		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, offset + 1);
			pstmt.setInt(2, offset + pageSize);
			ResultSet rs = pstmt.executeQuery();

			System.out.printf("%-7s | %-10s | %-20s | %-6s | %-10s\n",
					"게시물 번호", "작성자", "제목", "읽은수", "작성일");
			System.out.println("--------------------------------------------------------------------------");
			while (rs.next()) {
				++rows;
				Board board = new Board(rs.getInt("BOARD_NUM"));
				board.setWriter(rs.getString("WRITER"));
				board.setTitle(rs.getString("TITLE"));
				board.setContent(rs.getString("CONTENT"));
				board.setBdate(rs.getTimestamp("BDATE"));
				board.setHits(rs.getInt("HITS"));
				board.setPassword(rs.getString("PASSWORD"));
				boardList.add(board);

				System.out.printf("%-8d | %-10s | %-20s | %-6d | %-10s\n",
						rs.getInt("BOARD_NUM"), rs.getString("WRITER"), rs.getString("TITLE"),
						rs.getInt("HITS"), formatDate(rs.getTimestamp("BDATE")));
			}
		} catch (Exception e) {
			System.out.println("오류가 발생했습니다.");
			e.printStackTrace();
		}
		return rows;
	}

	public static int listMenu(int pageNo) {
		System.out.println("--------------------------------------------------------------------------");
		System.out.println("1. 이전 페이지\t2. 페이지 이동\t3. 다음 페이지");
		System.out.println("4. 상세 보기\t5. 게시물 등록\t6. 이전 화면으로");
		String input = getInput("메뉴 선택");

		switch(input) {
		case "1" : 
			pageNo -= 1;
			if(listBoard(pageNo) == 0) {
				System.out.println("이전 페이지가 없습니다.");
				pageNo += 1;
			}
			break;
		case "2" : 
			input = getInput("이동할 페이지");
			int newPageNo = Integer.parseInt(input);
			if (listBoard(newPageNo) != 0) {
				pageNo = newPageNo;
			} else {
				System.out.println(newPageNo + " 페이지가 없습니다.");
			}
			break;
		case "3" : 
			pageNo += 1;
			if(listBoard(pageNo) == 0) {
				System.out.println("다음 페이지가 없습니다.");
				pageNo -= 1;
			}
			break;
		case "4" : 
			detailView();
			return 1;
		case "5" : 
			registerBoard();
			return 1;
		case "6" : 
			System.out.println("이전 화면으로 돌아갑니다.");
			return 0;
		default : 
			System.out.println("메뉴를 잘못 입력하셨습니다");
			return pageNo;
		}
		return pageNo;
	}

	public static String formatDate(Timestamp tsp) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		LocalDateTime createdAt = tsp.toLocalDateTime();
		String formattedDate;
		if (ChronoUnit.HOURS.between(createdAt, now) < 24) {
			formattedDate = createdAt.format(timeFormatter);
		} else {
			formattedDate = createdAt.format(dateFormatter);
		}
		return formattedDate;
	}

	public static void detailView() {
		String input = getInput("게시물 번호");
		int inputNum = Integer.parseInt(input);

		Board foundBoard = null;
		for (Board board : boardList) {
			if (board.getBno() == inputNum) {
				foundBoard = board;
				foundBoard.setHits(foundBoard.getHits() + 1);
				updateBoardHits(foundBoard.getBno(), foundBoard.getHits());
				break;
			}
		}
		if (foundBoard != null) {
			System.out.println("게시물 번호: " + foundBoard.getBno());
			System.out.println("작성자: " + foundBoard.getWriter());
			System.out.println("제목: " + foundBoard.getTitle());
			System.out.println("내용: " + foundBoard.getContent());
			System.out.println("작성일: " + foundBoard.getBdate());
			System.out.println("조회수: " + foundBoard.getHits());

			while(detailViewMenu(foundBoard, foundBoard.getWriter().equals(member.getID())));
		} else {
			System.out.println("해당 게시물이 존재하지 않습니다.");
		}
	}

	private static void updateBoardHits(int bno, int hits) {
		String sql = "UPDATE BOARD SET HITS = ? WHERE BOARD_NUM = ?";

		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, hits);
			pstmt.setInt(2, bno);
			pstmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("게시물 조회수 업데이트 중 오류가 발생했습니다.");
		}
	}

	public static boolean detailViewMenu(Board foundBoard, boolean boardWriterCheck) {
		System.out.println("---------------------");
		System.out.println("1. 이전 화면으로");
		if (boardWriterCheck) {
			System.out.println("2. 게시물 수정");
			System.out.println("3. 게시물 삭제");
		}
		String input = getInput("메뉴 선택");

		switch (input) {
		case "1":
			System.out.println("이전 화면으로 돌아갑니다.");
			return false;
		case "2":
			input = getInput("게시물 비밀번호");
			if(input.equals(foundBoard.getPassword())) {
				updateBoard(foundBoard);
			} else {
				System.out.println("비밀번호가 틀립니다.");
			}
			return true;
		case "3":
			input = getInput("게시물 비밀번호");
			if(input.equals(foundBoard.getPassword())) {
				deleteBoard(foundBoard);
			} else {
				System.out.println("비밀번호가 틀립니다.");
			}
			return false;
		default:
			System.out.println("메뉴를 잘못 입력하셨습니다");
			return true;
		}
	}

	public static void updateBoard(Board foundBoard) {
		System.out.println("수정을 원하지 않는 항목은 Enter를 입력하세요.");
		String title = getInput("제목");
		String content = getInput("내용");
		String password = getInput("비밀번호");

		if (!title.equals("")) {
			foundBoard.setTitle(title);
		}
		if (!content.equals("")) {
			foundBoard.setContent(content);
		}
		if (!password.equals("")) {
			foundBoard.setPassword(password);
		}
		foundBoard.setBdate(Timestamp.valueOf(LocalDateTime.now()));
		updateBoardInDb(foundBoard);
		System.out.println("수정이 완료되었습니다.");
	}

	public static void updateBoardInDb(Board board) {
		String sql = "UPDATE BOARD SET TITLE = ?, CONTENT = ?, PASSWORD = ?, BDATE = ? WHERE BOARD_NUM = ?";

		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, board.getTitle());
			pstmt.setString(2, board.getContent());
			pstmt.setString(3, board.getPassword());
			pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			pstmt.setInt(5, board.getBno());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("게시물 수정 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}

	public static void deleteBoard(Board foundBoard) {
		boardList.remove(foundBoard);
		String sql = "DELETE FROM BOARD WHERE BOARD_NUM = ?";
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, foundBoard.getBno());
			pstmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("게시물 삭제 중 오류가 발생했습니다.");
		}
		System.out.println("게시물이 삭제되었습니다.");
	}

	public static void registerBoard() {
		System.out.println("게시물 등록 화면");
		String title = getInput("제목");
		String content = getInput("내용");
		String boardPassword = getInput("게시글 비밀번호");

		String sql = "INSERT INTO USER01.BOARD (BOARD_NUM, WRITER, TITLE, CONTENT, BDATE, HITS, PASSWORD) " +
				"VALUES (BOARD_SEQ.NEXTVAL, ?, ?, ?, SYSDATE, 0, ?)";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, member.getName());
			pstmt.setString(2, title);
			pstmt.setString(3, content);
			pstmt.setString(4, boardPassword);

			int rows = pstmt.executeUpdate();
			if (rows > 0) {
				System.out.println("게시글이 성공적으로 등록되었습니다.");
			} else {
				System.out.println("게시글 등록에 실패했습니다.");
			}
		} catch (Exception e) {
			System.out.println("게시글 등록 중 오류가 발생했습니다: ");
		}
	}

	public static void findID() throws Exception {
		System.out.println("---------------------");
		System.out.println("아이디 찾기 화면");
		String name = getInput("이름");
		String callNum = getInput("전화 번호");

		String sql = "SELECT ID FROM MEMBER WHERE NAME = ? AND CALLNUM = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, name);
		pstmt.setString(2, callNum);
		ResultSet rs = pstmt.executeQuery();

		boolean hasResults = false;
		while(rs.next()) {
			hasResults = true;
			System.out.println("아이디 : " + rs.getString("ID"));
		}
		if(!hasResults) {
			System.out.println("아이디를 찾을 수 없습니다.");
		}
		rs.close();
	}

	public static void initPW() throws Exception {
		System.out.println("---------------------");
		System.out.println("비밀번호 초기화 화면");
		String ID = getInput("아이디");
		String callNum = getInput("전화번호");

		String sql = "SELECT ID FROM MEMBER WHERE ID = ? AND CALLNUM = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, ID);
		pstmt.setString(2, callNum);
		ResultSet rs = pstmt.executeQuery();

		if(rs.next()) {
			String password = getInput("새로운 비밀번호");
			sql = "UPDATE MEMBER SET PASSWORD = ? WHERE ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, password);
			pstmt.setString(2, ID);
			int rows = pstmt.executeUpdate();
			if(rows >= 1) {
				System.out.println("비밀번호가 바뀌었습니다.");
			} else {
				System.out.println("비밀번호 변경에 실패했습니다.");
			}
		} else {
			System.out.println("아이디를 찾을 수 없습니다.");
		}
		rs.close();
	}

	public static void main(String[] args) {

		new BoardProject();
		System.out.println("-------------------------------------------");
		System.out.println("미니 프로젝트 1차");

		try {
			while(mainMenu());
		} catch (Exception e) {}
	}
}
