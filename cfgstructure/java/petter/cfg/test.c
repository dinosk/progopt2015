int globa;

int bar(){
	int b = -1;
	b = 2;
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
	globa = 2;
	return 1;
}

int foo(int a, int b){
	a = 2;
	// bar();
	// a = foo(3);
	if(a > 2)
		return 1;
	else
		return 2;
	return b;
}

int main(){
	int a, b;
	a = 1;
	b = a + 3;
	globa = 2;
	b = foo(a+1, b);
	bar();
	return a;
}