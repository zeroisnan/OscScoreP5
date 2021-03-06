/**
 * Copyright (C) 2014-present Nico L'Insalata aka zeroisnan
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package net.zeroisnan.oscscorep5.unitlevel;

import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;

import net.zeroisnan.oscscorep5.ScoreRecorder;

public class ScoreRecorderTest extends ScoreBaseTest {

  /**
   * send few OSC messages and check the generated XML against a reference file
   */
  @Test
  public void testXMLdump() {
    // path to the output XML dump generated by the test
    String xmltestfile = Paths.get("testScoreRecorder.xml").toAbsolutePath()
        .toString();

    try {
      // the file may have been left lying around from previous runs, so check
      // if exists, then delete
      File xml2delete = new File(xmltestfile);
      if (xml2delete.exists()) {
        xml2delete.delete();
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }

    // instantiate the class to test
    ScoreRecorder ut = new ScoreRecorder(testapplet, "testScoreRecorder.xml");

    // invoking pre() the first time will initialize the XML writer
    // pre() is implicitly called when running a processing sketch
    // thanks to the registered callback
    ut.pre();
    // invoke pre() a second time, nothing will happen
    ut.pre();

    // send the messages
    for (int i = 0; i < msgs.size(); i++) {
      switch (i) {
        case 2:
          testapplet.frameCount = 13;
          break;
        case 3:
          testapplet.frameCount = 15;
          break;
        case 4:
          testapplet.frameCount = 16;
          break;
      }
      ut.oscEvent(msgs.get(i));
    }

    // wrap up (flush and close) the XML writer; this function is called
    // automatically in a processing sketch thanks to the registered callback
    ut.dispose();

    // check against the reference
    checkFileDiffs(xmltestfile, "testScoreRecorder.ref.xml");
  }
}
