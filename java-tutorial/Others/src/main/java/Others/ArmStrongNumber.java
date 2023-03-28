package Others;

import java.util.Scanner;

public class ArmStrongNumber
{
    public static boolean armsStrong(int val)
    {
        int result = 0;
        int temp = val;
        while (temp > 0)
        {
            System.out.println("inital temp :" + temp);
            int reminder = temp % 10;
            System.out.println("inital reminder :" + reminder);
            result = ((int) (Math.pow(reminder, 3)) + result);
            System.out.println("result :" + result);
            temp = temp / 10;
        }
        System.out.println("final result: " + result);
        return result == val;
    }

    public static void main(String[] args)
    {
        System.out.println("Enter the value: ");
        Scanner scanner = new Scanner(System.in);
        int input = scanner.nextInt();
        System.out.println("Given number is armsstrong or not " + armsStrong(input));
    }

}
