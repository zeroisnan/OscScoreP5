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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import oscP5.OscBundle;
import oscP5.OscPacket;

// -----------------------------------------------
// these bean classes are used to marshal/unmarshal XML
//-----------------------------------------------
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "oscpacket")
class ScoreDataPacket {
  @XmlElement
  private int framecount;
  @XmlElement(name = "message", type = ScoreDataMessage.class)
  private List<ScoreDataMessage> messages = new ArrayList<ScoreDataMessage>();

  public ScoreDataPacket() {
  }

  public int getFramecount() {
    return framecount;
  }

  public void setFramecount(int framecount) {
    this.framecount = framecount;
  }

  public List<ScoreDataMessage> getMessages() {
    return messages;
  }

  public void setMessages(List<ScoreDataMessage> msgs) {
    this.messages = msgs;
  }

  public void addMsg(ScoreDataMessage mmm) {
    this.messages.add(mmm);
  }

  ScoreEvent toScoreEvent() {
    return new ScoreEvent(this.framecount, this.toOscPacket());
  }

  OscPacket toOscPacket() {
    if (messages.size() > 1) {
      // this is a bundle
      OscBundle o_pkt = new OscBundle();
      for (ScoreDataMessage m : messages) {
        o_pkt.add(m.toOscMessage());
      }
      return o_pkt;
    } else if (messages.size() == 1) {
      // this is a message
      return messages.get(0).toOscMessage();
    } else {
      // something went wrong
      throw new IllegalStateException(
          "No messages found in the ScoreDataPacket object");
    }
  }
}