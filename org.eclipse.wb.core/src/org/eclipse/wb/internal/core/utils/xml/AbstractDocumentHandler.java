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

import org.eclipse.wb.internal.core.utils.xml.parser.QAttribute;
import org.eclipse.wb.internal.core.utils.xml.parser.QHandlerAdapter;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * SAX handler class that supports {@link DocumentElement} objects reading with offset/length
 * tracking.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public class AbstractDocumentHandler extends QHandlerAdapter {
  private DocumentElement m_rootNode;
  private final Stack<DocumentElement> m_nodeStack = new Stack<DocumentElement>();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public DocumentElement getRootNode() {
    return m_rootNode;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // QDHandler
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void startElement(int offset,
      int length,
      String qName,
      Map<String, String> attributes,
      List<QAttribute> attrList,
      boolean closed) throws Exception {
    DocumentElement parent = m_nodeStack.isEmpty() ? null : m_nodeStack.peek();
    //setTextNodeOffsetLength(parent, offset);
    // create and initialize new node
    DocumentElement element;
    {
      element = getDocumentNode(qName, parent);
      element.setTag(qName);
      element.setOffset(offset);
      element.setClosed(closed);
      if (!closed) {
        element.setOpenTagOffset(offset);
        element.setOpenTagLength(length);
      }
    }
    // configure parent/child
    {
      appendChildToParent(parent, element);
      m_nodeStack.push(element);
    }
    // create attributes
    for (QAttribute qdAttribute : attrList) {
      String attName = qdAttribute.getName();
      String attValue = qdAttribute.getValue();
      DocumentAttribute attribute = getDocumentAttribute(element, attName, attValue);
      // name
      attribute.setNameOffset(qdAttribute.getNameOffset());
      attribute.setNameLength(qdAttribute.getNameLength());
      // value
      attribute.setValueOffset(qdAttribute.getValueOffset());
      attribute.setValueLength(qdAttribute.getValueLength());
    }
  }

  @Override
  public final void endElement(int offset, int endOffset, String tag) throws Exception {
    DocumentElement element = m_nodeStack.pop();
    element.setLength(endOffset - element.getOffset());
    if (!element.isClosed()) {
      element.setCloseTagOffset(offset);
      element.setCloseTagLength(endOffset - offset);
      //setTextNodeOffsetLength(element, element.getCloseTagOffset());
    }
  }

  /*private void setTextNodeOffsetLength(DocumentElement parent, int end) {
  	if (parent != null) {
  		DocumentTextNode textNode = parent.getTextNode();
  		if (textNode != null && textNode.getLength() == 0) {
  			int textOffset = parent.getOpenTagOffset() + parent.getOpenTagLength();
  			textNode.setOffset(textOffset);
  			textNode.setLength(end - textOffset);
  		}
  	}
  }*/
  @Override
  public final void text(String text, boolean isCDATA) throws Exception {
    if (!StringUtils.isWhitespace(text) && !m_nodeStack.isEmpty()) {
      DocumentElement element = m_nodeStack.peek();
      if (element.getTextNode() == null) {
        DocumentTextNode textNode = new DocumentTextNode(isCDATA);
        textNode.setText(text);
        // set offset/length
        int offset = element.getOpenTagOffset() + element.getOpenTagLength();
        int length = text.length();
        if (isCDATA) {
          length += "<![CDATA[]]>".length();
        }
        textNode.setLength(length);
        textNode.setOffset(offset);
        // append
        element.setTextNode(textNode);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new child to given parent.
   */
  private void appendChildToParent(DocumentElement parent, DocumentElement child) {
    if (child != null) {
      if (parent != null) {
        parent.addChild(child);
      } else {
        m_rootNode = child;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Nodes creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates specific {@link DocumentElement} subclass for given tag name and parent.
   */
  protected DocumentElement getDocumentNode(String name, DocumentElement parent) {
    DocumentElement element = new DocumentElement();
    if (parent == null) {
      element.setModel(new Model());
    }
    return element;
  }

  /**
   * Creates {@link DocumentAttribute} for given name/value in given {@link DocumentElement}.
   */
  private DocumentAttribute getDocumentAttribute(DocumentElement element, String name, String value) {
    return element.setAttribute(name, value);
  }
}
