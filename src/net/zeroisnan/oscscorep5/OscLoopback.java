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
 * Objects implementing this interface can send OscPackets to themselves
 *
 */
public interface OscLoopback {
  /**
   * send a message to itself
   *
   * @param msg message to send
   */
  abstract public void loopback(OscPacket msg);

}
