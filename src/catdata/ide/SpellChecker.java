package catdata.ide;



import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.Element;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;


public class SpellChecker extends AbstractParser {
	
	private static Collection<String> words0;

	private boolean spPrev = GlobalOptions.debug.general.spellcheck;
	
	private static Collection<String> getWords() {
		if (words0 != null) {
			return words0;
		}
		words0 = new HashSet<>();
		InputStream in = SpellChecker.class.getResourceAsStream("/words.txt"); 
		if (in == null) {
			System.err.println("Warning: no words for spellchecker found.  If you are building from source, make sure words.txt is on the classpath.");
			return words0;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			String line;
			while ((line = reader.readLine()) != null) {
				words0.add(line);
			}				
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return words0;
	}
	
	private DefaultParseResult result;
	private RSyntaxDocument doc;
	private Collection<String> local;

	public SpellChecker(Collection<String> local) {
		result = new DefaultParseResult(this);
		this.local = local;
	}


	private final int getLineOfOffset(int offs) {
		return doc.getDefaultRootElement().getElementIndex(offs);
	}

	private static Pattern pattern = Pattern.compile("\\S+");
	
	
	@Override
	public ParseResult parse(RSyntaxDocument doc1, String style) {
		boolean spNow = GlobalOptions.debug.general.spellcheck;
		if (spNow != spPrev) {
			spPrev = spNow;
			result.clearNotices();
			return parse(doc1, style);
		}
		spPrev = spNow;
		this.doc = doc1;
		Element root = doc.getDefaultRootElement();
		int lineCount = root.getElementCount();
		result.clearNotices();
		result.setParsedLines(0, lineCount-1);
		if (!spNow) {
			return result;
		}
		
		
		for (Token t : doc) {
			if (t.isComment()) {
				int startOffs = t.getOffset();
				String comment = t.getLexeme();
				if (comment.startsWith("//")) {
					comment = comment.substring(2);
					startOffs += 2;
				}
				Matcher matcher = pattern.matcher(comment); 
				outer: while (matcher.find()) {
					String word = matcher.group().toLowerCase();
					if (word.endsWith(".") || word.endsWith(",") || word.endsWith("!") || word.endsWith(";") || word.endsWith(":") || word.endsWith(")") || word.endsWith("]") || word.endsWith("\"") || word.endsWith("'")) {
						word = word.substring(0, word.length() - 1);
					} 
					if (word.startsWith("(") || word.startsWith("[") || word.startsWith("\"") || word.startsWith("'")) {
						word = word.substring(1);
						startOffs += 1;
					}
					if (word.startsWith("//")) {
						word = word.substring(2);
						startOffs += 2;
					}
					for (int i = 0; i < 10; i++) {
						if (word.contains(Integer.toString(i))) {
							continue;
						}
					}
					for (String a : local) {
						if (word.equals(a)) {
							continue outer;
						}
					}
					
					if (word.contains(",") || word.contains("/") || word.contains("*") || word.contains("-") || word.contains("{") || word.contains("}")) {
						continue;
					}
						

					if (!getWords().contains(word) &&  !word.contains("<") && !word.contains(">")&& !word.contains("=") && !word.contains("_") && !word.contains("@") && !word.contains("\"") && !word.contains("'") && !word.contains("\\") && !word.contains("%") && !word.contains(".") && !word.contains("$") && !word.contains("^")) {
						spellingError(word, startOffs + matcher.start());
					} 
				}
			}
		}
		return result;

	}


	
	private void spellingError(String word, int off) {
	//	System.out.println("error on " + word + " " + off);
		//int offs = startOffs + off;
		int line = getLineOfOffset(off);
		String text = word; //noticePrefix + word + noticeSuffix;
		SpellingParserNotice notice =
			new SpellingParserNotice(this, text, line, off, word.length());
		result.addNotice(notice);
		
	}


	private static class SpellingParserNotice extends DefaultParserNotice {
	
		public SpellingParserNotice(SpellChecker parser, String msg,
									int line, int offs, int len
									) {
			super(parser, msg, line, offs, len);
			setLevel(Level.WARNING);
		}

		@Override
		public Color getColor() {
			return Color.orange;
		}

	}

}
