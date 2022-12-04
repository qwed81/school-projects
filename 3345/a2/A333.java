import java.util.NoSuchElementException;

public class A333 {
	int[] arr;
	int front;
	int back;
	int size;
	
	public A333() {
		arr = new int[2];
		front = 0;
		back = -1;
		size = 0;
	}

	public int poll() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
	
		// return the value at the front
		// and then increase the front
		int val = arr[front];
		front += 1;
		size -= 1;

		// if the front is at the end, then wrap around
		if (front == arr.length) {
			front = 0;
		}

		return val;
	}

	public void add(int value) {
		// there is no more space, so it needs to be
		// reallocated
		if (size == arr.length) {
			int[] newArr = new int[arr.length * 2];
			// copy from front-end of arr into 0-n of the new arr
			for (int i = front; i < arr.length; i++) {
				newArr[i - front] = arr[i]; 
			}
			int n = arr.length - front;

			// copy from 0-back of arr, into n+1-n_2 of new arr
			for (int i = 0; i <= back; i++) {
				newArr[n + i] = arr[i];
			}
			front = 0;
			back = arr.length - 1;
			arr = newArr;
		}

		back += 1;
		// the back has gone to the end, so go back to the start
		if (back == arr.length) {
			back = 0;
		}
		size += 1;
		arr[back] = value;
	}

	 
}
