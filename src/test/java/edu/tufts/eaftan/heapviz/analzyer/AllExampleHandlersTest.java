/*
 * Copyright 2014 Edward Aftandilian. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.tufts.eaftan.heapviz.analzyer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import edu.tufts.eaftan.heapviz.analzyer.summarizehandler.SummarizeHandler;
import edu.tufts.eaftan.heapviz.summarizer.AllocSiteSummarizer;
import edu.tufts.eaftan.heapviz.summarizer.Summarizer;
import edu.tufts.eaftan.hprofparser.handler.RecordHandler;
import edu.tufts.eaftan.hprofparser.parser.HprofParser;

/**
 * Runs SummarizeHandler on the test file and ensures it doesn't crash.
 */
public class AllExampleHandlersTest {

  private static final String hprofFileRelativePath = "java.hprof";

  @Before
  public void setUp() {
    PrintStream throwawayPrintStream = new PrintStream(new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        // do nothing
      }
    });
    System.setOut(throwawayPrintStream);
    System.setErr(throwawayPrintStream);
  }

  @Test
  public void allocSiteSummarizerDoesntCrash() throws Exception {
    Summarizer allocSiteSummarizer = new AllocSiteSummarizer();
    RecordHandler summarizeHandler = new SummarizeHandler(true, false, true, allocSiteSummarizer);
    runParser(summarizeHandler);
  }

  private static String getAbsolutePathForResource(String relativePath) throws URISyntaxException {
    return new File(ClassLoader.getSystemResource(relativePath).toURI()).getAbsolutePath();
  }

  private static void runParser(RecordHandler handler)
      throws IOException, URISyntaxException {
    String hprofFileAbsolutePath = getAbsolutePathForResource(hprofFileRelativePath);
    HprofParser parser = new HprofParser(handler);
    FileInputStream fs = new FileInputStream(hprofFileAbsolutePath);
    DataInputStream in = new DataInputStream(new BufferedInputStream(fs));
    parser.parse(in);
    in.close();
  }

}
