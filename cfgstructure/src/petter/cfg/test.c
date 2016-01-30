int globa;

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
}

int main(){
	int a, b;
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