/*
* Project 1. Unix 3377.007.
* Created by Josh Cox and Zaid Hilweh
* 
* This is a simple shell script meant to be given basic shell commands, and pipes, to be properly parsed
* and executed. Some few implemented commands needed such as exit, and history and its arguments.
* 
* Meant to take user input, store history,
* and continue in doing so until user terminates program using 'exit'.
* 
*/


#define _GNU_SOURCE

#include 
#include 
#include 
#include 
#include 

void err_exit(char *msg) {
	perror(msg);
	exit(EXIT_FAILURE);
} // end of err_exit()

#define MAX_ARGS 1000
#define MAX_HISTORY 100

// (all the elements point to free_line)
// pipe seperated commands are NULL terminated, where
// pipes are replaced with NULL
typedef struct Command {
	size_t pipe_amt;
	char** args; // arguments in parsed form, head allocated string array of size MAX_ARGS
	char* free_line; // the line that the arguments come from (for purpose of freeing)
} Command, *CommandPTR;

static Command history[MAX_HISTORY];
static size_t history_start = -1;
static size_t history_size = 0;

void execute(Command command);

int parse_command(char* in, Command* cmd);

void print_history();
void clear_history();
Command get_history_offset(size_t offset);
void add_to_history(Command command);



/*
* Handle commands checks for 1st, if the users commands are valid, or custom command
* like exit and history. Deals with 'history', and the argument '-c' and if the users
* input for executing a command from history such as 'history 12' is valid or not.
* 
*/
int handle_command(Command c) {
	if (strcmp(c.args[0], "cd") == 0) { // checks to see cd command and args are valid.
		add_to_history(c); //adds to history if command is valid.
		if (c.args[1] == NULL) {
			printf("no path provided\n"); //prints error if no args given.
			return -1;
		}

		if (chdir(c.args[1]) < 0) { // if arg is valid, checks to see if it exists
			printf("path does not exist\n"); //print error message if no path exists
			return -1;
		}
	}
	else if (strcmp(c.args[0], "exit") == 0) {
		clear_history(); // free the memory before exitting
		exit(0);
	}
	// history command and its args, also adds to history if not 'history -c'
	else if (strcmp(c.args[0], "history") == 0) {  
		if (c.args[1] == NULL) {
			add_to_history(c);
			print_history();
		}
		else if (strcmp(c.args[1], "-c") == 0) {
			clear_history();
		}
		// runs the 'history #' command and checks if its within range. 
		else {
			int index = atoi(c.args[1]);
			if (index < 0 || index >= history_size || history_size == 0) {
				printf("invalid input, out of range\n");
				add_to_history(c);
				return - 1;
			}
			Command stored = get_history_offset((size_t)index);
			execute(stored);
			add_to_history(c);
		}
	}
	else { // if command given is valid adds to history and executes
		add_to_history(c);
		execute(c);	
	}

	return 0;
} // end of handle_command()


//User input, getline, allocs mem
int main() {
	while (1) {
		printf("sish> ");
		char* line = NULL; // let getline alloc
		size_t line_length = 0;
		size_t line_size; //used to determine if userinput is valid/working.
		if ((line_size = getline(&line, &line_length, stdin)) < 0) {
			free(line); // don't leak (doesnt matter cause exit but good practice)
			err_exit("could not read input");
		}


		//if inputs are empty, continues and asks for more commands
		if (line_size == 1) { //deemed line_size of one due to getlines \n
			continue;
		}

		Command c;
		if (parse_command(line, &c) < 0) { // if parsing was invalid, prompt user for input again
			continue;		
		}
	
		free(line);	
		handle_command(c);
		
	}
} // end of main()

// code for executing programs

void execute_one(Command command) {
	pid_t cpid = fork();
	if (cpid < 0) {
		err_exit("fork failed");
	}
	else if(cpid == 0) {
		execvp(command.args[0], command.args);
		err_exit("could not execute");
	}
	else {
		wait(NULL);
	}
} // end of execute_one()


