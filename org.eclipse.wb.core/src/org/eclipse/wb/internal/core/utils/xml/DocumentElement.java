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
package org.eclipse.wb.internal.core.utils.xml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.print.attribute.DocAttribute;

/**
 * Element in XML.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public class DocumentElement extends AbstractDocumentObject {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DocumentElement() {
  }

  public DocumentElement(String tag) {
    m_tag = tag;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    write(printWriter, "");
    return stringWriter.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tag
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final String P_XML_TAG_NAME = "P_XML_TAG_NAME";
  private String m_tag;
  private boolean m_closed;

  /**
   * @return the name of tag.
   */
  public String getTag() {
    return m_tag;
  }

  /**
   * @return the simple name of tag, without namespace.
   */
  public String getTagLocal() {
    int index = m_tag.indexOf(":");
    if (index == -1) {
      return m_tag;
    }
    return m_tag.substring(index + 1);
  }

  /**
   * @return the namespace of tag, including ":" character.
   */
  public String getTagNS() {
    int index = m_tag.indexOf(":");
    if (index == -1) {
      return StringUtils.EMPTY;
    }
    return m_tag.substring(0, index + 1);
  }

  /**
   * Sets the name of tag.
   */
  public void setTag(String tag) {
    if (!StringUtils.equals(m_tag, tag)) {
      String oldValue = m_tag;
      m_tag = tag;
      firePropertyChanged(this, P_XML_TAG_NAME, oldValue, m_tag);
    }
  }

  /**
   * Sets the simple name of tag, without namespace.
   */
  public void setTagLocal(String localTag) {
    String tag = getTagNS() + localTag;
    setTag(tag);
  }

  /**
   * @return <code>true</code> if this element is closed on start.
   */
  public boolean isClosed() {
    return m_closed;
  }

  /**
   * Specifies if this element is closed on start.
   */
  public void setClosed(boolean closed) {
    m_closed = closed;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_offset;
  private int m_length;

  public int getOffset() {
    return m_offset;
  }

  public void setOffset(int offset) {
    m_offset = offset;
  }

  public int getLength() {
    return m_length;
  }

  public void setLength(int length) {
    m_length = length;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location: open
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_openTagOffset;
  private int m_openTagLength;

  public int getOpenTagOffset() {
    return m_openTagOffset;
  }

  public void setOpenTagOffset(int openTagOffset) {
    m_openTagOffset = openTagOffset;
  }

  public int getOpenTagLength() {
    return m_openTagLength;
  }

  public void setOpenTagLength(int openTagLength) {
    m_openTagLength = openTagLength;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location: close
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_closeTagOffset;
  private int m_closeTagLength;

  public int getCloseTagOffset() {
    return m_closeTagOffset;
  }

  public void setCloseTagOffset(int closeTagOffset) {
    m_closeTagOffset = closeTagOffset;
  }

  public int getCloseTagLength() {
    return m_closeTagLength;
  }

  public void setCloseTagLength(int closeTagLength) {
    m_closeTagLength = closeTagLength;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parent
  //
  ////////////////////////////////////////////////////////////////////////////
  private DocumentElement m_parent;

  /**
   * Sets the parent {@link DocumentElement}, may be <code>null</code>.
   */
  public void setParent(DocumentElement parent) {
    m_parent = parent;
  }

  /**
   * @return the parent {@link DocumentElement}, may be <code>null</code> for root.
   */
  public DocumentElement getParent() {
    return m_parent;
  }

  /**
   * @return the root {@link DocumentElement}.
   */
  public DocumentElement getRoot() {
    if (m_parent == null) {
      return this;
    }
    return m_parent.getRoot();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  final List<DocumentElement> m_children = Lists.newArrayList();

  /**
   * Adds given {@link DocumentElement} as last child.
   */
  public void addChild(DocumentElement child) {
    addChild(child, m_children.size());
  }

  /**
   * Adds given {@link DocumentElement} into given position relative to other children.
   */
  public void addChild(DocumentElement child, int index) {
    m_children.add(index, child);
    child.setParent(this);
    child.setModel(getModel());
    fireStructureChanged(child, ModelChangedEvent.INSERT);
  }

  /**
   * Moves existing {@link DocumentElement} into given position relative to other children.
   */
  public void moveChild(DocumentElement child, int index) {
    if (index == -1) {
      index = m_children.size();
    }
    fireModelChanged(new ModelChangedEvent(ModelChangedEvent.MOVE,
        child,
        "" + index,
        child.getParent(),
        this));
  }

  /**
   * Removes given child {@link DocumentElement}.
   */
  public void removeChild(DocumentElement child) {
    int index = m_children.indexOf(child);
    if (index != -1) {
      m_children.remove(index);
      fireStructureChanged(child, ModelChangedEvent.REMOVE);
    }
  }

  /**
   * Removes this {@link DocumentElement} from parent.
   */
  public void remove() {
    m_parent.removeChild(this);
  }

  /**
   * Removes all children {@link DocumentElement}s.
   */
  public void removeChildren() {
    while (!m_children.isEmpty()) {
      m_children.get(0).remove();
    }
  }

  /**
   * @return the {@link DocumentElement} children.
   */
  public List<DocumentElement> getChildren() {
    return ImmutableList.copyOf(m_children);
  }

  /**
   * @return {@link DocumentElement}s that are instances of given {@link Class}.
   */
  @SuppressWarnings("unchecked")
  public final <T extends DocumentElement> List<T> getChildren(Class<T> elementClass) {
    List<T> result = Lists.newArrayList();
    for (DocumentElement child : getChildren()) {
      if (elementClass.isInstance(child)) {
        result.add((T) child);
      }
    }
    return result;
  }

  /**
   * @return the {@link DocumentElement} which is direct child of this one and is or contains given
   *         one. May be <code>null</code>, if given "child" is not really (in)direct child of this
   *         {@link DocumentElement}.
   */
  public DocumentElement getDirectChild(DocumentElement child) {
    while (child != null) {
      if (child.getParent() == this) {
        return child;
      }
      child = child.getParent();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Indexed access to children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of given {@link DocumentElement} child, may be <code>-1</code> if not child.
   */
  public int indexOf(DocumentElement child) {
    return m_children.indexOf(child);
  }

  /**
   * @return the {@link DocumentElement} at given index.
   *
   * @throws IndexOutOfBoundsException
   *           if the index is out of range.
   */
  public DocumentElement getChildAt(int index) {
    return m_children.get(index);
  }

  /**
   * @return the first child {@link DocumentElement} with given tag, may be <code>null</code>.
   */
  public DocumentElement getChild(String tag, boolean ignoreCase) {
    for (DocumentElement child : m_children) {
      if (ignoreCase) {
        if (child.getTag().equalsIgnoreCase(tag)) {
          return child;
        }
      } else {
        if (child.getTag().equals(tag)) {
          return child;
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Text
  //
  ////////////////////////////////////////////////////////////////////////////
  private DocumentTextNode m_textNode;

  /**
   * @return the {@link DocumentTextNode}, may be <code>null</code>.
   */
  public DocumentTextNode getTextNode() {
    return m_textNode;
  }

  /**
   * Sets the not <code>null</code> {@link DocumentTextNode} with text.
   */
  public void setTextNode(DocumentTextNode textNode) {
    m_textNode = textNode;
    m_textNode.setEnclosingElement(this);
    m_textNode.setModel(getModel());
    fireStructureChanged(m_textNode, ModelChangedEvent.INSERT);
  }

  /**
   * Removes {@link DocumentTextNode}, if exists. Ignored if no text node.
   */
  public void removeTextNode() {
    if (m_textNode != null) {
      DocumentTextNode textNode = m_textNode;
      m_textNode = null;
      fireStructureChanged(textNode, ModelChangedEvent.REMOVE);
    }
  }

  /**
   * <ul>
   * <li>If text is not <code>null</code>, then creates {@link DocumentTextNode} and uses
   * {@link #setTextNode(DocumentTextNode)}.</li>
   * <li>If text is <code>null</code>, then removes {@link DocumentTextNode}.</li>
   * </ul>
   */
  public void setText(String text, boolean isCDATA) {
    if (text != null) {
      DocumentTextNode textNode = new DocumentTextNode(isCDATA);
      textNode.setText(text);
      setTextNode(textNode);
    } else {
      removeTextNode();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attributes as values
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, DocumentAttribute> m_attributes = Maps.newLinkedHashMap();

  /**
   * Sets the value of attribute: adds new, updates or existing.
   *
   * @param name
   *          the name of attribute.
   * @param value
   *          new value of attribute, may be <code>null</code> to remove it.
   *
   * @return the handled attribute, may be <code>null</code>.
   */
  public DocumentAttribute setAttribute(String name, String value) {
    DocumentAttribute attribute = getDocumentAttribute(name);
    // may be delete
    if (value == null) {
      if (attribute != null) {
        removeDocumentAttribute(attribute);
      }
      return attribute;
    }
    // new attribute
    if (attribute == null) {
      attribute = new DocumentAttribute();
      attribute.setModel(getModel());
      attribute.setName(name);
      attribute.setValue(value);
      attribute.setEnclosingElement(this);
      // add
      m_attributes.put(attribute.getName(), attribute);
      // send notification
      fireStructureChanged(attribute, ModelChangedEvent.INSERT);
      return attribute;
    }
    // update attribute
    {
      attribute.setValue(value);
      return attribute;
    }
  }

  /**
   * @return the value of attribute with given name, may be <code>null</code> if no such attribute.
   */
  public String getAttribute(String name) {
    DocumentAttribute attribute = m_attributes.get(name);
    return attribute != null ? attribute.getValue() : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attributes as objects
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link DocumentAttribute} with given name, may be <code>null</code>.
   */
  public DocumentAttribute getDocumentAttribute(String name) {
    return m_attributes.get(name);
  }

  /**
   * @return the {@link List} with all attributes.
   */
  public List<DocumentAttribute> getDocumentAttributes() {
    return ImmutableList.copyOf(m_attributes.values());
  }

  /**
   * Removes given {@link DocAttribute}.
   */
  public void removeDocumentAttribute(DocumentAttribute attribute) {
    m_attributes.remove(attribute.getName());
    fireStructureChanged(attribute, ModelChangedEvent.REMOVE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Write
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Writes this {@link DocumentElement} and its attribute and children indented.
   */
  public void write(PrintWriter writer, String indent) {
    if (m_children.isEmpty()) {
      // write open tag
      writer.print(indent);
      writer.print("<");
      writer.print(m_tag);
      writeAttributes(writer);
      // write text and close tag
      if (m_textNode == null) {
        writer.print("/>\n");
      } else {
        // write end of open tag
        writer.print(">");
        // write text
        writer.print(m_textNode.getRawText());
        // write close tag
        writer.print("</");
        writer.print(m_tag);
        writer.print(">\n");
      }
    } else {
      // write open tag
      writer.print(indent);
      writer.print("<");
      writer.print(m_tag);
      writeAttributes(writer);
      writer.print(">\n");
      // write children
      String childrenIndent = indent + "\t";
      for (DocumentElement child : m_children) {
        child.write(writer, childrenIndent);
      }
      // write close tag
      writer.print(indent);
      writer.print("</");
      writer.print(m_tag);
      writer.print(">\n");
    }
  }

  /**
   * Writes this {@link DocumentElement} and its attribute, but not children.
   */
  public void writeShort(PrintWriter writer) {
    // write open tag
    writer.print("<");
    writer.print(m_tag);
    writeAttributes(writer);
    // close tag
    writer.print(">");
  }

  /**
   * Writers attributes of this element.
   */
  private void writeAttributes(PrintWriter writer) {
    for (DocumentAttribute attribute : getDocumentAttributes()) {
      writer.print(" ");
      attribute.write(writer);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visits this {@link DocumentElement}, its attributes and child {@link DocumentElement}s.
   */
  public final void accept(DocumentModelVisitor visitor) {
    if (visitor.visit(this)) {
      // visit attributes
      for (DocumentAttribute attribute : getDocumentAttributes()) {
        visitor.visit(attribute);
      }
      // visit text node
      if (m_textNode != null) {
        visitor.visit(m_textNode);
      }
      // visit children
      for (DocumentElement child : getChildren()) {
        child.accept(visitor);
      }
      // end visit
      visitor.endVisit(this);
    }
  }
}
