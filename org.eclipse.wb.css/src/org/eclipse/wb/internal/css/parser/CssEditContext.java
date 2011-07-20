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
package org.eclipse.wb.internal.css.parser;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.css.model.CssDeclarationNode;
import org.eclipse.wb.internal.css.model.CssDocument;
import org.eclipse.wb.internal.css.model.CssNode;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.model.CssVisitor;
import org.eclipse.wb.internal.css.model.at.CssCharsetNode;
import org.eclipse.wb.internal.css.model.punctuation.CssSemiColonNode;
import org.eclipse.wb.internal.css.model.root.IModelChangedListener;
import org.eclipse.wb.internal.css.model.root.ModelChangedEvent;
import org.eclipse.wb.internal.css.model.string.AbstractCssStringNode;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Context for editing CSS files.
 * 
 * @author scheglov_ke
 * @coverage CSS.parser
 */
public final class CssEditContext {
  private final IFile m_file;
  private final ITextFileBuffer m_buffer;
  private final IDocument m_bufferDocument;
  //
  private final IDocument m_document;
  private final CssDocument m_cssDocument;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CssEditContext(IFile file) throws Exception {
    m_file = file;
    // initialize buffer and document
    {
      IPath path = m_file.getFullPath();
      ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
      manager.connect(path, null);
      m_buffer = manager.getTextFileBuffer(path);
      m_bufferDocument = m_buffer.getDocument();
      m_document = new Document(m_bufferDocument.get());
    }
    // prepare CSS document
    m_cssDocument = prepareCssDocument();
  }

  public CssEditContext(IDocument document) throws Exception {
    m_file = null;
    m_buffer = null;
    m_bufferDocument = null;
    m_document = document;
    // prepare CSS document
    m_cssDocument = prepareCssDocument();
  }

