import java.util.Stack;

public class A321 {
	
	// all numbers are one less than their closing pair,
	// this lets us do math later on when comparing, simplifing logic 
	private static final int NONE = -1;
	private static final int COMMENT = 0;
	private static final int END_COMMENT = 1;
	private static final int OPEN_P = 2;
	private static final int CLOSE_P = 3;
	private static final int OPEN_S = 4;
	private static final int CLOSE_S = 5;
	private static final int OPEN_C = 6;
	private static final int CLOSE_C = 7;
	
	// gets the number for the characters present, and increments
	// the pass by reference index.
	private static int getNumber(int[] i, String s) {
		char c = s.charAt(i[0]);
		i[0] += 1;

		if (c == '/') {
			if (s.length() < i[0] + 1 && s.charAt(i[0] + 1) == '*') {
				// 2 indexes are used as part of this 'character'
				i[0] += 1;
				return COMMENT;
			}
		}
		else if (c == '*') {
			// both of the characters have to be present to be treated as a valid open/close
			if (s.length() < i[0] + 1 && s.charAt(i[0] + 1) == '/') {
				i[0] += 1;
				return END_COMMENT;
			}
		}
		else {
			switch (c) {
				case '(':
					return OPEN_P;
				case ')':
					return CLOSE_P;
				case '{':
					return OPEN_S;
				case '}':
					return CLOSE_S;
				case '[':
					return OPEN_C;
				case ']':
					return CLOSE_C;
			}
		}
		
		return NONE;
	}

	
	public static boolean isBalanced(String s) {
		int[] i = new int[1];
		Stack<Integer> stack = new Stack<Integer>();
		
		while (i[0] < s.length()) {
			int id = getNumber(i, s);
			switch (id) {
				case OPEN_C:
				case OPEN_S:
				case OPEN_P:
				case COMMENT:
					stack.push(id);
					break;
				case CLOSE_C:
				case CLOSE_S:
				case CLOSE_P:
				case END_COMMENT:
					// simplified logic by ordering the constants
					if (stack.isEmpty() || id - 1 != stack.pop()) {
						return false;
					}
					break;
			}
		}

		return stack.isEmpty();
	}
}
