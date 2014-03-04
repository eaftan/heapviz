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
import java.net.URISyntaxException;

import org.junit.Test;

import edu.tufts.eaftan.heapviz.analzyer.summarizehandler.SummarizeHandler;
import edu.tufts.eaftan.heapviz.summarizer.AllocSiteSummarizer;
import edu.tufts.eaftan.heapviz.summarizer.IdentitySummarizer;
import edu.tufts.eaftan.heapviz.summarizer.Softvis2010Summarizer;
import edu.tufts.eaftan.heapviz.summarizer.TypeGraphSummarizer;
import edu.tufts.eaftan.hprofparser.handler.RecordHandler;
import edu.tufts.eaftan.hprofparser.parser.HprofParser;

/**
 * Runs all summarizers on the test file and ensures they don't crash.
 */
public class SummarizersTest {

  private static final String hprofFileRelativePath = "java.hprof";

  @Test
  public void allocSiteSummarizerDoesntCrash() throws Exception {
    RecordHandler summarizeHandler = new SummarizeHandler(
        true, false, true, new AllocSiteSummarizer());
    runParser(summarizeHandler);
  }

  @Test
  public void identitySummarizerDoesntCrash() throws Exception {
    RecordHandler summarizeHandler = new SummarizeHandler(
        true, false, true, new IdentitySummarizer());
    runParser(summarizeHandler);
  }

  @Test
  public void softvis2010SummarizerDoesntCrash() throws Exception {
    RecordHandler summarizeHandler = new SummarizeHandler(
        true, false, true, new Softvis2010Summarizer());
    runParser(summarizeHandler);
  }

  @Test
  public void typeGraphSummarizerDoesntCrash() throws Exception {
    RecordHandler summarizeHandler = new SummarizeHandler(
        true, false, true, new TypeGraphSummarizer());
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
