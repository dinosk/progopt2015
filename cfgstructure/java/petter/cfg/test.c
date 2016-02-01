int globa;

<<<<<<< HEAD
int bar(){
	int b = -1;
	globa = 1;
	int c;
	c = bar2();
	int a = 1 + b + c;
	// b = a;
	if(b == 0){
		globa = 4;
		return b;
	}
	return a;
	// b = foo(2);
}

int bar2(){
	globa = -globa;
	return 1;
}

int foo(int a, int b){
	globa = globa + 1;
	a = b-2;
	return a;
=======
// int bar(){
// 	int b = -1;
// 	b = 2;
// 	globa = 1;
// 	int c;
// 	c = bar2();
// 	int a = 1 + b;
// 	// b = a;
// 	if(b == 1){
// 		globa = 4;
// 		return b;
// 	}
// 	else{
// 		return a;
// 	}
// 	b = foo(2);
// }

int bar2(){
	int a = 5;
	globa = 2;
	return 1;
>>>>>>> da057483376d0f98d6225d69f2580ab995d92169
}

// int foo(int a){
// 	a = 2;
// 	// bar();
// 	a = foo(3);
// 	if(a > 2)
// 		return 1;
// 	else
// 		return 2;
// }

int main(){
	int a, b;
<<<<<<< HEAD
	a = 1;
	if(a == 1){
		a = 101;
	}
	else{
		a = 106;
	}
	b = 2;
	return a;
	// b = a + 3;
	// globa = 2;
	// b = foo(a+1, b);
	// a = 4;
	// b = foo(a, b);
	// return globa;
}
=======
	a = 0;
	globa = 2;
	// b = foo(a+1);
	bar2();
	return a;
}
>>>>>>> da057483376d0f98d6225d69f2580ab995d92169
