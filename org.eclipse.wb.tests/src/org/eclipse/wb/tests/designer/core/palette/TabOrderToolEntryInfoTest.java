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
import org.eclipse.wb.core.gef.policy.TabOrderContainerEditPolicy;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.core.gef.tools.TabOrderTool;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

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
    {
      viewer = mock(IEditPartViewer.class);
      when(viewer.getSelectedEditParts()).thenReturn(ImmutableList.<EditPart>of());
    }
    // check tool
    assertTrue(entry.initialize(viewer, panel));
    assertNull(entry.createTool());
    //
    verify(viewer).getSelectedEditParts();
    verifyNoMoreInteractions(viewer);
  }

  /**
   * Single {@link EditPart} selected.
   */
  public void test_singleSelection() throws Exception {
    JavaInfo panel = parseEmptyPanel();
    // prepare tool entry
    TabOrderToolEntryInfo entry = new TabOrderToolEntryInfo();
    assertEquals("Tab Order", entry.getName());
    assertNotNull(entry.getIcon());
    // prepare viewer
    IEditPartViewer viewer;
    EditPart selectedEditPart;
    EditPolicy tabContainerRole;
    InOrder inOrder = null;
    {
      tabContainerRole = mock(EditPolicy.class);
      selectedEditPart = mock(EditPart.class);
      viewer = mock(IEditPartViewer.class);
      inOrder = inOrder(tabContainerRole, selectedEditPart, viewer);
      //
      when(selectedEditPart.getViewer()).thenReturn(viewer);
      when(selectedEditPart.getEditPolicy(TabOrderContainerEditPolicy.TAB_CONTAINER_ROLE)).thenReturn(tabContainerRole);
      when(viewer.getSelectedEditParts()).thenReturn(ImmutableList.of(selectedEditPart));
    }
    // check tool
    assertTrue(entry.initialize(viewer, panel));
    assertInstanceOf(TabOrderTool.class, entry.createTool());
    //
    inOrder.verify(viewer).getSelectedEditParts();
    inOrder.verify(viewer).addSelectionChangedListener(ArgumentMatchers.any());
    inOrder.verify(selectedEditPart).getEditPolicy(TabOrderContainerEditPolicy.TAB_CONTAINER_ROLE);
    inOrder.verifyNoMoreInteractions();
  }
}
