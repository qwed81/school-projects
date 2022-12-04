# Joshua Cox
# jac200012

# the specification on supported sorting positive integers of powers of 2. Therefore there is no bounds
# checking or sorting of different values. Also the lists only have enough space for 32 integers, so
# any input greater than that will break the system. The project is made of 2 parts, the merge function,
# which is a leaf function does not require us to allocate a stack frame. Therefore it uses the temporary
# registers so it does not overwrite the registers of the calling function. The calling function then
# goes in a loop merging one by one powers of 2 until the size goes over the total size of the list

.data
list: .space 128
new_list: .space 128 # room to store the list while sorting
output_text: .asciiz "Your sorted list is: "
input_count_text: .asciiz "How many numbers do you want: "
input_text: .asciiz "Input number: "
space_text: .asciiz " "

.text
# the main function's purpose is to perform the merge in a loop, where in that
# loop it continuously provides the size and the poiter to the first list to merge
main:
	# print out text
	li $v0, 4
	la $a0, input_count_text
	syscall

	# get count
	li $v0, 5
	syscall

	add $t0, $zero, $v0 #t0 is now the input size
	add $t1, $zero, $zero # initialize counter

	sll $s1, $t0, 2 # total size of list in bytes, multiply by 4 to accomidate word
	li $s0, 4 # current merge size (in bytes)
	la $t0, list # get the list to compute the end ptr
	add $s4, $t0, $s1 # compute the ending pointer of the list

	# for the amount specified by input, load into the list
input_start:
	# print out input text
	li $v0, 4
	la $a0, input_text	
	syscall
	
	li $v0, 5 # take input and store it
	syscall
	sw $v0, 0($t0)

	addi $t0, $t0, 4 # increment the pointer to the list

	blt $t0, $s4, input_start
	
	# now we are setup with input, we can merge the list

	# while size < the total, keep calling merge with each list
	# then we double the size to merge two bigger lists
outer_merge_loop:
	bge $s0, $s1, exit_outer_merge_loop

	li $s2, 0 # byte offset of the list
	la $s3, list # start at the beginning of the list every time
	# go through every list, and call merge
inner_merge_loop:
	bge $s3, $s4, exit_inner_merge_loop
	# move arguments for func call
	add $a0, $s3, $zero 
	add $a1, $s0, $zero
	jal merge_func
	
	# increment the pointer by 2 list size, because 2 lists are merged together
	add $s3, $s3, $s0
	add $s3, $s3, $s0

	j inner_merge_loop
exit_inner_merge_loop:
	sll $s0, $s0, 1 # double the size needed for the next loop
	j outer_merge_loop

exit_outer_merge_loop:

	# print out the output text
	li $v0, 4
	la $a0, output_text
	syscall

	la $t0, list
print_loop:
	# print out int
	li $v0, 1
	lw $a0, 0($t0)
	syscall

	# print out space
	li $v0, 4
	la $a0, space_text
	syscall

	addi $t0, $t0, 4 # add inc pointer by 1 word
	blt $t0, $s4, print_loop # while the pointer is less than end of list

	#exit
	li $v0, 10
	li $a0, 0
	syscall

# this is a leaf functions, and therefore needs to make sure it saves the state
# of s registers. Because of that only t registers are used so no values have to be
# pushed on to the stack
# usage: void (int* start, size_t size_in_bytes_of_each_list)
merge_func:
	
	add $t2, $a0, 0 # starting pointer for list1
	add $t0, $t2, $a1 # ending pointer for list1
	
	add $t3, $t0, $zero # starting of list2 same as ending of list 1
	add $t1, $t3, $a1 # compute ending of list2
	
	la $t4, new_list # use the new list to hold temp data

merge:
	# first check if one of the lists is done, if it is compute the other
	# if both, then the loop is done
	seq $t5, $t2, $t0 # list1 ptr is at end
	seq $t6, $t3, $t1 # list2 ptr is at end
	and $t5, $t5, $t6 # both are at the end
	beq $t5, 1, done # if they are both at the end, break out of loop
	beq $t2, $t0, add_list2 # list1 is done, list2 is not
	beq $t3, $t1, add_list1 # list2 is done, list1 is not
	# both are not done
	# compare the values at the two pointers in the list
	# take the lesser
	lw $t5, 0($t2)
	lw $t6, 0($t3)
	ble $t6, $t5, add_list2 # if list 2 is lesser, compute that instead
add_list1:
	lw $t5, 0($t2) # load value from list1 ptr
	sw $t5, 0($t4) # store into new list
	# add 4 bytes to increment both the pointers by a word
	addi $t2, $t2, 4
	addi $t4, $t4, 4
	j merge
add_list2:
	lw $t5, 0($t3) # load value in list2 ptr
	sw $t5, 0($t4) # store into new list
	# add 4 bytes to increment both the pointers by a word
	addi $t3, $t3, 4
	addi $t4, $t4, 4
	j merge
done:
	# the values are stored in new_list, we need to get them back
	# into the normal list, or it wouldn't be sorted
	
	# we already have $t1 as the end of the list, and $a0 as the start, we can just use those
	la $t0, new_list
copy_start: # while the pointer is less than the end, store all values from new_list into old_list
	lw $t2, 0($t0)
	sw $t2, 0($a0)
	addi $a0, $a0, 4
	addi $t0, $t0, 4
	blt $a0, $t1, copy_start
	
	# we don't need to do any cleanup because we didn't need to allocate stack frame,
	# and there is no return value as everything is moved into the list
	jr $ra
	
	


