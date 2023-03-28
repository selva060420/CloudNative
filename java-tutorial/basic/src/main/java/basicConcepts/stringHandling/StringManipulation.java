package basicConcepts.stringHandling;

public class StringManipulation
{
    public static void stringBuilder(String value)
    {
        StringBuilder builder = new StringBuilder(value);
        StringBuilder builder1 = new StringBuilder("selv");
        System.out.println("Output: " + builder);
    }

    public static void main(String[] args)
    {
//		stringBuilder(new String("selva"));
        stringBuilder("selva");
    }
}
