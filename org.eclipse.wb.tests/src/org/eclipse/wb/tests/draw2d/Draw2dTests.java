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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author lobas_av
 */


@RunWith(Suite.class)
@SuiteClasses({
	 // geometry
    RectangleTest.class,
    PointListTest.class,
    TransposerTest.class,
    // Border
    MarginBorderTest.class,
    LineBorderTest.class,
    CompoundBorderTest.class,
    // Figure
    GraphicsTest.class,
    FigureTest.class,
    FigurePaintingTest.class,
    LabelTest.class,
    PolylineTest.class,
    LayerTest.class,
    RootFigureTest.class,
    // events
    FigureEventTest.class,
})
public class Draw2dTests {
}