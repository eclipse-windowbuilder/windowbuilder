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
package org.eclipse.wb.internal.xwt.model.layout;

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;

/**
 * {@link CreationSupport} for explicit {@link AbsoluteLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.layout
 */
public final class AbsoluteLayoutCreationSupport extends CreationSupport
    implements
      IImplicitCreationSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "explicit-layout: absolute";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link CompositeInfo} parent.
   */
  private CompositeInfo getComposite() {
    return (CompositeInfo) m_object.getParent();
  }

  @Override
  public DocumentElement getElement() {
    return getComposite().getCreationSupport().getElement();
  }

  @Override
  public String getTitle() {
    return toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addElement(DocumentElement parent, int index) throws Exception {
    CompositeInfo composite = getComposite();
    composite.setAttribute("layout", "{x:Null}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void delete() throws Exception {
    CompositeInfo composite = getComposite();
    composite.removeAttribute("layout");
    composite.removeChild(m_object);
  }
}
