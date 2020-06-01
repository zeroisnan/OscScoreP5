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
 * This is an OscP5 class with recording and playback capabilities.
 *
 * <p>
 * Incoming OSC messages are recorded into a XML file (the score) and played
 * back by reading the XML file and generating OSC messages locally.
 * <p>
 * A typical use case is a Processing sketch controlled via OSC messages from
 * external agents like SuperCollider, MAX, TouchOsc, OSSIA score, etc. By
 * recording the incoming message stream, it is possible to reproduce the sketch
 * evolution without the external agent that generated the messages. This can be
 * particularly convenient when doing non real time rendering of the sketch, as
 * the same control messages will be received in the same frame rather than at
 * the same time, thus compensating for the slower reproduction speed.
 * <p>
 * This class can also be used as an OscP5 with loopback capabilities to be
 * attached to the {@link ScorePlayer} class. This is needed when implementing
 * lower level functionality bypassing the rec() and play() facilities offered
 * by this class.
 */
public class OscScoreP5 extends OscP5 implements OscLoopback {
  /** package version string */
  public final static String VERSION = "##library.prettyVersion##";
  /** local address */
  protected NetAddress loopback;
  /** debug flag */
  protected boolean debug;
  /** handle to score player (when enabled) */
  protected ScorePlayer player;
  /** handle to score recorder (when enabled) */
  protected ScoreRecorder recorder;

  /**
   * constructor (full blown)
   *
   * @param p reference to the parent sketch applet
   * @param localport UDP port where packets are received
   * @param debug enable/disable debug
   */
  public OscScoreP5(PApplet p, int localport, boolean debug) {
    // TODO check port number and throw exception if bas
    super(p, localport);
    this.loopback = new NetAddress("127.0.0.1", localport);
    this.setDebug(debug);
  }

  /**
   * constructor (default local port 12000)
   *
   * @param p reference to the parent sketch applet
   * @param debug enable/disable debug
   */
  public OscScoreP5(PApplet p, boolean debug) {
    this(p, 12000, debug);
  }

  /**
   * constructor (default local port 12000, no debug)
   *
   * @param p reference to the parent sketch applet
   */
  public OscScoreP5(PApplet p) {
    this(p, 12000, false);
  }

  @Override
  public void loopback(OscPacket msg) {
    super.send(msg, this.loopback);
  }

  /**
   * @return the version of the library.
   */
  @Override
  public String version() {
    return String.format("%s_OscP5%s", VERSION, super.version());
  }

  /**
   * @return debug flag value
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * enable/disable debug
   *
   * @param debug debug flag value
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
    if (this.player != null) {
      this.player.setDebug(debug);
    }
  }

  /**
   * playback the OSC score
   *
   * @param xmlpath path to XML OSC score file
   */
  public void play(String xmlpath) {
    this.player = new ScorePlayer((PApplet) this.parent, xmlpath, this,
        this.debug);
  }

  /**
   * rewind the OSC score
   */
  public void rewind() {
    if (this.player != null) {
      System.out.println(String.format("OscScoreP5: rewinding the score at %s",
          this.player.getScorePath()));
      this.player.rewind();
    } else {
      System.out.println(
          "OscScoreP5: rewind requested, but not score loaded for playback...");
    }
  }

  /**
   * Record incoming OSC packets
   *
   * @param xmlpath path to XML OSC score file
   */
  public void rec(String xmlpath) {
    this.recorder = new ScoreRecorder((PApplet) this.parent, xmlpath);
    this.addListener(this.recorder);
  }

}
