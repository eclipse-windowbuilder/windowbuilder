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
package org.eclipse.wb.internal.core.xml.model.association;

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.utils.ElementTarget;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * {@link Association} which put child element into sub-element with given name.
 * 
 * @author scheglov_ke
 * @coverage XML.model.association
 */
public final class IntermediateAssociation extends DirectAssociation {
  private final String m_name;
  private Map<String, String> m_attributes;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public IntermediateAssociation(String name) {
    m_name = name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    String s = "inter " + m_name;
    if (m_attributes != null) {
      s += " " + m_attributes;
    }
    return s;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets attributes which should be set for new "intermediate" element.
   */
  public void setAttributes(Map<String, String> attributes) {
    m_attributes = attributes;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void add(XmlObjectInfo object, ElementTarget target) throws Exception {
    target = prepareTarget(target);
    super.add(object, target);
  }

  @Override
  public void move(XmlObjectInfo object,
      ElementTarget target,
      XmlObjectInfo oldParent,
      XmlObjectInfo newParent) throws Exception {
    // if reorder, move with enclosing element
    if (newParent == oldParent) {
      DocumentElement targetElement = target.getElement();
      int targetIndex = target.getIndex();
      targetElement.moveChild(object.getElement().getParent(), targetIndex);
      return;
    }
    // create new "intermediate" element
    target = prepareTarget(target);
    super.move(object, target, oldParent, newParent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private ElementTarget prepareTarget(ElementTarget target) {
    // prepare "intermediate" element
    DocumentElement interElement;
    {
      DocumentElement targetElement = target.getElement();
      int targetIndex = target.getIndex();
      // create "intermediate" element
      interElement = new DocumentElement();
      {
        String tag = m_name;
        tag = StringUtils.replace(tag, "{parentNS}", targetElement.getTagNS());
        interElement.setTag(tag);
      }
      // add it
      targetElement.addChild(interElement, targetIndex);
      // add attributes
      if (m_attributes != null) {
        for (Map.Entry<String, String> entry : m_attributes.entrySet()) {
          interElement.setAttribute(entry.getKey(), entry.getValue());
        }
      }
    }
    // prepare new target
    return new ElementTarget(interElement, 0);
  }
}
