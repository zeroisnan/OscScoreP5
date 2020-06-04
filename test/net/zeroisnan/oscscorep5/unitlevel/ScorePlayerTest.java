/**
 * Copyright (C) 2014-present Nico L'Insalata aka zeroisnan
 * SPDX-License-Identifier: LGPL-3.0-or-later
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

import net.zeroisnan.oscscorep5.OscScoreP5;
import net.zeroisnan.oscscorep5.ScorePlayer;

public class ScorePlayerTest extends ScoreBaseTest {
  OscScoreP5 sca;
  OscScoreboard scb;

  @Before
  public void prepareOSCTest() {
    sca = new OscScoreP5(testapplet);
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
    // XML is parsed and played
    for (int i = 0; i < 7; i++) {
      // fetch it from the XML score
      oscplay.pre();
      String currstr = oscplay.toString();
      switch (i) {
        case 0:
          assertEquals(
              "ScorePlayer: Debug mode: false Current frame: 0 Scheduled event: Frame: 1 - Packet: null:0 | /aaa/bbb/xyz/3 fsdi",
              currstr);
          break;
        case 1:
          assertEquals(
              "ScorePlayer: Debug mode: false Current frame: 0 Scheduled event: Frame: 2 - Packet: null:0 | /zzz/yyy/aaa ii",
              currstr);
          break;
        case 2:
        case 3:
          assertEquals(
              "ScorePlayer: Debug mode: false Current frame: 0 Scheduled event: Frame: 4 - Packet: null:0 | /addr666 fis",
              currstr);
          break;
        case 4:
        case 5:
          assertThat(currstr, containsString(
              "ScorePlayer: Debug mode: false Current frame: 0 Scheduled event: Frame: 6 - Packet: oscP5.OscBundle@"));
          break;
        case 6:
          assertEquals(
              "ScorePlayer: Debug mode: false Current frame: 0 Scheduled event: NO EVENT",
              currstr);
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

    // only read one event, the first one, which happens at frame 1
    scb.oscExpect(msgs.get(0));
    oscplay.pre();
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
    String xmlempty = Paths.get("test/data/testScorePlayer_empty.xml")
        .toAbsolutePath().toString();
    // hijack stderr
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
    // new a ScorePlayer object pointing to an empty file
    @SuppressWarnings("unused")
    ScorePlayer oscplay = new ScorePlayer(testapplet, xmlempty, sca);
    // check the error message
    assertTrue(
        errContent.toString().contains("ERROR: Invalid OSC score content"));
    // release stderr
    System.setErr(null);
  }

  /**
   * read a truncated XML file
   */
  @Test
  public void invalidXMLInputTruncated() {
    // path to a XML file with truncated content
    String xmltrunc = Paths.get("test/data/testScorePlayer_truncated.xml")
        .toAbsolutePath().toString();

    // hijack stderr
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
    // new a ScorePlayer object pointing to a truncated XML
    ScorePlayer oscplay = new ScorePlayer(testapplet, xmltrunc, sca);
    // check the error message
    try {
      oscplay.pre();
    } catch (Exception e) {
      assertTrue(
          errContent.toString().contains("ERROR: Invalid OSC score content"));
    } finally {
      // release stderr
      System.setErr(null);
    }
  }

  /**
   * read a malformed XML file (wrong arg specifier)
   */
  @Test
  public void invalidXMLInputBadArgs() {
    // path to a XML file with truncated content
    String xmlbadargs = Paths.get("test/data/testScorePlayer_invalidargs.xml")
        .toAbsolutePath().toString();
    // hijack stderr
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
    // new a ScorePlayer object pointing to a XML with a bad argument
    // specifier
    ScorePlayer oscplay = new ScorePlayer(testapplet, xmlbadargs, sca);
    // invalid argument is on second message, so expect the first one
    scb.oscExpect(msgs.get(0));
    // check the error message
    try {
      oscplay.pre();
    } catch (Exception e) {
      assertTrue(
          errContent.toString().contains("ERROR: Invalid OSC score content"));
    } finally {
      // release stderr
      System.setErr(null);
    }
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
