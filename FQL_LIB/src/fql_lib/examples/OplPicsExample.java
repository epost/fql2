package fql_lib.examples;

public class OplPicsExample extends Example {
	
	@Override
	public String isPatrick() {
		return "OPL";
	}

	@Override
	public String getName() {
		return "O Team Pics";
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
			+ "\n		ri -> \"return new javax.swing.JLabel(new javax.swing.ImageIcon(new java.net.URL(\\\"http://wisnesky.net/pic.jpg\\\")));\",						"
			+ "\n		di -> \"return new javax.swing.JLabel(new javax.swing.ImageIcon(new java.net.URL(\\\"http://math.mit.edu/images/gallery/postdoc/spivak-david.jpg\\\")));\","
			+ "\n		ci -> \"return new javax.swing.JLabel(new javax.swing.ImageIcon(new java.net.URL(\\\"http://math.mit.edu/images/gallery/postdoc/vasilakopoulou.png\\\")));\","
			+ "\n		ji -> \"return new javax.swing.JLabel(new javax.swing.ImageIcon(new java.net.URL(\\\"http://www.joshuatan.com/wp-content/uploads/2014/11/cropped-IMG-0823.jpg\\\")));\","
			+ "\n		rn -> \"return \\\"Ryan\\\";\","
			+ "\n		dn -> \"return \\\"David\\\";\","
			+ "\n		pn -> \"return \\\"Patrick\\\";\","
			+ "\n		cn -> \"return \\\"Christina\\\";\","
			+ "\n		jn -> \"return \\\"Josh\\\";\""
			+ "\n		;			"
			+ "\n} : T"
			+ "\n"
			+ "\nI = instance S I0 M"
			+ "\n";



}
