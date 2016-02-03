int globa;

int bar2(){
	int b = 1;
	globa = globa-2;
	return 1;
}

int bar3(){
	globa = globa-2;
}

int set123(){
	int b = 1;
	globa = 123 + b;
	bar2();
	return globa;
}

int main(){
	int a = 1;
	set123();
	int b;
	int c;
	if(a == 1){
		b = 1;
		bar2();
		a = 2;
	}
	else{
		b = 0;
		bar3();
		a = 2;
	}
	a = 1;
	// b = set123();

}