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
package org.eclipse.wb.internal.rcp.swtawt.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.rcp.swtawt.Activator;
import org.eclipse.wb.internal.rcp.swtawt.Messages;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import java.awt.BorderLayout;
import java.awt.Panel;

import javax.swing.JRootPane;

/**
 * {@link EntryInfo} that drops {@link Composite} with {@link SWT_AWT#new_Frame(Composite)}.
 *
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class SwingCompositeEntryInfo extends ToolEntryInfo {
  private static final Image ICON = Activator.getImage("info/SWT_AWT/Composite_SWT_AWT.png");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwingCompositeEntryInfo() {
    setId(getClass().getName());
    setName(Messages.SwingCompositeEntryInfo_name);
    setDescription(Messages.SwingCompositeEntryInfo_description);
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
  public Tool createTool() throws Exception {
    ICreationFactory factory = new ICreationFactory() {
      private JavaInfo m_javaInfo;

      public void activate() throws Exception {
        m_javaInfo = createEmbeddedComposite();
      }

      public Object getNewObject() {
        return m_javaInfo;
      }
    };
    return new CreationTool(factory);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private JavaInfo createEmbeddedComposite() throws Exception {
    final JavaInfo composite =
        JavaInfoUtils.createJavaInfo(m_editor, Composite.class, new ConstructorCreationSupport());
    composite.addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child == composite) {
          composite.removeBroadcastListener(this);
          fillEmbeddedComposite(composite);
        }
      }
    });
    return composite;
  }

  private void fillEmbeddedComposite(JavaInfo composite) throws Exception {
    composite.getPropertyByTitle("Style").setValue(SWT.EMBEDDED);
    // SWT_AWT.new_Frame()
    ContainerInfo frame;
    {
      FactoryMethodDescription methodDescription =
          FactoryDescriptionHelper.getDescription(
              m_editor,
              SWT_AWT.class,
              "new_Frame(org.eclipse.swt.widgets.Composite)",
              true);
      ComponentDescription componentDescription =
          ComponentDescriptionHelper.getDescription(m_editor, methodDescription);
      frame =
          (ContainerInfo) JavaInfoUtils.createJavaInfo(
              m_editor,
              componentDescription,
              new StaticFactoryCreationSupport(methodDescription));
      JavaInfoUtils.add(frame, null, composite, null);
      frame.getRoot().refreshLight();
    }
    // java.awt.Panel
    ContainerInfo panel;
    {
      panel =
          (ContainerInfo) JavaInfoUtils.createJavaInfo(
              m_editor,
              Panel.class,
              new ConstructorCreationSupport());
      ((BorderLayoutInfo) frame.getLayout()).command_CREATE(panel, null);
      frame.getRoot().refreshLight();
    }
    // set java.awt.BorderLayout on Panel
    {
      BorderLayoutInfo borderLayout =
          (BorderLayoutInfo) JavaInfoUtils.createJavaInfo(
              m_editor,
              BorderLayout.class,
              new ConstructorCreationSupport());
      panel.setLayout(borderLayout);
    }
    // javax.swing.JRootPane
    {
      ContainerInfo rootPane =
          (ContainerInfo) JavaInfoUtils.createJavaInfo(
              m_editor,
              JRootPane.class,
              new ConstructorCreationSupport());
      ((BorderLayoutInfo) panel.getLayout()).command_CREATE(rootPane, null);
    }
  }
}