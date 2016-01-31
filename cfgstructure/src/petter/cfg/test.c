// int bar(){
// 	int b = -1;
// 	globa = 1;
// 	int c;
// 	c = bar2();
// 	int a = 1 + b + c;
// 	// b = a;
// 	if(b == 0){
// 		globa = 4;
// 		return b;
// 	}
// 	return a;
// 	// b = foo(2);
// }

// int bar2(){
// 	globa = -globa;
// 	return 1;
// }

int globa;

int foo(int a, int b){
	globa = globa + 1;
	a = b-2;
	return a;
}


int main(){
	int a, b, c;
	a = 1;
	// globa = 22;
	if(a == 1){
		b = 1;
	}
	else{
		// b = foo(a, a+2);
		a = 106;
	}
	c = 2;
	return globa;
}