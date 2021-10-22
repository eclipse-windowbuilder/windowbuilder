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
package org.eclipse.wb.tests.designer.core.palette;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.editor.palette.model.entry.TabOrderToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;

import static org.easymock.EasyMock.expect;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

/**
 * Test for {@link TabOrderToolEntryInfo}.
 *
 * @author scheglov_ke
 */
public class TabOrderToolEntryInfoTest extends AbstractPaletteTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No selection - no tab order tool.
   */
  public void test_noSelection() throws Exception {
    JavaInfo panel = parseEmptyPanel();
    // prepare tool entry
    TabOrderToolEntryInfo entry = new TabOrderToolEntryInfo();
    assertEquals("Tab Order", entry.getName());
    assertNotNull(entry.getIcon());
    // prepare viewer
    IEditPartViewer viewer;
    IMocksControl control = EasyMock.createStrictControl();
    {
      viewer = control.createMock(IEditPartViewer.class);
      expect(viewer.getSelectedEditParts()).andReturn(ImmutableList.<EditPart>of());
      control.replay();
    }
    // check tool
    assertTrue(entry.initialize(viewer, panel));
    assertNull(entry.createTool());
    control.verify();
  }

  /**
   * Single {@link EditPart} selected.
   */
  public void test_singleSelection() throws Exception {
    /*JavaInfo panel = parseEmptyPanel();
    // prepare tool entry
    TabOrderToolEntryInfo entry = new TabOrderToolEntryInfo();
    assertEquals("Tab Order", entry.getName());
    assertNotNull(entry.getIcon());
    // prepare viewer
    IEditPartViewer viewer;
    IMocksControl control = EasyMock.createStrictControl();
    {
    	EditPolicy tabContainerRole = control.createMock(EditPolicy.class);
    	EditPart selectedEditPart = control.createMock(EditPart.class);
    	viewer = control.createMock(IEditPartViewer.class);
    	expect(viewer.getSelectedEditParts()).andReturn(Lists.immutableList(selectedEditPart));
    	expect(selectedEditPart.getEditPolicy(TabOrderTool.TAB_CONTAINER_ROLE)).andReturn(tabContainerRole);
    	control.replay();
    }
    // check tool
    assertTrue(entry.initialize(viewer, panel));
    assertInstanceOf(TabOrderTool.class, entry.createTool());
    control.verify();*/
  }
}
