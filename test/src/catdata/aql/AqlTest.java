package catdata.aql;

import catdata.Program;
import catdata.Util;
import catdata.aql.exp.AqlParser;
import catdata.aql.exp.AqlEnv;
import catdata.aql.exp.AqlMultiDriver;
import catdata.aql.exp.Exp;
import catdata.ide.Example;
import catdata.ide.Examples;
import catdata.ide.Language;
import java.io.FileReader;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AqlTest {

    // This duplicated some of AqlTester, but as headless unit tests.
    @Test
    public void testAqlExamples() {
        for (Example e : Examples.getExamples(Language.AQL)) {
            System.out.println("testing example: " + e.getName());
            testSourceText("example: " + e.getName(), e.getText());
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    // helpers
    //
    /////////////////////////////////////////////////////////////////////////////////

    public void testSourceText(String description, String src) {
        try {
            Program<Exp<?>> prog = AqlParser.getParser().parseProgram(src);
            testAqlMultiDriver(prog);
        } catch (Exception e) {
            fail("Test failed for test case '" + description + "'.");
            // e.printStackTrace();
            System.out.println("Test failed for test case '" + description + "'.\n" + e.getStackTrace());
            fail("Test failed for test case '" + description + "'.\n" + e.getStackTrace());
        }
    }

    // TODO Code is similar to that in AqlTester and AqlCmdLine; refactor.
    public Program<Exp<?>> parseFileOrNull(String fileName) {
        try (FileReader r = new FileReader(fileName)) {
            String src = Util.readFile(r);
            Program<Exp<?>> prog = AqlParser.getParser().parseProgram(src);
            return prog;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // TODO Code is similar to that in AqlTester and AqlCmdLine; refactor.
    public void testAqlMultiDriver(Program<Exp<?>> prog) {
        AqlMultiDriver driver = new AqlMultiDriver(prog, new String[1], null);
        driver.start();
        AqlEnv lastEnv = driver.env;
        if (lastEnv.exn != null) {
            throw lastEnv.exn;
        }
    }
}
