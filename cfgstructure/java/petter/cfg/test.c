int globa;

int bar(){
	int b = -1;
	b = 2;
	globa = 1;
	int c;
	c = bar2();
	int a = 1 + b + c;
	// b = a;
	if(b == 1){
		globa = 4;
		return b;
	}
	else{
		return a;
	}
	// b = foo(2);
}

int bar2(){
	globa = 2;
	return 1;
}

int foo(int a){
	a = 2;
	// bar();
	// a = foo(3);
	if(a > 2)
		return 1;
	else
		return 2;
}

int main(){
	int a, b;
	a = 0;
	// globa = 2;
	b = foo(a+1);
	bar();
	return a;
}