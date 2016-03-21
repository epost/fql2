package catdata.fpqlpp;

import java.util.Map;
import java.util.Set;

public class ABox {

	public Object label;
	
	public Set<AInputPort> inputs;
	public Set<AOutputPort> outputs;
	
	public Map<AOutputPort, Object> value;
	
}
