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
package org.eclipse.wb.internal.core.utils;

import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;

/**
 * Intended to write xml file using {@link PrintWriter}.
 *
 * <pre>Sample usage:<code>
 * XMLWriter writer = new XMLWriter(stringWriter);
 * writer.openTag("tag1");
 * writer.openTag("tag2");
 * final DataOutputStream dataOutputStream = new DataOutputStream(writer.streamCDATA());
 * dataOutputStream.writeBytes("cdata contents");
 * dataOutputStream.close();
 * writer.closeTag();
 * writer.closeTag();
 * writer.close();
 * </code></pre>
 *
 * <pre>This will produce the following:<code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;tag1&gt;
 *     &lt;tag2&gt;&lt;![CDATA[
 * cdata contents]]&gt;&lt;/tag2&gt;
 * &lt;/tag1&gt;
 * </code></pre>
 *
 * @author mitin_aa
 * @coverage core.util
 */
public final class XmlWriter {
  private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  // state constants
  private static final int TAG_STATE_BEGIN_NOT_ENDED = 1 << 0;
  private static final int TAG_STATE_OPEN = 1 << 1;
  private static final int TAG_STATE_HAS_CHILDRED = 1 << 2;
  private static final int TAG_STATE_HAS_VALUE = 1 << 3;
  private static final int TAG_STATE_CDATA_OPEN = 1 << 4;
  // fields
  private final PrintWriter m_printWriter;
  private String m_indent = "\t";
  private final Stack<TagInfo> m_tagStack = new Stack<TagInfo>();
  private boolean m_closed;
  private boolean m_isShortTagClosed;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs {@link XmlWriter} and directs it into file with given {@link File}.
   *
   * @param file
   *          the file to store xml contents.
   */
  public XmlWriter(File file) throws Exception {
    this(new PrintWriter(file, "UTF-8"));
  }

  /**
   * Constructs {@link XmlWriter} and directs it into file with given <code>filePath</code>.
   *
   * @param filePath
   *          the path to file.
   */
  public XmlWriter(String filePath) throws Exception {
    this(new PrintWriter(filePath, "UTF-8"));
  }

