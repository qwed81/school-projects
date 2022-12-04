class TreeNode {
	int val;
	TreeNode left;
	TreeNode right;

	TreeNode(int val) {
		this.val = val;
	}
		
}

public class A4 {

	public static TreeNode rightLeftRotate(TreeNode parent) {
		// represented in post-rotation order
		TreeNode top = parent.right.left;
		TreeNode left = parent;
		TreeNode right = parent.right;
		
		// left.left stays the same
		// but right changes
		left.right = top.left;

		// right.right stays the same
		// but left changes
		right.left = top.right;

		// now set the top's pointers
		top.left = left;
		top.right = right;

		// return back so it can re-link to a new node
		return top;
	}

	// miror of the right-left
	public static TreeNode leftRightRotate(TreeNode parent) {
		TreeNode top = parent.left.right;
		TreeNode right = parent;
		TreeNode left = parent.left;

		right.left = top.right;
		left.right = top.left;

		top.left = left;
		top.right = right;

		return top;
	}

	public static TreeNode generateTree(int n) {
		if (n == 0) {
			return null;
		}

		boolean[] used = new boolean[n + 1];
		TreeNode head = new TreeNode((int) Math.ceil(Math.random() * n));
		used[head.val] = true;

		int amtUsed = 1;
		while (amtUsed != n) {
			// keep rolling until a unique value is there
			int i = (int) Math.ceil(Math.random() * n);
			if (used[i]) {
				continue;
			}

			// insert the unique value into the tree
			TreeNode current = head;
			TreeNode prev = null; // it's always initialized, just need to tell compiler
			while (current != null) {
				prev = current;
				if (i < current.val) {
					current = current.left;
				}
				else {
					current = current.right;
				}
			}

			// prev is now a leaf node
			if (i < prev.val) {
				prev.left = new TreeNode(i);
			}
			else {
				prev.right = new TreeNode(i);
			}
			
			// handle computing the used values
			used[i] = true;
			amtUsed += 1;
		}

		return head;
		
	}


}
