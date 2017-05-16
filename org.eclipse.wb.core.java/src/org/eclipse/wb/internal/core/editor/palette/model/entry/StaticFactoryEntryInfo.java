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
package org.eclipse.wb.internal.core.editor.palette.model.entry;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;

/**
 * Implementation of {@link EntryInfo} for "static-factory-method" contribution.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class StaticFactoryEntryInfo extends FactoryEntryInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public StaticFactoryEntryInfo() {
  }

  public StaticFactoryEntryInfo(CategoryInfo categoryInfo,
      String factoryClassName,
      AttributesProvider attributes) {
    super(categoryInfo, factoryClassName, attributes);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "StaticFactoryMethod(class='"
        + m_factoryClassName
        + "' signature='"
        + m_methodSignature
        + "')";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isStaticFactory() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ToolEntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Tool createTool() throws Exception {
    if (!ensureComponentDescription()) {
      return null;
    }
    // prepare factory
    ICreationFactory factory = new ICreationFactory() {
      private JavaInfo m_javaInfo;

      public void activate() throws Exception {
        CreationSupport creationSupport = new StaticFactoryCreationSupport(m_methodDescription);
        m_javaInfo = createJavaInfo(creationSupport);
        m_javaInfo = JavaInfoUtils.getWrapped(m_javaInfo);
        m_javaInfo.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
      }

      public Object getNewObject() {
        return m_javaInfo;
      }
    };
    // return tool
    return new CreationTool(factory);
  }
}