  /**
   * Constructs {@link XmlWriter} and directs it into <code>writer</code> {@link Writer}. This
   * {@link Writer} would be closed during {@link XmlWriter#close()} invocation.
   *
   * @param writer
   *          the {@link OutputStream} into which {@link XmlWriter} would be directed to.
   */
  public XmlWriter(Writer writer) {
    m_printWriter = new PrintWriter(writer);
    m_printWriter.print(XML_HEADER);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tag operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Open tag with given <code>tagName</code> for further adding tag attribute. Ex. calling
   * beginTag("tag1") will results writing "&lt;tag1" into underlying {@link PrintWriter}. After
   * calling this method only adding attributes and ending tag operations are possible.
   *
   * @param tagName
   *          the tag name with which tag would be made.
   */
  public void beginTag(String tagName) {
    checkOpen();
    Assert.isLegal(!StringUtils.isEmpty(tagName), "Can't add tag with empty name.");
    if (!m_tagStack.empty()) {
      TagInfo currentTag = m_tagStack.peek();
      if (isState(currentTag, TAG_STATE_BEGIN_NOT_ENDED)) {
        throw new IllegalStateException("Another tag already began for attribute adding.");
      }
      addState(currentTag, TAG_STATE_HAS_CHILDRED);
    }
    m_tagStack.push(new TagInfo(tagName, TAG_STATE_BEGIN_NOT_ENDED));
    if (!m_isShortTagClosed) {
      // don't put line separator between "short" tags (<tag attr="attr"/>)
      m_printWriter.println();
    }
    m_isShortTagClosed = false;
    m_printWriter.print(StringUtils.repeat(getIndent(), m_tagStack.size() - 1));
    m_printWriter.print("<" + tagName);
  }

  /**
   * Ends currently open tag. Calling endTag() will results writing "&gt;" into underlying
   * {@link PrintWriter}. After calling this method its able to write tag value but not adding
   * attributes.
   */
  public void endTag() {
    checkOpen();
    TagInfo currentTag = peek();
    if (isState(currentTag, TAG_STATE_BEGIN_NOT_ENDED)) {
      m_printWriter.print(">");
      currentTag.setState(TAG_STATE_OPEN);
    }
  }

  /**
   * Adds an attribute to tag with given <code>attrName</code> and <code>attrValue</code>. Calling
   * this method results writing " attr="value"" into underlying {@link PrintWriter}. After this
   * operation is possible to add another attribute or end tag, all other operations prohibited.
   *
   * Performs escaping the <code>attrValue</code> before writing.
   *
   * @param attrName
   *          the attribute name to add. Cannot be empty or <code>null</code>.
   * @param attrValue
   *          the attribute value. Can be <code>null</code>, in which case the "" added.
   */
  public void writeAttribute(String attrName, String attrValue) {
    checkOpen();
    Assert.isLegal(!StringUtils.isEmpty(attrName), "Can't add attribute with empty name.");
    TagInfo currentTag = peek();
    if (!isState(currentTag, TAG_STATE_BEGIN_NOT_ENDED)) {
      throw new IllegalStateException("Tag should be open for attribute adding.");
    }
    if (attrValue == null) {
      attrValue = "";
    }
    attrValue = StringEscapeUtils.escapeXml(attrValue);
    {
      StringBuilder escaped = new StringBuilder();
      for (int i = 0; i < attrValue.length(); i++) {
        char c = attrValue.charAt(i);
        if (c < 0x20) {
          escaped.append("&#");
          escaped.append((int) c);
          escaped.append(";");
        } else {
          escaped.append(c);
        }
      }
      attrValue = escaped.toString();
    }
    m_printWriter.print(" " + attrName + "=\"" + (attrValue == null ? "" : attrValue) + "\"");
  }

  /**
   * Open tag with given <code>tagName</code>. Calling openTag("tag1") will results writing
   * "&lt;tag1&gt;" into underlying {@link PrintWriter}. After calling this method its able to write
   * tag value but not adding attributes.
   *
   * @param tagName
   *          the tag name with which tag would be made.
   */
  public void openTag(String tagName) {
    beginTag(tagName);
    endTag();
  }

  /**
   * Closes previously open tag. Calling closeTag() when "tag1" is open will results writing
   * "&lt;/tag1&gt;" into underlying {@link PrintWriter}. If tag was open for adding attribute then
   * closing this tag causes write "/&gt;".After this operation is possibly to open new tag.
   */
  public void closeTag() {
    checkOpen();
    TagInfo currentTag = peek();
    boolean validState = false;
    if (isState(currentTag, TAG_STATE_BEGIN_NOT_ENDED)) {
      m_printWriter.println("/>");
      m_isShortTagClosed = true;
      validState = true;
    } else if (hasState(currentTag, TAG_STATE_OPEN)) {
      if (hasState(currentTag, TAG_STATE_HAS_CHILDRED)) {
        if (hasState(currentTag, TAG_STATE_HAS_VALUE)) {
          m_printWriter.println();
        }
        m_printWriter.print(StringUtils.repeat(getIndent(), m_tagStack.size() - 1));
      }
      m_printWriter.println("</" + currentTag.getName() + ">");
      validState = true;
    } else if (hasState(currentTag, TAG_STATE_CDATA_OPEN)) {
      m_printWriter.println("</" + currentTag.getName() + ">");
      validState = true;
    }
    if (!validState) {
      close();
      throw new IllegalStateException("Can't do close operation when currently no open tag.");
    }
    m_tagStack.pop();
  }

  /**
   * Writes tag value into underlying {@link PrintWriter}. Tag should be open (but not for adding
   * attributes).
   *
   * @param tagValue
   *          the {@link String} of tag value. Can be <code>null</code>, in which case the ""
   *          written.
   */
  public void write(String tagValue) {
    checkOpen();
    TagInfo currentTag = peek();
    if (hasState(currentTag, TAG_STATE_OPEN)) {
      if (hasState(currentTag, TAG_STATE_HAS_VALUE)) {
        throw new IllegalStateException("Current tag already have the value written.");
      }
      String valueToWrite = tagValue != null ? tagValue : "";
      m_printWriter.print(StringEscapeUtils.escapeXml(valueToWrite));
      addState(currentTag, TAG_STATE_HAS_VALUE);
    } else {
      throw new IllegalStateException("Can't write tag value when currently no tag open for writing tag value.");
    }
  }

  /**
   * Opens tag, writes tag value and closes the tag.
   *
   * @param tagName
   *          the tag name with which tag would be made.
   * @param tagValue
   *          the {@link String} of tag value. Can be <code>null</code>, in which case the ""
   *          written.
   */
  public void write(String tagName, String tagValue) {
    openTag(tagName);
    write(tagValue);
    closeTag();
  }

  /**
   * Opens CDATA section and returns the {@link OutputStream} to write data to it. The user
   * responsible to close the stream before any other operations with {@link XmlWriter}. Closing
   * stream automatically closes CDATA section.
   *
   * @return the {@link OutputStream} to write CDATA values.
   */
  public OutputStream streamCDATA() {
    checkOpen();
    final TagInfo currentTag = peek();
    if (hasState(currentTag, TAG_STATE_OPEN)) {
      if (hasState(currentTag, TAG_STATE_HAS_VALUE)) {
        throw new IllegalStateException("Current tag already have the value written.");
      }
      m_printWriter.println("<![CDATA[");
      addState(currentTag, TAG_STATE_CDATA_OPEN);
      return new OutputStream() {
        @Override
        public void write(int b) throws IOException {
          m_printWriter.print((char) b);
        }

        @Override
        public void close() throws IOException {
          m_printWriter.print("]]>");
          removeState(currentTag, TAG_STATE_CDATA_OPEN);
          addState(currentTag, TAG_STATE_HAS_VALUE);
        }
      };
    }
    throw new IllegalStateException("No open tag to write CDATA contents.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //  Close operation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Perform finish of writing xml. Flushes and closed underlying {@link PrintWriter}. No more any
   * other operations possible.
   */
  public void close() {
    m_printWriter.close();
    m_closed = true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks the closed state of {@link XmlWriter} and raises {@link IllegalStateException} if it
   * closed.
   */
  private void checkOpen() {
    if (m_closed) {
      throw new IllegalStateException("XMLWriter is closed.");
    }
  }

  /**
   * Check the stack for any open tag.
   *
   * @return currently open tag or throws assertion exception.
   */
  private TagInfo peek() {
    Assert.isTrue(!m_tagStack.empty(), "No any open tag.");
    return m_tagStack.peek();
  }

  /**
   * @return <code>true</code> if given tag has given state.
   */
  private boolean hasState(TagInfo tag, int stateMask) {
    return (tag.getState() & stateMask) != 0;
  }

  /**
   * Adds given state into state of given tag.
   */
  private void addState(TagInfo tag, int stateMask) {
    tag.setState(tag.getState() | stateMask);
  }

  /**
   * Removes given state from given tag state.
   */
  private void removeState(TagInfo tag, int stateMask) {
    tag.setState(tag.getState() & ~stateMask);
  }

  /**
   * @return <code>true</code> if the given tag is exactly in given state.
   */
  private boolean isState(TagInfo tag, int stateMask) {
    return tag.getState() == stateMask;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final String getIndent() {
    return m_indent;
  }

  public final void setIndent(String indent) {
    m_indent = indent;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner class
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class TagInfo {
    private final String m_name;
    private int m_state;

    public TagInfo(String tagName, int state) {
      m_name = tagName;
      m_state = state;
    }

    public final int getState() {
      return m_state;
    }

    public final void setState(int tagState) {
      m_state = tagState;
    }

    public final String getName() {
      return m_name;
    }
  }
}
