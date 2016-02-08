int globalA;
int globalB;

int add10(){
	int ten = 10;
	globalB = globalA + ten;
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
	int a;
	int b;
	int localA = 5;
//	a = 5;
	b = 5;
	globalA = 1;
	globalB = 99;
	setB(2);
	if(globalB == 13){
		globalA = b + globalA;
		return globalA;
	}
	else{
		return globalB;
	}
}