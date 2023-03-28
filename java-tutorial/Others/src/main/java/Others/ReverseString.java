package Others;

import java.util.Scanner;

public class ReverseString
{
    public static String reverse(String str)
    {
        char[] charArray = str.toCharArray();
        int j = str.length() - 1;
        for (int i = 0; i < (str.length() / 2); i++)
        {
            char temp = charArray[i];
            charArray[i] = charArray[j];
            charArray[j] = temp;
            j--;
        }
        String output = "";
        for (char i : charArray)
        {
            output = output.concat(String.valueOf(i));
        }
        return output;
    }

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the String: ");
        String str = scanner.nextLine();
        System.out.println("Reversed String :" + reverse(str));
    }
}
