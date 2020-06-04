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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

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
  protected String xmlfilepath;
  /** XML file stream object (used by the reader) */
  protected FileInputStream xmlfilestream;
  /** XML event reader */
  protected XMLEventReader xer;

  /** used for unmarshalling */
  protected Unmarshaller unmarshaller;

  /** hold a list of events currently scheduled */
  protected Queue<ScoreEvent> events;
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
    this.xmlfilepath = Paths.get(xmlpath).toAbsolutePath().toString();
    try {
      this.xmlfilestream = new FileInputStream(this.xmlfilepath);
    } catch (FileNotFoundException e) {
      System.err
          .println(String.format("ERROR: unable to open OSC score at %s - %s",
              this.xmlfilepath, e.getMessage()));
      return;
    }
    this.events = new LinkedList<ScoreEvent>();
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
    return this.xmlfilepath;
  }

  /**
   * parse the score looking for the next event and push that in the event queue
   *
   * @param num number of events to (attempt to) fetch
   * @return number of events fetched
   */
  protected int fetch(int num) {
    int retvalue = 0;

    for (int cnt = 0; cnt < num; cnt++) {
      try {
        if (xer.peek().isStartElement()) {
          JAXBElement<ScoreDataPacket> jb = this.unmarshaller.unmarshal(xer,
              ScoreDataPacket.class);
          this.events.add(jb.getValue().toScoreEvent());
          retvalue++;
        } else {
          // nothing left to fetch
          break;
        }
      } catch (JAXBException | XMLStreamException e) {
        ScoreUtils.handleException(e, "Invalid OSC score content");
      }
    }

    return retvalue;
  }

  /**
   * fetch/schedule/execute events from the current score
   */
  public void pre() {
    this.framecount++;

    // we want the event queue to underrun as little as possible
    // worst case scenario is one event per frame, so we try to
    // have always frameRate events in queue
    int eventsToFetch = (int) this.pp.frameRate - this.events.size();
    // we don't know what might happen to frameRate, so do constraint the number
    // of events to fetch just in case
    eventsToFetch = PApplet.constrain(eventsToFetch, 0,
        (int) this.pp.frameRate);
    this.fetch(eventsToFetch);

    if (this.events.isEmpty()) {
      // when we land here these is nothing left to fetch and nothing left to
      // schedule
      return;
    }

    // if we get here there is something in the event list: at what frame should
    // we schedule the next event? look at the head of the event queue
    int execFrame = this.events.peek().getFrame();
    if (execFrame == this.framecount) {
      // hey, it's your moment:
      // - pop the element from the queue
      // - get the packet in it
      // - send it over loopback
      sca.loopback(this.events.remove().getPkt());
    }
  }

  /**
   * rewind the score and restart the player
   */
  public void rewind() {
    this.framecount = -1;
    this.events.clear();

    try {
      // rewind the filestream
      this.xmlfilestream.getChannel().position(0);
      // create the XML event reader
      XMLInputFactory xif = XMLInputFactory.newInstance();
      this.xer = xif.createXMLEventReader(this.xmlfilestream);
      // initialize the unmarshaller
      JAXBContext jc = JAXBContext.newInstance(ScoreDataPacket.class);
      this.unmarshaller = jc.createUnmarshaller();
      // advance the reader, pointer right before the first packet
      do {
        xer.nextEvent();
      } while (!xer.peek().asStartElement().getName().getLocalPart()
          .equals("oscpacket"));
    } catch (XMLStreamException | JAXBException | IOException
        | ClassCastException e) {
      ScoreUtils.handleException(e, "Invalid OSC score content");
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

      String frameInfo = String.format(
          "current frame: %d (%d) - frame rate: %.1f", this.framecount,
          pp.frameCount, pp.frameRate);

      String eventInfo = String.format("Next scheduled event: %s\n",
          (this.events.size() > 0) ? this.events.peek().getFrame()
              : "event queue empty");

      pp.textSize(12);
      pp.fill(255, 255, 0);
      pp.text(timeInfo, 5, pp.height - 40);
      pp.text(frameInfo, 5, pp.height - 25);
      pp.text(eventInfo, 5, pp.height - 10);
    }
  }

  /**
   * @return string representation to ease drawing of real time debug
   *         information
   */
  @Override
  public String toString() {
    String str = "";
    str += "ScorePlayer: ";
    str += String.format("Debug mode: %b ", this.isDebug());
    str += String.format("Current frame: %d ", this.pp.frameCount);
    str += (this.events.isEmpty()) ? "Scheduled event: NO EVENT"
        : String.format("Scheduled event: %s", this.events.peek().toString());
    return str;
  }

}