//args index for exec commands
void update_args_index(char** args, size_t* i) {
	while (args[*i] != NULL) {
		*i += 1;
	}
	*i += 1;
} // end of update_args_index()

void execute_n(Command command) {
	int last_read_fd;
	int fd[2];
	size_t args_index = 0;
	
	// execute the first
	if (pipe(fd) < 0) {
		err_exit("could not pipe");
	}

	int cpid1 = fork();
	if (cpid1 < 0) {
		err_exit("could not execute");
	}
	
	if (cpid1 == 0) {
		close(fd[0]); // child not reading from pipe
		if (dup2(fd[1], STDOUT_FILENO) < 0) {
			err_exit("could not redirect pipe");
		}
		close(fd[1]); // don't need after redirect 
		execvp(command.args[args_index], command.args + args_index);
		err_exit("could not execute");
	}
	close(fd[1]); // parent does not need to write
	last_read_fd = fd[0];
	update_args_index(command.args, &args_index); 

	// all the ones that are not first/last are the same
	int mid_cpids[MAX_ARGS];
	for (size_t i = 0; i < command.pipe_amt - 1; i++) {
		// redirect both input and output
		if (pipe(fd) < 0) {
			err_exit("could not pipe");
		}

		int cpid = fork();
		if (cpid < 0) {
			err_exit("could not execute");
		}

		if (cpid == 0) {
			close(fd[0]); // child not reading from pipe
			if (dup2(last_read_fd, STDIN_FILENO) < 0) {
				err_exit("could not redirect pipe");
			}
			close(last_read_fd); // don't need anymore cause dup2 

			if (dup2(fd[1], STDOUT_FILENO) < 0) {
				err_exit("could not redirect pipe");
			}
			close(fd[1]); // don't need anymore cause dup2
			execvp(command.args[args_index], command.args + args_index);
			err_exit("could not execute");
		}
		close(fd[1]); // parent not writing
		close(last_read_fd); // parent not reading
		last_read_fd = fd[0];
		mid_cpids[i] = cpid;
		update_args_index(command.args, &args_index); 
	}

	// execute last, redirect last_read to std_in
	int cpid2 = fork();
	if (cpid2 < 0) {
		err_exit("could not pipe");
	}
	if (cpid2 == 0) {
		if (dup2(last_read_fd, STDIN_FILENO) < 0) {
			err_exit("could not redirect pipe");
		}
		close(last_read_fd);
		execvp(command.args[args_index], command.args + args_index);
		err_exit("could not execute");
	}
	close(last_read_fd); // close the last pipe

	waitpid(cpid1, NULL, 0);
	for (size_t i = 0; i < command.pipe_amt - 1; i += 1) {
		waitpid(mid_cpids[i], NULL, 0);
	}
	waitpid(cpid2, NULL, 0);
} // end of executen()

//excutes command
void execute(Command command) {
	if (command.pipe_amt == 0) {
		execute_one(command); // goes to execute one if no pipes are contained
	}
	else {
		execute_n(command); // executes n commands for amt of pipes given (with valid commands)
	}
} // end of execute()

// code for parsing inputs into our command struct

// determines if char within user input (from getline) is necessary to be parsed.
int should_remove(char c) {
	switch(c) {
		case ' ': return 1;
		case '|': return 1;
		case '\n': return 1;
	}

	return 0;
} // end of should_remove()


void trim(char* str, char** new_str) {
	size_t start = 0;
	size_t end = strlen(str) - 1;

	// remove the leading characters
	while (should_remove(str[start]) && str[start] != '\0') {
		start += 1;
	}

	// remove the ending chars
	while (should_remove(str[end]) && end > start) {
		end -= 1;
	}

	// plus 2, 1 for bc index instead of size, 1 for the null char
	size_t length = (end - start) + 2;
	*new_str = (char*) malloc(sizeof(char) * length);
	if (new_str == NULL) {
		err_exit("could not allocate");
	}
	
	// copy string an null terminate it
	memcpy(*new_str, str + start, length); 
	(*new_str)[length - 1] = '\0';
} // end of trim()

