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

import oscP5.OscPacket;

/**
 * bean class to represent score events
 * <ul>
 * <li>frame : the frame number at which the event is scheduled/captured
 * <li>pkt : OSC packet (message/bundle) describing the event
 * </ul>
 */
public class ScoreEvent {
  /** frame number associated to the event */
  protected int frame;
  /** osc packet (message/bundle) describing the event */
  protected OscPacket pkt;

  public ScoreEvent(int frame, OscPacket pkt) {
    this.frame = frame;
    this.pkt = pkt;
  }

  public int getFrame() {
    return frame;
  }

  public void setFrame(int frame) {
    this.frame = frame;
  }

  public OscPacket getPkt() {
    return this.pkt;
  }

  public void setPkt(OscPacket pkt) {
    this.pkt = pkt;
  }

  @Override
  public String toString() {
    String str = String.format("Frame: %d - Packet: %s", this.frame,
        this.pkt.toString());
    return str;
  }

}
