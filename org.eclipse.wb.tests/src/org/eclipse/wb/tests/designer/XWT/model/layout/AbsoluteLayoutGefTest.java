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
package org.eclipse.wb.tests.designer.XWT.model.layout;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.xwt.model.layout.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.palette.AbsoluteLayoutEntryInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link AbsoluteLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class AbsoluteLayoutGefTest extends XwtGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		prepareBox();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Canvas
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_set() throws Exception {
		CompositeInfo composite =
				openEditor(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Composite/>");
		// set Tool
		Tool tool;
		{
			AbsoluteLayoutEntryInfo entry = new AbsoluteLayoutEntryInfo();
			entry.initialize(m_viewerCanvas, composite);
			assertNotNull(entry.getIcon());
			tool = entry.createTool();
		}
		m_viewerCanvas.getEditDomain().setActiveTool(tool);
		// use canvas
		canvas.moveTo(composite, 100, 100).click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Composite layout='{x:Null}'/>");
	}

	@Test
	public void test_canvas_CREATE() throws Exception {
		CompositeInfo composite =
				openEditor(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Composite/>");
		// create Box
		loadBox();
		// use canvas
		canvas.sideMode().create(100, 50);
		canvas.target(composite).in(30, 40).move();
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"<Composite>",
				"  <t:Box bounds='30, 40, 100, 50'/>",
				"</Composite>");
	}

	@Test
	public void test_canvas_PASTE() throws Exception {
		CompositeInfo composite =
				openEditor(
						"// filler filler filler filler filler",
						"<Composite>",
						"  <t:Box wbp:name='box' bounds='30, 40, 100, 50'/>",
						"</Composite>");
		// copy/paste "box"
		{
			ControlInfo box = getObjectByName("box");
			doCopyPaste(box);
		}
		// move
		canvas.sideMode().create(100, 50);
		canvas.target(composite).inX(50).inY(150).move();
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"<Composite>",
				"  <t:Box wbp:name='box' bounds='30, 40, 100, 50'/>",
				"  <t:Box bounds='50, 150, 100, 50'/>",
				"</Composite>");
	}

	@Test
	public void test_canvas_MOVE() throws Exception {
		CompositeInfo composite =
				openEditor(
						"// filler filler filler filler filler",
						"<Composite>",
						"  <t:Box wbp:name='box' bounds='30, 40, 100, 50'/>",
						"</Composite>");
		ControlInfo button = getObjectByName("box");
		// move
		canvas.sideMode().beginMove(button);
		canvas.target(composite).inX(50).inY(80).drag();
		canvas.endDrag();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Composite>",
				"  <t:Box wbp:name='box' bounds='50, 80, 100, 50'/>",
				"</Composite>");
	}

	@Test
	public void test_canvas_RESIZE() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<Composite>",
				"  <t:Box wbp:name='box' bounds='30, 40, 100, 50'/>",
				"</Composite>");
		ControlInfo button = getObjectByName("box");
		//
		canvas.beginResize(button, IPositionConstants.SOUTH_EAST);
		canvas.dragTo(button, 150, 100).endDrag();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Composite>",
				"  <t:Box wbp:name='box' bounds='30, 40, 150, 100'/>",
				"</Composite>");
	}

	@Test
	public void test_canvas_ADD() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Composite wbp:name='target' bounds='10, 10, 400, 250'/>",
				"  <t:Box wbp:name='box' text='Box' bounds='1, 2, 100, 50'/>",
				"</Shell>");
		CompositeInfo target = getObjectByName("target");
		ControlInfo box = getObjectByName("box");
		// move
		canvas.sideMode().beginMove(box);
		canvas.target(target).inX(50).inY(20).drag();
		canvas.endDrag();
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Composite wbp:name='target' bounds='10, 10, 400, 250'>",
				"    <t:Box wbp:name='box' text='Box' bounds='50, 20, 100, 50'/>",
				"  </Composite>",
				"</Shell>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_tree_CREATE() throws Exception {
		CompositeInfo composite =
				openEditor(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Composite/>");
		// create Box
		ControlInfo newBox = loadBox();
		// use tree
		tree.moveOn(composite);
		tree.assertFeedback_on(composite);
		tree.click();
		assertXML(
				"// filler filler filler filler filler",
				"<Composite>",
				"  <t:Box bounds='0, 0, 100, 50'/>",
				"</Composite>");
		tree.assertPrimarySelected(newBox);
	}

	@Test
	public void test_tree_PASTE() throws Exception {
		CompositeInfo composite =
				openEditor(
						"// filler filler filler filler filler",
						"<Composite>",
						"  <t:Box wbp:name='box' bounds='30, 40, 100, 50'/>",
						"</Composite>");
		// copy/paste "box"
		{
			ControlInfo box = getObjectByName("box");
			doCopyPaste(box);
		}
		// use tree
		tree.moveOn(composite);
		tree.assertFeedback_on(composite);
		tree.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Composite>",
				"  <t:Box wbp:name='box' bounds='30, 40, 100, 50'/>",
				"  <t:Box bounds='0, 0, 100, 50'/>",
				"</Composite>");
	}

	@Test
	public void test_tree_MOVE() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<Composite>",
				"  <t:Box wbp:name='box_1' bounds='10, 20, 100, 50'/>",
				"  <t:Box wbp:name='box_2' bounds='10, 100, 100, 50'/>",
				"</Composite>");
		ControlInfo box_1 = getObjectByName("box_1");
		ControlInfo box_2 = getObjectByName("box_2");
		// use tree
		tree.startDrag(box_2);
		tree.dragBefore(box_1);
		tree.assertFeedback_before(box_1);
		tree.endDrag();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Composite>",
				"  <t:Box wbp:name='box_2' bounds='10, 100, 100, 50'/>",
				"  <t:Box wbp:name='box_1' bounds='10, 20, 100, 50'/>",
				"</Composite>");
	}

	@Test
	public void test_tree_ADD() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Composite wbp:name='target' bounds='10, 10, 400, 250'/>",
				"  <t:Box wbp:name='box' text='Box' bounds='1, 2, 100, 50'/>",
				"</Shell>");
		CompositeInfo target = getObjectByName("target");
		ControlInfo box = getObjectByName("box");
		// use tree
		tree.startDrag(box);
		tree.dragOn(target);
		tree.assertFeedback_on(target);
		tree.endDrag();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Composite wbp:name='target' bounds='10, 10, 400, 250'>",
				"    <t:Box wbp:name='box' text='Box' size='100, 50'/>",
				"  </Composite>",
				"</Shell>");
	}
}
