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
package org.eclipse.wb.internal.swing.FormLayout.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.FormLayout.parser.DefaultComponentFactoryCreationSupport;

import org.eclipse.swt.graphics.Image;

import com.jgoodies.forms.factories.DefaultComponentFactory;

/**
 * {@link EntryInfo} creates {@link DefaultComponentFactory#createLabel(String)}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class DefaultComponentFactoryCreateLabelEntryInfo
    extends
      DefaultComponentFactoryEntryInfo {
  private static final Image ICON =
      Activator.getImage("DefaultComponentFactory/createLabel_java.lang.String_.gif");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DefaultComponentFactoryCreateLabelEntryInfo() {
    setId(getClass().getName());
    setName("createLabel(String)");
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

      @Override
      public void activate() throws Exception {
        String source = "createLabel(\"New JGoodies label\")";
        m_javaInfo =
            JavaInfoUtils.createJavaInfo(
                m_editor,
                "javax.swing.JLabel",
                new DefaultComponentFactoryCreationSupport(source));
        m_javaInfo.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
      }

      @Override
      public Object getNewObject() {
        return m_javaInfo;
      }
    };
    // return tool
    ensureLibrary();
    return new CreationTool(factory);
  }
}
