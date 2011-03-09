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
package org.eclipse.wb.internal.core.xml.model.creation;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * {@link CreationSupport} for {@link XmlObjectInfo} without component class, using just tag name.
 * <p>
 * TODO Introduce Java class based {@link XmlObjectInfo} and just tag based (such as HTML elements).
 * 
 * @author scheglov_ke
 * @coverage XML.model.creation
 */
public final class TagCreationSupport extends CreationSupport {
  private DocumentElement m_element;
  private String m_tag;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TagCreationSupport(DocumentElement element) {
    m_element = element;
  }

  public TagCreationSupport(String tag) {
    m_tag = tag;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    if (m_element == null) {
      return m_tag;
    }
    return getElementString(m_element);
  }

  /**
   * @return the "toString()" version of {@link DocumentElement}.
   */
  public static String getElementString(DocumentElement element) {
    // use XML source
    String result;
    {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      element.writeShort(printWriter);
      result = stringWriter.toString();
    }
    // remove "xmlns" declarations
    result = result.replaceAll("\\s*xmlns:*\\w*=\"[^\"]*\"", "");
    // done
    return result;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public DocumentElement getElement() {
    return m_element;
  }

  @Override
  public String getTitle() {
    return m_element.getTag();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Deletes this {@link XmlObjectInfo}.
   */
  @Override
  public void delete() throws Exception {
    // delete children
    List<ObjectInfo> children = ImmutableList.copyOf(m_object.getChildren());
    for (ObjectInfo child : children) {
      child.delete();
    }
    // remove element
    {
      DocumentElement elementToRemove = getElementToRemove();
      elementToRemove.remove();
    }
    // remove from parent
    m_object.getParent().removeChild(m_object);
  }

  private DocumentElement getElementToRemove() {
    XmlObjectInfo parent = m_object.getParentXML();
    return XmlObjectUtils.getElementInParent(parent, m_element);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link DocumentElement} for creating this {@link XmlObjectInfo}.
   */
  @Override
  public void addElement(DocumentElement parent, int index) throws Exception {
    // new DocumentElement
    m_element = new DocumentElement();
    m_element.setTag(m_tag);
    // add DocumentElement
    parent.addChild(m_element, index);
  }
}
