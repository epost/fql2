package catdata.ide;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class Examples {

	private static Example[] examples;
	public static Example[] getAllExamples() {
		if (examples != null) {
			return examples;
		}
		
		List<Example> e = new LinkedList<>();
		for (Language l : Language.values()) {
			e.addAll(l.getExamples());
		}
		
		examples = e.toArray(new Example[0]);
		Arrays.sort(examples);
		
		return examples;
	}
	
	public static List<Example> getExamples(Class<?> c) {
		List<Example> ret = new LinkedList<>();
		try {
			Field[] fields = c.getFields();
			for (Field field : fields) {
				Example e = (Example) field.get(null);
				ret.add(e);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}
	
	public static Vector<Example> filterBy(String string) {
		Vector<Example> ret = new Vector<>();
		for (Example e : examples) {
			if (e.lang().toString().equals(string) || string.equals("All")) {
				ret.add(e);
			}
		}
		return ret;
	}
}

