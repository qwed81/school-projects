public class A55 {
	private static final double THRESH_HOLD = 0.8;
	class ListNode {
		ListNode next;
		int val;
		int key;
	}

	ListNode[] nums;
	int size;

	public A55() {
		nums = new ListNode[1];
		size = 0;
	}
	
	int hashIndex(int key) {
		return key % nums.length;
	}


	// made to resize the array if it needs
	void checkResize() {
		if ((double)size / nums.length > THRESH_HOLD) {
			ListNode[] old = nums;
			nums = new ListNode[nums.length * 2];

			// go through all values, and all lists and remap them
			// with the new size of the hashtable
			for(int i = 0; i < old.length; i++) {
				while(old[i] != null) {
					insert(old[i].key, old[i].val);
					old[i] = old[i].next;
				}
			}
		}
	}

	public void insert(int key, int val) {
		// the key already exists, so just remap value
		ListNode exists = getNode(key);
		if (exists != null) {
			exists.val = val;
			return;
		}

		// insert the new node at the front
		// because we know it already doesn't exist, we don't need
		// to search to the back again
		ListNode current = nums[hashIndex(key)];
		ListNode newNode = new ListNode();
		newNode.val = val;
		newNode.key = key;
		newNode.next = current;

		nums[hashIndex(key)] = newNode;
	}

	private ListNode getNode(int key) {
		// go through and find if any node has the same key
		ListNode current = nums[hashIndex(key)];
		while (current != null) {
			if (key == current.key) {
				return current;
			}
			current = current.next;
		}
		return null;
	}

	public Integer get(int key) {
		// public wrapper around getNode
		ListNode n = getNode(key);
		if (n == null) {
			return null;
		}
		else {
			return n.val;
		}
	}

}
