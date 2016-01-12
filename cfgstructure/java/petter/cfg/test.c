int bar(){
	int b = -1;
	// b = foo(2);
	return b;
	bar();
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
	b = foo(a);
	bar();
	return a;
}