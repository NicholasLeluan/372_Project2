public class Program2 {
	public static void main(String[] args){
int x = Integer.parseInt(args[0]);
int half = x / 2;
boolean flag = false;
if(x >= 2){
for(int i = 2; i < half; i += 1){
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