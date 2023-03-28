package Others.serialization;

import java.io.Serializable;

public class SerializationTest
{
    public static void main(String[] args)
    {
        Employee emp = new Employee();
        emp.setDepartment("Sales");
        emp.setEmployeeId(0);
        emp.setEmployeeName("Joe");
        System.out.println("employee obj: " + emp);
    }
}

class Employee implements Serializable
{

    private static final long serialVersionUID = 1L;
    int employeeId;
    String employeeName;
    String department;

    public int getEmployeeId()
    {
        return employeeId;
    }

    public void setEmployeeId(int employeeId)
    {
        this.employeeId = employeeId;
    }

    public String getEmployeeName()
    {
        return employeeName;
    }

    public void setEmployeeName(String employeeName)
    {
        this.employeeName = employeeName;
    }

    public String getDepartment()
    {
        return department;
    }

    public void setDepartment(String department)
    {
        this.department = department;
    }

    @Override
    public String toString()
    {
        return "Employee [employeeId=" + employeeId + ", employeeName=" + employeeName + ", department=" + department + "]";
    }

}
