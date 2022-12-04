/*
Name: Zaid Hilweh, Joshua Cox

Description: Program that takes a input file, and amt of threads wanting to use to hash the file.
Opens the file, maps it to arr using mmap(). Then proceeds to create a binary tree of threads
to hash certain consecuitive blocks (data) pass to its parents, and repeat until only the root
thread remains.
*/

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <inttypes.h>
#include <errno.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <sys/mman.h>
#include <pthread.h>

void Usage(char*);
uint32_t calcHash(const uint8_t*, size_t);
void* tree(void*);

typedef struct TreeThreadData {
	int64_t index;
	int64_t blocksPerThread;
	int64_t totalThreads;
	uint8_t* arr;
} TreeThreadData, *ThreadTreeDataPtr;

//Define block size
#define BSIZE 4096

int main(int argc, char** argv)
{
    uint8_t * arr;
    struct stat sb;
    int num_threads;
    int32_t fd;
    
    //input checking
    if(argc != 3)
    {
        Usage(argv[0]);
    }
    
    //opening file
    fd = open(argv[1], O_RDWR);
    if(fd == -1) {
        perror("open failed");
        exit(EXIT_FAILURE);
    }
    
    //getting file size for mmap
    if(fstat(fd,&sb) == -1)
    {
        perror("fstat");
        exit(EXIT_FAILURE);
    }
    
    num_threads = atoi(argv[2]);
    
    arr = mmap(NULL, sb.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
    
    // determine n/m;
    int totalBlockNum = sb.st_size / BSIZE; // gives us how many blocks there are.
    int blocksPerThread = totalBlockNum / num_threads; // blocks per thread.
    
    // Create thread, into hashtree function, pass total # thread.
	// this struct stores all the data that is going to be shared between the threads,
	// the index is mutable and is changed on every call, while the other fields are not
	TreeThreadData data = { 0 };
	data.arr = arr;
	data.index = 0;
	data.blocksPerThread = blocksPerThread;
	data.totalThreads = num_threads;

	printf("block per thread: %d\n", blocksPerThread);

  clock_t start, end;
  double time;
	// have the tree attach to the main thread, so we do not need to join it later
  start = clock();
	int* a = tree(&data);
  end = clock();
 
 
  time = (double)(end-start)/ CLOCKS_PER_SEC;
  time = time / ((double)num_threads);
 
	printf("hash: %u\n", *a);
  printf("time spent: %f seconds\n", (time));
	free(a);

	return 0;
}


/*
This function reates N threads(nodes) for the amt given by the argv[2] and assign its necessary
block to hash. blocks to compute is i* n/m, i being the thread #. once
children are done hashing, send to parent, hash, concat, and rehash. repeat. Returning final hash
to the root node (main thread)

*/

void* tree(void* arg) 
{
    pthread_t p1, p2;

	// copy the data into new structs, so their indexes
	// can be changed and copied into the parent
	TreeThreadData data = *((TreeThreadData*)arg);
	TreeThreadData data1 = *((TreeThreadData*)arg);
	data1.index = data.index * 2 + 1;
	TreeThreadData data2 = *((TreeThreadData*)arg);
	data2.index = data.index * 2 + 2;
	
	// tests based on indexes, to tell if the threads on the left and right
	// should be created. Because we are doing index based, the time at which
	// each segment is hashed does not matter, all the indexes will be exausted
	int shouldSpawnLeft = (data.totalThreads - (data1.index + 1)) >= 0;
	int shouldSpawnRight = (data.totalThreads - (data2.index + 1)) >= 0;

	// spawn the threads if they are going to be needed
	if (shouldSpawnLeft) {
        if (pthread_create(&p1, NULL, tree, &data1) < 0) {
			perror("could not create thread");
			exit(1);
		}
	}
	if (shouldSpawnRight) {
        if (pthread_create(&p2, NULL, tree, &data2) < 0) {
			perror("could not create thread");
			exit(1);
		}
	}

	// compute the hash at this index
	uint8_t* start = data.arr + data.index * data.blocksPerThread * BSIZE;
	size_t length = data.blocksPerThread * BSIZE;
	uint32_t hash = calcHash(start, length);

	// heap allocate to pass the return value. It could be casted into
	// the pointer, and fit inside of the return value, and would be more
	// performant, however this is more idiomatic
	uint32_t* totalPtr = (uint32_t*) malloc(sizeof(uint32_t) * 1);

	// the pointers of the two children processes will be saved here
	void* resultRight;
	void* resultLeft;

	// join the values only if they were spawned, and recieve the 
	// values back into the pointers
	if (shouldSpawnLeft) {
		if (pthread_join(p1, &resultLeft) < 0) {
			perror("could not create thread");
			exit(1);
		}
	}
	if (shouldSpawnRight) {
		if (pthread_join(p2, &resultRight) < 0) {
			perror("could not create thread");
			exit(1);
		}
	}
	
	uint8_t buffer[60]; // 60 is enough for all max uin32 as strings
	
	// compute the values based on how many threads were created
	// if both, format all 3, if 2, format 2 of them, if 1 just return it
	if(shouldSpawnLeft && shouldSpawnRight) {
		int length = sprintf((char*)buffer, "%u%u%u", hash, *(uint32_t*)resultLeft, *(uint32_t*)resultRight);
		*totalPtr = calcHash(buffer, length);
		free(resultLeft);
		free(resultRight);
	}
	else if (shouldSpawnLeft) {
		int length = sprintf((char*)buffer, "%u%u", hash, *(uint32_t*)resultLeft);
		*totalPtr = calcHash(buffer, length);
		free(resultLeft);
	}
	else {
		*totalPtr = hash;
	}

	//printf("index: %u computed: %u\n", data.index, *totalPtr);
    return totalPtr;
}


// Hash Function given
uint32_t calcHash(const uint8_t* key, size_t length)
{
  size_t i = 0;
  uint32_t hash = 0;
  while (i != length) {
    hash += key[i++];
    hash += hash << 10;
    hash ^= hash >> 6;
  }
  hash += hash << 3;
  hash ^= hash >> 11;
  hash += hash << 15;
  return hash;
}


//Usage error function, just prints message.
void Usage(char* s) 
{
 fprintf(stderr, "Usage: %s filename num_threads \n", s);
 exit(EXIT_FAILURE);
}



