int globa;

int set123(){
	globa = 123;
	return 1;
}

int bar2(){
	int b = -121;
	int c = 24;
	int a = 5;
	globa = globa-2;
	if(globa == 0){
		globa = globa + 1;
		bar2();
	}
	else{
		globa = globa - 1;
	}
}

int main(){
	// set123();
	int a = 1;
	int b;
	int c;
	if(a == 1){
		b = 1;
		bar2();
	}
	else{
		b = 0;
	}
	c = set123();
	return c;
}