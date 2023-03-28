package Others;

public class ExceptionHandling
{
    public void greaterThan5(int num) throws CustomException
    {
        if (num > 5)
        {
            System.out.println("success");
        }
        else
        {
            throw new CustomException("lesservalue");
        }
    }

    public static void main(String[] args)
    {
        ExceptionHandling num = new ExceptionHandling();
        try
        {
            num.greaterThan5(2);
        }
        catch (CustomException e)
        {
            e.printStackTrace();
        }
    }
}
