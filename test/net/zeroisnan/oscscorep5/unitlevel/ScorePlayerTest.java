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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.zeroisnan.oscscorep5.ScorePlayer;
import net.zeroisnan.saf.base.SCAudio;

public class ScorePlayerTest extends ScoreBaseTest {
  SCAudio sca;
  OscScoreboard scb;

  @Before
  public void prepareOSCTest() {
    sca = new SCAudio(testapplet);
    // create the scoreboard
    scb = new OscScoreboard();
    sca.addListener(scb);

  }

  @After
  public void shutdownOSCTest() {
    scb.balanceScoreboard();
    sca.stop();
  }

  /**
   * parse a XML score and check the generated OSC messages
   */
  @Test
  public void testScorePlayback() {
    // path to the input XML score
    String xmlscore = Paths.get("test/data/testScorePlayer.xml")
        .toAbsolutePath().toString();

    // build the object under test
    ScorePlayer oscplay = new ScorePlayer(testapplet, xmlscore, sca);

    // all these messages are expected
    for (int i = 0; i < msgs.size(); i++) {
      scb.oscExpect(msgs.get(i));
    }

    // 6 is the highest framecount in the reference file,
    // so step forward the player 6 times to ensure the whole
    // XML is parsed and replayed
    for (int i = 0; i < 7; i++) {
      // fetch it from the XML score
      oscplay.pre();
      switch (i) {
        case 2:
          assertEquals(
              "ScorePlayer: Debug mode: false Current frame: 0 Scheduled event: Frame: 3 - Packet: null:0 | /addr666 fis",
              oscplay.toString());
          break;
        case 3:
        case 6:
          assertEquals(
              "ScorePlayer: Debug mode: false Current frame: 0 Scheduled event: NO EVENT",
              oscplay.toString());
          break;
        case 4:
        case 5:
          assertThat(oscplay.toString(), containsString(
              "ScorePlayer: Debug mode: false Current frame: 0 Scheduled event: Frame: 6 - Packet: oscP5.OscBundle@"));

          break;
      }
    }
    scb.balanceScoreboard();

    // keep advancing the player, nothing will happen
    for (int i = 0; i < 5; i++) {
      oscplay.pre();
    }
    scb.balanceScoreboard();

    // rewind the player
    oscplay.rewind();

    // only read one event, the first one, which happens at frame 0
    scb.oscExpect(msgs.get(0));
    oscplay.pre();
    scb.balanceScoreboard();

    // rewind again
    oscplay.rewind();

    // read everything in full, everything should be there
    // plus some extra cycle when nothing will happen
    // let the end of test code do the last balance
    for (int i = 0; i < msgs.size(); i++) {
      scb.oscExpect(msgs.get(i));
    }
    for (int i = 0; i < 15; i++) {
      oscplay.pre();
    }
  }

  /**
   * read an empty score
   */
  @Test
  public void invalidXMLInputEmpty() {
    // path to a XML file which is actually empty
    String xmlempty = Paths.get("test/data/testOscScoreReader_empty.xml")
        .toAbsolutePath().toString();

    // hijack stderr
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
    // new a ScorePlayer object pointing to an empty file
    @SuppressWarnings("unused")
    ScorePlayer oscplay = new ScorePlayer(testapplet, xmlempty, sca);
    // check the error message
    assertTrue(errContent.toString().contains("ERROR: Invalid XML input"));
    // release stderr
    System.setErr(null);
  }

  /**
   * read a truncated XML file
   */
  @Test
  public void invalidXMLInputTruncated() {
    // path to a XML file with truncated content
    String xmltrunc = Paths.get("test/data/testOscScoreReader_truncated.xml")
        .toAbsolutePath().toString();

    // hijack stderr
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
    // new a ScorePlayer object pointing to a truncated XML
    @SuppressWarnings("unused")
    ScorePlayer oscplay = new ScorePlayer(testapplet, xmltrunc, sca);
    // check the error message
    assertTrue(errContent.toString().contains("ERROR: Invalid XML input"));
    // release stderr
    System.setErr(null);
  }

  /**
   * read a malformed XML file (wrong arg specifier)
   */
  @Test
  public void invalidXMLInputBadArgs() {
    // path to a XML file with truncated content
    String xmlbadargs = Paths
        .get("test/data/testOscScoreReader_invalidargs.xml").toAbsolutePath()
        .toString();

    // hijack stderr
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
    // new a ScorePlayer object pointing to a XML with a bad argument
    // specifier
    @SuppressWarnings("unused")
    ScorePlayer oscplay = new ScorePlayer(testapplet, xmlbadargs, sca);
    // check the error message
    assertTrue(errContent.toString().contains("ERROR: Invalid XML input"));
    // release stderr
    System.setErr(null);
  }

  /**
   * debug/draw/toString
   */
  @Test
  public void debugMode() {
    // path to the input XML score
    String xmlscore = Paths.get("test/data/testScorePlayer.xml")
        .toAbsolutePath().toString();

    // build the object under test
    ScorePlayer oscplay = new ScorePlayer(testapplet, xmlscore, sca);
    assertEquals(
        "ScorePlayer: Debug mode: false Current frame: 0 Scheduled event: NO EVENT",
        oscplay.toString());

    // can only test draw() when debug is off
    oscplay.draw();

    // set and check debug mode
    assertFalse(oscplay.isDebug());
    oscplay.setDebug(true);
    assertTrue(oscplay.isDebug());
    assertEquals(
        "ScorePlayer: Debug mode: true Current frame: 0 Scheduled event: NO EVENT",
        oscplay.toString());
  }

}
