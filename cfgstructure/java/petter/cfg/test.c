int bar(){
	int b = -1;
	b = 2;
	int a = 1;
	// b = a;
	if(b == 1){
		return b;
	}
	else{
		return a;
	}
	// b = foo(2);
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
	b = foo(a+1);
	bar();
	return a;
}