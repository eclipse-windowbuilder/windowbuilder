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
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.IWrapperInfo;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.CreationAttributeDescription;
import org.eclipse.wb.internal.core.xml.model.description.CreationDescription;
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Support for holding or adding {@link DocumentElement} of {@link XmlObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.model.creation
 */
public class ElementCreationSupport extends CreationSupport implements ILiveCreationSupport {
  private DocumentElement m_element;
  private String m_creationId;
  private boolean m_addAttributes;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ElementCreationSupport(DocumentElement element) {
    m_element = element;
  }

  public ElementCreationSupport() {
    this((String) null);
  }

  public ElementCreationSupport(String creationId) {
    this(creationId, true);
  }

  public ElementCreationSupport(String creationId, boolean addAttributes) {
    m_creationId = creationId;
    m_addAttributes = addAttributes;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    if (m_element == null) {
      return m_creationId + " " + m_addAttributes;
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

  @Override
  public void setObject(XmlObjectInfo object) throws Exception {
    super.setObject(object);
    // apply parameters from CreationDescription
    {
      CreationDescription creation = m_object.getDescription().getCreation(m_creationId);
      if (creation != null) {
        for (Map.Entry<String, String> entry : creation.getParameters().entrySet()) {
          XmlObjectUtils.setParameter(m_object, entry.getKey(), entry.getValue());
        }
      }
    }
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
    // we can really "delete" object only if it is not root
    if (m_object.isRoot()) {
      return;
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
    if (m_object instanceof IWrapperInfo) {
      return m_element;
    } else {
      XmlObjectInfo parent = m_object.getParentXML();
      return XmlObjectUtils.getElementInParent(parent, m_element);
    }
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
    ComponentDescription description = m_object.getDescription();
    Class<?> componentClass = description.getComponentClass();
    // prepare creation
    CreationDescription creation;
    {
      creation = description.getCreation(m_creationId);
      Assert.isNotNull2(creation, "No creation: {0} {1}", componentClass, m_creationId);
    }
    // new DocumentElement
    m_element = new DocumentElement();
    // tag
    {
      String tag = XmlObjectUtils.getTagForClass(m_object, componentClass);
      m_element.setTag(tag);
    }
    // add DocumentElement
    parent.addChild(m_element, index);
    // set attributes
    if (m_addAttributes) {
      for (CreationAttributeDescription attribute : creation.getAttributes()) {
        new AddAttributeHelper(m_element).addAttribute(attribute);
      }
    }
    // set content
    if (m_addAttributes) {
      String content = creation.getContent();
      if (content != null) {
        m_element.setText(content, false);
      }
    }
  }

  private static class AddAttributeHelper extends NamespacesHelper {
    private final DocumentElement m_element;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AddAttributeHelper(DocumentElement element) {
      super(element.getRoot());
      m_element = element;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    void addAttribute(CreationAttributeDescription attribute) {
      String prefix = getNamespacePrefix(attribute);
      m_element.setAttribute(prefix + attribute.getName(), attribute.getValue());
    }

    private String getNamespacePrefix(CreationAttributeDescription attribute) {
      String namespace = attribute.getNamespace();
      // no namespace
      if (StringUtils.isEmpty(namespace)) {
        return "";
      }
      // prepare name of namespace
      return ensureName(namespace, "a") + ":";
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IClipboardCreationSupport getClipboard() {
    return new IClipboardCreationSupport() {
      private static final long serialVersionUID = 0L;

      @Override
      public CreationSupport create(XmlObjectInfo rootObject) throws Exception {
        return new ElementCreationSupport(null, false);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILiveCreationSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreationSupport getLiveComponentCreation() {
    return new ElementCreationSupport(m_creationId, m_addAttributes);
  }
}
