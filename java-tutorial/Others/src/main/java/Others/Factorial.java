package Others;

import java.util.Scanner;

public class Factorial
{

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the given value: ");
        int input = scanner.nextInt();
        System.out.println("Factorial of " + input + " is: " + forLoopFactorial(input));
    }

    private static int forLoopFactorial(int input)
    {
        int result = 1;
        while (input > 0)
        {
            result = result * (input--);
        }
        return result;
    }

    private static int recursiveFactorial(int input)
    {
        if (input == 1)
        {
            return 1;
        }
        int result = input * recursiveFactorial(input - 1);
        return result;
    }

}
