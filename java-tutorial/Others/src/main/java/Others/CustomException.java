package Others;

public class CustomException extends Exception
{
    public CustomException(String message)
    {
        super(message);
    }

    public CustomException(Exception e)
    {
        super(e);
    }
}
