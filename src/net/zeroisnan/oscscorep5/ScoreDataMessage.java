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

import javax.xml.bind.annotation.*;

import oscP5.OscMessage;

//-----------------------------------------------
//these bean classes are used to marshal/unmarshal XML
//-----------------------------------------------
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "message")
class ScoreDataMessage {
  @XmlAttribute
  private String address;
  @XmlAttribute
  private String typetag;
  @XmlElement(name = "arg", type = ScoreDataArg.class)
  private List<ScoreDataArg> args = new ArrayList<ScoreDataArg>();

  public ScoreDataMessage() {
  }

  public ScoreDataMessage(List<ScoreDataArg> args) {
    this.args = args;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getTypetag() {
    return typetag;
  }

  public void setTypetag(String typetag) {
    this.typetag = typetag;
  }

  public List<ScoreDataArg> getArgs() {
    return args;
  }

  public void setArgs(List<ScoreDataArg> args) {
    this.args = args;
  }

  OscMessage toOscMessage() {
    OscMessage msg = new OscMessage(address);
    for (ScoreDataArg a : args) {
      switch (a.getType()) {
        case "s":
          // <arg type="s">this is a string</arg>
          msg.add(a.getValue());
          break;
        case "i":
          // <arg type="i">11</arg>
          msg.add(Integer.parseInt(a.getValue()));
          break;
        case "f":
          // <arg type="f">3.457</arg>
          msg.add(Float.parseFloat(a.getValue()));
          break;
        case "d":
          // <arg type="d">3.198698469846981</arg>
          msg.add(Double.parseDouble(a.getValue()));
          break;
        default:
          throw new IllegalStateException(String.format(
              "OSC message argument %s is invalid or not supported",
              a.getType()));
      }
    }
    return msg;
  }
}
