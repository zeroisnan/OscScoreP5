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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.zeroisnan.oscscorep5.ScoreEvent;

public class ScoreEventTest extends ScoreBaseTest {

  /**
   * ScoreEvent full testing
   */
  @Test
  public void testOscScoreEvent() {
    // initialize with element #2 and later change it to #4
    ScoreEvent eee = new ScoreEvent(2, msgs.get(2));

    // test setters against getters
    eee.setFrame(4);
    assertEquals("Frame", 4, eee.getFrame());

    eee.setPkt(msgs.get(4));
    OscScoreboard.compare(msgs.get(4), eee.getPkt());

    // test toString()
    assertTrue("Unexpected toString() results: " + eee.toString(),
        eee.toString().equals("Frame: 4 - Packet: null:0 | /base/addr1 sssi"));

  }
}
