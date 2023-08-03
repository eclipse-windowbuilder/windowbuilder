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

import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.xwt.model.layout.RowDataInfo;
import org.eclipse.wb.internal.xwt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.xwt.model.layout.VirtualLayoutDataCreationSupport;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.junit.Test;

/**
 * Test for {@link RowLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class RowLayoutTest extends XwtModelTest {
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
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_setLayout() throws Exception {
		CompositeInfo shell = parse("<Shell/>");
		RowLayoutInfo layout = createObject("org.eclipse.swt.layout.RowLayout");
		shell.setLayout(layout);
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <RowLayout/>",
				"  </Shell.layout>",
				"</Shell>");
	}

	@Test
	public void test_getRowData() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <RowLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		//
		RowDataInfo rowData = RowLayoutInfo.getRowData(button);
		assertNotNull(rowData);
		assertVisible(rowData, false);
		// getRowData2()
		{
			RowLayoutInfo layout = getObjectByName("layout");
			assertSame(layout.getRowData2(button), rowData);
		}
		// virtual initially
		assertInstanceOf(VirtualLayoutDataCreationSupport.class, rowData.getCreationSupport());
		// set width/height
		rowData.setWidth(150);
		rowData.setHeight(100);
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <RowLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'>",
				"    <Button.layoutData>",
				"      <RowData width='150' height='100'/>",
				"    </Button.layoutData>",
				"  </Button>",
				"</Shell>");
		// applied
		refresh();
		assertEquals(150, button.getModelBounds().width);
		assertEquals(100, button.getModelBounds().height);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// isHorizontal()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link RowLayoutInfo#isHorizontal()}.
	 */
	@Test
	public void test_isHorizontal_default() throws Exception {
		CompositeInfo shell =
				parse(
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Shell.layout>",
						"    <RowLayout/>",
						"  </Shell.layout>",
						"</Shell>");
		shell.refresh();
		RowLayoutInfo layout = (RowLayoutInfo) shell.getLayout();
		//
		assertTrue(layout.isHorizontal());
	}

	/**
	 * Test for {@link RowLayoutInfo#isHorizontal()}.
	 */
	@Test
	public void test_isHorizontal_true() throws Exception {
		CompositeInfo shell =
				parse(
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Shell.layout>",
						"    <RowLayout type='HORIZONTAL'/>",
						"  </Shell.layout>",
						"</Shell>");
		shell.refresh();
		RowLayoutInfo layout = (RowLayoutInfo) shell.getLayout();
		//
		assertTrue(layout.isHorizontal());
	}

	/**
	 * Test for {@link RowLayoutInfo#isHorizontal()}.
	 */
	@Test
	public void test_isHorizontal_false() throws Exception {
		CompositeInfo shell =
				parse(
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Shell.layout>",
						"    <RowLayout type='VERTICAL'/>",
						"  </Shell.layout>",
						"</Shell>");
		shell.refresh();
		RowLayoutInfo layout = (RowLayoutInfo) shell.getLayout();
		//
		assertFalse(layout.isHorizontal());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_clipboard_copyLayout() throws Exception {
		CompositeInfo shell =
				parse(
						"<Shell>",
						"  <Shell.layout>",
						"    <FillLayout/>",
						"  </Shell.layout>",
						"  <Composite wbp:name='composite'>",
						"    <Composite.layout>",
						"      <RowLayout/>",
						"    </Composite.layout>",
						"    <Button text='Button 1'/>",
						"    <Button text='Button 2'>",
						"      <Button.layoutData>",
						"        <RowData width='150' height='100'/>",
						"      </Button.layoutData>",
						"    </Button>",
						"  </Composite>",
						"</Shell>");
		refresh();
		CompositeInfo composite = getObjectByName("composite");
		//
		shell.startEdit();
		{
			XmlObjectMemento memento = XmlObjectMemento.createMemento(composite);
			CompositeInfo newComposite = (CompositeInfo) memento.create(shell);
			shell.getLayout().command_CREATE(newComposite, null);
			memento.apply();
		}
		assertXML(
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <Composite wbp:name='composite'>",
				"    <Composite.layout>",
				"      <RowLayout/>",
				"    </Composite.layout>",
				"    <Button text='Button 1'/>",
				"    <Button text='Button 2'>",
				"      <Button.layoutData>",
				"        <RowData width='150' height='100'/>",
				"      </Button.layoutData>",
				"    </Button>",
				"  </Composite>",
				"  <Composite>",
				"    <Composite.layout>",
				"      <RowLayout/>",
				"    </Composite.layout>",
				"    <Button text='Button 1'/>",
				"    <Button text='Button 2'>",
				"      <Button.layoutData>",
				"        <RowData height='100' width='150'/>",
				"      </Button.layoutData>",
				"    </Button>",
				"  </Composite>",
				"</Shell>");
	}
}