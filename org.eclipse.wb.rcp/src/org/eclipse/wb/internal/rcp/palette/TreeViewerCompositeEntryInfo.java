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
package org.eclipse.wb.internal.rcp.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * {@link EntryInfo} that allows user to drop new {@link Composite} with
 * {@link org.eclipse.jface.layout.TreeColumnLayout} and {@link TreeViewer}.
 *
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class TreeViewerCompositeEntryInfo extends ToolEntryInfo {
  private static final Image ICON =
      Activator.getImage("info/AbstractColumnLayout/TreeViewerComposite.gif");
  private final ComponentEntryInfo m_compositeEntry;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeViewerCompositeEntryInfo() {
    setName(PaletteMessages.TreeViewerCompositeEntryInfo_name);
    setDescription(PaletteMessages.TreeViewerCompositeEntryInfo_description);
    m_compositeEntry = new ComponentEntryInfo();
    m_compositeEntry.setComponentClassName("org.eclipse.swt.widgets.Composite");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getIcon() {
    return ICON;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ToolEntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean initialize(IEditPartViewer editPartViewer, JavaInfo rootJavaInfo) {
    return super.initialize(editPartViewer, rootJavaInfo)
        && ProjectUtils.hasType(m_javaProject, "org.eclipse.jface.layout.TreeColumnLayout")
        && m_compositeEntry.initialize(editPartViewer, rootJavaInfo);
  }

  @Override
  public Tool createTool() throws Exception {
    final CreationTool creationTool = (CreationTool) m_compositeEntry.createTool();
    // configure CreationTool to add also TreeColumnLayout and Tree
    m_rootJavaInfo.addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        ICreationFactory creationFactory = creationTool.getFactory();
        CompositeInfo composite = (CompositeInfo) creationFactory.getNewObject();
        if (child == composite) {
          composite.removeBroadcastListener(this);
          configureComposite(composite);
        }
      }
    });
    // OK, return CreationTool
    return creationTool;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures {@link CompositeInfo} by creating {@link Tree} and
   * {@link org.eclipse.jface.layout.TreeColumnLayout}.
   */
  private void configureComposite(CompositeInfo composite) throws Exception {
    composite.putArbitraryValue(CompositeInfo.KEY_DONT_INHERIT_LAYOUT, Boolean.TRUE);
    composite.getRootJava().refreshLight();
    // set some known layout, such as RowLayout
    RowLayoutInfo rowLayout;
    {
      rowLayout =
          (RowLayoutInfo) JavaInfoUtils.createJavaInfo(
              m_editor,
              "org.eclipse.swt.layout.RowLayout",
              new ConstructorCreationSupport());
      m_editor.setResolveImports(false);
      composite.setLayout(rowLayout);
      m_editor.setResolveImports(true);
    }
    // create new TreeViewer using RowLayout
    {
      ViewerInfo treeViewer =
          (ViewerInfo) JavaInfoUtils.createJavaInfo(
              m_editor,
              "org.eclipse.jface.viewers.TreeViewer",
              new ConstructorCreationSupport());
      ControlInfo tree = (ControlInfo) JavaInfoUtils.getWrapped(treeViewer);
      rowLayout.command_CREATE(tree, null);
      tree.getPropertyByTitle("linesVisible").setValue(true);
      tree.getPropertyByTitle("headerVisible").setValue(true);
    }
    // set TreeColumnLayout for Composite
    {
      LayoutInfo treeColumnLayout =
          (LayoutInfo) JavaInfoUtils.createJavaInfo(
              m_editor,
              "org.eclipse.jface.layout.TreeColumnLayout",
              new ConstructorCreationSupport());
      composite.setLayout(treeColumnLayout);
    }
  }
}
