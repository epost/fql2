package catdata.fqlpp;

import java.io.Serializable;
import java.util.function.Function;

public interface FUNCTION<X,Y> extends Function<X,Y>, Serializable {

	public default <Z> FUNCTION<X,Z> andThen(FUNCTION<Y,Z> f) {
		return x -> f.apply(apply(x));
	}
	
}
