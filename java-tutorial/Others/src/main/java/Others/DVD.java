package Others;

public class DVD
{
    public String name;
    public int releaseYear;
    public String director;

    public DVD(String name, int releaseYear, String director)
    {
        this.name = name;
        this.releaseYear = releaseYear;
        this.director = director;
    }

    @Override
    public String toString()
    {
        return this.name + ", directed by " + this.director + ", released in " + this.releaseYear;
    }

    public static void main(String[] args)
    {
        DVD[] dvdCollections = new DVD[15];
        dvdCollections[0] = new DVD("selva", 2022, "selvakumar");
        dvdCollections[1] = new DVD("kumar", 2030, "selvakumar");
        dvdCollections[2] = new DVD("sel", 2040, "selvakumar");
        for (DVD i : dvdCollections)
        {
            System.out.println("output: " + i.toString());
        }
    }

}
