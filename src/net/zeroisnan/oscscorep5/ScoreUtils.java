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

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * Implement utility functions used across the library
 */
public final class ScoreUtils {

  /**
   * prevent the class from being newed
   */
  private ScoreUtils() {
    throw new AssertionError();
  }

  protected static void handleException(XMLStreamException e, String msgText) {
    String msg;
    if (e.getNestedException() == null) {
      msg = e.getMessage();
    } else {
      msg = e.getNestedException().getMessage();
    }
    System.err.println(String.format("ERROR: %s: %s", msgText, msg));
    e.printStackTrace();
  }

  protected static void handleException(JAXBException e, String msgText) {
    String msg;
    if (e.getLinkedException() == null) {
      msg = e.getMessage();
    } else {
      msg = e.getLinkedException().getMessage();
    }
    System.err.println(String.format("ERROR: %s: %s", msgText, msg));
    e.printStackTrace();
  }

  protected static void handleException(Exception e, String msgText) {
    String msg = e.getMessage();
    System.err.println(String.format("ERROR: %s: %s", msgText, msg));
    e.printStackTrace();
  }

}
