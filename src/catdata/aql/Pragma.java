package catdata.aql;

public abstract class Pragma implements Semantics {

	@Override
	public Kind kind() {
		return Kind.PRAGMA;
	}
	
	public abstract void execute();

}
