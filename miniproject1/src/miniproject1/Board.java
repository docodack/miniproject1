package miniproject1;

import java.util.Date;
import lombok.Data;

@Data
public class Board {
	private final int bno;
	private String writer;
	private String title;
	private String content;
	private Date bdate;
	private int hits;
	private String password;
}