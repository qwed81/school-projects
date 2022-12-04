import java.util.function.Consumer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;

public class ArraySorting {

	// has to be declared final and tested multiple times so the compiler can
	// optimize away the cutoff if statement and not change the results
	public static final int CUTOFF = 50;
	
	public static final int DISTRIBUTION = 9000000; // spread of numbers
	public static final int[] TEST_LENGTHS = {50, 500, 1000, 2000, 5000, 10000, 100000, 1000000, 10000000}; // length of tests

	public static void main(String[] args) {
		for (int length : TEST_LENGTHS) {
			// it would take too long to run insertion sort,
			// so skip it to test the others
			if (length <= 1000000) {
				runTest(ArraySorting::insertionSort, "\"insertion sort\"", length, DISTRIBUTION, false);
			}

			runTest(ArraySorting::mergeSort, "\"merge sort\"", length, DISTRIBUTION, false);
			runTest(ArraySorting::quickSort, "\"quick sort\"", length, DISTRIBUTION, false);
			runTest(ArraySorting::heapSort, "\"heap sort\"", length, DISTRIBUTION, false);
			System.out.println();
		}

	}

	public static void runTest(Consumer<int[]> testFunction, String sortFuncName, int length, 
		int numRange, boolean printArray) {

		// generate the array
		int[] nums = genRandomArray(length, numRange);

		if (printArray == true) {
			System.out.println(Arrays.toString(nums));
		}

		// build up a HashMap containing the frequency of all characters that will be 
		// used later when checking if it is sorted
		HashMap<Integer, Long> set = new HashMap<>();
		for (int num : nums) {
			Long n = set.get(num);
			if (n == null) {
				set.put(num, 1l);
			}
			else {
				set.put(num, n + 1l);
			}
		}

		// time the sort
		long time = System.currentTimeMillis();
		testFunction.accept(nums);
		time = System.currentTimeMillis() - time;

		// make sure that it sorted properly
		boolean sorted = true;
		for (int i = 0; i < nums.length - 1; i += 1) {
			// make sure that all arr[i] < arr[i + 1]
			if (nums[i] > nums[i + 1]) {
				sorted = false;
				break;
			}
		}
		
		// decrement all values in the set as they come
		// to ensure that the array has the same values as before
		for (int i = 0; i < nums.length; i += 1) {
			Long n = set.get(nums[i]);
			set.put(nums[i], n - 1);
		}

		// if any value in the set is != 0, then there is a difference between
		// the numbers in the array before and after the sort
		for (Long val : set.values()) {
		//	System.out.println(val);
			if (val != 0l) {
				sorted = false;
				break;
			}
		}

		// print out information about the test
		if (printArray == true) {
			System.out.println(Arrays.toString(nums));
		}
		
		String sortMessage = sorted ? " sorted successfully" : " did not sort successfully";
		String arrayMessage = " an array of size " + length + " and range of " + numRange;
		String timeMessage = " in " + time + " milliseconds";
		System.out.println(sortFuncName + sortMessage + arrayMessage + timeMessage);
	}

	public static int[] genRandomArray(int length, int numRange) {
		int[] arr = new int[length];
		for (int i = 0; i < length; i += 1) {
			arr[i] = (int)Math.floor(Math.random() * numRange);
		}

		return arr;
	}

	public static void swap(int[] arr, int i1, int i2) {
		int hold = arr[i1];
		arr[i1] = arr[i2];
		arr[i2] = hold;
	}

	private static void insertionSortSegment(int[] arr, int start, int end) {
		// go through every index in the array
		for (int i = start; i <= end; i += 1) {
			// start at the first value, this is ok because
			// the outer loop is arr.length - 1
			int smallest = arr[i];
			int smallestIndex = i;

			// start at the value after the first
			// and locate the smallest
			for (int j = i + 1; j <= end; j += 1) {
				if (arr[j] < smallest) {
					smallest = arr[j];
					smallestIndex = j;
				}
			}

			// swap with the smallest value
			swap(arr, i, smallestIndex);
		}
	}

	public static void insertionSort(int[] arr) {
		insertionSortSegment(arr, 0, arr.length - 1);
	}

