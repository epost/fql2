package catdata.aql.examples;

public class AqlQuotientExample extends AqlExample {

	@Override
	public String getName() {
		return "Quotient";
	}

	@Override
	public String getText() {
		return s;
	}
	
	private final String s = "typeside Ty = empty"
			+ "\nschema Likes = literal : Ty {"
			+ "\n	entities"
			+ "\n		Like "
			+ "\n		Person"
			+ "\n	foreign_keys"
			+ "\n		likee : Like -> Person"
			+ "\n		liker : Like -> Person"
			+ "\n}"
			+ "\n"
			+ "\nschema Connections = literal : Ty {"
			+ "\n	entities"
			+ "\n		Connection "
			+ "\n}"
			+ "\n"
			+ "\ninstance SimpsonsLikes = literal : Likes {"
			+ "\n	generators"
			+ "\n		Ned Maud Rodd Todd MrBurns Smithers : Person"
			+ "\n		l1 l2 l3 l4 : Like"
			+ "\n	equations"
			+ "\n		l1.liker = Ned  l1.likee = Maud"
			+ "\n		l2.liker = Maud l2.likee = Rodd"
			+ "\n		l3.liker = Rodd l3.likee =  Todd"
			+ "\n		"
			+ "\n		l4.liker = Smithers l4.likee = MrBurns"
			+ "\n}"
			+ "\n"
			+ "\nmapping FindConnections = literal : Likes -> Connections {"
			+ "\n	entities"
			+ "\n		Person -> Connection"
			+ "\n		Like -> Connection"
			+ "\n	foreign_keys"
			+ "\n		likee -> Connection"
			+ "\n		liker -> Connection	"
			+ "\n}"
			+ "\n"
			+ "\ninstance SimpsonsConnections = sigma FindConnections SimpsonsLikes"
			+ "\n"
			+ "\ntransform whichConnection = unit FindConnections SimpsonsLikes"
			+ "\n";



}