  /**
   * Parses {@link CssDocument} using existing JFace document and installs listener for it.
   */
  private CssDocument prepareCssDocument() {
    // prepare CSS document
    CssDocument cssDocument;
    {
      CssParser parser = new CssParser(new StringReader(m_document.get()));
      cssDocument = parser.parse();
    }
    // set listener
    cssDocument.getModel().addModelChangedListener(m_modelChangedListener);
    return cssDocument;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buffer operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Commits changes made in this context to file.
   */
  public final void commit() throws CoreException {
    if (m_bufferDocument != null) {
      m_bufferDocument.set(m_document.get());
    }
    if (m_buffer != null) {
      m_buffer.commit(null, false);
    }
  }

  /**
   * Disconnects this context from file. All changes made in model after this point are ignored and
   * not reflected in document/file.
   */
  public final void disconnect() throws CoreException {
    // disconnect buffer
    if (m_file != null) {
      ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
      manager.disconnect(m_file.getFullPath(), null);
    }
    // remove document update listener
    m_cssDocument.getModel().removeModelChangedListener(m_modelChangedListener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the editing {@link IDocument}.
   */
  public IDocument getDocument() {
    return m_document;
  }

  /**
   * @return the model {@link CssDocument}.
   */
  public CssDocument getCssDocument() {
    return m_cssDocument;
  }

  /**
   * @return the complete text of document.
   */
  public final String getText() {
    return m_document.get();
  }

  /**
   * @return the sub-string of document's text.
   */
  public final String getText(int offset, int length) {
    try {
      return m_document.get(offset, length);
    } catch (Throwable e) {
      String message = String.format("Can not get offset:%d length:%d", offset, length);
      throw new IllegalArgumentException(message, e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Replaces given rule with new source and returns new parsed rule.
   */
  public CssRuleNode replaceRule(CssRuleNode oldRule, String newSource) throws Exception {
    // prepare new rule
    CssRuleNode newRule;
    {
      CssParser parser = new CssParser(new StringReader(newSource));
      CssDocument document = parser.parse();
      if (document.getRules().size() != 1) {
        throw new IllegalArgumentException("Exactly one rule expected in source.");
      }
      newRule = document.getRule(0);
      newRule.setModel(oldRule.getModel());
    }
    // replace rule and source
    m_cssDocument.replaceRule(oldRule, newRule);
    replaceString(oldRule.getOffset(), oldRule.getLength(), newSource, newRule);
    // set old offset for new rule parts
    {
      final int oldOffset = oldRule.getOffset();
      newRule.accept(new CssVisitor() {
        @Override
        public void postVisit(CssNode node) {
          node.setOffset(oldOffset + node.getOffset());
        }
      });
    }
    //
    return newRule;
  }

  /**
   * Move given {@link CssNode} to the new position. This methods just changes start positions for
   * given node and its children, it does not modifies source.
   */
  private static void move(CssNode nodeToMove, int targetOffset) {
    final int moveDelta = targetOffset - nodeToMove.getOffset();
    nodeToMove.accept(new CssVisitor() {
      @Override
      public void postVisit(CssNode node) {
        node.setOffset(node.getOffset() + moveDelta);
      }
    });
  }

  /**
   * Sorts CSS rules using given {@link Comparator}.
   */
  public void sortRules(Comparator<CssRuleNode> comparator) throws Exception {
    // sort nodes
    List<CssRuleNode> rules = m_cssDocument.getRules();
    Collections.sort(rules, comparator);
    // prepare new source
    StringBuffer buffer;
    {
      String eol = null;
      buffer = new StringBuffer();
      int offset = 0;
      for (CssRuleNode rule : rules) {
        // prepare EOL
        if (eol == null) {
          eol = getEOL(rule.getOffset());
        }
        // empty line before non-first rule
        if (buffer.length() != 0) {
          buffer.append(eol);
        }
        // add rule
        buffer.append(m_document.get(rule.getOffset(), rule.getLength()));
        move(rule, offset);
        offset += rule.getLength();
        // new line
        buffer.append(eol);
        offset += 2 * eol.length();
      }
    }
    // set new source and rules
    m_document.set(buffer.toString());
    m_cssDocument.setRules(rules);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Document modification on model modifications
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IModelChangedListener m_modelChangedListener = new IModelChangedListener() {
    public void modelChanged(ModelChangedEvent event) {
      try {
        handleModelChange(event);
      } catch (Throwable e) {
        Throwables.propagate(e);
      }
    }
  };

  /**
   * @return the line delimiter of line at the given offset, or default line delimiter for this
   *         {@link Document} .
   */
  private String getEOL(int offset) throws Exception {
    int line = m_document.getLineOfOffset(offset);
    for (; line >= 0; line--) {
      String eol = m_document.getLineDelimiter(line);
      if (eol != null) {
        return eol;
      }
    }
    return "\n";
  }

  /**
   * Performs document/model updates using given model modification event.
   */
  private void handleModelChange(ModelChangedEvent event) throws Exception {
    if (event.getChangeType() == ModelChangedEvent.CHANGE) {
      if (event.getChangedObject() instanceof AbstractCssStringNode) {
        AbstractCssStringNode string = (AbstractCssStringNode) event.getChangedObject();
        replaceString(string.getOffset(), string.getLength(), (String) event.getNewValue(), null);
      }
    } else if (event.getChangeType() == ModelChangedEvent.INSERT) {
      if (event.getChangedObject() instanceof CssCharsetNode) {
        CssCharsetNode charset = (CssCharsetNode) event.getChangedObject();
        insertCharset(charset);
      }
      if (event.getChangedObject() instanceof CssRuleNode) {
        CssRuleNode rule = (CssRuleNode) event.getChangedObject();
        insertRule(rule);
      }
      if (event.getChangedObject() instanceof CssDeclarationNode) {
        CssDeclarationNode declaration = (CssDeclarationNode) event.getChangedObject();
        insertDeclaration(declaration);
      }
    } else if (event.getChangeType() == ModelChangedEvent.REMOVE) {
      if (event.getChangedObject() instanceof CssCharsetNode) {
        CssCharsetNode charset = (CssCharsetNode) event.getChangedObject();
        removeCharset(charset);
      }
      if (event.getChangedObject() instanceof CssRuleNode) {
        CssRuleNode rule = (CssRuleNode) event.getChangedObject();
        removeRule(rule);
      }
      if (event.getChangedObject() instanceof CssDeclarationNode) {
        CssDeclarationNode declaration = (CssDeclarationNode) event.getChangedObject();
        removeDeclaration(declaration);
      }
    }
  }

  private void removeCharset(CssCharsetNode charset) throws Exception {
    int begin = getBeginOfRemoveText(charset.getOffset());
    int end = getEndOfRemoveText(charset.getEnd());
    replaceString(begin, end - begin, "", null);
  }

  private void removeRule(CssRuleNode rule) throws Exception {
    int begin = getBeginOfRemoveText(rule.getOffset());
    int end = getEndOfRemoveText(rule.getEnd());
    replaceString(begin, end - begin, "", null);
  }

  private void removeDeclaration(CssDeclarationNode declaration) throws Exception {
    int begin = getBeginOfRemoveText(declaration.getOffset());
    int end = getEndOfRemoveText(declaration.getEnd());
    replaceString(begin, end - begin, "", null);
  }

  private void insertDeclaration(CssDeclarationNode declaration) throws Exception {
    String text =
        declaration.getProperty().getValue() + ": " + declaration.getValue().getValue() + ";";
    // prepare position
    int pos;
    {
      CssRuleNode rule = (CssRuleNode) declaration.getParent();
      String indent = getLineIndent(rule);
      int index = rule.getIndex(declaration);
      if (index == 0) {
        pos = rule.getLeftBrace().getEnd();
        String prefix = getEOL(pos) + indent + "\t";
        // add text for new declaration
        replaceString(pos, 0, prefix + text, declaration);
        pos += prefix.length();
      } else {
        CssDeclarationNode prevDeclaration = rule.getDeclaration(index - 1);
        pos = prevDeclaration.getEnd();
        String prefix = getEOL(pos) + indent + "\t";
        // ensure that previous declaration has ';' at the end, so we can add new declarations after it
        {
          char lastPrevDeclarationChar = m_document.getChar(pos - 1);
          if (lastPrevDeclarationChar != ';') {
            replaceString(pos, 0, ";", declaration);
            prevDeclaration.setSemiColon(new CssSemiColonNode(pos));
            prevDeclaration.setEnd(prevDeclaration.getSemiColon().getEnd());
            pos++;
          }
        }
        // add text for new declaration
        replaceString(pos, 0, prefix + text, declaration);
        pos += prefix.length();
      }
      // update offset/length
      {
        declaration.setOffset(pos);
        // property
        {
          declaration.getProperty().setOffset(pos);
          pos += declaration.getProperty().getLength();
        }
        // ':'
        {
          declaration.getColon().setOffset(pos);
          pos += 1;
        }
        // skip space
        pos += 1;
        // value
        {
          declaration.getValue().setOffset(pos);
          pos += declaration.getValue().getLength();
        }
        // ';'
        {
          declaration.setSemiColon(new CssSemiColonNode(pos));
          pos++;
        }
        // set declaration length
        declaration.setEnd(pos);
      }
    }
  }

  private void insertCharset(CssCharsetNode charset) throws Exception {
    // prepare position
    int pos = 0;
    String eol = getEOL(pos);
    // add text
    replaceString(pos, 0, "@charset " + charset.getString().getValue() + ";" + eol, charset);
    // update location
    {
      charset.setOffset(pos);
      pos += "@charset ".length();
      // String
      {
        int length = charset.getString().getValue().length();
        charset.getString().setOffset(pos);
        charset.getString().setLength(length);
        pos += length;
      }
      // ';'
      charset.getSemiColon().setOffset(pos);
      pos += 1;
      // set charset length
      charset.setEnd(pos);
    }
  }

  private void insertRule(CssRuleNode rule) throws Exception {
    // prepare position for adding new rule text
    int pos;
    String prefix;
    String text;
    {
      CssDocument document = (CssDocument) rule.getParent();
      int index = document.getIndex(rule);
      if (index == 0) {
        pos = 0;
        String eol = getEOL(pos);
        prefix = eol;
        text = rule.getSelector().getValue() + " {" + prefix + "}";
        // first rule, so add EOL after it
        text += eol;
      } else {
        CssRuleNode prevRule = document.getRule(index - 1);
        pos = prevRule.getEnd();
        prefix = getEOL(pos) + getLineIndent(prevRule);
        text = rule.getSelector().getValue() + " {" + prefix + "}";
        // there is rule before, so add EOL before this new rule
        replaceString(pos, 0, prefix, rule);
        pos += prefix.length();
      }
    }
    // add rule and update offset/length
    {
      // add rule text and update location
      replaceString(pos, 0, text, rule);
      rule.setOffset(pos);
      // selector
      {
        rule.getSelector().setOffset(pos);
        rule.getSelector().setLength(rule.getSelector().getValue().length());
        pos += rule.getSelector().getLength();
      }
      // skip space
      pos += 1;
      // '{'
      {
        rule.getLeftBrace().setOffset(pos);
        pos += 1;
      }
      // skip prefix
      pos += prefix.length();
      // '}'
      {
        rule.getRightBrace().setOffset(pos);
        pos += 1;
      }
      // set rule length
      rule.setEnd(pos);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // String operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns begin position of text that should be removed starting from given position. We skip any
   * spaces when move left. So, we will find end of previous element or end of previous line.
   */
  private int getBeginOfRemoveText(int begin) throws Exception {
    // skip ' ' and '\t'
    while (begin != 0) {
      char c = m_document.getChar(begin - 1);
      if (!(c == ' ' || c == '\t')) {
        break;
      }
      begin--;
    }
    return begin;
  }

  /**
   * Returns end position of text that should be removed starting from given position. We skip any
   * spaces first, then any new lines. So, we will find beginning of next element of beginning next
   * non-empty line.
   */
  private int getEndOfRemoveText(int end) throws Exception {
    // skip spaces
    while (end < m_document.getLength()) {
      char c = m_document.getChar(end);
      if (!(c == ' ' || c == '\t')) {
        break;
      }
      end++;
    }
    // skip end of lines ('\r' or '\n')
    while (end < m_document.getLength()) {
      char c = m_document.getChar(end);
      if (!(c == '\r' || c == '\n')) {
        break;
      }
      end++;
    }
    return end;
  }

  // XXX
  /**
   * @return the indent of given {@link CssNode}.
   */
  private String getLineIndent(CssNode element) throws Exception {
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

  /**
   * Replaces given region of document with new string. It updates also offset/length for nodes.
   */
  private void replaceString(final int start,
      int oldLength,
      String replacement,
      final CssNode excludeRoot) throws Exception {
    // modify document
    m_document.replace(start, oldLength, replacement);
    // build set of nodes that should not be moved
    final Set<CssNode> excludeSet = Sets.newHashSet();
    if (excludeRoot != null) {
      excludeRoot.accept(new CssVisitor() {
        @Override
        public void postVisit(CssNode node) {
          excludeSet.add(node);
        }
      });
    }
    // update offset/length for model
    final int newLength = replacement.length();
    final int difference = newLength - oldLength;
    if (difference != 0) {
      final int oldEnd = start + oldLength;
      m_cssDocument.accept(new CssVisitor() {
        @Override
        public void postVisit(CssNode node) {
          if (!excludeSet.contains(node)) {
            Position position = updatePosition(node.getOffset(), node.getLength());
            node.setOffset(position.offset);
            node.setLength(position.length);
          }
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
            assert offset <= start;
            position.length += difference;
            return position;
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Position
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Simple position object, wrapper for offset and length.
   */
  private static final class Position {
    int offset;
    int length;

    public Position(int offset, int length) {
      this.offset = offset;
      this.length = length;
    }
  }
}
