package Others;

public class Person
{
    private String name;
    private int age;
    private Address address;

    public String getName()
    {
        return name;
    }

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Address getAddress()
    {
        return address;
    }

    public void setAddress(Address address)
    {
        this.address = address;
    }

    public class Address
    {
        private Integer doorNo;
        private String streetName;
        private String city;
        private String Country;

        public Integer getDoorNo()
        {
            return doorNo;
        }

        public String getStreetName()
        {
            return streetName;
        }

        public String getCity()
        {
            return city;
        }

        public String getCountry()
        {
            return Country;
        }

        public void setDoorNo(Integer doorNo)
        {
            this.doorNo = doorNo;
        }

        public void setStreetName(String streetName)
        {
            this.streetName = streetName;
        }

        public void setCity(String city)
        {
            this.city = city;
        }

        public void setCountry(String country)
        {
            Country = country;
        }

        @Override
        public String toString()
        {
            return "Address has :" + doorNo + " ," + streetName + " ," + city + " ," + Country;
        }

    }
}
