/**
 * Copyright (C) 2014-present Nico L'Insalata aka zeroisnan
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * 
 * Demonstrate how to record a OSC score.
 *
 * Use the arrow keys to move the cursor on the screen.
 * 
 * In this example the incoming OSC messages are generated by the sketch
 * itself using a loopback function. This is so this example would not
 * depend on any external agent.
 *
 * IMPORTANT: you need to close the sketch clicking on
 * the skecth window close button. Pressing the 'Stop'
 * button in the PDE will leave the XML score unterminated.
 * 
 * This is a confirmed bug in the PDE
 *  https://github.com/processing/processing/issues/4445
 * dispose handlers not called when stop button pressed
 */

import net.zeroisnan.oscscorep5.*;
import oscP5.OscMessage;

// this is the object that handles incoming messages
OscScoreP5 rcvr;
// this is a shape we move on the screen
PShape shp;
// this is the position of the shape
PVector pos;

// sketch configuration variables
final int FRAME_RATE = 24;
final boolean DEBUG = true;
final String SCORE = "recorded_score.xml";


// ----------------------------------------------------
// sketch setup function
// ----------------------------------------------------
void settings() {
  size(1280, 720);
}

void setup() {
  frameRate(FRAME_RATE);
  
  // create the shape and position it in the origin
  pos = new PVector(0, 0);
  shp = createShape(RECT, pos.x, pos.y, 10, 10);
  shp.setFill(color(255, 0, 0));

  // initialie the OscP5 receiver
  rcvr = new OscScoreP5(this);
  
  // register some callbacks to do something when a message is received
  // this is standard oscP5 functionality
  rcvr.plug(this, "goUp",    "/example/up");
  rcvr.plug(this, "goDown",  "/example/down");
  rcvr.plug(this, "goLeft",  "/example/left");
  rcvr.plug(this, "goRight", "/example/right");

  // enable recording of incoming OSC messages
  rcvr.rec(sketchPath("recorded_score.xml"));
}

// ----------------------------------------------------
// message callbacks
// ----------------------------------------------------
void goUp(int idx) {
  float val = pos.y - idx;
  pos.y = constrain(val, 0, height);
}

void goDown(int idx) {
  float val = pos.y + idx;
  pos.y = constrain(val, 0, height);
}

void goLeft(int idx) {
  float val = pos.x - idx;
  pos.x = constrain(val, 0, width);
}

void goRight(int idx) {
  float val = pos.x + idx;
  pos.x = constrain(val, 0, width);
}


// ----------------------------------------------------
// sketch draw function
// ----------------------------------------------------
void draw() {
  background(0);
  shape(shp, pos.x, pos.y);
}

//----------------------------------------------------
//handle keyboard control
//----------------------------------------------------
void keyPressed() {
  if (key != CODED) {
    return;
  }

  OscMessage test;
  switch (keyCode) {
    case UP:
      test = new OscMessage("/example/up");
      test.add(5);
      break;

    case DOWN:
      test = new OscMessage("/example/down");
      test.add(5);
      break;

    case LEFT:
      test = new OscMessage("/example/left");
      test.add(5);
      break;

    case RIGHT:
      test = new OscMessage("/example/right");
      test.add(5);
      break;

    default:
      return;
  }

  // when we get here we've got something to send
  rcvr.loopback(test);
}
