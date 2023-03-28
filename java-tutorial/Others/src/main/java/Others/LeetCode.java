package Others;

public class LeetCode
{
    private static int result;

    public static int recursion(int first, int second, int series)
    {
        while (series == 0)
        {
            System.out.print(result + ".");
            return result;
        }
        result = first + second;
        System.out.print(result + ", ");
        recursion(second, first + second, --series);
        return result;
    }

    public static void main(String[] args) throws Exception
    {
        System.out.print("Fibonacci Series: ");
        System.out.print(0 + ", ");
        System.out.print(1 + ", ");
        recursion(0, 1, 10);

//		Map<Integer, Integer> map=new HashMap<Integer,Integer>();
//		Set<Integer> set=new HashSet<Integer>();
////		int[] arr=new int[] {2,2,3,4,4};
//		int[] arr=new int[5];
//		Scanner scanner=new Scanner(System.in);
//		System.out.println("Enter your values: ");
//		for(int i=0;i<5;i++) {
//			int val=scanner.nextInt();
//			arr[i]=val;
//		}
//		for(int i:arr) {
//			System.out.println("Array Elements: "+i);
//		}
//		for(int i=0;i<arr.length;i++) {
//			if(set.contains(arr[i])) {
//				set.remove(arr[i]);
//			}
//			else {
//				set.add(arr[i]);
//			}
//		}
//		System.out.println("Result: "+set.iterator().next());
    }

}
