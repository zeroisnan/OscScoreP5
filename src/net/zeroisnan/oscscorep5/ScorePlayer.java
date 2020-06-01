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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import processing.core.PApplet;

/**
 * Replay the content of a XML score generated with {@link ScoreRecorder}.
 *
 * <p>
 * The recommended usage model is through the play() function of the
 * {@link OscScoreP5} class. If a finer level control is required, a ScorePlayer
 * object can be created directly and shall be connected to an OscP5 object
 * implementing the {@link OscLoopback} interface, like {@link OscScoreP5}.
 *
 * <p>
 * Low level usage example:
 *
 * <pre>
 * // instantiate an OscP5 object implementing he {@link OscLoopback} interface,
 * // like {@link OscScoreP5} as ScorePlayer needs a loopback() method to replay
 * // the messages
 * OscScoreP5 sca = new OscScoreP5(this, localport);
 *
 * // create an ScorePlayer instance to replay the content of a XML score
 * ScorePlayer oscplay = new ScorePlayer(this, &quot;mydump.xml&quot;, sca);
 * </pre>
 */
public class ScorePlayer {
  /** reference to the parent PApplet */
  protected PApplet pp;
  /** local frame count */
  protected int framecount;
  /** path to the XML score */
  protected String xmlscore;
  /** XML event reader */
  protected XMLEventReader xer;
  /** used for unmarshalling */
  protected Unmarshaller unmarshaller;

  /** hold a list of events currently scheduled */
  protected List<ScoreEvent> events;
  /** used to send OSC messages */
  protected OscLoopback sca;
  /** debug attribute */
  protected boolean debug;

  /**
   * constructor (complete)
   *
   * @param p reference to the parent applet
   * @param xmlpath path to XML OSC score file
   * @param sca {@link OscLoopback} instance used to send OSC messages
   * @param debug enable/disable debug while drawing
   */
  public ScorePlayer(PApplet p, String xmlpath, OscLoopback sca,
      boolean debug) {
    this.pp = p;
    // store the path to the XML score
    this.xmlscore = Paths.get(xmlpath).toAbsolutePath().toString();
    this.events = new ArrayList<ScoreEvent>(3);
    this.sca = sca;
    this.setDebug(debug);

    // rewind (initialize in this case) the player
    this.rewind();

    // register the callbacks to automatically send OSC messages when the right
    // frame is reached (and to draw debugging information when enabled)
    pp.registerMethod("pre", this);
    pp.registerMethod("draw", this);
  }

  /**
   * constructor (debug off)
   *
   * @param p reference to the parent applet
   * @param xmlpath path to XML OSC score file
   * @param sca {@link OscLoopback} instance used to send OSC messages
   */
  public ScorePlayer(PApplet p, String xmlpath, OscLoopback sca) {
    this(p, xmlpath, sca, false);
  }

  /**
   * @return debug is enabled
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * @param debug enable/disable debug while drawing
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * @return path to Osc score
   */
  public String getScorePath() {
    return this.xmlscore;
  }

  /**
   * parse the XML score looking for the next event and update the relevant
   * fields in the class (next framecount and next OSC message to send) which
   * can then be accessed with getNextFrame() and getNextMsg()
   */
  protected boolean next() {
    boolean retvalue = false;

    try {
      if (xer.peek().isStartElement()) {
        JAXBElement<ScoreDataPacket> jb = this.unmarshaller.unmarshal(xer,
            ScoreDataPacket.class);
        this.events.add(jb.getValue().toScoreEvent());
        retvalue = true;
      } else {
        retvalue = false;
      }
    } catch (JAXBException | XMLStreamException e) {
      // TODO Auto-generated catch block - issue error
      e.printStackTrace();
      retvalue = false;
    }

    return retvalue;
  }

  /**
   * fetch/schedule/execute events from the current score
   */
  public void pre() {
    this.framecount++;

    // is the event list empty ?
    if (this.events.isEmpty()) {
      // try to fetch something
      if (!this.next()) {
        // if there is nothing to fetch, there is not much to do here
        return;
      }
    }

    // if we get here there is something in the event list: at what frame should
    // we schedule the next event?
    int execFrame = this.events.get(0).getFrame();
    if (execFrame == this.framecount) {
      // hey, it's your moment!
      sca.loopback(this.events.remove(0).getPkt());
    }
  }

  /**
   * rewind the score and restart the player
   */
  public void rewind() {
    this.framecount = -1;
    this.events.clear();

    try {
      // create the stream reader
      XMLInputFactory xif = XMLInputFactory.newInstance();
      StreamSource xml = new StreamSource(this.xmlscore);
      this.xer = xif.createXMLEventReader(xml);
      System.out.println(
          String.format("ScorePlayer: reading XML at %s", this.xmlscore));

      // advance the stream and position the pointer right before the first
      // packet
      do {
        xer.nextEvent();
      } while (!xer.peek().asStartElement().getName().getLocalPart()
          .equals("oscpacket"));

      // initialize the unmarshaller
      JAXBContext jc = JAXBContext.newInstance(ScoreDataPacket.class);
      this.unmarshaller = jc.createUnmarshaller();

    } catch (XMLStreamException | JAXBException e) {
      e.printStackTrace();
      System.err.println("ERROR: Invalid XML input: " + e.getMessage());
    }
  }

  /**
   * When debug mode is enabled, this function will display debug information on
   * the screen. (Registered in the PApplet as post-draw method)
   */
  public void draw() {
    if (this.isDebug()) {
      String timeInfo = String.format("elapsed time: %.2fs",
          pp.millis() / 1000.0);

      String frameInfo = String.format("current frame: %d - frame rate: %.1f",
          pp.frameCount, pp.frameRate);

      String eventInfo = String.format("Next scheduled event: %s\n",
          (this.events.size() > 0) ? this.events.get(0).getFrame() : -1);

      pp.textSize(12);
      pp.fill(255, 255, 0);
      pp.text(timeInfo, 5, pp.height - 40);
      pp.text(frameInfo, 5, pp.height - 25);
      pp.text(eventInfo, 5, pp.height - 10);
    }
  }

  /**
   * @return string representation to ease drawing of realtime debug information
   */
  @Override
  public String toString() {
    String str = "";
    str += "ScorePlayer: ";
    str += String.format("Debug mode: %b ", this.isDebug());
    str += String.format("Current frame: %d ", this.pp.frameCount);
    str += (this.events.isEmpty()) ? "Scheduled event: NO EVENT"
        : String.format("Scheduled event: %s", this.events.get(0).toString());
    return str;
  }
}
