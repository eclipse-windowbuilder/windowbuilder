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

import org.eclipse.wb.draw2d.border.CompoundBorder;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.border.MarginBorder;
import org.eclipse.wb.internal.draw2d.Label;
import org.eclipse.wb.tests.gef.TestLogger;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

/**
 * @author lobas_av
 *
 */
public class LabelTest extends Draw2dFigureTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LabelTest() {
		super(Label.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Test
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_text() throws Exception {
		// check text for new empty Label
		assertEquals("", new Label().getText());
		//
		// check text for Label created by constructor Label(String)
		assertEquals("Column: 1", new Label("Column: 1").getText());
		//
		Label label = new Label();
		//
		// check work setText()/getText()
		label.setText("123ZzzzZ");
		assertEquals("123ZzzzZ", label.getText());
		//
		// check work setText()/getText()
		label.setText("Row: 0");
		assertEquals("Row: 0", label.getText());
		//
		// check work setText()/getText()
		label.setText(null);
		assertEquals("", label.getText());
	}

	public void test_resetState() throws Exception {
		TestLogger actualLogger = new TestLogger();
		//
		TestCaseRootFigure parentFigure = new TestCaseRootFigure(actualLogger);
		//
		TestLogger expectedLogger = new TestLogger();
		//
		Label label = new Label();
		parentFigure.add(label);
		actualLogger.clear();
		//
		// check no reset state during setText() if text not change
		label.setText("");
		actualLogger.assertEmpty();
		//
		// check no reset state during setText() if text not change
		label.setText(null);
		actualLogger.assertEmpty();
		//
		// check reset state during setText()
		label.setText("123");
		expectedLogger.log("repaint(true, 0, 0, 0, 0)");
		actualLogger.assertEquals(expectedLogger);
		//
		// check no reset state during setText() if text not change
		label.setText("123");
		actualLogger.assertEmpty();
		//
		// check reset state during setText()
		label.setText("231");
		expectedLogger.log("repaint(true, 0, 0, 0, 0)");
		actualLogger.assertEquals(expectedLogger);
		//
		// check reset state during setText()
		label.setText(null);
		expectedLogger.log("repaint(true, 0, 0, 0, 0)");
		actualLogger.assertEquals(expectedLogger);
	}

	public void test_getPreferredSize() throws Exception {
		Label label = new Label();
		assertTextSize(label);
		Dimension size1 = label.getPreferredSize();
		//
		// check calc preferred size if text is changed
		label.setText("1234");
		Dimension size2 = label.getPreferredSize();
		assertTextSize(label);
		assertNotSame(size1, size2);
		assertSame(size2, label.getPreferredSize());
		//
		// check calc preferred size if font is changed
		label.setFont(new Font(null, "", 100, SWT.NONE));
		assertNotSame(size2, label.getPreferredSize());
		assertTextSize(label);
		//
		// check calc preferred size if set border
		label.setBorder(new CompoundBorder(new LineBorder(), new MarginBorder(2)));
		assertTextSize(label);
	}

	private static final void assertTextSize(Label label) throws Exception {
		// create calc GC
		GC gc = new GC(Display.getDefault());
		// set label font
		gc.setFont(label.getFont());
		// calc text size
		org.eclipse.swt.graphics.Point size = gc.textExtent(label.getText());
		// dispose calc GC
		gc.dispose();
		// get label border insets and calc expected preferred size
		Insets insets = label.getInsets();
		Dimension expectedSize = new Dimension(size).expand(insets.getWidth(), insets.getHeight());
		//
		assertEquals(expectedSize, label.getPreferredSize());
	}
}