	private static void mergeSortRecur(int[] arr, int start, int end, int[] scratch) {
		// if there are 2 or more elements, divide the array
		int mid = (end + start) / 2;
		if (end - start > 0) {
			mergeSortRecur(arr, start, mid, scratch);
			mergeSortRecur(arr, mid + 1, end, scratch);
		}

		// we know that start through mid are sorted, and
		// mid + 1 through end are sorted, so just combine them
		int left = start;
		int right = mid + 1;
		int scratchPtr = 0;
		while (left <= mid && right <= end) {
			// take the smaller value and add it to scratch
			if (arr[left] < arr[right]) {
				scratch[scratchPtr] = arr[left];
				left += 1;
			}
			else {
				scratch[scratchPtr] = arr[right];
				right += 1;
			}
			scratchPtr += 1;
		}

		// copy all values from left (right would be done)
		while (left <= mid) {
			scratch[scratchPtr] = arr[left];
			left += 1;
			scratchPtr += 1;
		}

		// copy all values from right (left would be done)
		while (right <= end) {
			scratch[scratchPtr] = arr[right];
			right += 1;
			scratchPtr += 1;
		}

		// put all the values back into the arr, scratchPtr is the amount needed
		for (int i = 0; i < scratchPtr; i += 1) {
			arr[start + i] = scratch[i];
		}
	}

	public static void mergeSort(int[] arr) {
		// the combined components will at max be the size of arr.length
		// by allocating the biggest array we can skip all the heap allocations of 
		// individual arrays
		int[] scratch = new int[arr.length];
		mergeSortRecur(arr, 0, arr.length - 1, scratch);
	}

	// selects the median of three as the pivot, but at the same time
	// moves the greatest value to the end so it does not need to sort 
	// as much
	public static int selectPivot(int[] arr, int start, int end) {
		int mid = (end + start) / 2;
		if (arr[start] > arr[mid]) {
			swap(arr, start, mid);
		}

		if (arr[mid] > arr[end]) {
			swap(arr, mid, end);
		}

		if (arr[start] > arr[mid]) {
			swap(arr, start, mid);
		}

		swap(arr, mid, end - 1);
		return arr[end - 1];
	}

	private static void quickSortRecur(int[] arr, int start, int end) {
		if (CUTOFF == -1 && end - start < 3) {
			insertionSortSegment(arr, start, end); // simple way of sorting the 3 items remaining
			return;
		}
		else if (end - start < CUTOFF) { // insertion sort on the cutoff
			insertionSortSegment(arr, start, end);
			return;
		}

		int pivot = selectPivot(arr, start, end);

		// because we know end - 1 is pivot and start is smaller than pivot
		// as well as i += 1 and j += 1 at the beginning, this is where it
		// needs to start
		int i = start; 
		int j = end - 1; // start behind both pivot and median of 3 swap
		while (i < j) {
			i += 1;
			j -= 1;

			// select the first pair where i > pivot & j < pivot
			// we don't need to check i < j in these loops cause they will
			// hit a value greater
			while (arr[i] < pivot) {
				i += 1;
			}

			while (arr[j] > pivot) {
				j -= 1;
			}

			if (i < j) {
				swap(arr, i, j);
			}
		}

		// i is the point where everything is divided, we can swap the pivot
		// back in there
		swap(arr, i, end - 1);

		// quick sort in the portion less than i and greater than i
		quickSortRecur(arr, start, i - 1);
		quickSortRecur(arr, i + 1, end);
	}

	public static void quickSort(int[] arr) {
		quickSortRecur(arr, 0, arr.length - 1);
	}

	private static void buildMaxHeap(int[] arr) {
		for (int i = arr.length / 2; i >= 0; i--) {
			bubbleDown(arr, arr.length, i);
		}
	}

	private static void bubbleDown(int[] arr, int len, int i) {
		// because these are 0-based indexes, (i + 1) * 2 - 1 solved algebraically
		int left = i * 2 + 1;
		int right = i * 2 + 2;
	
		// if the right is out of bounds either take
		// the left, or finish if they are both
		if (right >= len) {
			if (left < len && arr[left] > arr[i]) {
				swap(arr, left, i);
			}

			// the next value will always be out of bounds
			// no need to check it
			return;
		}
		else if (left >= len) {
			return; // there is nowhere to swap
		}

		// choose which way to bubble down, otherwise it is already at its position
		if (arr[left] > arr[i] && arr[left] >= arr[right]) { // bubble down left
			swap(arr, left, i);
			bubbleDown(arr, len, left);
		}
		else if (arr[right] > arr[i] && arr[right] > arr[left]) { // bubble down right
			swap(arr, right, i);
			bubbleDown(arr, len, right);
		}
	}

	public static void heapSort(int[] arr) {
		buildMaxHeap(arr);
		for (int i = 0; i < arr.length; i++) {
			// move the max value to the "end of the array"
			// then bubble the swapped value back to its place
			swap(arr, arr.length - i - 1, 0);
			bubbleDown(arr, arr.length - i - 1, 0);
		}
	}
}
