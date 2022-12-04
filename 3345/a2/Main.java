import java.util.ArrayList;

public class Main {

	private static void test311LinkedList() {
		A311<Integer> list = new A311<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);

		System.out.println("size is: " + list.size());
		list.print();
		System.out.println();
		System.out.println("list contains 3: " + list.contains(3));
		System.out.println("removed 2");
		list.remove(2);
		list.print();
		System.out.println();
	}

	private static void test321Balanced() {
		String test1 = "/*(())()*/[]";
		String test2 = "(])[]";

		String b1 = A321.isBalanced(test1) ? ": balanced" : ": unbalanced";
		String b2 = A321.isBalanced(test2) ? ": balanced" : ": unbalanced";
		System.out.println(test1 + b1);
		System.out.println(test2 + b2);
	}
	
	private static void test333Queue() {
		A333 queue = new A333();
		queue.add(1);
		queue.add(2);
		queue.add(3);
		queue.poll();
		queue.poll();
		queue.add(5);
		queue.add(6);
		System.out.println("should be: 356");
		System.out.println("polled: "  + queue.poll() + queue.poll() + queue.poll());
	}
	
	private static void test426Avl() {
		TreeNode root = new TreeNode(2);
		TreeNode l = new TreeNode(1);
		TreeNode r = new TreeNode(3);
		TreeNode r2 = new TreeNode(5);
		TreeNode r2l = new TreeNode(4);
		root.right = r;
		root.left = l;
		r.right = r2;
		r2.left = r2l;
		System.out.print("pre-order of tree prior of rotate: ");
		printPreOrder(root);
		root.right = A4.rightLeftRotate(root.right);

		System.out.print("\npre-order of tree after right-left rotate on 3: ");
		printPreOrder(root);
		System.out.println();
	}

	private static void printPreOrder(TreeNode node) {
		if (node == null) {
			return;
		}

		System.out.print(node.val + " ");
		printPreOrder(node.left);
		printPreOrder(node.right);
	}
	
	private static void test434GenTree() {
		System.out.print("pre-order of tree: ");
		TreeNode root = A4.generateTree(10);
		printPreOrder(root);
		System.out.println();
	}

	private static void test53HashCollisions() {
		int linearCollisions = 0;
		A53 map = new A53();
		for (int i = 0; i < 5000; i++) {
			linearCollisions += map.insertLinear((int)(Math.random() * 10000));
		}

		int quadCollisions = 0;
		map = new A53();
		for (int i = 0; i < 5000; i++) {
			quadCollisions += map.insertQuadratic((int)(Math.random() * 10000));
		}

		int hashCollisions = 0;
		map = new A53();
		for (int i = 0; i < 5000; i++) {
			hashCollisions += map.insertDoubleHash((int)(Math.random() * 10000));
		}

		System.out.println("linear: " + linearCollisions);
		System.out.println("quadratic: " + quadCollisions);
		System.out.println("double hash: " + hashCollisions);
	}

	public static void test55HashTable() {
		A55 ht = new A55();
		ht.insert(5, 3);
		ht.insert(15, 5);
		ht.insert(16, 4);
		ht.insert(22, 5);

		System.out.println("result should be: 3545");
		System.out.println("result: " + ht.get(5) + ht.get(15) + ht.get(16) + ht.get(22));
	}

	public static void main(String[] args) {
		System.out.println("Test 3.11");
		test311LinkedList();
		System.out.println();
		
		System.out.println("Test 3.21");
		test321Balanced();
		System.out.println();

		System.out.println("Test 3.33");
		test333Queue();
		System.out.println();

		System.out.println("Test 4.26");
		test426Avl();
		System.out.println();

		System.out.println("Test 4.34");
		test434GenTree();
		System.out.println();

		System.out.println("Test 5.3");
		test53HashCollisions();
		System.out.println();
		
		System.out.println("Test 5.5");
		test55HashTable();
		System.out.println();

	}
}
