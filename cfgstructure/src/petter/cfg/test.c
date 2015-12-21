int bar(){
	int b = -1;
	return b;
}

int foo(int a){
	a = 2;
	return 1;
}

int main(){
	int a, b;
	a = 0;
	// b = foo(a);
	bar();
	return a;
}