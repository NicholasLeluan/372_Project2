public class Program1 {
	public static void main(String[] args){
int x = Integer.parseInt(args[0]);
int y = Integer.parseInt(args[1]);
int m = Integer.parseInt(args[2]);
int count = 0;
for(int i = 0; i < m; i += 1){
if(i % x == 0){
count = count + 1;
}
if(i % y == 0){
count = count + 1;
}
int num = x * y;
if(i % num == 0){
count = count - 1;
}
}
System.out.print(count);

}
}