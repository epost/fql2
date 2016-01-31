package fql_lib.examples;

import fql_lib.core.Example;
import fql_lib.core.Language;

public class OplPicsExample extends Example {
	
	@Override
	public Language lang() {
		return Language.OPL;
	}

	@Override
	public String getName() {
		return "Team Pics";
	}

	@Override
	public String getText() {
		return s;
	}
	
	String s = "S0 = theory { "
			+ "\n sorts"
			+ "\n 	Person, Image, String;"
			+ "\n symbols"
			+ "\n 	cn, dn, rn, pn, jn : String,"
			+ "\n 	ci, di, ri, ji : Image,"
			+ "\n 	picture : Person -> Image,"
			+ "\n 	name : Person -> String;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\nS = schema {"
			+ "\n	entities Person;	"
			+ "\n} : S0"
			+ "\n"
			+ "\nT = types S"
			+ "\n"
			+ "\nI0 = presentation {"
			+ "\n	generators c, d, r, p, j : Person;"
			+ "\n	equations picture(d) = di, picture(r) = ri, picture(j) = ji, picture(c) = ci,"
			+ "\n			name(p) = pn, name(d) = dn, name(r) = rn, name(c) = cn, name(j) = jn;"
			+ "\n} : S0"
			+ "\n"
			+ "\nM = javascript {"
			+ "\n	symbols"
			+ "\n	    _preamble -> \"var image = Java.type('fql_lib.opl.OplImage');\"," 
			+ "\n		ri -> \"return new image('http://wisnesky.net/pic.jpg')\",						"
			+ "\n		di -> \"return new image('http://math.mit.edu/images/gallery/postdoc/spivak-david.jpg')\","
			+ "\n		ci -> \"return new image('http://math.mit.edu/images/gallery/postdoc/vasilakopoulou.png')\","
			+ "\n		ji -> \"return new image('http://www.joshuatan.com/wp-content/uploads/2014/11/cropped-IMG-0823.jpg')\","
			+ "\n		rn -> \"return 'Ryan'\","
			+ "\n		dn -> \"return 'David'\","
			+ "\n		pn -> \"return 'Patrick'\","
			+ "\n		cn -> \"return 'Christina'\","
			+ "\n		jn -> \"return 'Josh'\""
			+ "\n		;			"
			+ "\n} : T"
			+ "\n"
			+ "\nI = instance S I0 M"
			+ "\n";



}
