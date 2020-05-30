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

package net.zeroisnan.oscscorep5;

import netP5.NetAddress;
import oscP5.OscP5;
import oscP5.OscPacket;
import processing.core.PApplet;

/**
 * An OscP5 object with loopback capabilities.
 */
public class OscScoreP5 extends OscP5 implements OscLoopback {
  public final static String VERSION = "##library.prettyVersion##";

  /** local address */
  protected NetAddress loopback;

  /**
   * constructor (full blown)
   *
   * @param p reference to the parent sketch applet
   * @param localport UDP port where packets are received
   */
  public OscScoreP5(PApplet p, int localport) {
    // TODO check port number and throw exception if bas
    super(p, localport);
    this.loopback = new NetAddress("127.0.0.1", localport);
  }

  /**
   * constructor (default local port 12000)
   *
   * @param p reference to the parent sketch applet
   */
  public OscScoreP5(PApplet p) {
    this(p, 12000);
  }

  @Override
  public void loopback(OscPacket msg) {
    super.send(msg, this.loopback);
  }

}
