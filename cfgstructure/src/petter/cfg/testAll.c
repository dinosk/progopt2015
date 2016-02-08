int globalA;
int globalB;

int add10(){
	globalB = globalA + 10;
}

int setB(int a){
	globalA = globalA + a;
	add10();
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
	int localA = 5;
	setB(2);
	if(globalB == 13){
		int b;
		b = 1;
		globalA = b + globalA;
		return globalA;
	}
	else{
		return globalB;
	}
}