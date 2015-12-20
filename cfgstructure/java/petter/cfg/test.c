int foo(int a){
	return a+1;
}

int main(){
	int a, b;
	a = 0;
	b = 0;
	a = b+1;
	b = foo(a);
	return 0;
}