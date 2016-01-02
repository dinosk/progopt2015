int bar(){
	int b = -1;
	b = foo(2);
	bar();
	return b;
}

int foo(int a){
	a = 2;
	bar();
	a = foo(3);
	return 1;
}

int main(){
	int a, b;
	a = 0;
	b = foo(a);
	bar();
	return a;
}