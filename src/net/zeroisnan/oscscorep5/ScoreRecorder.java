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

import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscStatus;
import processing.core.PApplet;

/**
 * Record incoming OSC messages into a XML file. XML files generated with this
 * class can then be played back using {@link ScorePlayer} .
 *
 * <p>
 * The recommended usage model is through the rec() function of the
 * {@link OscScoreP5} class. If a finer level control is required, a
 * ScoreRecorder object can be created directly and registered as an event
 * listener on an OscP5 instance.
 *
 * <p>
 * Low level usage example:
 *
 * <pre>
 * // an OscP5 object for which we want to record incoming messages
 * OscP5 sca = new OscP5(this, 12000);
 * // the recorder
 * ScoreRecorder rec = new ScoreRecorder(this, &quot;mydump.xml&quot;)
 * // register the recorder as an event listener to log incoming OSC messages
 * sca.addListener(rec);
 * </pre>
 *
 * <p>
 * The XML schema used to represent OSC messages is inspired by
 * <a href="http://opensoundcontrol.org/publication/bidirectional-xml-mapping">
 * this work</a> by Ben Chun. The frameCount variable from the Processing applet
 * is used as a timestamp.
 *
 * <pre>
 * &lt;oscscore generator="processing sketch name"&gt;
 *   &lt;oscpacket&gt;
 *     &lt;framecount&gt;34&lt;/framecount&gt;
 *     &lt;message address='/zzz/yyy/xxx' typetag='fsdi'&gt;
 *       &lt;arg type="f" value="3.457"/&gt;
 *       &lt;arg type="s" value="this is a string"/&gt;
 *       &lt;arg type="d" value="3.198698469846981"/&gt;
 *       &lt;arg type="i" value="11"/&gt;
 *     &lt;/message&gt;
 *   &lt;/oscpacket&gt;
 *   &lt;oscpacket&gt;
 *     &lt;framecount&gt;38&lt;/framecount&gt;
 *     &lt;message address='/zzz/yyy/aaa' typetag='ii'&gt;
 *       &lt;arg type="i" value="967"/&gt;
 *       &lt;arg type="i" value="21"/&gt;
 *     &lt;/message&gt;
 *   &lt;/oscpacket&gt;
 *   &lt;oscpacket&gt;
 *     &lt;framecount&gt;46&lt;/framecount&gt;
 *     &lt;message address='/zzz/yyy/aaa' typetag='s'&gt;
 *       &lt;arg type="s" value="a_string"/&gt;
 *     &lt;/message&gt;
 *     &lt;message address='/zzz/yyy/bbb' typetag='ii'&gt;
 *       &lt;arg type="i" value="-1"/&gt;
 *       &lt;arg type="i" value="3"/&gt;
 *     &lt;/message&gt;
 *   &lt;/oscpacket&gt;
 * &lt;/oscscore&gt;
 * </pre>
 *
 */
public class ScoreRecorder implements OscEventListener {
  /** reference to Processing parent applet */
  protected PApplet parent;
  /** used to implement some pre-draw actions only once */
  protected boolean pre_done;
  /** next event (OscPacket) to stream to XML */
  protected ScoreDataPacket pkt2write;

  /** XML writer */
  protected XMLStreamWriter xtw;
  /** path to the output XML file */
  protected String xmlfilepath;
  /** used for marshalling */
  protected Marshaller marshaller;

  /**
   * constructor
   *
   * @param p reference to the parent sketch applet
   * @param xmlfilepath path to the XML output
   */
  public ScoreRecorder(PApplet p, String xmlfilepath) {
    this.parent = p;
    this.pre_done = false;
    this.xmlfilepath = Paths.get(xmlfilepath).toAbsolutePath().toString();
    this.pkt2write = null;

    // register pre method which will initialize the XML file
    this.parent.registerMethod("pre", this);
    // register dispose method which will flush XML content and close the stream
    this.parent.registerMethod("dispose", this);
  }

  /**
   * pre-draw method that creates the XML output stream. This method is
   * automatically registered as a callback in the Processing sketch and should
   * not be invoked directly.
   */
  public void pre() {
    if (!pre_done) {

      XMLOutputFactory xof = XMLOutputFactory.newInstance();

      try {
        // create the marshaller
        JAXBContext jc = JAXBContext.newInstance(ScoreDataPacket.class);
        marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        // create the XML stream writer
        xtw = xof.createXMLStreamWriter(new FileOutputStream(this.xmlfilepath),
            "utf-8");
        System.out.println(String.format("ScoreRecorder: dumping XML at %s",
            this.xmlfilepath));
        // write initial XML content
        xtw.writeStartDocument("utf-8", "1.0");
        xtw.writeStartElement("oscscore");
        xtw.writeAttribute("generator", parent.getClass().getSimpleName());
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
    // do this only once
    pre_done = true;
  }

  /**
   * end of sketch method that flushes and closes the XML output stream. This
   * method is automatically registered as a callback in the Processing sketch
   * and should not be invoked directly.
   */
  public void dispose() {
    try {
      // write out anything still pending
      this.writeXMLElement();

      xtw.writeEndElement();
      xtw.writeEndDocument();
      xtw.flush();
      xtw.close();
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
  }

  /**
   * not used in this implementation
   *
   * @param status not used in this implementation
   */
  @Override
  public void oscStatus(OscStatus status) {
  }

  /**
   * invoked whenever an OSC message is received, it translates the message into
   * a XML node
   *
   * @param msg the message
   */
  @Override
  public void oscEvent(OscMessage msg) {
    if (this.pkt2write != null) {
      if (this.pkt2write.getFramecount() < parent.frameCount) {
        // this message does not belong to the last created bundle
        // so write out the bundle and start a new one
        this.writeXMLElement();
      }
    }

    // extract all relevant information from the message
    ScoreDataMessage mmm = new ScoreDataMessage();
    mmm.setAddress(msg.addrPattern());
    mmm.setTypetag(msg.typetag());

    ArrayList<ScoreDataArg> args = new ArrayList<ScoreDataArg>();
    for (int i = 0; i < msg.typetag().length(); i++) {
      ScoreDataArg arg = new ScoreDataArg();
      char c = msg.typetag().charAt(i);
      arg.setType(String.valueOf(c));
      switch (c) {
        case 's':
          arg.setValue(msg.get(i).stringValue());
          break;
        case 'i':
          arg.setValue(Integer.toString(msg.get(i).intValue()));
          break;
        case 'f':
          arg.setValue(Float.toString(msg.get(i).floatValue()));
          break;
        case 'd':
          arg.setValue(Double.toString(msg.get(i).doubleValue()));
          break;
        default:
          throw new IllegalStateException(String.format(
              "OSC message argument %s is invalid or not supported", c));
      }
      args.add(arg);
    }

    mmm.setArgs(args);

    if (this.pkt2write == null) {
      this.pkt2write = new ScoreDataPacket();
      this.pkt2write.setFramecount(parent.frameCount);
    }

    this.pkt2write.addMsg(mmm);
  }

  /**
   * write the content of pkt2write into the XML stream
   */
  protected void writeXMLElement() {
    JAXBElement<ScoreDataPacket> je = new JAXBElement<ScoreDataPacket>(
        new QName("oscpacket"), ScoreDataPacket.class, this.pkt2write);

    try {
      marshaller.marshal(je, this.xtw);
    } catch (JAXBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      this.pkt2write = null;
    }
  }
}
