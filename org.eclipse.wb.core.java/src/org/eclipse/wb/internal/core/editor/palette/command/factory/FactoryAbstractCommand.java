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
package org.eclipse.wb.internal.core.editor.palette.command.factory;

import org.eclipse.wb.core.editor.palette.model.AbstractElementInfo;
import org.eclipse.wb.internal.core.editor.palette.command.AbstractElementCommand;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.model.entry.FactoryEntryInfo;

import org.xml.sax.Attributes;

/**
 * Abstract {@link Command} for {@link FactoryEntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public abstract class FactoryAbstractCommand extends AbstractElementCommand {
  protected final String m_factoryClassName;
  protected final String m_methodSignature;
  protected final boolean m_forStatic;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactoryAbstractCommand(String id,
      String name,
      String description,
      boolean visible,
      String factoryClassName,
      String methodSignature,
      boolean forStatic) {
    super(id, name, description, visible);
    m_factoryClassName = factoryClassName;
    m_methodSignature = methodSignature;
    m_forStatic = forStatic;
  }

  public FactoryAbstractCommand(Attributes attributes) {
    super(attributes);
    m_factoryClassName = attributes.getValue("class");
    m_methodSignature = attributes.getValue("signature");
    m_forStatic = "true".equals(attributes.getValue("static"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final void updateElement(AbstractElementInfo element) {
    super.updateElement(element);
    FactoryEntryInfo component = (FactoryEntryInfo) element;
    component.setFactoryClassName(m_factoryClassName);
    component.setMethodSignature(m_methodSignature);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    super.addAttributes();
    addAttribute("class", m_factoryClassName);
    addAttribute("signature", m_methodSignature);
    addAttribute("static", m_forStatic);
  }
}
