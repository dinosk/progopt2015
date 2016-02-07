int globa;


int bar3(){
	globa = globa-2;
}

int bar2(int b, int a){
	globa = globa-b;
	bar3();
	return globa;
}

int set123(){
	int b = 1;
	globa = 123 + b;
	bar2();
	return globa;
}

int main(){
	int a = 1;
	// set123();
	int b;
	int c;
	globa = 2;
	if(a == 1){
		b = bar2(2, 3);
	}
	else{
		b = a;
	}
	a = b++;

	while(a < 10){
		a++;
	}
	c = a++;
	// if(a == 1){
	// 	b = 1;
	// 	bar2(3, 5);
	// 	a = 2;
	// }
	// else{
	// 	b = 0;
	// 	bar3();
	// 	a = 2;
	// }
	// a = 1;
	// // b = set123();

}