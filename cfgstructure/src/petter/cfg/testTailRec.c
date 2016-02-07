int globalA;
int globalB;

int setB(int a){
	globalB = a;
}

int foo(){
	int a;
	a = 1;
	globalA = globalA - a;
	if(globalA > 100){
		foo();
	}
}

int main(){
	globalA = 1;
	globalB = 99;
	foo();
	setB(globalA);
	return globalB;
}