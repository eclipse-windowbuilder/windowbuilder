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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 *
 */
public class GraphicalViewerTest extends GefTestCase {
  private Shell m_shell;
  private EditDomain m_domain;
  private GraphicalViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GraphicalViewerTest() {
    super(GraphicalViewer.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SetUp
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // configure
    m_shell = new Shell();
    // create domain
    m_domain = new EditDomain() {
      @Override
      public Tool getDefaultTool() {
        return null;
      }
    };
    // create viewer
    m_viewer = new GraphicalViewer(m_shell);
    m_viewer.getControl().setSize(500, 400);
    m_viewer.setEditDomain(m_domain);
  }

  @Override
  protected void tearDown() throws Exception {
    m_shell.dispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Selection() throws Exception {
    TestEditPart part1 = new TestEditPart(m_viewer);
    TestEditPart part2 = new TestEditPart(m_viewer);
    TestEditPart part3 = new TestEditPart(m_viewer);
    //
    // check ini state
    assertEquals(EditPart.SELECTED_NONE, part1.getSelected());
    assertEquals(EditPart.SELECTED_NONE, part2.getSelected());
    assertEquals(EditPart.SELECTED_NONE, part3.getSelected());
    assertTrue(m_viewer.getSelectedEditParts().isEmpty());
    //
    // check work select for "part1"
    m_viewer.select(part1);
    //
    assertEquals(EditPart.SELECTED_PRIMARY, part1.getSelected());
    assertEquals(EditPart.SELECTED_NONE, part2.getSelected());
    assertEquals(EditPart.SELECTED_NONE, part3.getSelected());
    assertEquals(1, m_viewer.getSelectedEditParts().size());
    assertSame(part1, m_viewer.getSelectedEditParts().get(0));
    //
    // check work select for "part1"
    m_viewer.select(part2);
    //
    assertEquals(EditPart.SELECTED_NONE, part1.getSelected());
    assertEquals(EditPart.SELECTED_PRIMARY, part2.getSelected());
    assertEquals(EditPart.SELECTED_NONE, part3.getSelected());
    assertEquals(1, m_viewer.getSelectedEditParts().size());
    assertSame(part2, m_viewer.getSelectedEditParts().get(0));
    //
    // check work append selection for "part3"
    m_viewer.appendSelection(part3);
    //
    assertEquals(EditPart.SELECTED_NONE, part1.getSelected());
    assertEquals(EditPart.SELECTED, part2.getSelected());
    assertEquals(EditPart.SELECTED_PRIMARY, part3.getSelected());
    assertEquals(2, m_viewer.getSelectedEditParts().size());
    assertSame(part2, m_viewer.getSelectedEditParts().get(0));
    assertSame(part3, m_viewer.getSelectedEditParts().get(1));
    //
    // check work append selection for "part1"
    m_viewer.appendSelection(part1);
    //
    assertEquals(EditPart.SELECTED_PRIMARY, part1.getSelected());
    assertEquals(EditPart.SELECTED, part2.getSelected());
    assertEquals(EditPart.SELECTED, part3.getSelected());
    assertEquals(3, m_viewer.getSelectedEditParts().size());
    assertSame(part2, m_viewer.getSelectedEditParts().get(0));
    assertSame(part3, m_viewer.getSelectedEditParts().get(1));
    assertSame(part1, m_viewer.getSelectedEditParts().get(2));
    //
    // check work append selection for "part2"
    m_viewer.appendSelection(part2);
    //
    assertEquals(EditPart.SELECTED, part1.getSelected());
    assertEquals(EditPart.SELECTED_PRIMARY, part2.getSelected());
    assertEquals(EditPart.SELECTED, part3.getSelected());
    assertEquals(3, m_viewer.getSelectedEditParts().size());
    assertSame(part3, m_viewer.getSelectedEditParts().get(0));
    assertSame(part1, m_viewer.getSelectedEditParts().get(1));
    assertSame(part2, m_viewer.getSelectedEditParts().get(2));
    //
    // check work append selection for "part2" again
    m_viewer.deselect(part2);
    //
    assertEquals(EditPart.SELECTED_PRIMARY, part1.getSelected());
    assertEquals(EditPart.SELECTED_NONE, part2.getSelected());
    assertEquals(EditPart.SELECTED, part3.getSelected());
    assertEquals(2, m_viewer.getSelectedEditParts().size());
    assertSame(part3, m_viewer.getSelectedEditParts().get(0));
    assertSame(part1, m_viewer.getSelectedEditParts().get(1));
    //
    // check work all deselection
    m_viewer.deselectAll();
    //
    assertEquals(EditPart.SELECTED_NONE, part1.getSelected());
    assertEquals(EditPart.SELECTED_NONE, part2.getSelected());
    assertEquals(EditPart.SELECTED_NONE, part3.getSelected());
    assertTrue(m_viewer.getSelectedEditParts().isEmpty());
    //
    // check work multi selection
    List<EditPart> selection = new ArrayList<>();
    selection.add(part2);
    selection.add(part1);
    m_viewer.setSelection(selection);
    //
    assertEquals(EditPart.SELECTED_PRIMARY, part1.getSelected());
    assertEquals(EditPart.SELECTED, part2.getSelected());
    assertEquals(EditPart.SELECTED_NONE, part3.getSelected());
    assertEquals(2, m_viewer.getSelectedEditParts().size());
    assertSame(part2, m_viewer.getSelectedEditParts().get(0));
    assertSame(part1, m_viewer.getSelectedEditParts().get(1));
    //
    // check work multi selection
    m_viewer.setSelection(new StructuredSelection());
    //
    assertEquals(EditPart.SELECTED_NONE, part1.getSelected());
    assertEquals(EditPart.SELECTED_NONE, part2.getSelected());
    assertEquals(EditPart.SELECTED_NONE, part3.getSelected());
    assertTrue(m_viewer.getSelectedEditParts().isEmpty());
  }

  public void test_Invoke_SelectionListener() throws Exception {
    final TestLogger actualLogger = new TestLogger();
    ISelectionChangedListener listener = new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        actualLogger.log("selectionChanged(" + event + ")");
      }
    };
    //
    // check not invoke during addSelectionListener()
    m_viewer.addSelectionChangedListener(listener);
    actualLogger.assertEmpty();
    //
    // check invoke during appendSelection()
    TestEditPart part1 = new TestEditPart(m_viewer);
    m_viewer.appendSelection(part1);
    //
    TestLogger expectedLogger = new TestLogger();
    expectedLogger.log(
        "selectionChanged("
            + new SelectionChangedEvent(m_viewer, new StructuredSelection(part1))
            + ")");
    actualLogger.assertEquals(expectedLogger);
    //
    // check invoke during select()
    TestEditPart part2 = new TestEditPart(m_viewer);
    m_viewer.select(part2);
    //
    expectedLogger.log(
        "selectionChanged("
            + new SelectionChangedEvent(m_viewer, new StructuredSelection(part2))
            + ")");
    actualLogger.assertEquals(expectedLogger);
    //
    // check not invoke during select() if argument part already selection
    m_viewer.select(part2);
    actualLogger.assertEmpty();
    //
    // check invoke during deselectAll()
    m_viewer.deselectAll();
    expectedLogger.log(
        "selectionChanged(" + new SelectionChangedEvent(m_viewer, new StructuredSelection()) + ")");
    actualLogger.assertEquals(expectedLogger);
    //
    // check invoke during setSelection(List)
    List<EditPart> selection = new ArrayList<>();
    selection.add(part2);
    selection.add(part1);
    m_viewer.setSelection(selection);
    expectedLogger.log(
        "selectionChanged("
            + new SelectionChangedEvent(m_viewer, new StructuredSelection(selection))
            + ")");
    actualLogger.assertEquals(expectedLogger);
    //
    // check invoke during deselect()
    m_viewer.deselect(part1);
    expectedLogger.log(
        "selectionChanged("
            + new SelectionChangedEvent(m_viewer, new StructuredSelection(part2))
            + ")");
    actualLogger.assertEquals(expectedLogger);
    //
    // check invoke during setSelection(ISelection)
    m_viewer.setSelection(new StructuredSelection());
    expectedLogger.log(
        "selectionChanged(" + new SelectionChangedEvent(m_viewer, new StructuredSelection()) + ")");
    actualLogger.assertEquals(expectedLogger);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditPart inner class
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class TestEditPart extends GraphicalEditPart {
    private final IEditPartViewer m_viewer;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public TestEditPart(IEditPartViewer viewer) {
      m_viewer = viewer;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // EditPart
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected Figure createFigure() {
      return null;
    }

    @Override
    public IEditPartViewer getViewer() {
      return m_viewer;
    }
  }
}