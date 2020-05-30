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

import static org.junit.Assert.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import oscP5.*;

/**
 * Log all received messages and compare them against a queue of expected
 * messages. Instances of the OscScoreboard needs to be registered as listeners
 * on the class being tested
 */
class OscScoreboard implements OscEventListener {
  /** queue of expected messages */
  BlockingQueue<OscMessage> expd;
  /** queue of received messages */
  BlockingQueue<OscMessage> rcvd;

  public static void compare(OscPacket exp, OscPacket act) {
    // check both expected and actual are of the same kind
    assertTrue(String.format("Expected %s Actual %s",
        exp.getClass().toString(), act.getClass().toString()), act.getClass()
        .equals(exp.getClass()));

    if (exp instanceof OscBundle) {
      // comparing a bundle
      OscBundle b_exp = (OscBundle) exp;
      OscBundle b_act = (OscBundle) act;
      assertEquals("Bundle size mismatch", b_exp.size(), b_act.size());
      for (int i = 0; i < b_act.size(); i++) {
        OscScoreboard.compare(b_exp.getMessage(i), b_act.getMessage(i));
      }
    } else {
      // comparing two simple messages
      OscMessage i_exp = (OscMessage) exp;
      OscMessage i_act = (OscMessage) act;
      assertEquals("Address pattern must match", i_exp.addrPattern(),
          i_act.addrPattern());
      assertEquals("Type tag must match", i_exp.typetag(), i_act.typetag());
      assertArrayEquals("Arguments must match", i_exp.arguments(),
          i_act.arguments());
    }
  }

  /**
   * constructor
   */
  public OscScoreboard() {
    this.expd = new ArrayBlockingQueue<OscMessage>(16);
    this.rcvd = new ArrayBlockingQueue<OscMessage>(16);
  }

  /**
   * the tests invoke this method to set expectations on received messages
   *
   * @param theOscMessage expected message
   */
  public void oscExpect(OscMessage theOscMessage) {
    // we expect the queues to be overdimensioned, but you never know
    assertTrue(this.expd.offer(theOscMessage));
  }

  @Override
  public void oscStatus(OscStatus arg0) {
    // not implemented
  }

  /**
   * OscP5 automatically invokes this method whenever a message is received.
   * This method simply push the received message in a dedicated queue, as
   * issuing any Assert error from here would cause OscP5 to throw an error to
   * the logger, which would go undetected by JUnit
   *
   * @param theOscMessage inbound message
   */
  @Override
  public void oscEvent(OscMessage theOscMessage) {
    // we expect the queues to be over dimensioned, but you never know
    // note that this error will only appear in the OSC log, however the test
    // will most likely fail due to unbalanced scoreboard
    assertTrue(this.rcvd.offer(theOscMessage));
  }

  /**
   * balance the score board (everything expected was received, nothing
   * unexpected was received) at the end of every test: wait for all messages to
   * be received and check them against the expectation queue, then stop the
   * snooping port
   */
  public void balanceScoreboard() {
    // while we expect something to be received
    while (this.expd.size() > 0) {
      // this is our expectation
      OscMessage expected = this.expd.poll();
      // messages are sent in order, so wait for the next one to be checked
      OscMessage received = null;
      try {
        received = this.rcvd.poll(5L, TimeUnit.SECONDS);
        assertNotNull(
            String.format("Timeout while waiting for message %s",
                expected.toString()), received);
      } catch (InterruptedException e) {
        fail("Test waiting thread was interrupted! Bailing out!");
      }
      // ok we have a message, let's see if expected == received
      compare(expected, received);
    }

    // at this point the received queue is empty
    assertEquals("Received queue must be empty!", 0, this.rcvd.size());

    // wait for 5 other seconds, we should receive nothing!
    OscMessage nothing = null;
    try {
      nothing = this.rcvd.poll(5L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      fail("Test waiting thread was interrupted! Bailing out!");
    }

    // null means nothing was received
    assertNull("Unexpected message received!", nothing);
  }
}
