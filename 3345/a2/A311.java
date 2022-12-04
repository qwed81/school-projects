public class A311<T> {

	class ListNode {
		ListNode next;
		T value;
	}

	// keep track of 1 node prior to the actual head node to simplify logic
	ListNode head;

	public A311() {
		head = new ListNode();
	}

	// go through one by one and increment the count
	public int size() {
		int count = 0;
		ListNode current = head.next;
		while (current != null) {
			count += 1;
			current = current.next;
		}
		return count;
	}

	// go through each and print them
	public void print() {
		ListNode current = head.next;
		while (current != null) {
			System.out.print(current.value.toString() + " ");
			current = current.next;
		}
	}

	public boolean contains(T value) {
		ListNode current = head.next;
		while (current != null) {
			// if there is one that equals, return true it will always be true
			if (current.value.equals(value)) {
				return true;
			}
			current = current.next;
		}
		return false;

	}

	public void add(T value) {
		// start at 1 prior to actual head, so it can be added as new head
		ListNode current = head;
		// go through until the last element 
		while (current.next != null) {
			// if the element exists then stop counting
			if (current.next.value.equals(value)) {
				return;
			}

			current = current.next;
		}

		// add it to the end of the list
		current.next = new ListNode();
		current.next.value = value;
	}

	public void remove(T value) {
		// start 1 before actual head, so we can remove it
		ListNode current = head;
		// if current.next = null, we can't remove a null
		while (current.next != null) {
			// if the next value equals, then remove it
			if (current.next.value.equals(value)) {
				current.next = current.next.next;
				break;
			}
			current = current.next;
		}
	}
}
