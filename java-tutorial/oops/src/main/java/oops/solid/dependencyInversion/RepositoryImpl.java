package oops.solid.dependencyInversion;

interface Repository{
	public void repo();
}
class MySqlRepository implements Repository{

	@Override
	public void repo() {
		System.out.println("This is SQL repo");
	}
	
}
class OracleRepository implements Repository{

	@Override
	public void repo() {
		System.out.println("This is Orcale repo");
	}
	
}
public class RepositoryImpl {
	private Repository repository;
	public RepositoryImpl(Repository repository) {
		this.repository=repository;
	}
	public void process() {
		repository.repo();
	}
	public static void main(String[] args) {
		Repository obj1=new OracleRepository();
		RepositoryImpl result=new RepositoryImpl(obj1);
		result.process();
	}
}
