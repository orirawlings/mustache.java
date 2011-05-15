package com.sampullara.mustache;

import com.sampullara.util.FutureWriter;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

/**
 * TODO: Edit this
 * <p/>
 * User: sam
 * Date: 5/14/11
 * Time: 9:28 PM
 */
public class BenchmarkTest extends TestCase {
  private File root;

  protected void setUp() throws Exception {
    super.setUp();
    root = new File("src/test/resources");
  }

  protected String getContents(File root, String file) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(root, file)),"UTF-8"));
    StringWriter capture = new StringWriter();
    char[] buffer = new char[8192];
    int read;
    while ((read = br.read(buffer)) != -1) {
      capture.write(buffer, 0, read);
    }
    return capture.toString();
  }

  public void testComplex() throws MustacheException, IOException {
    {
      long start = System.currentTimeMillis();
      MustacheInterpreter c = new MustacheInterpreter(root);
      Mustache m = c.parseFile("complex.html");
      System.out.println("Interpreted compilation: " + (System.currentTimeMillis() - start));
      complextest(m);
      start = System.currentTimeMillis();
      long end;
      int total = 0;
      while (true) {
        complextest(m);
        end = System.currentTimeMillis();
        total++;
        if (end - start > 1000) break;
      }
      System.out.println("Interpreted: " + total);
    }
    {
      long start = System.currentTimeMillis();
      MustacheCompiler c = new MustacheCompiler(root);
      Mustache m = c.parseFile("complex.html");
      System.out.println("Native compilation: " + (System.currentTimeMillis() - start));
      complextest(m);
      start = System.currentTimeMillis();
      long end;
      int total = 0;
      while (true) {
        complextest(m);
        end = System.currentTimeMillis();
        total++;
        if (end - start > 1000) break;
      }
      System.out.println("Compiler: " + total);
    }
  }

  private StringWriter complextest(Mustache m) throws MustacheException, IOException {
    Scope scope = new Scope(new Object() {
      String header = "Colors";
      List item = Arrays.asList(
              new Object() {
                String name = "red";
                boolean current = true;
                String url = "#Red";
              },
              new Object() {
                String name = "green";
                boolean current = false;
                String url = "#Green";
              },
              new Object() {
                String name = "blue";
                boolean current = false;
                String url = "#Blue";
              }
      );

      boolean link(Scope s) {
        return !((Boolean) s.get("current"));
      }

      boolean list(Scope s) {
        return ((List) s.get("item")).size() != 0;
      }

      boolean empty(Scope s) {
        return ((List) s.get("item")).size() == 0;
      }
    });
    StringWriter sw = new StringWriter();
    FutureWriter writer = new FutureWriter(sw);
    m.execute(writer, scope);
    writer.flush();
    return sw;
  }
}
