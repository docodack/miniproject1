package miniproject1;

import lombok.Data;

@Data
public class Member {
	private String ID;
	private String password;
	private String name;
	private String callNum;
	private String address;
	private String gender;
	private String user_role;
	private String signUp_date;

	@Override
	public String toString() {
		String str = "아이디: "+ID+"\n이름: "+name+"\n전화번호: "+callNum
				+"\n주소: "+address+"\n성별: "+gender+"\n가입일: "+signUp_date;
		return str;
	}
}