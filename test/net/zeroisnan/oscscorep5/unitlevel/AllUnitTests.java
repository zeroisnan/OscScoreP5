/**
 * Copyright (C) 2014-present Nico L'Insalata aka zeroisnan
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package net.zeroisnan.oscscorep5.unitlevel;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ScoreEventTest.class, ScoreRecorderTest.class,
    ScorePlayerTest.class })
public class AllUnitTests {

}
