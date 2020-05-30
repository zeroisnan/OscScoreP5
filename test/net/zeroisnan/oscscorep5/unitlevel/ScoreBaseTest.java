/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * @author      ##author##
 * @version     ##library.prettyVersion## (##library.version##)
 *              Last modified: ##date##
 */

package net.zeroisnan.oscscorep5.unitlevel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;

import oscP5.OscMessage;
import processing.core.PApplet;

/**
 * base class for testing Score related classes
 */
@Ignore
abstract public class ScoreBaseTest {
  /**
   * this is not really an applet, just a non null stub used for unit testing to
   * fulfill the requirements of public interfaces
   */
  static PApplet testapplet;

  /** path to the folder where support files are */
  String testdata = "test/data";

  /** store a number of OSC messages used for testing */
  static List<OscMessage> msgs;

  @BeforeClass
  public static void sketchSetup() {
    testapplet = new PApplet();
  }

  /**
   * build a series of messages to be used for testing
   */
  @BeforeClass
  public static void prepareData() {
    msgs = new ArrayList<OscMessage>();
    OscMessage m = new OscMessage("/aaa/bbb/xyz/3");
    m.add(3.457f);
    m.add("this is a string");
    m.add(3.198698469846981);
    m.add(11);
    msgs.add(m);

    m = new OscMessage("/zzz/yyy/aaa");
    m.add(967);
    m.add(21);
    msgs.add(m);

    m = new OscMessage("/addr666");
    m.add(1.34f);
    m.add(-369868);
    m.add("The quick brown fox jumps over the lazy dog");
    msgs.add(m);

    m = new OscMessage("/a_long_address/pattern/made/up/of/several/parts");
    m.add(-12571.34f);
    m.add("test_string_1");
    m.add("test_string_2");
    m.add("test_string_666");
    m.add(-0.89708751);
    msgs.add(m);

    m = new OscMessage("/base/addr1");
    m.add("a_string");
    m.add("a_string");
    m.add("a_string");
    m.add(-45);
    msgs.add(m);
  }

  /**
   * Check the output of a toString() function call against the content of a
   * reference file
   *
   * @param totest string to test
   * @param reffile name of the file containing the reference text
   */
  protected void checkToString(String totest, String reffile) {
    try {
      // read the reference from file
      String[] ref = Files.readAllLines(Paths.get(testdata + "/" + reffile),
          StandardCharsets.UTF_8).toArray(new String[0]);

      // change the String into an array to use with assertArrayEquals
      String[] totestArray = totest.split("\n");
      // check
      assertArrayEquals(ref, totestArray);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  /**
   * does a line by line comparison of two files and fails in case of
   * differences
   *
   * @param testfile file to test (absolute path is preferable)
   * @param reffile reference file (relative path to test/data folder)
   */
  protected void checkFileDiffs(String testfile, String reffile) {
    try {
      // read the file to test
      String[] chk = Files
          .readAllLines(Paths.get(testfile), StandardCharsets.UTF_8)
          .toArray(new String[0]);
      // read the reference file
      String[] ref = Files.readAllLines(Paths.get(testdata + "/" + reffile),
          StandardCharsets.UTF_8).toArray(new String[0]);
      // check
      assertArrayEquals(ref, chk);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }
}
