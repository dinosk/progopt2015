int main() {

    int a;
    int b;
    int c;
    int z;
    int k;
    int p[1];

    a=2;
    b=2;
    p[0] = 0;
    c = (*(&(p[0])));
    z=b;
    k=z+4;
    a = b-3;
    while (a > 0) {
        if(c+1 == 3)
            a++;
        else
            a--;
    }
    p[a+3] = k;

    return a;
}





// int main() {

//     int a;
//     int b;
//     int c;
//     int z;
//     int k;
//     int p[1];

//     a=2;
//     b=2;
//     p[0] = 0;
//     b = (*(&(p[0])));
//     z=b+3;
//     k=z;
//     a = c-3;
//     while (a > 0) {
//         if(c+1 == 3)
//             a++;
//         else
//             a--;
//     }
//     p[a+3] = k;

//     return a;
// }



    // b=a;
    // z=b;
    // z = b+8;
    // c=b+3+z;

    // p[0]=0;
    // // p[b+3+z] = p[z];
    // // c=a-5;
    // if(c==5) {
    //     a=2;
    // }

   // int c[1];
   // int j;
   // int i;
   // int x;

    // b = 4;
    // i = b+7;
    // c[0] = 13;

    // j = &b;
    // i = *j;
    // i = (*(&(c[0])));
    // a = 4;
    // j = b+7;   // T3: j = i; -----> ARA me T2: j is dead
    // x = j;   // T3: x = i;
    // if(!a == 0){
    //     a++;
    //     return a;
    // }
    // else{
    //     a--;
    //     return a;


    // if(!a ==0)
    //     a++;
    // else
    //     a--;
    // if(b == 0) {
    //     b=1;
    // }

    // if(a==0) {}
    //     // return a;
    // else{}
    //     // return a;
    // while(a==4){
    //     a=0;
    // }




// #include <stdio.h>

// int main(){
//     int a=0;
//     int b;
//    int c[1];
//    int* j;
//    int i;
//     // int d;
//     // int z;

//     // a = 0;
//     b = 4;
//     c[0] = 13;
//     // b = c[0]+4;
//     j = &b;
//     i = *j;
//     i = (*(&(c[0])));
//     // i = (*(&b));
//     // printf("%d\n", j);
//     // a = c[0];
//     // c = b * 3;
//     // d = b * 3;
//     // z = d;
//     a = 4;
//     // if(!a == 0)
//     //     a++;
//     // else
//     //     a--;
//     while(a==4){
//         a=0;
//     }

//     return a;
// }

