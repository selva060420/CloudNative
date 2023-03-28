package Others;

import java.util.Scanner;

public class PrimeNumberOrNot
{
    public static boolean isPrime(int val)
    {
        for (int i = 2; i <= Math.sqrt(val); i++)
        {
            if (val % i == 0)
            {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter given value: ");
        int input = scanner.nextInt();
        System.out.println("PrimeNo or not: " + isPrime(input));
    }

}
