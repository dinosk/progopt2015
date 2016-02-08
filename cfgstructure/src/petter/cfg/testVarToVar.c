int main(){
	int a;
	int b;
	int c;
	int d;
	int p[1];

	a = 5;
	b = a + 3;
	a = 2;
	c = a + 3;

	p[0] = 0;
	d = p[0];
	while(a > 0){
		if(d == 0){
			b = b + 1;
		}
		a = a - 1;
	}
	return a;
}