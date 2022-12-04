import java.util.function.Function;

public class A53 {

	private static final double THRESH_HOLD = 0.8;
	Integer[] nums;
	int size;

	public A53() {
		nums = new Integer[1];
		size = 0;
	}

	// resize the full array
	// so that it can fit more elements
	void checkResize(Function<Integer, Integer> insert) {
		if ((double)size / nums.length > THRESH_HOLD) {
			// resize while setting the new nums, so
			// the insert methods can use the new array
			Integer[] old = nums;
			nums = new Integer[nums.length * 2];

			for(int i = 0; i < old.length; i++) {
				// insert all elements into the new arr,
				// with the proper function being used
				if (old[i] != null) {
					insert.apply(old[i]);
				}
			}
		}
	}

	int hashIndex(int val) {
		return val % nums.length;
	}

	public int insertLinear(int val) {
		checkResize(this::insertLinear);
		int i = hashIndex(val);
		int numCollisions = 0;
		// it can go forever, because check resize ensures there is
		// an open spot somewhere
		while (true) {
			// place the item
			if (nums[i] == null) {
				nums[i] = val;	
				break;
			}
			i += 1;
			numCollisions += 1;
			// wrap around
			if (i == nums.length) {
				i = 0;
			}
		}
		
		size += 1;
		return numCollisions;
	}

	public int insertQuadratic(int val) {
		checkResize(this::insertQuadratic);
		int numCollisions = 0;
		int i = hashIndex(val);
		
		for (int j = 0; j < nums.length; j++) {
			// recompute the hash value
			int index = (j * j + i) % nums.length;
			// insert the item
			if (nums[index] == null) {
				nums[index] = val;
				size += 1;
				return numCollisions;
			}
			numCollisions += 1;
		}

		// the item did not get placed with quadritic, resort to linear
		return numCollisions + insertLinear(val);
	}

	public int insertDoubleHash(int val) {
		checkResize(this::insertDoubleHash);
		int i = hashIndex(val);
		int numCollisions = 0;
		int secondHash = 7 - val % 7;

		for (int j = 0; j < nums.length; j++) {
			// recompute the hash value
			int index = (j * secondHash + i) % nums.length;
			if (nums[index] == null) {
				nums[index] = val;
				size += 1;
				return numCollisions;
			}
			numCollisions += 1;
		}

		// item did not get inserted, resort to linear
		return numCollisions + insertLinear(val);
	}

}