// takes the command and fills the buffer with the command
int parse_command(char* in, Command* cmd) {
	// turn it into a trimmed line
	char* line;
	trim(in, &line);

	Command command;

	char** args = (char**) malloc(sizeof(char*) * MAX_ARGS);
	if (args == NULL) {
		err_exit("could not allocate");
	}

	size_t pipe_amt = -1;
	size_t arg_index = 0;	
	char* saveptr1;
	char* saveptr2;

	char* prog;
	char* arg;
	// handle no command and buffer overflow
	for (char* line_entry = line; 1; line_entry = NULL) {
		prog = strtok_r(line_entry, "|", &saveptr1);
		if (prog == NULL) {
			break;
		}

		for (char* prog_entry = prog; 1; prog_entry = NULL) {
			if (arg_index == MAX_ARGS) {
				printf("too many arguments\n");
				return -1;
			}
			
			arg = strtok_r(prog_entry, " ", &saveptr2);
			args[arg_index] = arg;	
			arg_index += 1;
			if (arg == NULL) {
				break;
			}			
		} 
		pipe_amt += 1;
	}	

	// set up the rest of the command
	command.free_line = line;
	command.args = args;
	command.pipe_amt = pipe_amt;

	*cmd = command;
	return 0;
} // end of parse_command()



void free_command(Command command) {
	free(command.args); // free's the actual array
	free(command.free_line); // free's all of the arguments in the array
} // end of free_command()


//Adds to history and increments history_start
void add_to_history(Command command) {
	history_start += 1;	
	if (history_start == MAX_HISTORY) {
		history_start = 0;
	}

	if (history_size == MAX_HISTORY) {
		free_command(history[history_start]); // frees a history if user has reached max history
	}
	else {
		history_size += 1; // keeps track of current history size (if < 100
	}

	history[history_start] = command;
} // end of add_to_history()


//Recieves the command, and proceeds to print it for user.
void print_history_line(Command c, size_t i) {
	int p = 0;
	printf("%ld ", i);
	for (int i = 0; p < c.pipe_amt + 1; i++) {
		if (c.args[i] != NULL) {
			printf("%s ", c.args[i]); 
		}
		else if (p < c.pipe_amt) {
			p += 1;
			printf("| ");
		}
		else {
			p += 1;
		}
	}

	printf("\n");
} // end of print_history_line()


/*
* History loops within the history array
* so as we hit the 100th slot, it loops back to the start of the
* array and begans there so we must get the offset.
* 
* Determines if necessary offset is needed (less than or greater than or equal to 100 commands)
* once offset is determined we prompt the print_history_line function, and proceed to print up to
* the last 100 commands.
* 
*/
void print_history() {
	if (history_size < MAX_HISTORY) {
		for (size_t i = 0; i < history_size; i++) {
			print_history_line(history[i], i);
		}
	}
	else {
		for (size_t i = history_start + 1; i < MAX_HISTORY; i++) {
			print_history_line(history[i], i - history_start - 1);
		}

		for (size_t i = 0; i < history_start + 1; i++) {
			print_history_line(history[i], MAX_HISTORY + i - history_start - 1);
		}
	}
} //end of print_history()


// Function Clears all of history, used for history -c command
void clear_history() {
	history_start = -1;
	for (size_t i = 0; i < history_size; i++) {
		free_command(history[i]);
	}
	history_size = 0;
} //end of clear_history()


/*
* History loops within the history array
* so as we hit the 100th slot, it loops back to the start of the
* array and begans there.
* 
* To properly deal with commands within the array in the correct order, we determine if theres
* a necessary offset (executed more than 100 commands) and determine the proper location
* to deal with the correct command
*/
Command get_history_offset(size_t offset) {
	if (history_size < MAX_HISTORY) {
		return history[offset];
	}

	size_t computed_offset = offset + history_start + 1;
	if (computed_offset > MAX_HISTORY - 1) {
		computed_offset -= MAX_HISTORY;
	}

	return history[computed_offset];
}//end of get_history_offset()





