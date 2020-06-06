/**
 * Copyright (C) 2014-present Nico L'Insalata aka zeroisnan
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * Demonstrate how to playback a previously recorded OSC score
 * 
 * Use the 'r' key to restart the sketch
 */

import net.zeroisnan.oscscorep5.*;
import oscP5.OscMessage;

// this is the object that will read the score and replay the OSC messages
OscScoreP5 score;
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

  // initialie the OscP5 object - enable debug during drawing
  score = new OscScoreP5(this, DEBUG);
  
  // register some callbacks to do something when a message is received
  // this is standard oscP5 functionality
  score.plug(this, "goUp",    "/example/up");
  score.plug(this, "goDown",  "/example/down");
  score.plug(this, "goLeft",  "/example/left");
  score.plug(this, "goRight", "/example/right");

  // playback
  score.play(sketchPath(SCORE));
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
// handle keyboard control
//----------------------------------------------------
void keyPressed() {
  switch (key) {
    case 'r':
      // stop the drawing loop
      noLoop();
      // rewind the score
      score.rewind();
      // bring the object back to its starting position
      pos = new PVector(0, 0);
      // restart the loop
      loop();
      break;
  }
  return;
}
