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

import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.parser.QParser;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.apache.commons.lang.StringUtils;

import java.io.Reader;
import java.io.StringReader;

/**
 * Wrapper for {@link IDocument} and tree of {@link DocumentElement}'s.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public abstract class AbstractDocumentEditContext {
  private IDocument m_bufferDocument;
  private IDocument m_document;
  private DocumentElement m_root;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractDocumentEditContext() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void parse(IDocument bufferDocument) throws Exception {
    m_bufferDocument = bufferDocument;
    m_document = new Document(m_bufferDocument.get());
    // parse and prepare root
    {
      AbstractDocumentHandler documentHandler = createDocumentHandler();
      Reader reader = new StringReader(m_document.get());
      try {
        QParser.parse(reader, documentHandler);
        m_root = documentHandler.getRootNode();
      } catch (Throwable e) {
        disconnect();
        throw ReflectionUtils.propagate(e);
      } finally {
        reader.close();
      }
    }
    // install document update listener
    m_root.getModel().addModelChangedListener(m_modelChangedListener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buffer operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Commits changes made in this context to original {@link IDocument}.
   */
  public void commit() throws Exception {
    String newContent = m_document.get();
    String oldContent = m_bufferDocument.get();
    if (!newContent.equals(oldContent)) {
      int[] intervals = StringUtilities.getDifferenceIntervals(oldContent, newContent);
      m_bufferDocument.replace(
          intervals[0],
          intervals[1],
          newContent.substring(intervals[2], intervals[2] + intervals[3]));
    }
  }

  /**
   * Disconnects this context from {@link IDocument}. All changes made in model after this point are
   * ignored and not reflected in document.
   */
  public void disconnect() throws CoreException {
    // remove document update listener
    if (m_root != null) {
      m_root.getModel().removeModelChangedListener(m_modelChangedListener);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the root {@link DocumentElement}.
   */
  public final DocumentElement getRoot() {
    return m_root;
  }

  /**
   * @return the complex text of document.
   */
  public final String getText() {
    return m_document.get();
  }

  /**
   * @return the sub-string of document's text.
   */
  public final String getText(final int offset, final int length) {
    return ExecutionUtils.runObject(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        return m_document.get(offset, length);
      }
    }, "Can not get offset:%d length:%d", offset, length);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Abstract methods
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates document handler for loading model.
   */
  protected AbstractDocumentHandler createDocumentHandler() {
    return new AbstractDocumentHandler();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Document modification on model modifications
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IModelChangedListener m_modelChangedListener = new IModelChangedListener() {
    public void modelChanged(final ModelChangedEvent event) {
      ExecutionUtils.runRethrow(new RunnableEx() {
        public void run() throws Exception {
          handleModelChange(event);
        }
      });
    }
  };

  /**
   * Performs document/model updates using given model modification event.
   */
  private void handleModelChange(ModelChangedEvent event) throws Exception {
    int type = event.getChangeType();
    Object changedObject = event.getChangedObject();
    if (type == ModelChangedEvent.CHANGE) {
      if (changedObject instanceof DocumentElement) {
        if (event.getChangedProperty() == DocumentElement.P_XML_TAG_NAME) {
          DocumentElement element = (DocumentElement) changedObject;
          String oldXMLTagName = (String) event.getOldValue();
          handleNodeRename(element, oldXMLTagName);
        } else {
          handleAttributeChange(event);
        }
      } else if (changedObject instanceof DocumentTextNode) {
        DocumentTextNode textNode = (DocumentTextNode) changedObject;
        handleTextChange(textNode);
      }
    } else if (type == ModelChangedEvent.INSERT) {
      if (changedObject instanceof DocumentElement) {
        DocumentElement element = (DocumentElement) changedObject;
        handleNodeInsert(element);
      } else if (changedObject instanceof DocumentAttribute) {
        DocumentAttribute attribute = (DocumentAttribute) changedObject;
        handleAttributeInsert(attribute);
      } else if (changedObject instanceof DocumentTextNode) {
        DocumentTextNode textNode = (DocumentTextNode) changedObject;
        handleTextInsert(textNode);
      }
    } else if (type == ModelChangedEvent.MOVE) {
      if (changedObject instanceof DocumentElement) {
        DocumentElement oldParent = (DocumentElement) event.getOldValue();
        DocumentElement newParent = (DocumentElement) event.getNewValue();
        DocumentElement element = (DocumentElement) changedObject;
        int position = Integer.parseInt(event.getChangedProperty());
        handleNodeMove(element, oldParent, newParent, position);
      }
    } else if (type == ModelChangedEvent.REMOVE) {
      if (changedObject instanceof DocumentElement) {
        DocumentElement element = (DocumentElement) changedObject;
        handleNodeDelete(element);
      } else if (changedObject instanceof DocumentAttribute) {
        DocumentAttribute attribute = (DocumentAttribute) changedObject;
        handleAttributeDelete(attribute);
      } else if (changedObject instanceof DocumentTextNode) {
        DocumentTextNode textNode = (DocumentTextNode) changedObject;
        handleTextDelete(textNode);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Nodes
  //
  ////////////////////////////////////////////////////////////////////////////
  private void handleNodeDelete(DocumentElement element) throws Exception {
    int stringStartIndex = getElementSourceOffset(element);
    // delete part of document with node and any white spaces before it
    // so, in usual situation, when node is on its own line, we will remove this line fully
    int length = element.getOffset() + element.getLength() - stringStartIndex;
    replaceString(stringStartIndex, length, "");
    // if last child removed, then parent should be closed
    closeIfNoChildren(element.getParent());
  }

  /**
   * @return the indent of given {@link DocumentElement}.
   */
  private String getLineIndent(DocumentElement element) throws Exception {
    int end = element.getOffset();
    int start = end;
    while (start != 0) {
      char c = m_document.getChar(start - 1);
      // in any case we need whitespace
      if (!Character.isWhitespace(c)) {
        break;
      }
      // stop on \r and \n
      if (c == '\r' || c == '\n') {
        break;
      }
      //
      start--;
    }
    // return result
    return m_document.get(start, end - start);
  }

  private void handleNodeRename(DocumentElement element, String oldXMLTagName) throws Exception {
    replaceString(element.getOffset() + 1, oldXMLTagName.length(), element.getTag());
    if (!element.isClosed()) {
      replaceString(element.getCloseTagOffset() + 2, oldXMLTagName.length(), element.getTag());
    }
  }

  private void handleNodeInsert(DocumentElement element) throws Exception {
    DocumentElement parentNode = element.getParent();
    ensureElementOpen(parentNode);
    TargetInformation targetInfo =
        prepareTargetInformation(parentNode, parentNode.indexOf(element));
    int baseOffset = targetInfo.offset;
    String prefix = targetInfo.prefix;
    // add new tag
    String text = "<" + element.getTag() + "/>";
    replaceString(baseOffset, 0, prefix + text);
    // remember offset and length
    int offset = baseOffset + prefix.length();
    element.setOffset(offset);
    element.setLength(text.length());
    element.setClosed(true);
  }

  /**
   * If given "element" has no children, then convert in into closed.
   */
  private void closeIfNoChildren(DocumentElement element) throws Exception {
    if (element.getChildren().isEmpty() && element.getTextNode() == null) {
      int begin = element.getOpenTagOffset() + element.getOpenTagLength() - 1;
      int end = element.getCloseTagOffset() + element.getCloseTagLength();
      replaceString(begin, end - begin, "/>");
      element.setClosed(true);
    }
  }

  /**
   * Ensures that given {@link DocumentElement} is in form <tag></tag>.
   */
  private void ensureElementOpen(DocumentElement element) throws Exception {
    if (!element.isClosed()) {
      return;
    }
    //
    int oldLength = element.getLength();
    int end = element.getOffset() + oldLength;
    int line = m_document.getLineOfOffset(end);
    String lineDelimiter = m_document.getLineDelimiter(line);
    if (lineDelimiter == null) {
      lineDelimiter = "\n";
    }
    // add closing tag
    String closeTagReplacement =
        lineDelimiter + getLineIndent(element) + "</" + element.getTag() + ">";
    replaceString(end, 0, closeTagReplacement);
    // remove close from initial tag
    replaceString(end - 2, 2, ">");
    // update length
    element.setLength(oldLength + closeTagReplacement.length() - 1);
    updateOpenCloseTags(element, end);
  }

  private static class TargetInformation {
    int offset;
    String prefix;
  }

  private TargetInformation prepareTargetInformation(DocumentElement parentNode, int index)
      throws Exception {
    // prepare:
    //  1. baseOffset = (end of parent) or (end of previous child node)
    //  2. indent = (indent of parent + "\t") or (indent of previous child node)
    int baseOffset;
    String indent;
    {
      if (index == 0) {
        baseOffset = parentNode.getOpenTagOffset() + parentNode.getOpenTagLength();
        indent = getLineIndent(parentNode) + "\t";
      } else {
        DocumentElement previousChild = parentNode.getChildAt(index - 1);
        baseOffset = previousChild.getOffset() + previousChild.getLength();
        indent = getLineIndent(previousChild);
      }
    }
    // prepare prefix = EOL + indent
    String prefix;
    {
      int line = m_document.getLineOfOffset(baseOffset);
      String lineDelimiter = m_document.getLineDelimiter(line);
      if (lineDelimiter == null) {
        prefix = "";
      } else {
        prefix = lineDelimiter + indent;
      }
    }
    //
    TargetInformation target = new TargetInformation();
    target.offset = baseOffset;
    target.prefix = prefix;
    return target;
  }

  private void handleNodeMove(DocumentElement element,
      DocumentElement oldParent,
      DocumentElement newParent,
      int index) throws Exception {
    // remove from old parent
    // if reorder in same parent, tweak index
    {
      int oldIndex = oldParent.indexOf(element);
      oldParent.m_children.remove(element);
      if (newParent == oldParent && oldIndex < index) {
        index--;
      }
    }
    // prepare begin of String to move
    int beginOffset = getElementSourceOffset(element);
    // extract String from old parent
    String elementString;
    {
      int endOffset = element.getOffset() + element.getLength();
      elementString = m_document.get(beginOffset, endOffset - beginOffset);
      elementString = StringUtils.stripStart(elementString, null);
      replaceString(beginOffset, endOffset - beginOffset, "");
    }
    // prepare target information
    ensureElementOpen(newParent);
    TargetInformation targetInfo = prepareTargetInformation(newParent, index);
    // insert prefix
    String prefix = targetInfo.prefix;
    int newOffset = targetInfo.offset;
    replaceString(newOffset, 0, prefix);
    newOffset += prefix.length();
    // insert "element" String
    replaceString(newOffset, 0, elementString);
    // add into new parent
    newParent.m_children.add(index, element);
    element.setParent(newParent);
    moveElementOffsets(element, newOffset - element.getOffset());
    // if last child was moved, then parent should be closed
    closeIfNoChildren(oldParent);
  }

  /**
   * @return the begin of {@link String} to move/remove when we move/remove given
   *         {@link DocumentElement}.
   */
  private int getElementSourceOffset(DocumentElement element) throws Exception {
    int offset = element.getOffset();
    while (true) {
      // may be hit of "text node"
      {
        DocumentTextNode textNode = element.getParent().getTextNode();
        if (textNode != null
            && textNode.getOffset() <= offset
            && offset <= textNode.getOffset() + textNode.getLength()) {
          break;
        }
      }
      // non-whitespace character
      {
        char c = m_document.getChar(offset - 1);
        if (!Character.isWhitespace(c)) {
          break;
        }
      }
      // check previous
      offset--;
    }
    return offset;
  }

  private void moveElementOffsets(DocumentElement start, final int delta) {
    start.accept(new DocumentModelVisitor() {
      @Override
      public void endVisit(DocumentElement element) {
        element.setOffset(element.getOffset() + delta);
        if (!element.isClosed()) {
          element.setOpenTagOffset(element.getOpenTagOffset() + delta);
          element.setCloseTagOffset(element.getCloseTagOffset() + delta);
        }
      }

      @Override
      public void visit(DocumentAttribute attribute) {
        attribute.setNameOffset(attribute.getNameOffset() + delta);
        attribute.setValueOffset(attribute.getValueOffset() + delta);
      }

      @Override
      public void visit(DocumentTextNode node) {
        node.setOffset(node.getOffset() + delta);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attributes
  //
  ////////////////////////////////////////////////////////////////////////////
  private void handleAttributeChange(ModelChangedEvent event) throws Exception {
    DocumentElement element = (DocumentElement) event.getChangedObject();
    DocumentAttribute attribute = element.getDocumentAttribute(event.getChangedProperty());
    // prepare existing offset/length
    int offset = attribute.getValueOffset();
    int length = attribute.getValueLength();
    // update document
    String newValue = (String) event.getNewValue();
    replaceString(offset, length, newValue);
  }

  private void handleAttributeInsert(DocumentAttribute attribute) throws Exception {
    DocumentElement element = attribute.getEnclosingElement();
    // prepare offset where to insert attribute
    int offset;
    {
      // find last attribute
      DocumentAttribute lastAttribute = null;
      for (DocumentAttribute existingAttribute : element.getDocumentAttributes()) {
        if (existingAttribute != attribute) {
          lastAttribute = existingAttribute;
        }
      }
      //
      if (lastAttribute == null) {
        offset = element.getOffset() + element.getTag().length() + 1;
      } else {
        offset = lastAttribute.getValueOffset() + lastAttribute.getValueLength() + 1;
      }
    }
    // insert space
    {
      replaceString(offset, 0, " ");
      offset++;
    }
    // update document
    String assignOpen = "=\"";
    {
      String assignClose = "\"";
      String text = attribute.getName() + assignOpen + attribute.getValue() + assignClose;
      replaceString(offset, 0, text);
    }
    // set offset/length for name/value
    {
      int nameLength = attribute.getName().length();
      attribute.setNameOffset(offset);
      attribute.setNameLength(nameLength);
      attribute.setValueOffset(offset + nameLength + assignOpen.length());
      attribute.setValueLength(attribute.getValue().length());
    }
  }

  private void handleAttributeDelete(DocumentAttribute attribute) throws Exception {
    // prepare start of string to delete
    int stringStartIndex = attribute.getNameOffset();
    while (Character.isWhitespace(m_document.getChar(stringStartIndex - 1))) {
      stringStartIndex--;
    }
    // delete part of document with attribute and any white spaces before it
    int length = attribute.getValueOffset() + attribute.getValueLength() + 1 - stringStartIndex;
    replaceString(stringStartIndex, length, "");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Text
  //
  ////////////////////////////////////////////////////////////////////////////
  private void handleTextChange(DocumentTextNode textNode) throws Exception {
    int offset = textNode.getOffset();
    int length = textNode.getLength();
    String text = textNode.getRawText();
    replaceString(offset, length, text);
  }

  private void handleTextInsert(DocumentTextNode textNode) throws Exception {
    DocumentElement parentElement = textNode.getEnclosingElement();
    // ensure that parent is in form <tag></tag>
    if (parentElement.isClosed()) {
      int oldLength = parentElement.getLength();
      // add closing tag
      String closeTagReplacement = "</" + parentElement.getTag() + ">";
      int parentEnd = parentElement.getOffset() + oldLength;
      replaceString(parentEnd, 0, closeTagReplacement);
      // remove close from initial tag
      replaceString(parentEnd - 2, 2, ">");
      // update length
      parentElement.setLength(oldLength + closeTagReplacement.length() - 1);
      updateOpenCloseTags(parentElement, parentEnd);
    }
    //
    {
      // add new text node
      int startOffset = parentElement.getOpenTagOffset() + parentElement.getOpenTagLength();
      int oldLength = parentElement.getCloseTagOffset() - startOffset;
      String text = textNode.getRawText();
      replaceString(startOffset, oldLength, text);
      // remember offset and length
      textNode.setOffset(startOffset);
      textNode.setLength(text.length());
    }
  }

  private void handleTextDelete(DocumentTextNode textNode) throws Exception {
    replaceString(textNode.getOffset(), textNode.getLength(), "");
    closeIfNoChildren(textNode.getEnclosingElement());
  }

  private static void updateOpenCloseTags(DocumentElement parentNode, int parentEnd) {
    parentNode.setClosed(false);
    // update open tag position
    parentNode.setOpenTagOffset(parentNode.getOffset());
    parentNode.setOpenTagLength(parentEnd - 1 - parentNode.getOffset());
    // update close tag position
    parentNode.setCloseTagLength(parentNode.getTag().length() + 3);
    parentNode.setCloseTagOffset(parentNode.getOffset()
        + parentNode.getLength()
        - parentNode.getCloseTagLength());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // String operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Replaces given region of document with new string. It updates also offset/length for nodes.
   */
  private void replaceString(final int start, int oldLength, String replacement) throws Exception {
    // modify document
    //System.out.println("|" + m_document.get(start, oldLength) + "| -> |" + replacement + "|");
    m_document.replace(start, oldLength, replacement);
    // update offset/length for model
    final int newLength = replacement.length();
    final int difference = newLength - oldLength;
    if (difference != 0) {
      final int oldEnd = start + oldLength;
      m_root.accept(new DocumentModelVisitor() {
        @Override
        public void endVisit(DocumentElement element) {
          Position position = updatePosition(element.getOffset(), element.getLength());
          // update node position
          element.setOffset(position.offset);
          element.setLength(position.length);
          if (!element.isClosed()) {
            int openTagOffset = element.getOpenTagOffset();
            int openTagLength = element.getOpenTagLength();
            // update open tag position
            if (openTagOffset < start && oldEnd < openTagOffset + openTagLength) {
              element.setOpenTagLength(openTagLength + difference);
            }
            element.setOpenTagOffset(position.offset);
            // update close tag position
            int closeTagOffset = element.getCloseTagOffset();
            if (start <= closeTagOffset) {
              element.setCloseTagOffset(closeTagOffset + difference);
            } else {
              element.setCloseTagLength(position.offset + position.length - closeTagOffset);
            }
          }
        }

        @Override
        public void visit(DocumentAttribute attribute) {
          // update name position
          {
            Position position =
                updatePosition(attribute.getNameOffset(), attribute.getNameLength());
            attribute.setNameOffset(position.offset);
            attribute.setNameLength(position.length);
          }
          // update value position
          {
            Position position =
                updatePosition(attribute.getValueOffset(), attribute.getValueLength());
            attribute.setValueOffset(position.offset);
            attribute.setValueLength(position.length);
          }
        }

        @Override
        public void visit(DocumentTextNode node) {
          Position position = updatePosition(node.getOffset(), node.getLength());
          node.setOffset(position.offset);
          node.setLength(position.length);
        }

        private Position updatePosition(int offset, int length) {
          Position position = new Position(offset, length);
          // special case: insert into empty position
          if (offset == start && length == 0) {
            position.length += difference;
            return position;
          }
          // before changed region, no change
          if (offset + length <= start) {
            return position;
          }
          // after changed region, move it
          if (offset >= oldEnd) {
            position.offset += difference;
            return position;
          }
          // encompasses the changed region, so extend it
          /*if (offset <= start)*/{
            Assert.isTrue(offset <= start);
            position.length += difference;
            return position;
          }
        }
      });
    }
  }
}
