/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.draw2d;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author lobas_av
 */
public class Draw2dTests extends TestCase {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.draw2d");
    // geometry
    suite.addTestSuite(IntervalTest.class);
    suite.addTestSuite(PointTest.class);
    suite.addTestSuite(InsetsTest.class);
    suite.addTestSuite(DimensionTest.class);
    suite.addTestSuite(RectangleTest.class);
    suite.addTestSuite(PointListTest.class);
    suite.addTestSuite(TransposerTest.class);
    // Border
    suite.addTestSuite(MarginBorderTest.class);
    suite.addTestSuite(LineBorderTest.class);
    suite.addTestSuite(CompoundBorderTest.class);
    // Figure
    suite.addTestSuite(GraphicsTest.class);
    suite.addTestSuite(FigureTest.class);
    suite.addTestSuite(FigurePaintingTest.class);
    suite.addTestSuite(LabelTest.class);
    suite.addTestSuite(PolylineTest.class);
    suite.addTestSuite(LayerTest.class);
    suite.addTestSuite(RootFigureTest.class);
    // events
    suite.addTestSuite(FigureEventTest.class);
    return suite;
  }
}