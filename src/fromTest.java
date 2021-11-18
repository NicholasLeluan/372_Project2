public class fromTest {
	public static void main(String[] args){
double x = 3.4;
double y = 3.4;
double half = -2 % -y;
boolean flag = false;
half = 100;
if(x > 0){
for(int i = 0; i < half; i += 1){
if(x % i == 0){
flag = true;
break;}
}
if(flag == true){
System.out.print("not prime");

}else{
System.out.print("prime");
}

}else{
System.out.print("not prime");
}

}
}