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
package org.eclipse.wb.internal.core.utils.ast;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.ast.binding.BindingContext;
import org.eclipse.wb.internal.core.utils.ast.binding.DesignerMethodBinding;
import org.eclipse.wb.internal.core.utils.ast.binding.DesignerTypeBinding;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link AstEditor} is central point for all AST and source editing operations, such as adding new
 * statements, method invocations, methods, etc.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public final class AstEditor {
  public static final String DEFAULT_END_OF_LINE = System.getProperty("line.separator", "\n");
  private static final String REMOVED_COMMENT = "ASTEditor.REMOVED_COMMENT";
  private final ICompilationUnit m_modelUnit;
  private final CompilationUnit m_astUnit;
  private String m_oldContent;
  private final Document m_document;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AstEditor(ICompilationUnit modelUnit) throws Exception {
    m_modelUnit = modelUnit;
    m_astUnit = CodeUtils.parseCompilationUnit(modelUnit);
    m_oldContent = m_modelUnit.getBuffer().getContents();
    m_document = new Document(m_oldContent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IJavaProject}.
   */
  public IJavaProject getJavaProject() {
    return m_modelUnit.getJavaProject();
  }

  /**
   * @return the {@link IProject}.
   */
  public IProject getProject() {
    return getJavaProject().getProject();
  }

  /**
   * @return {@link ICompilationUnit} - model unit.
   */
  public ICompilationUnit getModelUnit() {
    return m_modelUnit;
  }

  /**
   * @return {@link CompilationUnit} - ast unit.
   */
  public CompilationUnit getAstUnit() {
    return m_astUnit;
  }

  /**
   * @return <code>true</code> if {@link CompilationUnit} has error problems.
   */
  public boolean hasCompilationErrors() {
    for (IProblem problem : m_astUnit.getProblems()) {
      if (problem.isError()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the primary {@link TypeDeclaration}.
   */
  public TypeDeclaration getPrimaryType() {
    String unitName = m_modelUnit.getElementName();
    String typeName = StringUtils.removeEnd(unitName, ".java");
    return AstNodeUtils.getTypeByName(m_astUnit, typeName);
  }

  /**
   * @return the {@link IType} of given {@link TypeDeclaration}.
   */
  public IType getModelType(TypeDeclaration typeDeclaration) {
    String name = typeDeclaration.getName().getIdentifier();
    return m_modelUnit.getType(name);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Enclosing" utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ASTNode} that starts with given source, such that there are no child node
   *         that also covers same position.
   */
  public ASTNode getEnclosingNode(String source) {
    int position = getSource().indexOf(source);
    Assert.isTrue(position != -1, "Can not find %s in %s.", source, getSource());
    return AstNodeUtils.getEnclosingNode(m_astUnit, position);
  }

  /**
   * @return the {@link ASTNode} that covers passed position, such that there are no child node that
   *         also covers same position.
   */
  public ASTNode getEnclosingNode(int position) {
    return AstNodeUtils.getEnclosingNode(m_astUnit, position);
  }

  /**
   * @return the {@link Statement} that encloses given position.
   */
  public Statement getEnclosingStatement(int position) {
    return AstNodeUtils.getEnclosingStatement(getEnclosingNode(position));
  }

  /**
   * @return the {@link Block} that encloses given position.
   */
  public Block getEnclosingBlock(int position) {
    return AstNodeUtils.getEnclosingBlock(getEnclosingNode(position));
  }

  /**
   * @return the {@link MethodDeclaration} that encloses given position.
   */
  public MethodDeclaration getEnclosingMethod(int position) {
    return AstNodeUtils.getEnclosingMethod(getEnclosingNode(position));
  }

  /**
   * @return the {@link TypeDeclaration} that encloses given position.
   */
  public TypeDeclaration getEnclosingType(int position) {
    return AstNodeUtils.getEnclosingType(getEnclosingNode(position));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commit
  //
  ////////////////////////////////////////////////////////////////////////////
  private IASTEditorCommitListener m_commitListener;

  /**
   * Sets the {@link IASTEditorCommitListener}.
   */
  public void setCommitListener(IASTEditorCommitListener commitListener) {
    m_commitListener = commitListener;
  }

  /**
   * Saves current source code into underlying {@link ICompilationUnit}.
   */
  public void commitChanges() throws Exception {
    // pre-listener
    if (m_commitListener != null) {
      m_commitListener.aboutToCommit();
    }
    // set contents, if changed
    {
      String newContent = m_document.get();
      if (!m_oldContent.equals(newContent)
          && (m_commitListener == null || m_commitListener.canEditBaseFile())) {
        int[] intervals = StringUtilities.getDifferenceIntervals(m_oldContent, newContent);
        m_modelUnit.getBuffer().replace(
            intervals[0],
            intervals[1],
            newContent.substring(intervals[2], intervals[2] + intervals[3]));
        m_oldContent = newContent;
      }
    }
    // post-listener
    if (m_commitListener != null) {
      m_commitListener.commitDone();
    }
  }

  /**
   * Commits changes into {@link ICompilationUnit} and saves it, if not opened in editor.
   */
  public void saveChanges(boolean forceSave) throws Exception {
    commitChanges();
    // save, if not working copy, i.e. not opened in editor
    if (forceSave || !m_modelUnit.isWorkingCopy()) {
      m_modelUnit.getBuffer().save(null, false);
      m_modelUnit.save(null, false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Text reading
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the character from document.
   */
  public char getChar(int position) {
    try {
      return m_document.getChar(position);
    } catch (BadLocationException e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * @return the whitespace substring that ends at given <code>end</code> position and starts
   *         somewhere before it.
   *
   * @param includeEOL
   *          is <code>true</code> if characters '\r' and '\n' also should be skipped.
   */
  public String getWhitespaceToLeft(int end, boolean includeEOL) {
    // find first non-whitespace index
    int start = end;
    while (start != 0) {
      char c = getChar(start - 1);
      // in any case we need whitespace
      if (!Character.isWhitespace(c)) {
        break;
      }
      // if EOL is not enabled, check \r and \n
      if (!includeEOL && (c == '\r' || c == '\n')) {
        break;
      }
      //
      start--;
    }
    // return result
    return getSourceBeginEnd(start, end);
  }

  /**
   * @return the line number that contains given index.
   */
  public int getLineNumber(int index) {
    try {
      return m_document.getLineOfOffset(index);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * @return the index of first character of line that contains given index.
   */
  public int getLineBegin(int index) {
    try {
      IRegion lineInformation = m_document.getLineInformationOfOffset(index);
      return lineInformation.getOffset();
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * @return the index of the first character of the end-of-line marker for the line containing the
   *         given position, or the length of the source if the position is on the last line and is
   *         therefore not followed by an end-of-line marker.
   */
  public int getLineEnd(int index) {
    try {
      IRegion lineInformation = m_document.getLineInformationOfOffset(index);
      return lineInformation.getOffset() + lineInformation.getLength();
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * @return the index of first non-whitespace character.
   *
   * @param includeEOL
   *          is <code>true</code> if characters '\r' and '\n' also should be skipped.
   */
  public int skipWhitespaceToLeft(int end, boolean includeEOL) {
    // find first non-whitespace index
    int start = end;
    while (start != 0) {
      char c = getChar(start - 1);
      // in any case we need whitespace
      if (!Character.isWhitespace(c)) {
        break;
      }
      // if EOL is not enabled, check \r and \n
      if (!includeEOL && (c == '\r' || c == '\n')) {
        break;
      }
      //
      start--;
    }
    // return result
    return start;
  }

  /**
   * @return the index of first non-whitespace character or start of first line that is empty or
   *         contains only end of line comments (EOLC).
   */
  public int skipWhitespaceAndPureEOLCToLeft(int end) throws Exception {
    // skip whitespace to the left
    int index = end - getWhitespaceToLeft(end, false).length();
    if (index == 0) {
      return index;
    }
    // skip lines with pure EOLC
    while (true) {
      int line = m_document.getLineOfOffset(index - 1);
      int lineOffset = m_document.getLineOffset(line);
      String lineString = getSource(lineOffset, m_document.getLineLength(line));
      int firstNonWhitespace = StringUtils.indexOfAnyBut(lineString, " \t\r\n");
      // check for empty line
      if (firstNonWhitespace == -1) {
        index = lineOffset;
        continue;
      }
      // check if line starts with EOLC, so contains only it
      if (lineString.substring(firstNonWhitespace).startsWith("//")) {
        index = lineOffset;
        continue;
      }
      // previous line was last empty one
      break;
    }
    //
    return index;
  }

  /**
   * If characters at left are EOL, skip one.
   */
  public int skipSingleEOLToLeft(int index) throws Exception {
    if (m_document.getChar(index - 1) == '\n') {
      index--;
    }
    if (m_document.getChar(index - 1) == '\r') {
      index--;
    }
    return index;
  }

  /**
   * @return single source {@link String} for given lines of source.
   *
   * @param lines
   *          the lines of source
   * @param indent
   *          the "base" indentation
   * @param singleIndent
   *          the indentation that to replace each leading "\t"
   * @param eol
   *          the EOL string
   */
  private static String getIndentedSource(List<String> lines,
      String indent,
      String singleIndent,
      String eol) {
    StringBuffer buffer = new StringBuffer();
    for (String line : lines) {
      // EOL
      if (buffer.length() != 0) {
        buffer.append(eol);
      }
      // indentation
      buffer.append(indent);
      // line
      if (line.length() != 0) {
        int tabsCount = StringUtils.indexOfAnyBut(line, "\t");
        if (tabsCount != -1) {
          buffer.append(StringUtils.repeat(singleIndent, tabsCount));
          buffer.append(line.substring(tabsCount));
        } else {
          buffer.append(StringUtils.repeat(singleIndent, line.length()));
        }
      }
    }
    return buffer.toString();
  }

  /**
   * @return the first occurrence of given sub-string.
   *
   * @throws IllegalArgumentException
   *           if no such index can be found.
   */
  public int indexOf(String subString) {
    return indexOf(subString, 0);
  }

  /**
   * @return the first occurrence of given sub-string.
   *
   * @throws IllegalArgumentException
   *           if no such index can be found.
   */
  public int indexOf(String subString, int startPos) {
    int index = indexOf_noEx(subString, startPos);
    if (index != -1) {
      return index;
    }
    // not found
    throw new IllegalArgumentException("Can not find '" + subString + "' starting from " + startPos);
  }

  /**
   * @return the first occurrence of given sub-string, may be <code>-1</code> if not found.
   */
  private int indexOf_noEx(String subString, int startPos) {
    return m_document.get().indexOf(subString, startPos);
  }

  /**
   * @return the first index of any character in the given set of characters.
   *
   * @throws IllegalArgumentException
   *           if no such index can be found.
   */
  private int indexOfAny(String searchChars, int startPos) throws Exception {
    for (int i = startPos; i < m_document.getLength(); i++) {
      char c = m_document.getChar(i);
      for (int j = 0; j < searchChars.length(); j++) {
        if (searchChars.charAt(j) == c) {
          return i;
        }
      }
    }
    // not found
    throw new IllegalArgumentException("Can not find '"
        + searchChars
        + "' starting from "
        + startPos);
  }

  /**
   * @return the first index of any character not in the given set of characters.
   *
   * @throws IllegalArgumentException
   *           if no such index can be found.
   */
  private int indexOfAnyBut(String searchChars, int startPos) throws Exception {
    outer : for (int i = startPos; i < m_document.getLength(); i++) {
      char c = m_document.getChar(i);
      for (int j = 0; j < searchChars.length(); j++) {
        if (searchChars.charAt(j) == c) {
          continue outer;
        }
      }
      return i;
    }
    // not found
    throw new IllegalArgumentException("Can not find '"
        + searchChars
        + "' starting from "
        + startPos);
  }

  /**
   * @return the nearest index of character when move backward from given position.
   *
   * @throws IllegalArgumentException
   *           if no such index can be found.
   */
  public int indexOfCharBackward(char searchChar, int endPos) {
    for (int i = endPos - 1; i != 0; i--) {
      char c = getChar(i);
      if (c == searchChar) {
        return i;
      }
    }
    // not found
    throw new IllegalArgumentException("Can not find '" + searchChar + "' starting from " + endPos);
  }

  /**
   * @return the first index of any character not in the given set of characters.
   *
   * @throws IllegalArgumentException
   *           if no such index can be found.
   */
  private int indexOfAnyButBackward(String searchChars, int startPos) throws Exception {
    outer : for (int i = startPos - 1; i != 0; i--) {
      char c = m_document.getChar(i);
      for (int j = 0; j < searchChars.length(); j++) {
        if (searchChars.charAt(j) == c) {
          continue outer;
        }
      }
      return i;
    }
    // not found
    throw new IllegalArgumentException("Can not find '"
        + searchChars
        + "' starting from "
        + startPos);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EOL comments
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of passed {@link StringLiteral} on same line.
   */
  public int getStringLiteralNumberOnLine(StringLiteral stringLiteral) {
    final int slLine = getLineNumber(stringLiteral.getStartPosition());
    // find shortest node that fills full line
    ASTNode lineNode = stringLiteral;
    while (getLineNumber(lineNode.getStartPosition()) == slLine
        && getLineNumber(lineNode.getStartPosition() + lineNode.getLength()) == slLine) {
      lineNode = lineNode.getParent();
    }
    // find all literals on this line
    final List<StringLiteral> literals = Lists.newArrayList();
    lineNode.accept(new ASTVisitor() {
      @Override
      public void endVisit(StringLiteral literal) {
        if (getLineNumber(literal.getStartPosition()) == slLine) {
          literals.add(literal);
        }
      }
    });
    // find index of given literal
    return literals.indexOf(stringLiteral);
  }

  /**
   * Insert the given EOL comment at the end of the line containing the given position.
   */
  public void addEndOfLineComment(int position, String comment) throws Exception {
    int endOfLinePosition = getLineEnd(position);
    replaceSubstring(endOfLinePosition, 0, comment);
  }

  /**
   * @return the end of line comment (including leading <code>"//"</code>) at the end of the line
   *         containing the given position.
   */
  public String getEndOfLineComment(int position) {
    int lineBegin = getLineBegin(position);
    int lineEnd = getLineEnd(position);
    int commentBegin = indexOf_noEx("//", lineBegin);
    if (commentBegin != -1 && commentBegin < lineEnd) {
      return getSource(commentBegin, lineEnd - commentBegin);
    }
    return null;
  }

  /**
   * Remove the end of line comment at the end of the line containing the given position.
   */
  public void removeEndOfLineComment(int position, String commentToRemove) throws Exception {
    int lineBegin = getLineBegin(position);
    int lineEnd = getLineEnd(position);
    int commentBegin = indexOf_noEx(commentToRemove, lineBegin);
    if (commentBegin != -1 && commentBegin < lineEnd) {
      int commentEnd = commentBegin + commentToRemove.length();
      // update comment end
      commentEnd = indexOfAnyBut(" \t", commentEnd);
      // replace
      replaceSubstring(commentBegin, commentEnd - commentBegin, "");
      // remove leading whitespace (but keep at least one, if there is comment)
      {
        int newCommentBegin = indexOfAnyButBackward(" \t", commentBegin) + 1;
        if (newCommentBegin != commentBegin) {
          if (m_document.get(commentBegin, 2).equals("//")) {
            newCommentBegin++;
          }
          replaceSubstring(newCommentBegin, commentBegin - newCommentBegin, "");
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Text editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the internal {@link Document}, should be used only internally.
   */
  Document getBuffer() {
    return m_document;
  }

  /**
   * @return the full source of {@link CompilationUnit}.
   */
  public String getSource() {
    return m_document.get();
  }

  /**
   * Sets the full source of {@link CompilationUnit}. Note that it does not update AST, so
   * practically can be used only to restore some old source as last step of using this
   * {@link AstEditor}.
   */
  public void setSource(String source) {
    m_document.set(source);
  }

  /**
   * @return the source corresponding to the given {@link ASTNode}.
   */
  public String getSource(ASTNode node) {
    return getSource(node.getStartPosition(), node.getLength());
  }

  /**
   * @return the substring of source with given start position and length.
   */
  public String getSource(int start, int length) {
    try {
      return m_document.get(start, length);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * @return the substring of source with given begin/end positions.
   */
  public String getSourceBeginEnd(int begin, int end) {
    return getSource(begin, end - begin);
  }

  /**
   * Examples:
   *
   * <pre>
	 * SWT.NONE = org.eclipse.swt.SWT.NONE
	 * new JButton() = new javax.swing.JButton()
	 * </pre>
   *
   * @param theNode
   *          the {@link ASTNode} to get the source.
   * @param transformer
   *          the {@link Function} that can participate in node to source transformation by
   *          providing alternative source for some nodes. Can be <code>null</code>, in not
   *          additional transformation required. If transformer returns not <code>null</code>, we
   *          use it instead of its original source; if <code>null</code> - we continue with
   *          original source.
   *
   * @return the source of {@link ASTNode} in "external form", i.e. with fully qualified types.
   */
  @SuppressWarnings("restriction")
  public String getExternalSource(final ASTNode theNode, final Function<ASTNode, String> transformer) {
    final StringBuffer buffer = new StringBuffer(getSource(theNode));
    // remember positions for all nodes
    final Map<ASTNode, Integer> nodePositions = Maps.newHashMap();
    theNode.accept(new ASTVisitor() {
      @Override
      public void postVisit(ASTNode _node) {
        nodePositions.put(_node, _node.getStartPosition());
      }
    });
    // replace "name" with "qualified name"
    theNode.accept(new org.eclipse.jdt.internal.corext.dom.GenericVisitor() {
      @Override
      protected boolean visitNode(ASTNode node) {
        if (transformer != null) {
          String source = transformer.apply(node);
          if (source != null) {
            replace(node, source);
            return false;
          }
        }
        return true;
      }

      @Override
      public void endVisit(SimpleName name) {
        if (!AstNodeUtils.isVariable(name)) {
          StructuralPropertyDescriptor location = name.getLocationInParent();
          if (location == SimpleType.NAME_PROPERTY
              || location == QualifiedName.QUALIFIER_PROPERTY
              || location == ClassInstanceCreation.NAME_PROPERTY
              || location == MethodInvocation.EXPRESSION_PROPERTY) {
            String fullyQualifiedName = AstNodeUtils.getFullyQualifiedName(name, false);
            replace(name, fullyQualifiedName);
          }
        }
      }

      /**
       * Replace given ASTNode with different source, with updating positions for other nodes.
       */
      private void replace(ASTNode node, String newSource) {
        int nodePosition = nodePositions.get(node);
        // update source
        {
          int sourceStart = nodePosition - theNode.getStartPosition();
          int sourceEnd = sourceStart + node.getLength();
          buffer.replace(sourceStart, sourceEnd, newSource);
        }
        // update positions for nodes
        int lengthDelta = newSource.length() - node.getLength();
        for (Map.Entry<ASTNode, Integer> entry : nodePositions.entrySet()) {
          Integer position = entry.getValue();
          if (position > nodePosition) {
            entry.setValue(position + lengthDelta);
          }
        }
      }
    });
    // OK, we have updated source
    return buffer.toString();
  }

  /**
   * @return the source of given {@link ITypeBinding}, including generics.
   */
  public String getTypeBindingSource(ITypeBinding typeBinding) {
    String genericTypeName = AstNodeUtils.getFullyQualifiedName(typeBinding, false, true);
    genericTypeName = StringUtils.replace(genericTypeName, ",", ", ");
    return genericTypeName;
  }

  /**
   * @see #replaceSubstring(int, int, String)
   */
  public void replaceSubstring(ASTNode node, String replacement) throws Exception {
    replaceSubstring(node.getStartPosition(), node.getLength(), replacement);
  }

  /**
   * Replaces the substring starting at the given start position with the given length by the given
   * replacement text, modifying the source ranges for the nodes in the AST in the process.
   *
   * @param oldStart
   *          the index of the first character being replaced
   * @param oldLength
   *          the number of characters being replaced
   * @param replacement
   *          the text with which they are being replaced
   */
  public void replaceSubstring(final int oldStart, int oldLength, String replacement)
      throws Exception {
    replaceSubstring_markRemovedComments(oldStart, oldLength);
    List<Comment> commentList = getCommentList();
    // replace text
    //System.out.println("|" + m_document.get(oldStart, oldLength) + "| -> |" + replacement + "|");
    m_document.replace(oldStart, oldLength, replacement);
    // prepare positions
    final int newLength = replacement.length();
    final int difference = newLength - oldLength;
    final int oldEnd = oldStart + oldLength;
    // prepare visitor
    ASTVisitor visitor = new ASTVisitor(true) {
      @Override
      public void postVisit(ASTNode node) {
        int position = node.getStartPosition();
        int length = node.getLength();
        int end = position + length;
        // sanity checks
        {
          // we can not start replacement inside of node, but end outside
          if (position < oldStart && oldStart < end && oldEnd > end) {
            throw new DesignerException(ICoreExceptionConstants.AST_EDITOR_REPLACE);
          }
          // we can not start replacement outside of node, but end inside
          if (position < oldEnd && oldEnd < end && oldStart < position) {
            throw new DesignerException(ICoreExceptionConstants.AST_EDITOR_REPLACE);
          }
        }
        //
        if (end <= oldStart) {
          // before changed region: no change
        } else if (position >= oldEnd && !(node instanceof CompilationUnit)) {
          // after changed region: move
          node.setSourceRange(position + difference, length);
        } else if (position <= oldStart && position + length >= oldEnd) {
          // embraces changed region: change length
          node.setSourceRange(position, length + difference);
        }
        // special handling for AnonymousTypeDeclaration
        {
          TypeDeclaration anonymous = AnonymousTypeDeclaration.get(node);
          if (anonymous != null) {
            anonymous.setSourceRange(node.getStartPosition(), node.getLength());
          }
        }
      }
    };
    // modify position/length for nodes in AST
    m_astUnit.accept(visitor);
    // update comments
    for (Comment comment : commentList) {
      if (!(comment instanceof Javadoc)) {
        comment.accept(visitor);
      }
    }
  }

  /**
   * When we replace region, we should remove {@link Comment} in it. We can not really remove them
   * (JDT returns unmodifiable list), so we mark them as removed.
   */
  private void replaceSubstring_markRemovedComments(int begin, int length) {
    List<Comment> comments = DomGenerics.getCommentList(m_astUnit);
    for (Iterator<Comment> I = comments.iterator(); I.hasNext();) {
      Comment comment = I.next();
      if (AstNodeUtils.getSourceBegin(comment) >= begin
          && AstNodeUtils.getSourceEnd(comment) < begin + length) {
        comment.setProperty(REMOVED_COMMENT, Boolean.TRUE);
      }
    }
  }

  /**
   * @return the {@link List} of {@link Comment}'s in this {@link CompilationUnit}.
   */
  public List<Comment> getCommentList() throws Exception {
    List<Comment> comments = Lists.newArrayList();
    comments.addAll(DomGenerics.getCommentList(m_astUnit));
    // clean up
    int documentLength = m_document.getLength();
    for (Iterator<Comment> I = comments.iterator(); I.hasNext();) {
      Comment comment = I.next();
      if (comment.getProperty(REMOVED_COMMENT) != null) {
        I.remove();
        continue;
      }
      if (AstNodeUtils.getSourceBegin(comment) < 0
          || AstNodeUtils.getSourceEnd(comment) > documentLength) {
        I.remove();
      } else if (comment instanceof LineComment) {
        if (!getSource(comment).startsWith("//")) {
          I.remove();
        }
      } else if (comment instanceof BlockComment) {
        if (!getSource(comment).startsWith("/*")) {
          I.remove();
        }
      }
    }
    // protect from modifications
    return Collections.unmodifiableList(comments);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTNode's replacement
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Set<Method> m_invalidNodeMethods = Sets.newHashSet();

  /**
   * Replaces given <code>originalNode</code> with <code>replacementNode</code>. This method uses
   * "duck type" implementation, i.e. uses names and types to identify location of nodes instead of
   * direct checks for {@link ASTNode} structures.
   */
  @SuppressWarnings("unchecked")
  public static void replaceNode(ASTNode originalNode, ASTNode replacementNode) throws Exception {
    ASTNode parent = originalNode.getParent();
    // special case: QualifiedName -> FieldAccess
    if (replaceNode_QualifiedName_to_FieldAccess(originalNode, replacementNode)) {
      return;
    }
    // use "duck type"
    Class<?> parentClass = parent.getClass();
    for (Method method : parentClass.getMethods()) {
      if (method.getParameterTypes().length == 0 && !m_invalidNodeMethods.contains(method)) {
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        try {
          // check for getXXX() and use setXXX()
          if (methodName.startsWith("get")
              && returnType.isAssignableFrom(originalNode.getClass())
              && method.invoke(parent) == originalNode) {
            String setMethodName = "set" + methodName.substring("get".length());
            Method setMethod = parentClass.getMethod(setMethodName, new Class[]{returnType});
            setMethod.invoke(parent, new Object[]{replacementNode});
            break;
          }
          // check for "List xxx()" and use "List.set()"
          if (returnType == List.class) {
            List<ASTNode> elements = (List<ASTNode>) method.invoke(parent);
            int index = elements.indexOf(originalNode);
            if (index != -1) {
              elements.set(index, replacementNode);
              break;
            }
          }
        } catch (InvocationTargetException e) {
          Assert.isTrue(e.getCause() instanceof UnsupportedOperationException);
          // it is possible that we will try to call method that is not supported in JLS3 AST,
          // so we should catch exception and ignore such methods
          m_invalidNodeMethods.add(method);
        }
      }
    }
  }

  /**
   * If we try to replace qualifier of {@link QualifiedName} with generic {@link Expression}, not
   * just {@link Name}, then {@link QualifiedName} itself should be replaced with
   * {@link FieldAccess}.
   */
  private static boolean replaceNode_QualifiedName_to_FieldAccess(ASTNode originalNode,
      ASTNode replacementNode) throws Exception {
    ASTNode parent = originalNode.getParent();
    if (parent instanceof QualifiedName && !(replacementNode instanceof Name)) {
      QualifiedName qualifiedName = (QualifiedName) parent;
      // prepare FieldAccess
      FieldAccess fieldAccess = originalNode.getAST().newFieldAccess();
      AstNodeUtils.copySourceRange(fieldAccess, qualifiedName);
      fieldAccess.setExpression((Expression) replacementNode);
      // use same "name" node
      {
        SimpleName fieldName = qualifiedName.getName();
        qualifiedName.setName(originalNode.getAST().newSimpleName("__wbp_tmp"));
        fieldAccess.setName(fieldName);
      }
      // replace QualifiedName with FieldAccess
      replaceNode(qualifiedName, fieldAccess);
      return true;
    }
    return false;
  }

  /**
   * Replaces given old {@link Expression} with new {@link Expression} corresponding to the given
   * Java source. If source contains EOL, it will be indented.
   *
   * @return the new {@link Expression}.
   */
  public Expression replaceExpression(Expression oldExpression, String source) throws Exception {
    // check for source with EOL, should be handled as indented
    if (source.indexOf("\n") != -1) {
      String[] lines = StringUtils.split(source, "\n");
      return replaceExpression(oldExpression, Arrays.asList(lines));
    }
    // "normal" source, without EOL
    return replaceExpressionString(oldExpression, source);
  }

  /**
   * Replaces given old {@link Expression} with new {@link Expression} corresponding the Java source
   * given as array of lines.
   *
   * @return the new {@link Expression}.
   */
  public Expression replaceExpression(Expression oldExpression, List<String> lines)
      throws Exception {
    // prepare source
    String source;
    {
      // prepare code generation constants
      AstCodeGeneration generation = getGeneration();
      String singleIndent = generation.getIndentation(1);
      String eol = generation.getEndOfLine();
      // prepare enclosing node
      ASTNode enclosingNode = null;
      {
        enclosingNode = AstNodeUtils.getEnclosingStatement(oldExpression);
        if (enclosingNode == null) {
          enclosingNode = AstNodeUtils.getEnclosingNode(oldExpression, BodyDeclaration.class);
        }
        Assert.isNotNull(enclosingNode, "No enclosing node found for " + oldExpression);
      }
      // indent source
      String indent = getWhitespaceToLeft(enclosingNode.getStartPosition(), false);
      source = getIndentedSource(lines, indent, singleIndent, eol);
      // remove indentation for first line
      source = source.trim();
    }
    //
    return replaceExpressionString(oldExpression, source);
  }

  /**
   * Replaces given old {@link Expression} with new {@link Expression} corresponding to the given
   * Java source. Source used as is, without checks for EOL, indentation, etc.
   *
   * @return the new {@link Expression}.
   */
  private Expression replaceExpressionString(Expression oldExpression, String source)
      throws Exception {
    int oldStart = oldExpression.getStartPosition();
    int oldLength = oldExpression.getLength();
    // update source
    source = replaceSourceTemplates(oldStart, source);
    // replace expression
    Expression newExpression = getParser().parseExpression(oldStart, source);
    replaceSubstring(oldStart, oldLength, source);
    replaceNode(oldExpression, newExpression);
    // final step
    resolveImports(newExpression);
    return newExpression;
  }

  /**
   * Replaces single argument of given {@link MethodInvocation}.
   */
  public Expression replaceInvocationArgument(MethodInvocation invocation, int index, String source)
      throws Exception {
    Expression argument = DomGenerics.arguments(invocation).get(index);
    return replaceExpression(argument, source);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Replaces {@link Type} in {@link VariableDeclarationStatement} with new type.<br>
   * We use this method in morphing.
   *
   * @param declaration
   *          the fragment from {@link VariableDeclarationStatement}. {@link FieldDeclaration}
   *          should have only one fragment.
   * @param newTypeName
   *          the fully qualified name of type to use.
   */
  public void replaceVariableType(VariableDeclaration declaration, String newTypeName)
      throws Exception {
    Type type;
    VariableDeclarationStatement declarationStatement = null;
    FieldDeclaration fieldDeclaration = null;
    if (declaration.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
      declarationStatement = (VariableDeclarationStatement) declaration.getParent();
      type = declarationStatement.getType();
    } else if (declaration.getLocationInParent() == FieldDeclaration.FRAGMENTS_PROPERTY) {
      fieldDeclaration = (FieldDeclaration) declaration.getParent();
      type = fieldDeclaration.getType();
    } else {
      throw new IllegalArgumentException("Unknown argument: " + declaration.getClass());
    }
    // do replace
    {
      Type newType = getParser().parseQualifiedType(type.getStartPosition(), newTypeName);
      // replace Type source
      replaceSubstring(type, newTypeName);
      // replace Type node
      if (declarationStatement != null) {
        declarationStatement.setType(newType);
      }
      if (fieldDeclaration != null) {
        fieldDeclaration.setType(newType);
      }
      resolveImports(newType);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Global values
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, Object> m_globalMap = Maps.newTreeMap();

  /**
   * @return the current global value for given key.
   */
  public Object getGlobalValue(String key) {
    return m_globalMap.get(key);
  }

  /**
   * Sets new global value for given key.
   */
  public void putGlobalValue(String key, Object value) {
    m_globalMap.put(key, value);
  }

  /**
   * Removes global value for given key.
   */
  public void removeGlobalValue(String key) {
    m_globalMap.remove(key);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Unique names generation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the unique variable name (for local variable or field).
   *
   * @param position
   *          the position where new variable will be used, <code>-1</code> if all variables of
   *          {@link CompilationUnit} should be considered
   * @param baseName
   *          the base name for generating
   * @param excludedVariable
   *          the {@link VariableDeclaration} that should be excluded from checking of uniqueness.
   *          We need this for example when we convert local variable to field, in this case using
   *          same unique name of variable as field name is OK. May be <code>null</code>.
   */
  public String getUniqueVariableName(int position,
      String baseName,
      VariableDeclaration excludedVariable) {
    // prepare declarations...
    List<VariableDeclaration> declarations = Lists.newArrayList();
    if (position != -1) {
      // ...visible + shadows
      declarations.addAll(AstNodeUtils.getVariableDeclarationsVisibleAt(m_astUnit, position));
      declarations.addAll(AstNodeUtils.getVariableDeclarationsAfter(m_astUnit, position));
    } else {
      // ...all
      declarations.addAll(AstNodeUtils.getVariableDeclarationsAll(m_astUnit));
    }
    // exclude "excluded"
    declarations.remove(excludedVariable);
    // do generation
    return getUniqueVariableName(declarations, baseName);
  }

  /**
   * Generates unique variable name that does not conflict with other variables.
   *
   * @param declarations
   *          the {@link VariableDeclaration}'s that can conflict with new variable.
   * @param baseName
   *          the base name for generating
   *
   * @return the unique variable name (for local variable or field).
   */
  public static String getUniqueVariableName(List<VariableDeclaration> declarations, String baseName) {
    // prepare set of conflicting variables identifiers
    final Set<String> existingIdentifiers = Sets.newTreeSet();
    for (VariableDeclaration declaration : declarations) {
      existingIdentifiers.add(declaration.getName().getIdentifier());
    }
    // generate unique name
    return CodeUtils.generateUniqueName(baseName, new Predicate<String>() {
      public boolean apply(String name) {
        return !existingIdentifiers.contains(name);
      }
    });
  }

  /**
   * @return the unique method name.
   */
  public String getUniqueMethodName(String baseName) {
    // prepare set of methods names
    final Set<String> existingMethods = Sets.newTreeSet();
    m_astUnit.accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        existingMethods.add(node.getName().getIdentifier());
      }

      @Override
      public void endVisit(TypeDeclaration node) {
        addMethodNames(existingMethods, AstNodeUtils.getTypeBinding(node));
      }
    });
    // generate unique name
    return CodeUtils.generateUniqueName(baseName, new Predicate<String>() {
      public boolean apply(String name) {
        return !existingMethods.contains(name);
      }
    });
  }

  /**
   * @return the unique inner type name.
   */
  public String getUniqueTypeName(String baseName) {
    // prepare set of methods names
    final Set<String> existingTypes = Sets.newTreeSet();
    m_astUnit.accept(new ASTVisitor() {
      @Override
      public void endVisit(TypeDeclaration node) {
        existingTypes.add(node.getName().getIdentifier());
      }
    });
    // generate unique name
    return CodeUtils.generateUniqueName(baseName, new Predicate<String>() {
      public boolean apply(String name) {
        return !existingTypes.contains(name);
      }
    });
  }

  /**
   * Adds names for methods declared in given {@link ITypeBinding} and its super-classes.
   */
  private static void addMethodNames(Set<String> existingMethods, ITypeBinding typeBinding) {
    if (typeBinding != null) {
      for (IMethodBinding method : typeBinding.getDeclaredMethods()) {
        existingMethods.add(method.getName());
      }
      addMethodNames(existingMethods, typeBinding.getSuperclass());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SimpleName
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets new identifier of given {@link SimpleName}.
   */
  public void setIdentifier(SimpleName simpleName, String newIdentifier) throws Exception {
    replaceSubstring(simpleName, newIdentifier);
    simpleName.setIdentifier(newIdentifier);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  private final BindingContext m_bindingContext = new BindingContext();
  private final AstParser m_parser = new AstParser(this);

  /**
   * @return the {@link BindingContext} for this {@link AstEditor}.
   */
  public BindingContext getBindingContext() {
    return m_bindingContext;
  }

  /**
   * @return the {@link AstParser} for this {@link AstEditor}.
   */
  public AstParser getParser() {
    return m_parser;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final AstCodeGeneration m_generation = new AstCodeGeneration(this);

  /**
   * @return the {@link AstCodeGeneration} for this {@link AstEditor}.
   */
  public AstCodeGeneration getGeneration() {
    return m_generation;
  }

  /**
   * @return the source with replaced "{wbp_XXX}" templates.
   */
  private String replaceSourceTemplates(int position, String src) {
    // replace {wbp_class} with class reference
    if (src.indexOf("{wbp_class}") != -1) {
      String replacement;
      {
        ASTNode coveringNode = getEnclosingNode(position);
        // prepare enclosing method binding
        IMethodBinding methodBinding;
        {
          MethodDeclaration methodDeclaration = AstNodeUtils.getEnclosingMethod(coveringNode);
          Assert.isNotNull(methodDeclaration);
          methodBinding = AstNodeUtils.getMethodBinding(methodDeclaration);
        }
        // prepare replacement
        if (AstNodeUtils.isStatic(methodBinding)) {
          TypeDeclaration typeDeclaration = AstNodeUtils.getEnclosingType(coveringNode);
          ITypeBinding typeBinding = typeDeclaration.resolveBinding();
          replacement = AstNodeUtils.getFullyQualifiedName(typeBinding, false) + ".class";
        } else {
          replacement = "getClass()";
        }
      }
      // do replace
      src = StringUtils.replace(src, "{wbp_class}", replacement);
    }
    // replace {wbp_classTop} with top-level class reference
    if (src.indexOf("{wbp_classTop}") != -1) {
      String replacement;
      {
        ASTNode coveringNode = getEnclosingNode(position);
        TypeDeclaration typeDeclaration = AstNodeUtils.getEnclosingTypeTop(coveringNode);
        ITypeBinding typeBinding = typeDeclaration.resolveBinding();
        replacement = AstNodeUtils.getFullyQualifiedName(typeBinding, false) + ".class";
      }
      // do replace
      src = StringUtils.replace(src, "{wbp_classTop}", replacement);
    }
    //
    return src;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ParenthesizedExpression
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Inlines all {@link ParenthesizedExpression} that enclose given {@link Expression}.
   */
  public void inlineParenthesizedExpression(Expression expression) throws Exception {
    while (expression.getParent() instanceof ParenthesizedExpression) {
      ParenthesizedExpression parent = (ParenthesizedExpression) expression.getParent();
      parent.setExpression(expression.getAST().newSimpleName("__wbp_tmp"));
      replaceNode(parent, expression);
      replaceSubstring(parent.getStartPosition(), parent.getLength(), getSource(expression));
      AstNodeUtils.setSourceBegin(expression, parent.getStartPosition());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TryStatement
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link Statement} is enclosed into {@link TryStatement} with
   *         {@link CatchClause} which is same or superclass for given.
   */
  public boolean hasEnclosingTryStatement(Statement statement, String requiredException)
      throws Exception {
    ITypeHierarchy requiredHierarchy;
    {
      IType requiredExceptionType = getJavaProject().findType(requiredException);
      Assert.isNotNull2(requiredExceptionType, "No such exception type: {0}", requiredException);
      requiredHierarchy = requiredExceptionType.newSupertypeHierarchy(null);
    }
    // visit parents
    while (true) {
      ASTNode parent = statement.getParent();
      // TryStatement
      if (parent instanceof TryStatement) {
        TryStatement tryStatement = (TryStatement) parent;
        List<CatchClause> catchClauses = DomGenerics.catchClauses(tryStatement);
        for (CatchClause catchClause : catchClauses) {
          SingleVariableDeclaration exception = catchClause.getException();
          String exceptionName = AstNodeUtils.getFullyQualifiedName(exception, false);
          IType exceptionType = getJavaProject().findType(exceptionName);
          if (requiredHierarchy.contains(exceptionType)) {
            return true;
          }
        }
      }
      // enclosing Statement or stop
      if (parent instanceof Statement) {
        statement = (Statement) parent;
      } else {
        return false;
      }
    }
  }

  /**
   * Encloses given {@link Statement} in {@link TryStatement}.
   *
   * @return the enclosing {@link TryStatement}.
   */
  public TryStatement encloseInTryStatement(Statement statement, String catchExceptionType)
      throws Exception {
    TryStatement tryStatement;
    {
      String line_1 = "try {";
      String line_2 = "} catch (" + catchExceptionType + " e) {";
      String line_3 = "}";
      tryStatement =
          (TryStatement) addStatement(
              ImmutableList.of(line_1, line_2, line_3),
              new StatementTarget(statement, true));
    }
    moveStatement(statement, new StatementTarget(tryStatement.getBody(), true));
    return tryStatement;
  }

  /**
   * Removes empty {@link TryStatement}s.
   */
  public void removeEmptyTryStatements() {
    m_astUnit.accept(new AstVisitorEx() {
      @Override
      public void endVisitEx(TryStatement node) throws Exception {
        if (node.getBody().statements().isEmpty()) {
          removeStatement(node);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Statement operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Encloses given {@link Statement} in {@link Block}.
   *
   * @return the enclosing {@link Block}.
   */
  public Block encloseInBlock(Statement statement) throws Exception {
    // add new Block
    Block block =
        (Block) addStatement(ImmutableList.of("{", "}"), new StatementTarget(statement, true));
    // move Statement into Block
    moveStatement(statement, new StatementTarget(block, true));
    // OK, return Block
    return block;
  }

  /**
   * Inlines given {@link Block} into its parent {@link Block}.
   */
  public void inlineBlock(Block block) throws Exception {
    StatementTarget target = new StatementTarget((Statement) block, true);
    // move Statement's
    List<Statement> statements = Lists.newArrayList(DomGenerics.statements(block));
    for (Statement statement : statements) {
      moveStatement(statement, target);
    }
    // remove Block
    removeStatement(block);
  }

  /**
   * Ensures that given {@link Statement} is child of {@link Block}, so we can add new
   * {@link Statement}s relative given one.
   */
  private Block ensureParentBlock(Statement statement) throws Exception {
    ASTNode parent = statement.getParent();
    // already in Block
    if (parent instanceof Block) {
      return (Block) parent;
    }
    // prepare code generation constants
    AstCodeGeneration generation = getGeneration();
    String singleIndent = generation.getIndentation(1);
    String eol = generation.getEndOfLine();
    String indent = getWhitespaceToLeft(parent.getStartPosition(), false);
    // initial state
    boolean statementIsLastInParent =
        AstNodeUtils.getSourceEnd(parent) == AstNodeUtils.getSourceEnd(statement);
    // prepare new Block
    int positionForBlock;
    int positionForStatement;
    Block newBlock;
    {
      int statementBegin = statement.getStartPosition();
      positionForBlock = indexOfAnyButBackward(" \t\r\n", statementBegin) + 1;
      String source = " {" + eol;
      source += indent + singleIndent;
      positionForStatement = positionForBlock + source.length();
      source += eol;
      source += indent + "}";
      replaceSubstring(positionForBlock, statementBegin - positionForBlock, source);
      // parse
      newBlock = (Block) getParser().parseStatement(positionForBlock, source);
    }
    // move Statement into Block: source
    {
      int statementBegin = statement.getStartPosition();
      int statementEnd = getStatementEndIndex(statement);
      int statementLength = statementEnd - statementBegin;
      moveSource(positionForStatement, statementBegin, statementLength);
      AstNodeUtils.setSourceLength(newBlock, newBlock.getLength() + statementLength);
    }
    // move Statement into Block: node
    replaceNode(statement, newBlock);
    DomGenerics.statements(newBlock).add(statement);
    // cut parent to Block
    if (statementIsLastInParent) {
      AstNodeUtils.setSourceEnd(parent, newBlock);
    }
    // done
    return newBlock;
  }

  /**
   * Adds new {@link Statement} with given source in given {@link StatementTarget}.
   *
   * @param source
   *          the source for new {@link Statement} (with trailing ';'). It can contains
   *          <code>\n</code>, so can be multi-line and will be indented.
   * @param target
   *          describes location for new {@link Statement}.
   */
  public Statement addStatement(String source, StatementTarget target) throws Exception {
    String[] lines = StringUtils.split(source, '\n');
    return addStatement(Arrays.asList(lines), target);
  }

  /**
   * Adds new {@link Statement} with given source in given {@link StatementTarget}.
   *
   * @param lines
   *          the lines of source (possible with empty lines) with single statement.
   * @param target
   *          describes location for new {@link Statement}.
   */
  public Statement addStatement(List<String> lines, StatementTarget target) throws Exception {
    Assert.isNotNull(lines);
    // statement or method declaration required
    Assert.isTrue(target.getBlock() != null || target.getStatement() != null);
    // prepare code generation constants
    AstCodeGeneration generation = getGeneration();
    String singleIndent = generation.getIndentation(1);
    String eol = generation.getEndOfLine();
    //
    if (target.getStatement() != null) {
      Statement targetStatement = target.getStatement();
      Block targetBlock = ensureParentBlock(targetStatement);
      // prepare information about target statement
      String indent = getWhitespaceToLeft(targetStatement.getStartPosition(), false);
      int index = targetBlock.statements().indexOf(targetStatement);
      // prepare source
      String source = getIndentedSource(lines, indent, singleIndent, eol);
      source = replaceSourceTemplates(targetStatement.getStartPosition(), source);
      // add statement
      Statement newStatement;
      if (target.isBefore()) {
        // add before any empty lines and EOLC lines
        int position = skipWhitespaceAndPureEOLCToLeft(targetStatement.getStartPosition());
        // add source
        replaceSubstring(position, 0, source + eol);
        // parse statement
        newStatement = getParser().parseStatement(position, source);
        DomGenerics.statements(targetBlock).add(index, newStatement);
      } else {
        // move at the end of target
        int position = getStatementEndIndex(targetStatement);
        source = eol + source;
        // add source
        replaceSubstring(position, 0, source);
        // parse statement
        newStatement = getParser().parseStatement(position, source);
        DomGenerics.statements(targetBlock).add(index + 1, newStatement);
      }
      //
      resolveImports(newStatement);
      return newStatement;
    } else {
      Block targetBlock = target.getBlock();
      // prepare indentation
      String indent;
      {
        if (targetBlock.getParent() instanceof MethodDeclaration) {
          indent = getWhitespaceToLeft(targetBlock.getParent().getStartPosition(), false);
        } else {
          indent = getWhitespaceToLeft(targetBlock.getStartPosition(), false);
        }
        indent += singleIndent;
      }
      // prepare source
      String source = getIndentedSource(lines, indent, singleIndent, eol);
      source = replaceSourceTemplates(targetBlock.getStartPosition(), source);
      // add statement
      Statement newStatement;
      if (target.isBefore()) {
        // prepare position as position after '{' of block
        int position;
        {
          position = targetBlock.getStartPosition();
          Assert.isTrue(position != -1);
          position++;
        }
        // add source
        {
          String prefix = eol;
          replaceSubstring(position, 0, prefix + source);
          position += prefix.length();
        }
        // parse statement
        newStatement = getParser().parseStatement(position, source);
        DomGenerics.statements(targetBlock).add(0, newStatement);
      } else {
        // prepare position as position of '}' of block
        int position = AstNodeUtils.getSourceEnd(targetBlock) - 1;
        String endMethodIndent = getWhitespaceToLeft(position, false);
        position -= endMethodIndent.length();
        // add source
        replaceSubstring(position, 0, source + eol);
        // parse statement
        newStatement = getParser().parseStatement(position, source);
        DomGenerics.statements(targetBlock).add(newStatement);
      }
      //
      resolveImports(newStatement);
      return newStatement;
    }
  }

  /**
   * Removes given {@link Statement}. It removes also any whitespace and comments between removing
   * {@link Statement}.
   */
  public void removeStatement(Statement statement) throws Exception {
    if (AstNodeUtils.isDanglingNode(statement)) {
      return;
    }
    Block block = (Block) statement.getParent();
    List<Statement> statements = DomGenerics.statements(block);
    //
    if (statements.size() == 1 && block.getParent() instanceof Block) {
      removeStatement(block);
    } else {
      // prepare start of source to remove
      int startIndex;
      {
        int index = statements.indexOf(statement);
        if (index != 0) {
          Statement prevStatement = statements.get(index - 1);
          startIndex = AstNodeUtils.getSourceEnd(prevStatement);
        } else {
          startIndex = block.getStartPosition() + "{".length();
        }
      }
      // prepare end of source to remove
      int endIndex = getStatementEndIndex(statement);
      // remove statement and corresponding source
      statements.remove(statement);
      replaceSubstring(startIndex, endIndex - startIndex, "");
    }
  }

  /**
   * Removes the {@link Statement} that encloses given {@link ASTNode}.
   */
  public void removeEnclosingStatement(ASTNode node) throws Exception {
    Statement statement = AstNodeUtils.getEnclosingStatement(node);
    removeStatement(statement);
  }

  /**
   * Re-indents source at place.
   */
  private void reindentSource(int sourceStart, int sourceLength, String indent, String eol)
      throws Exception {
    // prepare lines
    String[] lines;
    {
      String source = getSource(sourceStart, sourceLength);
      lines = StringUtils.splitByWholeSeparatorPreserveAllTokens(source, eol);
    }
    // change indentation for lines
    int position = sourceStart;
    String oldIndent = null;
    for (String line : lines) {
      // prepare line indentation
      String lineIndent = "";
      {
        int endOfIndent = StringUtils.indexOfAnyBut(line, "\t ");
        if (endOfIndent != -1) {
          lineIndent = line.substring(0, endOfIndent);
        } else {
          lineIndent = line;
        }
      }
      // use indentation of first line as base
      if (oldIndent == null) {
        oldIndent = lineIndent;
      }
      // replace indentation
      if (lineIndent.startsWith(oldIndent)) {
        replaceSubstring(position, oldIndent.length(), indent);
        position += indent.length() - oldIndent.length();
      }
      // move position to next line
      position += line.length() + eol.length();
    }
  }

  /**
   * Move statement to given {@link StatementTarget}.
   *
   * @param target
   *          the new position for statement
   * @param statement
   *          the statement to move
   * @param includePrefixComment
   *          flag to mark that comment before statement also should be moved
   */
  public void moveStatement(Statement statement, StatementTarget target) throws Exception {
    // statement or method declaration required
    Assert.isTrue(target.getBlock() != null || target.getStatement() != null);
    // check for no-op
    if (target.getStatement() != null) {
      Statement targetStatement = target.getStatement();
      // adding relative same statement
      if (targetStatement == statement) {
        return;
      }
      // adding before current next statement
      Block targetBlock = (Block) targetStatement.getParent();
      List<Statement> targetStatements = DomGenerics.statements(targetBlock);
      if (statement.getParent() == targetStatement.getParent()) {
        if (target.isBefore()) {
          // statement before target
          if (targetStatements.indexOf(statement) == targetStatements.indexOf(targetStatement) - 1) {
            return;
          }
        } else {
          // statement after target
          if (targetStatements.indexOf(statement) == targetStatements.indexOf(targetStatement) + 1) {
            return;
          }
        }
      }
    } else {
      Block targetBlock = target.getBlock();
      List<Statement> targetStatements = DomGenerics.statements(targetBlock);
      if (statement.getParent() == targetBlock) {
        if (target.isBefore()) {
          if (targetStatements.indexOf(statement) == 0) {
            return;
          }
        } else {
          if (targetStatements.indexOf(statement) == targetStatements.size() - 1) {
            return;
          }
        }
      }
    }
    // prepare code generation constants
    AstCodeGeneration generation = getGeneration();
    String singleIndent = generation.getIndentation(1);
    String eol = generation.getEndOfLine();
    // prepare source location
    Block sourceBlock = (Block) statement.getParent();
    int sourceBegin = skipWhitespaceAndPureEOLCToLeft(statement.getStartPosition());
    int sourceLength = getStatementEndIndex(statement) - sourceBegin;
    // remove leading EOL
    {
      int leftEOLEnd = sourceBegin;
      int leftEOLBegin = skipSingleEOLToLeft(leftEOLEnd);
      int leftEOLLength = leftEOLEnd - leftEOLBegin;
      //
      replaceSubstring(leftEOLBegin, leftEOLLength, "");
      sourceBegin -= leftEOLLength;
    }
    //
    if (target.getStatement() != null) {
      Statement targetStatement = target.getStatement();
      String indent = getWhitespaceToLeft(targetStatement.getStartPosition(), false);
      //
      int position;
      if (target.isBefore()) {
        // move before any empty lines and EOLC lines
        position = skipWhitespaceAndPureEOLCToLeft(targetStatement.getStartPosition());
        // move source
        position = moveSource(position, sourceBegin, sourceLength);
        // add EOL after statement
        replaceSubstring(position + sourceLength, 0, eol);
      } else {
        // move at the end of target
        position = getStatementEndIndex(targetStatement);
        // move source
        position = moveSource(position, sourceBegin, sourceLength);
        // add EOL before statement
        replaceSubstring(position, 0, eol);
        position += eol.length();
      }
      // move node
      {
        Block targetBlock = (Block) targetStatement.getParent();
        int index = targetBlock.statements().indexOf(targetStatement);
        if (!target.isBefore()) {
          index++;
        }
        if (sourceBlock == targetBlock && sourceBlock.statements().indexOf(statement) < index) {
          index--;
        }
        sourceBlock.statements().remove(statement);
        DomGenerics.statements(targetBlock).add(index, statement);
      }
      // re-indent
      reindentSource(position, sourceLength, indent, eol);
    } else {
      Block targetBlock = target.getBlock();
      // prepare indentation
      String indent;
      {
        ASTNode indentNode = targetBlock;
        if (targetBlock.getLocationInParent() == MethodDeclaration.BODY_PROPERTY
            || targetBlock.getLocationInParent() == TryStatement.BODY_PROPERTY) {
          indentNode = targetBlock.getParent();
        }
        indent = getWhitespaceToLeft(indentNode.getStartPosition(), false);
        indent += singleIndent;
      }
      //
      int position;
      if (target.isBefore()) {
        // prepare position as position after '{' of block
        {
          position = targetBlock.getStartPosition();
          Assert.isTrue(position != -1);
          position++;
        }
        // move source
        position = moveSource(position, sourceBegin, sourceLength);
        // add EOL before statement
        replaceSubstring(position, 0, eol);
        position += eol.length();
        // move node
        sourceBlock.statements().remove(statement);
        DomGenerics.statements(targetBlock).add(0, statement);
      } else {
        // prepare position as position of '}' of block
        position = AstNodeUtils.getSourceEnd(targetBlock) - 1;
        String endMethodIndent = getWhitespaceToLeft(position, false);
        position -= endMethodIndent.length();
        // move source
        position = moveSource(position, sourceBegin, sourceLength);
        // add EOL after statement
        replaceSubstring(position + sourceLength, 0, eol);
        // move node
        sourceBlock.statements().remove(statement);
        DomGenerics.statements(targetBlock).add(statement);
      }
      // re-indent
      reindentSource(position, sourceLength, indent, eol);
    }
  }

  /**
   * Moves piece of source with given start/length to target position.
   *
   * @return the new value of target
   */
  private int moveSource(int target, int start, int length) throws Exception {
    String source = getSource(start, length);
    // prepare locations
    final int b_pos = start;
    final int b_len = length;
    final int b_end = b_pos + b_len;
    final int t_pos = target;
    //
    if (b_pos > t_pos) {
      m_document.replace(b_pos, b_len, "");
      m_document.replace(t_pos, 0, source);
      // modify source ranges for AST nodes
      m_astUnit.accept(new ASTVisitor(true) {
        @Override
        public void postVisit(ASTNode node) {
          int n_pos = node.getStartPosition();
          int n_len = node.getLength();
          int n_end = n_pos + n_len;
          while (true) {
            // node starts after source end
            if (n_pos >= b_end) {
              return;
            }
            // node ends before target
            if (n_end <= t_pos) {
              return;
            }
            // node contains source and target
            if (n_pos < t_pos && n_end > b_end) {
              return;
            }
            // node is inside of block
            if (n_pos >= b_pos && n_end <= b_end) {
              node.setSourceRange(t_pos + n_pos - b_pos, n_len);
              return;
            }
            // node is between target and source
            if (n_pos >= t_pos && n_end <= b_pos) {
              node.setSourceRange(n_pos + b_len, n_len);
              return;
            }
            // node contains target
            if (n_pos < t_pos && n_end > t_pos) {
              node.setSourceRange(n_pos, n_len + b_len);
              return;
            }
            // node contains source
            /*if (n_pos < b_pos && n_end > b_end)*/{
              node.setSourceRange(n_pos + b_len, n_len - b_len);
              return;
            }
          }
        }
      });
    } else {
      m_document.replace(t_pos, 0, source);
      m_document.replace(b_pos, b_len, "");
      // modify source ranges for AST nodes
      m_astUnit.accept(new ASTVisitor(true) {
        @Override
        public void postVisit(ASTNode node) {
          int n_pos = node.getStartPosition();
          int n_len = node.getLength();
          int n_end = n_pos + n_len;
          // node ends before source
          if (n_end <= b_pos) {
            return;
          }
          // node starts after target
          if (n_pos >= t_pos) {
            return;
          }
          // node contains source and target
          if (n_pos < b_pos && n_end > t_pos) {
            return;
          }
          // node was inside of block
          if (n_pos >= b_pos && n_end <= b_end) {
            node.setSourceRange(t_pos - b_len + n_pos - b_pos, n_len);
            return;
          }
          // node is between source and target
          if (n_pos >= b_end && n_end <= t_pos) {
            node.setSourceRange(n_pos - b_len, n_len);
            return;
          }
          // node contains target
          if (n_pos < t_pos && n_end > t_pos) {
            node.setSourceRange(n_pos - b_len, n_len + b_len);
            return;
          }
          // node contains source
          /*if (n_pos < b_pos && n_end > b_end)*/{
            node.setSourceRange(n_pos, n_len - b_len);
            return;
          }
        }
      });
    }
    // update target
    if (start < target) {
      target -= length;
    }
    return target;
  }

  /**
   * @return the index of character directly after end of given {@link Statement}. This can be
   *         beginning of the next statement or EOL. This method skips any whitespace characters and
   *         end-of-line comments.
   */
  public int getStatementEndIndex(Statement statement) {
    int index = statement.getStartPosition() + statement.getLength();
    char c;
    // skip spaces
    while (true) {
      c = getChar(index++);
      if (Character.isWhitespace(c)) {
        if (c == '\r' || c == '\n') {
          break;
        }
      } else {
        break;
      }
    }
    // skip end-of-line comment
    if (c == '/' && getChar(index) == '/') {
      while (true) {
        c = getChar(index++);
        if (c == '\r' || c == '\n') {
          break;
        }
      }
    }
    // return result
    return index - 1;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Fields operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link FieldDeclaration} with given source.
   */
  public FieldDeclaration addFieldDeclaration(String source, BodyDeclarationTarget target)
      throws Exception {
    return addFieldDeclaration(ImmutableList.of(source), target);
  }

  /**
   * Adds new {@link FieldDeclaration} with given source lines.
   */
  public FieldDeclaration addFieldDeclaration(List<String> lines, BodyDeclarationTarget target)
      throws Exception {
    return (FieldDeclaration) addBodyDeclaration(lines, target);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Methods operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link MethodDeclaration} with given source in location of given
   * {@link StatementTarget}.
   *
   * @param header
   *          the header of method without '{'
   * @param bodyLines
   *          the lines of method body
   * @param target
   *          describes location for new {@link BodyDeclaration}
   */
  public MethodDeclaration addMethodDeclaration(String header,
      List<String> bodyLines,
      BodyDeclarationTarget target) throws Exception {
    return addMethodDeclaration(ImmutableList.<String>of(), header, bodyLines, target);
  }

  /**
   * Adds new {@link MethodDeclaration} with given source in location of given
   * {@link StatementTarget}.
   *
   * @param bodyLines
   *          the lines of method JavaDoc and/or annotations
   * @param header
   *          the header of method without '{'
   * @param bodyLines
   *          the lines of method body
   * @param target
   *          describes location for new {@link BodyDeclaration}
   */
  public MethodDeclaration addMethodDeclaration(List<String> annotations,
      String header,
      List<String> bodyLines,
      BodyDeclarationTarget target) throws Exception {
    // prepare full method lines
    List<String> lines = Lists.newArrayList();
    {
      lines.addAll(annotations);
      if (bodyLines != null) {
        lines.add(header + " {");
        // body
        String singleIndent = getGeneration().getIndentation(1);
        for (String bodyLine : bodyLines) {
          lines.add(singleIndent + bodyLine);
        }
        // close method
        lines.add("}");
      } else {
        lines.add(header);
      }
    }
    // add method
    return (MethodDeclaration) addBodyDeclaration(lines, target);
  }

  /**
   * Adds new {@link MethodDeclaration} without body in location of given {@link StatementTarget}.
   *
   * @param header
   *          the header of method.
   * @param target
   *          describes location for new {@link BodyDeclaration}
   */
  public MethodDeclaration addInterfaceMethodDeclaration(String header, BodyDeclarationTarget target)
      throws Exception {
    List<String> lines = ImmutableList.of(header + ";");
    return (MethodDeclaration) addBodyDeclaration(lines, target);
  }

  /**
   * @return the source for parameters of given {@link MethodDeclaration}.<br>
   *
   *         <code>public void split(String s, int count)</code> -> <code>String s, int count</code>
   */
  public String getParametersSource(MethodDeclaration method) {
    StringBuffer sb = new StringBuffer();
    for (SingleVariableDeclaration parameter : DomGenerics.parameters(method)) {
      if (sb.length() != 0) {
        sb.append(", ");
      }
      sb.append(getSource(parameter));
    }
    return sb.toString();
  }

  /**
   * @return the array of parameter names of given {@link MethodDeclaration}.
   *
   *         <code>public void split(String s, int count)</code> -> <code>{"s", "count"}</code>
   */
  public String[] getParameterNames(MethodDeclaration method) {
    List<SingleVariableDeclaration> parameters = DomGenerics.parameters(method);
    String names[] = new String[parameters.size()];
    //
    for (int i = 0; i < names.length; i++) {
      SingleVariableDeclaration parameter = parameters.get(i);
      names[i] = parameter.getName().getIdentifier();
    }
    //
    return names;
  }

  /**
   * Replaces the name of {@link MethodDeclaration}.
   */
  public void replaceMethodName(MethodDeclaration method, String newName) throws Exception {
    setIdentifier(method.getName(), newName);
    replaceMethodBinding(method);
  }

  /**
   * Replaces {@link IMethodBinding} for given {@link MethodDeclaration} according to actual source.
   */
  private void replaceMethodBinding(MethodDeclaration method) throws Exception {
    MethodDeclaration parsedMethod = parseExistingMethod(method);
    replaceMethodBinding(method, parsedMethod);
  }

  /**
   * When we parse existing {@link MethodDeclaration} we should ensure that it is not added into
   * parsing context, or this will cause duplicate method compilation problem
   */
  private MethodDeclaration parseExistingMethod(MethodDeclaration method) throws Exception {
    method.setProperty(AstParser.KEY_IGNORE_THIS_METHOD, Boolean.TRUE);
    try {
      return (MethodDeclaration) m_parser.parseBodyDeclaration(method.getStartPosition(), "");
    } finally {
      method.setProperty(AstParser.KEY_IGNORE_THIS_METHOD, null);
    }
  }

  /**
   * Replaces the return {@link Type} in {@link MethodDeclaration} with new type.
   *
   * @param method
   *          the {@link MethodDeclaration} to replace return type.
   * @param newTypeName
   *          the fully qualified name of type to use.
   */
  public void replaceMethodType(MethodDeclaration method, String newTypeName) throws Exception {
    // replace source/AST
    {
      Type oldType = method.getReturnType2();
      Type newType = getParser().parseQualifiedType(oldType.getStartPosition(), newTypeName);
      // replace Type source
      replaceSubstring(oldType, newTypeName);
      // replace Type node
      {
        method.setReturnType2(newType);
        resolveImports(newType);
      }
    }
    // replace binding
    replaceMethodBinding(method);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the source of stub copy of given {@link MethodDeclaration}.
   */
  public String getMethodStubSource(MethodDeclaration methodDeclaration) throws Exception {
    IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(methodDeclaration);
    // header
    StringBuilder sb = new StringBuilder();
    sb.append("\t");
    sb.append(getMethodHeaderSource(methodDeclaration));
    sb.append(" {\n");
    // return stub
    {
      ITypeBinding returnType = methodBinding.getReturnType();
      String returnTypeName = AstNodeUtils.getFullyQualifiedName(returnType, false);
      if (!returnTypeName.equals("void")) {
        sb.append("\t\treturn ");
        sb.append(AstParser.getDefaultValue(returnTypeName));
        sb.append(";\n");
      }
    }
    // close body
    sb.append("\t}");
    return sb.toString();
  }

  /**
   * @return the copy of {@link MethodDeclaration} header source, which uses same names for
   *         parameters and fully qualified names for all types.
   */
  public String getMethodHeaderSource(MethodDeclaration method) {
    StringBuffer sb = new StringBuffer();
    IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(method);
    // append modifiers
    {
      List<ASTNode> modifiersNodes = DomGenerics.modifiersNodes(method);
      List<Modifier> modifiers = GenericsUtils.select(modifiersNodes, Modifier.class);
      for (Modifier modifier : modifiers) {
        sb.append(modifier);
        sb.append(" ");
      }
    }
    // append return type
    {
      ITypeBinding returnType = methodBinding.getReturnType();
      String returnTypeName = AstNodeUtils.getFullyQualifiedName(returnType, false);
      sb.append(returnTypeName);
      sb.append(" ");
    }
    // append name
    sb.append(method.getName().getIdentifier());
    sb.append("(");
    // append parameters
    {
      ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
      List<SingleVariableDeclaration> parameters = DomGenerics.parameters(method);
      for (int i = 0; i < parameterTypes.length; i++) {
        ITypeBinding parameterType = parameterTypes[i];
        String parameterTypeName = AstNodeUtils.getFullyQualifiedName(parameterType, false);
        String parameterName = parameters.get(i).getName().getIdentifier();
        if (i != 0) {
          sb.append(", ");
        }
        sb.append(parameterTypeName);
        sb.append(" ");
        sb.append(parameterName);
      }
    }
    // done
    sb.append(")");
    return sb.toString();
  }

  /**
   * @return the source for list of type arguments.
   */
  public String getTypeArgumentsSource(ITypeBinding[] typeArguments) {
    if (typeArguments.length == 0) {
      return "";
    }
    // prepare type arguments source
    StringBuilder typeArgumentsBuilder = new StringBuilder();
    typeArgumentsBuilder.append("<");
    for (ITypeBinding typeArgument : typeArguments) {
      if (typeArgumentsBuilder.length() > 1) {
        typeArgumentsBuilder.append(", ");
      }
      typeArgumentsBuilder.append(AstNodeUtils.getFullyQualifiedName(typeArgument, false));
    }
    typeArgumentsBuilder.append(">");
    return typeArgumentsBuilder.toString();
  }

  /**
   * @return the source for type arguments of {@link ClassInstanceCreation}, including support for
   *         possible {@link AnonymousClassDeclaration}
   */
  public String getTypeArgumentsSource(ClassInstanceCreation creation) {
    // prepare ITypeBinding with type arguments
    ITypeBinding typeBinding;
    AnonymousClassDeclaration anonymousDeclaration = creation.getAnonymousClassDeclaration();
    if (anonymousDeclaration != null) {
      typeBinding = AstNodeUtils.getTypeBinding(anonymousDeclaration).getSuperclass();
    } else {
      typeBinding = AstNodeUtils.getTypeBinding(creation);
    }
    // prepare type arguments
    ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
    return getTypeArgumentsSource(typeArguments);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Classes operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link TypeDeclaration} with given source in location of given {@link StatementTarget}
   * .
   *
   * @param lines
   *          the lines of class
   * @param target
   *          describes location for new {@link BodyDeclaration}
   */
  public TypeDeclaration addTypeDeclaration(List<String> lines, BodyDeclarationTarget target)
      throws Exception {
    return (TypeDeclaration) addBodyDeclaration(lines, target);
  }

  /**
   * Ensures that given {@link TypeDeclaration} interface with given name.
   *
   * @return <code>false</code> if class already implements given interface and <code>true</code> in
   *         other case.
   */
  public boolean ensureInterfaceImplementation(TypeDeclaration type, String interfaceClassName)
      throws Exception {
    // find last implement interface and check, may be required interface is already implemented
    Type lastInterface = null;
    for (Type interfaceType : DomGenerics.superInterfaces(type)) {
      String implementedInterface = AstNodeUtils.getFullyQualifiedName(interfaceType, true);
      if (implementedInterface.equals(interfaceClassName)) {
        return false;
      }
      lastInterface = interfaceType;
    }
    // prepare position
    int pos;
    String codePrefix;
    if (lastInterface == null) {
      ASTNode typeName = type.getName();
      if (type.getSuperclassType() != null) {
        typeName = type.getSuperclassType();
      }
      pos = typeName.getStartPosition() + typeName.getLength();
      codePrefix = " implements ";
    } else {
      pos = lastInterface.getStartPosition() + lastInterface.getLength();
      codePrefix = ", ";
    }
    // prepare new interfaceType node
    SimpleType interfaceType;
    {
      TypeLiteral typeLiteral =
          (TypeLiteral) m_parser.parseExpression(type.getStartPosition(), interfaceClassName
              + ".class");
      AstNodeUtils.moveNode(typeLiteral, pos + codePrefix.length());
      interfaceType = (SimpleType) typeLiteral.getType();
      typeLiteral.setType(typeLiteral.getAST().newPrimitiveType(PrimitiveType.BOOLEAN));
    }
    // update source/AST
    replaceSubstring(pos, 0, codePrefix + interfaceClassName);
    DomGenerics.superInterfaces(type).add(interfaceType);
    // update ITypeBinding
    {
      ITypeBinding interfaceBinding = AstNodeUtils.getTypeBinding(interfaceType);
      DesignerTypeBinding typeBinding = getDesignerTypeBinding(type);
      typeBinding.addInterface(interfaceBinding);
    }
    // finalize
    resolveImports(interfaceType);
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BodyDeclaration operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that {@link MethodDeclaration} declares as thrown exception with given name, or its
   * superclass.
   */
  public void ensureThrownException(MethodDeclaration method, String requiredException)
      throws Exception {
    ITypeHierarchy requiredHierarchy;
    {
      IType requiredExceptionType = getJavaProject().findType(requiredException);
      Assert.isNotNull2(requiredExceptionType, "No such exception type: {0}", requiredException);
      requiredHierarchy = requiredExceptionType.newSupertypeHierarchy(null);
    }
    // find last exception and check, may be required exception is already declared
    Name lastName = null;
    for (Name declaredTypeName : DomGenerics.thrownExceptions(method)) {
      String declaredName = AstNodeUtils.getFullyQualifiedName(declaredTypeName, false);
      IType declaredType = getJavaProject().findType(declaredName);
      if (requiredHierarchy.contains(declaredType)) {
        return;
      }
      lastName = declaredTypeName;
    }
    // prepare position
    int pos;
    String codePrefix;
    if (lastName == null) {
      ASTNode name = method.getName();
      pos = AstNodeUtils.getSourceEnd(name);
      pos = indexOf(")", pos) + 1;
      codePrefix = " throws ";
    } else {
      pos = AstNodeUtils.getSourceEnd(lastName);
      codePrefix = ", ";
    }
    // prepare new exception nodes
    SimpleType newExceptionType;
    Name newExceptionTypeName;
    {
      TypeLiteral typeLiteral =
          (TypeLiteral) m_parser.parseExpression(pos, requiredException + ".class");
      AstNodeUtils.moveNode(typeLiteral, pos + codePrefix.length());
      newExceptionType = (SimpleType) typeLiteral.getType();
      newExceptionTypeName = newExceptionType.getName();
      newExceptionType.setName(typeLiteral.getAST().newSimpleName("filler"));
    }
    // update source/AST
    replaceSubstring(pos, 0, codePrefix + requiredException);
    DomGenerics.thrownExceptions(method).add(newExceptionTypeName);
    // update ITypeBinding
    {
      ITypeBinding newExceptionBinding = AstNodeUtils.getTypeBinding(newExceptionType);
      DesignerMethodBinding methodBinding = getDesignerMethodBinding(method);
      methodBinding.addExceptionType(newExceptionBinding);
    }
    // finalize
    resolveImports(method);
  }

  /**
   * Adds new {@link BodyDeclaration} with given source in location of given {@link StatementTarget}
   * .
   *
   * @param lines
   *          the lines of source for new {@link BodyDeclaration}.
   * @param target
   *          describes location for new {@link BodyDeclaration}.
   */
  private BodyDeclaration addBodyDeclaration(List<String> lines, BodyDeclarationTarget target)
      throws Exception {
    Assert.isNotNull(lines);
    Assert.isNotNull(target);
    // type or body declaration required
    TypeDeclaration targetType = target.getType();
    BodyDeclaration targetDecl = target.getDeclaration();
    Assert.isTrue(targetType != null || targetDecl != null);
    // prepare code generation constants
    AstCodeGeneration generation = getGeneration();
    String singleIndent = generation.getIndentation(1);
    String eol = generation.getEndOfLine();
    // relative to BodyDeclaration
    if (targetDecl != null) {
      // prepare information about target body declaration
      targetType = (TypeDeclaration) targetDecl.getParent();
      String indent = getWhitespaceToLeft(targetDecl.getStartPosition(), false);
      int index = targetType.bodyDeclarations().indexOf(targetDecl);
      // add new declaration
      BodyDeclaration newDeclaration;
      String source = getIndentedSource(lines, indent, singleIndent, eol);
      if (target.isBefore()) {
        // add before any empty lines and EOLC lines
        int position = skipWhitespaceAndPureEOLCToLeft(targetDecl.getStartPosition());
        source = source + eol;
        // parse declaration
        newDeclaration = getParser().parseBodyDeclaration(position, source);
        // add source
        replaceSubstring(position, 0, source);
        // add declaration
        DomGenerics.bodyDeclarations(targetType).add(index, newDeclaration);
      } else {
        // move at the end of target
        int position = AstNodeUtils.getSourceEnd(targetDecl);
        position = skipWhitespaceEOLCToRight(position);
        source = eol + source;
        // parse declaration
        newDeclaration = getParser().parseBodyDeclaration(position, source);
        // add source
        replaceSubstring(position, 0, source);
        // add declaration
        DomGenerics.bodyDeclarations(targetType).add(index + 1, newDeclaration);
      }
      //
      resolveImports(newDeclaration);
      return newDeclaration;
    }
    // relative to TypeDeclaration
    {
      removeDanglingJavadoc();
      // prepare indent
      String indent;
      {
        ASTNode indentNode;
        if (AnonymousTypeDeclaration.is(targetType)) {
          indentNode = AstNodeUtils.getEnclosingStatement(targetType);
        } else {
          indentNode = targetType;
        }
        indent = getWhitespaceToLeft(indentNode.getStartPosition(), false) + singleIndent;
      }
      String source = getIndentedSource(lines, indent, singleIndent, eol);
      // add new body declaration
      BodyDeclaration newDeclaration;
      if (target.isBefore()) {
        // prepare position as position after '{' of type declaration
        int position = indexOfAny("{", targetType.getName().getStartPosition()) + 1;
        source = eol + source;
        // parse declaration
        newDeclaration = getParser().parseBodyDeclaration(position, source);
        // add source
        replaceSubstring(position, 0, source);
        // add declaration
        DomGenerics.bodyDeclarations(targetType).add(0, newDeclaration);
      } else {
        // prepare position as position of '}' of type declaration
        int position = AstNodeUtils.getSourceEnd(targetType) - 1;
        position -= getWhitespaceToLeft(position, false).length();
        source = source + eol;
        // parse declaration
        newDeclaration = getParser().parseBodyDeclaration(position, source);
        // add source
        replaceSubstring(position, 0, source);
        // add declaration
        DomGenerics.bodyDeclarations(targetType).add(newDeclaration);
      }
      //
      resolveImports(newDeclaration);
      return newDeclaration;
    }
  }

  /**
   * Removes given {@link BodyDeclaration}.
   */
  public void removeBodyDeclaration(BodyDeclaration declaration) throws Exception {
    List<BodyDeclaration> declarations;
    if (declaration.getParent() instanceof TypeDeclaration) {
      TypeDeclaration typeDeclaration = (TypeDeclaration) declaration.getParent();
      declarations = DomGenerics.bodyDeclarations(typeDeclaration);
    } else {
      declarations =
          DomGenerics.bodyDeclarations((AnonymousClassDeclaration) declaration.getParent());
    }
    // prepare start of source to remove
    int startIndex;
    {
      int index = declarations.indexOf(declaration);
      if (index != 0) {
        BodyDeclaration prevDeclaration = declarations.get(index - 1);
        startIndex = prevDeclaration.getStartPosition() + prevDeclaration.getLength();
        startIndex = skipWhitespaceEOLCToRight(startIndex);
      } else {
        if (declaration.getParent() instanceof TypeDeclaration) {
          TypeDeclaration typeDeclaration = (TypeDeclaration) declaration.getParent();
          startIndex = indexOfAny("{", typeDeclaration.getName().getStartPosition()) + 1;
        } else {
          startIndex = indexOfAny("{", declaration.getParent().getStartPosition()) + 1;
        }
      }
    }
    // prepare end of source to remove
    int endIndex;
    {
      endIndex = declaration.getStartPosition() + declaration.getLength();
      endIndex = skipWhitespaceEOLCToRight(endIndex);
    }
    // remove declaration and corresponding source
    declarations.remove(declaration);
    replaceSubstring(startIndex, endIndex - startIndex, "");
  }

  /**
   * @return the position of first (to right) non whitespace character (excluding EOL) on same line.
   *         This method also skips EOL comments.
   */
  private int skipWhitespaceEOLCToRight(int position) throws BadLocationException, Exception {
    for (;; position++) {
      char c = m_document.getChar(position);
      // stop on EOL
      if (c == '\r' || c == '\n') {
        break;
      }
      // skip whitespace
      if (Character.isWhitespace(c)) {
        continue;
      }
      // skip EOL comments
      if (c == '/' && m_document.getChar(position + 1) == '/') {
        position = getLineEnd(position);
        break;
      }
      // stop
      break;
    }
    return position;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // VariableDeclaration operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes {@link VariableDeclaration}.
   */
  public void removeVariableDeclaration(VariableDeclaration declaration) throws Exception {
    ASTNode parent = declaration.getParent();
    if (parent instanceof FieldDeclaration) {
      // field
      FieldDeclaration fieldDeclaration = (FieldDeclaration) parent;
      List<VariableDeclarationFragment> fragments = DomGenerics.fragments(fieldDeclaration);
      if (fragments.size() == 1) {
        removeBodyDeclaration(fieldDeclaration);
      } else {
        removeVariableDeclaration(fieldDeclaration, fragments, fragments.indexOf(declaration));
      }
    } else if (parent instanceof VariableDeclarationStatement) {
      // local variable
      VariableDeclarationStatement variableDeclaration =
          (VariableDeclarationStatement) declaration.getParent();
      List<VariableDeclarationFragment> fragments = DomGenerics.fragments(variableDeclaration);
      if (fragments.size() == 1) {
        removeEnclosingStatement(declaration);
      } else {
        removeVariableDeclaration(variableDeclaration, fragments, fragments.indexOf(declaration));
      }
    } else {
      // can't remove other cases
      throw new IllegalArgumentException("Can not remove VariableDeclaration '"
          + declaration.toString()
          + "'");
    }
  }

  /**
   * Removes {@link VariableDeclarationFragment} from its parent.
   */
  private void removeVariableDeclaration(ASTNode parent,
      List<VariableDeclarationFragment> fragments,
      int index) throws Exception {
    VariableDeclarationFragment declaration = fragments.get(index);
    Assert.isTrue(
        fragments.size() > 1,
        "Last variable must be removed with parent body.",
        getSource(declaration),
        getSource(parent));
    // prepare source interval to remove
    int sourceBegin;
    int sourceEnd;
    {
      sourceBegin = AstNodeUtils.getSourceBegin(declaration);
      sourceEnd = AstNodeUtils.getSourceEnd(declaration);
      if (index == 0) {
        if (fragments.size() > 1) {
          sourceEnd = indexOfAnyBut(", \t\r\n", sourceEnd);
        }
      } else {
        sourceBegin = indexOfAnyButBackward(", \t\r\n", sourceBegin) + 1;
      }
    }
    // remove node
    fragments.remove(index);
    // remove source
    replaceSubstring(sourceBegin, sourceEnd - sourceBegin, "");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Javadoc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes dangling {@link Javadoc} sources at the end of {@link TypeDeclaration}s.
   */
  public void removeDanglingJavadoc() {
    m_astUnit.accept(new AstVisitorEx() {
      @Override
      public void endVisitEx(TypeDeclaration node) throws Exception {
        int typeEnd = AstNodeUtils.getSourceEnd(node) - 1;
        int trailingJavadocEnd = skipWhitespaceAndPureEOLCToLeft(typeEnd);
        trailingJavadocEnd = skipWhitespaceToLeft(trailingJavadocEnd, true);
        // prepare begin/end of last JavaDoc at end of type
        String source = getSource().substring(0, typeEnd);
        int javadocBegin = source.lastIndexOf("/**");
        if (javadocBegin != -1) {
          int javadocEnd = source.indexOf("*/", javadocBegin) + "*/".length();
          // if end of last JavaDoc is end of TypeDeclaration, i.e. no BodyDeclaration, remove it
          if (javadocEnd == trailingJavadocEnd) {
            javadocBegin = skipWhitespaceToLeft(javadocBegin, false);
            replaceSubstring(javadocBegin, typeEnd - javadocBegin, "");
          }
        }
      }
    });
  }

  /**
   * Sets new text for JavaDoc {@link TagElement}, i.e. {@link TextElement}.
   *
   * @param declaration
   *          the {@link BodyDeclaration} to update {@link Javadoc}.
   * @param tagName
   *          the name of tag, such as <code>"myTag"</code>, with leading <code>"@"</code>.
   * @param tagText
   *          the text to set for tag, with leading space, or <code>null</code> if tag should be
   *          removed.
   *
   * @return the {@link TagElement} that has single {@link TextElement} fragment with
   *         <code>tagText</code> as text; or <code>null</code> if tag was removed.
   */
  public TagElement setJavadocTagText(BodyDeclaration declaration, String tagName, String tagText)
      throws Exception {
    Assert.isNotNull(tagName);
    Assert.isLegal(tagName.length() != 0, "Empty name of tag.");
    Assert.isLegal(tagName.startsWith("@"), "Tag name should start with '@'.");
    Javadoc javadoc = declaration.getJavadoc();
    // update/add tag
    if (tagText != null) {
      // update existing JavaDoc
      if (javadoc != null) {
        // try to find existing tag
        for (TagElement tagElement : DomGenerics.tags(javadoc)) {
          if (tagName.equals(tagElement.getTagName())) {
            setJavadocTagText_replaceFragments(tagElement, tagText);
            return tagElement;
          }
        }
        // add new tag
        {
          List<TagElement> tags = DomGenerics.tags(javadoc);
          // prepare position for new TagElement
          int position;
          {
            int javadocPosition = javadoc.getStartPosition();
            String prefix = getWhitespaceToLeft(javadocPosition, false);
            String endOfLine = getGeneration().getEndOfLine();
            if (tags.isEmpty()) {
              position = javadocPosition + "/**".length();
            } else {
              position = AstNodeUtils.getSourceEnd(tags.get(tags.size() - 1));
            }
            String newCommentLine = endOfLine + prefix + " * ";
            replaceSubstring(position, 0, newCommentLine);
            position += newCommentLine.length();
          }
          // replace source
          String tagSource = tagName + tagText;
          replaceSubstring(position, 0, tagSource);
          // add TagElement
          TagElement tagElement = javadoc.getAST().newTagElement();
          tagElement.setSourceRange(position, tagSource.length());
          tagElement.setTagName(tagName);
          tags.add(tagElement);
          // add TextElement
          TextElement textElement = javadoc.getAST().newTextElement();
          textElement.setSourceRange(position + tagName.length(), tagText.length());
          textElement.setText(tagText);
          DomGenerics.fragments(tagElement).add(textElement);
          // new TagElement
          return tagElement;
        }
      } else {
        javadoc = setJavadoc(declaration, new String[]{tagName + tagText});
        return DomGenerics.tags(javadoc).get(0);
      }
    }
    // remove tag
    if (javadoc != null) {
      for (Iterator<TagElement> I = DomGenerics.tags(javadoc).iterator(); I.hasNext();) {
        TagElement tagElement = I.next();
        if (tagName.equals(tagElement.getTagName())) {
          int begin = AstNodeUtils.getSourceBegin(tagElement);
          int end = AstNodeUtils.getSourceEnd(tagElement);
          end = indexOfAnyBut(" \t\r\n*", end + 1);
          // check for removing last line
          if (getChar(end) == '/') {
            begin = indexOfAnyButBackward("*", begin);
          }
          // replace source
          replaceSubstring(begin, end - begin, "");
          // remove tag
          I.remove();
          // remove JavaDoc is empty
          if (DomGenerics.tags(javadoc).isEmpty()) {
            setJavadoc(declaration, null);
          }
          // done
          break;
        }
      }
    }
    return null;
  }

  private void setJavadocTagText_replaceFragments(TagElement tagElement, String tagText)
      throws Exception {
    List<ASTNode> fragments = DomGenerics.fragments(tagElement);
    // replace source
    int fragmentsPosition;
    if (!fragments.isEmpty()) {
      ASTNode firstFragment = fragments.get(0);
      fragmentsPosition = AstNodeUtils.getSourceBegin(firstFragment);
      int fragmentsLength = AstNodeUtils.getSourceEnd(tagElement) - fragmentsPosition;
      replaceSubstring(fragmentsPosition, fragmentsLength, tagText);
    } else {
      fragmentsPosition = AstNodeUtils.getSourceEnd(tagElement);
      replaceSubstring(fragmentsPosition, 0, tagText);
      AstNodeUtils.setSourceLength(tagElement, tagElement.getLength() + tagText.length());
    }
    // replace fragments
    fragments.clear();
    TextElement textElement = tagElement.getAST().newTextElement();
    textElement.setSourceRange(fragmentsPosition, tagText.length());
    textElement.setText(tagText);
    fragments.add(textElement);
  }

  /**
   * Sets new {@link Javadoc} comment for {@link BodyDeclaration}.
   *
   * @param declaration
   *          the {@link BodyDeclaration} to adds comment to.
   * @param lines
   *          the lines for {@link Javadoc} comment, may be <code>null</code> if {@link Javadoc}
   *          should be removed.
   *
   * @return the added {@link Javadoc} object, or <code>null</code> if {@link Javadoc} was removed.
   */
  public Javadoc setJavadoc(BodyDeclaration declaration, String[] lines) throws Exception {
    Javadoc oldJavadoc = declaration.getJavadoc();
    // set new JavaDoc
    if (lines != null) {
      int position = declaration.getStartPosition();
      // prepare code generation constants
      AstCodeGeneration generation = getGeneration();
      String eol = generation.getEndOfLine();
      String indent = getWhitespaceToLeft(declaration.getStartPosition(), false);
      // prepare source for comment
      String comment;
      {
        StringBuilder sb = new StringBuilder();
        sb.append("/**");
        sb.append(eol);
        for (String line : lines) {
          sb.append(indent);
          sb.append(" * ");
          sb.append(line);
          sb.append(eol);
        }
        sb.append(indent);
        sb.append(" */");
        comment = sb.toString();
      }
      // prepare JavaDoc
      Javadoc javadoc;
      {
        BodyDeclaration tmpMethod =
            getParser().parseBodyDeclaration(position, comment + " void __wbp_tmpMethod() {}");
        javadoc = tmpMethod.getJavadoc();
        tmpMethod.setJavadoc(null);
      }
      // set JavaDoc
      if (oldJavadoc != null) {
        int oldLength = oldJavadoc.getLength();
        replaceSubstring(position, oldLength, comment);
      } else {
        comment += eol + indent;
        replaceSubstring(position, 0, comment);
        declaration.setSourceRange(position, comment.length() + declaration.getLength());
      }
      declaration.setJavadoc(javadoc);
      return javadoc;
    }
    // remove existing JavaDoc
    if (oldJavadoc != null) {
      int sourceBegin = oldJavadoc.getStartPosition();
      int sourceEnd = sourceBegin + oldJavadoc.getLength();
      sourceEnd = indexOfAnyBut(" \t\r\n", sourceEnd);
      replaceSubstring(sourceBegin, sourceEnd - sourceBegin, "");
      declaration.setJavadoc(null);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Import operations
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_resolveImports = true;

  /**
   * Ensure that the compilation unit includes an import for the given fully-qualified class name,
   * adding one if it does not already exist.
   *
   * @param className
   *          the fully qualified name of the class that must be imported
   *
   * @return the string that should be used as reference on class, can be short name (if import was
   *         successful) or fully qualified name (if there is already imported class with same short
   *         name).
   */
  public String ensureClassImport2(final String className) throws Exception {
    final String shortClassName = CodeUtils.getShortClass(className);
    List<ImportDeclaration> imports = DomGenerics.imports(m_astUnit);
    // check, may be we don't need to add import
    {
      String packageName = CodeUtils.getPackage(className);
      // check for java.lang
      if ("java.lang".equals(packageName)) {
        return shortClassName;
      }
      // check for existing import
      for (Iterator<ImportDeclaration> I = imports.iterator(); I.hasNext();) {
        ImportDeclaration currentImport = I.next();
        String importName = currentImport.getName().toString();
        if (importName.equals(className)
            || currentImport.isOnDemand()
            && importName.equals(packageName)) {
          return shortClassName;
        }
      }
      // check for class in same package
      {
        IPackageDeclaration[] packageDeclarations = m_modelUnit.getPackageDeclarations();
        if (packageDeclarations.length != 0
            && packageDeclarations[0].getElementName().equals(packageName)) {
          return shortClassName;
        }
      }
      // check for inner class
      {
        final AtomicBoolean hasInner = new AtomicBoolean();
        m_astUnit.accept(new ASTVisitor() {
          @Override
          public boolean visit(Block node) {
            return false;
          }

          @Override
          public void endVisit(TypeDeclaration node) {
            String qualifiedTypeName = AstNodeUtils.getFullyQualifiedName(node, false);
            if (qualifiedTypeName.equals(className)) {
              hasInner.set(true);
            }
          }
        });
        if (hasInner.get()) {
          return shortClassName;
        }
      }
    }
    // check, may be we can not import because of conflict
    {
      boolean hasOnDemand = false;
      // check, may be there is already import for class with same short name
      for (Iterator<ImportDeclaration> I = imports.iterator(); I.hasNext();) {
        ImportDeclaration currentImport = I.next();
        if (currentImport.isOnDemand()) {
          hasOnDemand = true;
          break;
        } else {
          String importName = currentImport.getName().getFullyQualifiedName();
          if (CodeUtils.getShortClass(importName).equals(shortClassName)) {
            return className;
          }
        }
      }
      // there are on demand imports, we should check AST for imports
      if (hasOnDemand) {
        final AtomicBoolean conflict = new AtomicBoolean();
        m_astUnit.accept(new ASTVisitor() {
          @Override
          public void endVisit(SimpleType node) {
            if (!conflict.get()) {
              String qualifiedTypeName = AstNodeUtils.getFullyQualifiedName(node, false);
              boolean isQualified = qualifiedTypeName.equals(getSource(node));
              boolean hasSameShort =
                  CodeUtils.getShortClass(qualifiedTypeName).equals(shortClassName);
              if (!isQualified && hasSameShort) {
                conflict.set(true);
              }
            }
          }
        });
        // if short class name conflicts, use long one
        if (conflict.get()) {
          return className;
        }
      }
      // may be has TypeDeclaration with same short name
      {
        final AtomicBoolean hasTypeDeclarationWithSameShort = new AtomicBoolean();
        m_astUnit.accept(new ASTVisitor() {
          @Override
          public boolean visit(Block node) {
            return false;
          }

          @Override
          public void endVisit(TypeDeclaration node) {
            if (node.getName().getIdentifier().equals(shortClassName)) {
              hasTypeDeclarationWithSameShort.set(true);
            }
          }
        });
        if (hasTypeDeclarationWithSameShort.get()) {
          return className;
        }
      }
    }
    // OK, we need to add new import
    {
      String eol = getGeneration().getEndOfLine();
      String sourcePrefix = "";
      String sourceSuffix = "";
      // prepare position for new import
      int position;
      if (!imports.isEmpty()) {
        ImportDeclaration lastImport = imports.get(imports.size() - 1);
        position = AstNodeUtils.getSourceEnd(lastImport);
        sourcePrefix = eol;
      } else if (m_astUnit.getPackage() != null) {
        position = AstNodeUtils.getSourceEnd(m_astUnit.getPackage());
        sourcePrefix = eol;
      } else {
        position = 0;
        sourceSuffix = eol;
      }
      // add new import
      {
        // add import source
        {
          String source = sourcePrefix + "import " + className + ";" + sourceSuffix;
          replaceSubstring(position, 0, source);
        }
        // add import node
        {
          int importPosition = position + sourcePrefix.length();
          ImportDeclaration newImport =
              getParser().parseImportDeclaration(importPosition, className);
          imports.add(newImport);
        }
      }
      // import successful, return short name
      return shortClassName;
    }
  }

  /**
   * Sets the flag if imports should be automatically resolved.
   */
  public void setResolveImports(boolean resolveImports) {
    m_resolveImports = resolveImports;
  }

  /**
   * Given the {@link ASTNode} that can use fully qualified types, this method tries to import
   * qualified types and update node/source accordingly. It is expected that node already added to
   * the AST and source already modified.
   */
  public void resolveImports(final ASTNode node) {
    if (!m_resolveImports) {
      return;
    }
    node.accept(new AstVisitorEx() {
      @Override
      public boolean visitEx(QualifiedName qualifiedName) throws Exception {
        String qualifiedClassName = qualifiedName.getFullyQualifiedName();
        // if we can find IType with this name, consider it as type name
        if (getJavaProject().findType(qualifiedClassName) != null) {
          String shortClassName = ensureClassImport2(qualifiedClassName);
          if (!shortClassName.equals(qualifiedClassName)) {
            int sourceBegin = AstNodeUtils.getSourceBegin(qualifiedName);
            // replace source
            replaceSubstring(qualifiedName, shortClassName);
            // replace node
            {
              ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(qualifiedName);
              SimpleName simpleName = m_parser.parseSimpleName(sourceBegin, shortClassName);
              simpleName.setProperty(AstParser.KEY_TYPE_BINDING, typeBinding);
              replaceNode(qualifiedName, simpleName);
            }
            // done
            return false;
          }
        }
        return true;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodInvocation operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes argument from {@link MethodInvocation}.
   */
  public void removeInvocationArgument(MethodInvocation invocation, int index) throws Exception {
    removeInvocationArgument(invocation, DomGenerics.arguments(invocation), index);
  }

  /**
   * Removes argument from {@link ClassInstanceCreation}.
   */
  public void removeCreationArgument(ClassInstanceCreation creation, int index) throws Exception {
    removeInvocationArgument(creation, DomGenerics.arguments(creation), index);
  }

  /**
   * Removes argument from {@link MethodInvocation} or {@link ClassInstanceCreation}.
   */
  private void removeInvocationArgument(ASTNode parent, List<Expression> arguments, int index)
      throws Exception {
    // prepare source interval to remove
    int sourceBegin;
    int sourceEnd;
    {
      Expression argument = arguments.get(index);
      sourceBegin = AstNodeUtils.getSourceBegin(argument);
      sourceEnd = AstNodeUtils.getSourceEnd(argument);
      if (index == 0) {
        if (arguments.size() == 1) {
          sourceEnd = indexOf(")", sourceEnd);
        } else {
          sourceEnd = indexOfAnyBut(", \t\r\n", sourceEnd);
        }
      } else {
        sourceBegin = indexOfAnyButBackward(", \t\r\n", sourceBegin) + 1;
      }
    }
    // remove node
    arguments.remove(index);
    // remove parameter type from binding
    DesignerMethodBinding methodBinding = getDesignerMethodBinding(parent);
    if (index < methodBinding.getParameterTypes().length) {
      // remove when not ellipsis argument
      methodBinding.removeParameterType(index);
    }
    // remove source
    replaceSubstring(sourceBegin, sourceEnd - sourceBegin, "");
  }

  /**
   * @return the new argument added to the {@link MethodInvocation}.
   */
  public Expression addInvocationArgument(MethodInvocation invocation, int index, String source)
      throws Exception {
    return addInvocationArgument(invocation, DomGenerics.arguments(invocation), index, source);
  }

  /**
   * @return the new argument added to the {@link ClassInstanceCreation}.
   */
  public Expression addCreationArgument(ClassInstanceCreation creation, int index, String source)
      throws Exception {
    return addInvocationArgument(creation, DomGenerics.arguments(creation), index, source);
  }

  /**
   * Move argument from position with index <code>oldIndex</code> to position with index
   * <code>newIndex</code>.
   */
  public Expression moveInvocationArgument(MethodInvocation invocation, int oldIndex, int newIndex)
      throws Exception {
    List<Expression> arguments = DomGenerics.arguments(invocation);
    // prepare move expression
    Expression expression = arguments.get(oldIndex);
    // prepare move source
    String source = getSource(expression);
    // remove from old array
    removeInvocationArgument(invocation, oldIndex);
    // add to new array
    int position = insertToInvocationBody(invocation, arguments, newIndex, source);
    // add node
    arguments.add(newIndex, expression);
    AstNodeUtils.moveNode(expression, position);
    return expression;
  }

  /**
   * Adds new argument {@link Expression} into {@link MethodInvocation} or
   * {@link ClassInstanceCreation}.
   *
   * @param parent
   *          the {@link MethodInvocation} or {@link ClassInstanceCreation}.
   * @param arguments
   *          the {@link List} of argument {@link Expression}'s.
   * @param index
   *          the index for new argument.
   * @param source
   *          the source for new argument.
   *
   * @return the new argument {@link Expression}.
   */
  private Expression addInvocationArgument(Expression parent,
      List<Expression> arguments,
      int index,
      String source) throws Exception {
    int position = insertToInvocationBody(parent, arguments, index, source);
    // add node
    Expression argument = getParser().parseExpression(position, source);
    arguments.add(index, argument);
    // replace binding for method
    replaceInvocationBinding(parent);
    // return added argument
    resolveImports(argument);
    return argument;
  }

  /**
   * Add to invocation body of source new argument with given index.
   *
   * @return the start position of inserted code.
   */
  private int insertToInvocationBody(Expression parent,
      List<Expression> arguments,
      int index,
      String source) throws Exception {
    int position;
    String sourcePrefix = "";
    String sourceSuffix = "";
    if (index == 0) {
      if (arguments.size() == 0) {
        position = AstNodeUtils.getSourceEnd(parent) - 1;
      } else {
        Expression firstArgument = arguments.get(index);
        position = AstNodeUtils.getSourceBegin(firstArgument);
        sourceSuffix = ", ";
      }
    } else {
      Expression prevArgument = arguments.get(index - 1);
      position = AstNodeUtils.getSourceEnd(prevArgument);
      sourcePrefix = ", ";
    }
    // add source
    replaceSubstring(position, 0, sourcePrefix + source + sourceSuffix);
    position += sourcePrefix.length();
    return position;
  }

  /**
   * Replaces the arguments of given {@link ClassInstanceCreation}.
   *
   * @param creation
   *          the {@link ClassInstanceCreation} to replace arguments.
   * @param lines
   *          the {@link String}'s of arguments. Number of lines is not related with number of
   *          arguments, lines are used to format code better.
   */
  public void replaceCreationArguments(ClassInstanceCreation creation, List<String> lines)
      throws Exception {
    replaceInvocationArguments(creation, creation.getType(), DomGenerics.arguments(creation), lines);
  }

  /**
   * Replaces the arguments of given {@link MethodInvocation}.
   *
   * @param invocation
   *          the {@link MethodInvocation} to replace arguments.
   * @param lines
   *          the {@link String}'s of arguments. Number of lines is not related with number of
   *          arguments, lines are used to format code better.
   */
  public void replaceInvocationArguments(MethodInvocation invocation, List<String> lines)
      throws Exception {
    replaceInvocationArguments(
        invocation,
        invocation.getName(),
        DomGenerics.arguments(invocation),
        lines);
  }

  /**
   * Replaces the arguments of given {@link ClassInstanceCreation} with new arguments specified as
   * array of lines. Number of lines is not related with number of arguments, lines are used to
   * format code better.
   */
  /**
   * Replaces arguments of {@link MethodInvocation} or {@link ClassInstanceCreation}.
   *
   * @param parent
   *          the {@link MethodInvocation} or {@link ClassInstanceCreation}.
   * @param arguments
   *          the {@link List} of <code>parent</code> arguments.
   * @param lines
   *          the {@link String}'s of arguments. Number of lines is not related with number of
   *          arguments, lines are used to format code better.
   */
  private void replaceInvocationArguments(Expression parent,
      ASTNode nameNode,
      List<Expression> arguments,
      List<String> lines) throws Exception {
    // prepare new arguments source
    String source;
    {
      // prepare code generation constants
      AstCodeGeneration generation = getGeneration();
      String singleIndent = generation.getIndentation(1);
      String eol = generation.getEndOfLine();
      //
      Statement statement = AstNodeUtils.getEnclosingStatement(parent);
      String indent = getWhitespaceToLeft(statement.getStartPosition(), false);
      source = getIndentedSource(lines, indent, singleIndent, eol);
      // remove indentation for first line
      source = source.trim();
    }
    // replace source
    int sourceBegin = indexOf("(", AstNodeUtils.getSourceBegin(nameNode)) + 1;
    int sourceEnd = AstNodeUtils.getSourceEnd(parent) - 1;
    replaceSubstring(sourceBegin, sourceEnd - sourceBegin, source);
    // replace binding
    ASTNode newInvocation = replaceInvocationBinding(parent);
    // replace arguments
    {
      List<Expression> newArguments = DomGenerics.arguments(newInvocation);
      List<Expression> newArgumentsCopy = new ArrayList<Expression>(newArguments);
      newArguments.clear();
      arguments.clear();
      arguments.addAll(newArgumentsCopy);
    }
    // finalize
    resolveImports(parent);
  }

  /**
   * Replaces the name of method in given {@link MethodInvocation}.
   */
  public void replaceInvocationName(MethodInvocation invocation, String newIdentifier)
      throws Exception {
    setIdentifier(invocation.getName(), newIdentifier);
    replaceInvocationBinding(invocation);
  }

  /**
   * Replaces the expression part in given {@link MethodInvocation}.
   */
  public void replaceInvocationExpression(MethodInvocation invocation, String newExpressionSource)
      throws Exception {
    if (invocation.getExpression() != null) {
      replaceExpression(invocation.getExpression(), newExpressionSource);
    } else {
      int position = invocation.getStartPosition();
      // insert Expression
      {
        Expression newExpression = getParser().parseExpression(position, newExpressionSource);
        replaceSubstring(position, 0, newExpressionSource + ".");
        invocation.setExpression(newExpression);
      }
      // we inserted Expression, update "begin" for parent nodes
      {
        int newInvocationPosition = invocation.getStartPosition();
        ASTNode node = invocation;
        while (node.getStartPosition() == newInvocationPosition) {
          AstNodeUtils.setSourceBegin_keepEnd(node, position);
          node = node.getParent();
        }
      }
    }
    replaceInvocationBinding(invocation);
  }

  /**
   * Parses the source of given {@link MethodInvocation} or {@link ClassInstanceCreation} and
   * installs its {@link IMethodBinding} as binding for given invocation.
   *
   * @param invocation
   *          the {@link MethodInvocation} or {@link ClassInstanceCreation} to replace
   *          {@link IMethodBinding}.
   *
   * @return the parsed {@link ASTNode}.
   */
  public ASTNode replaceInvocationBinding(Expression invocation) throws Exception {
    Assert.isLegal(invocation instanceof MethodInvocation
        || invocation instanceof ClassInstanceCreation);
    ASTNode parsedInvocation =
        m_parser.parseExpression(invocation.getStartPosition(), getSource(invocation));
    replaceMethodBinding(invocation, parsedInvocation);
    //
    return parsedInvocation;
  }

  /**
   * Replaces {@link IMethodBinding} in of existing {@link ASTNode} by using {@link IMethodBinding}
   * from equivalent parsed {@link ASTNode}.
   */
  private void replaceMethodBinding(ASTNode oldNode, ASTNode parsedNode) {
    IMethodBinding parsedBinding =
        (IMethodBinding) parsedNode.getProperty(AstParser.KEY_METHOD_BINDING);
    Assert.isTrue(parsedBinding instanceof DesignerMethodBinding);
    oldNode.setProperty(AstParser.KEY_METHOD_BINDING, parsedBinding);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassInstanceCreation += AnonymousClassDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link AnonymousClassDeclaration} into given {@link ClassInstanceCreation}.
   */
  public void addAnonymousClassDeclaration(ClassInstanceCreation creation) throws Exception {
    Assert.isNull2(creation.getAnonymousClassDeclaration(), "Already has anonymous: {0}", creation);
    // prepare indent
    String indent;
    {
      Statement statement = AstNodeUtils.getEnclosingStatement(creation);
      indent = getWhitespaceToLeft(statement.getStartPosition(), false);
    }
    // prepare positions
    int begin = AstNodeUtils.getSourceBegin(creation);
    int end = AstNodeUtils.getSourceEnd(creation);
    // prepare source
    String sourceNewCreation;
    {
      String eol = m_generation.getEndOfLine();
      String sourceInsert = " {" + eol + indent + "}";
      sourceNewCreation = getSource(creation) + sourceInsert;
      // insert source
      replaceSubstring(end, 0, sourceInsert);
    }
    // copy AnonymousClassDeclaration
    ClassInstanceCreation newCreation =
        (ClassInstanceCreation) getParser().parseExpression(begin, sourceNewCreation);
    AnonymousClassDeclaration newAnonymous = newCreation.getAnonymousClassDeclaration();
    newCreation.setAnonymousClassDeclaration(null);
    creation.setAnonymousClassDeclaration(newAnonymous);
    // we append {}, so replaceSubstring() will not update length, so we should update it manually
    {
      ASTNode enclosingNode = creation;
      while (AstNodeUtils.getSourceEnd(enclosingNode) == end) {
        AstNodeUtils.setSourceEnd(enclosingNode, newAnonymous);
        enclosingNode = enclosingNode.getParent();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bindings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the new {@link DesignerTypeBinding} for given {@link ASTNode}, which has
   *         {@link ITypeBinding}.
   */
  private DesignerTypeBinding getDesignerTypeBinding(ASTNode node) {
    // prepare current binding
    ITypeBinding currentBinding;
    {
      currentBinding = AstNodeUtils.getTypeBinding((TypeDeclaration) node);
    }
    // set new DesignerTypeBinding
    DesignerTypeBinding designerBinding = m_bindingContext.getCopy(currentBinding);
    node.setProperty(AstParser.KEY_TYPE_BINDING, designerBinding);
    return designerBinding;
  }

  /**
   * @return existing or new {@link DesignerMethodBinding} for given {@link MethodInvocation} or
   *         {@link ClassInstanceCreation}. We use this {@link DesignerMethodBinding} for low-level
   *         modifications.
   */
  private DesignerMethodBinding getDesignerMethodBinding(ASTNode node) {
    // prepare current binding for method/constructor
    IMethodBinding currentMethodBinding;
    if (node instanceof MethodDeclaration) {
      currentMethodBinding = AstNodeUtils.getMethodBinding((MethodDeclaration) node);
    } else if (node instanceof MethodInvocation) {
      currentMethodBinding = AstNodeUtils.getMethodBinding((MethodInvocation) node);
    } else {
      currentMethodBinding = AstNodeUtils.getCreationBinding((ClassInstanceCreation) node);
    }
    // get existing DesignerMethodBinding, or create new wrapper
    if (currentMethodBinding instanceof DesignerMethodBinding) {
      return (DesignerMethodBinding) currentMethodBinding;
    } else {
      DesignerMethodBinding designerMethodBinding = m_bindingContext.get(currentMethodBinding);
      node.setProperty(AstParser.KEY_METHOD_BINDING, designerMethodBinding);
      return designerMethodBinding;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Array
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add element with given <code>index</code> to {@link ArrayInitializer}.
   */
  public Expression addArrayElement(ArrayInitializer arrayInitializer, int index, String source)
      throws Exception {
    // prepare source
    int position = insertToArrayBody(arrayInitializer, index, source);
    // add node
    Expression element = getParser().parseExpression(position, source);
    List<Expression> elements = DomGenerics.expressions(arrayInitializer);
    elements.add(index, element);
    // return added element
    resolveImports(element);
    return element;
  }

  /**
   * Move element form old array <code>oldArray</code> with index <code>oldIndex</code> to new array
   * <code>newArray</code> with index <code>newIndex</code>.
   */
  public Expression moveArrayElement(ArrayInitializer oldArray,
      ArrayInitializer newArray,
      int oldIndex,
      int newIndex) throws Exception {
    // prepare move expression
    Expression expression = DomGenerics.expressions(oldArray).get(oldIndex);
    // prepare move source
    String source = getSource(expression);
    // remove from old array
    removeArrayElement(oldArray, oldIndex);
    // add to new array
    int position = insertToArrayBody(newArray, newIndex, source);
    // add node
    DomGenerics.expressions(newArray).add(newIndex, expression);
    AstNodeUtils.moveNode(expression, position);
    return expression;
  }

  /**
   * Add to array elements body of source new element with given index.
   *
   * @return the start position of inserted code.
   */
  private int insertToArrayBody(ArrayInitializer arrayInitializer, int index, String source)
      throws Exception {
    // prepare elements
    List<Expression> elements = DomGenerics.expressions(arrayInitializer);
    int position;
    String sourcePrefix = "";
    String sourceSuffix = "";
    if (index == 0) {
      if (elements.size() == 0) {
        position = AstNodeUtils.getSourceEnd(arrayInitializer) - 1;
      } else {
        Expression firstElement = elements.get(index);
        position = AstNodeUtils.getSourceBegin(firstElement);
        sourceSuffix = ", ";
      }
    } else {
      Expression prevElement = elements.get(index - 1);
      position = AstNodeUtils.getSourceEnd(prevElement);
      sourcePrefix = ", ";
    }
    // add source
    replaceSubstring(position, 0, sourcePrefix + source + sourceSuffix);
    position += sourcePrefix.length();
    //
    return position;
  }

  /**
   * Remove element with given <code>index</code> from {@link ArrayInitializer}.
   */
  public void removeArrayElement(ArrayInitializer arrayInitializer, int index) throws Exception {
    // prepare elements
    List<Expression> elements = DomGenerics.expressions(arrayInitializer);
    if (index >= elements.size()) {
      return;
    }
    // prepare source interval to remove
    int sourceBegin;
    int sourceEnd;
    {
      Expression element = elements.get(index);
      sourceBegin = AstNodeUtils.getSourceBegin(element);
      sourceEnd = AstNodeUtils.getSourceEnd(element);
      if (index == 0) {
        if (elements.size() == 1) {
          sourceEnd = indexOf("}", sourceEnd);
        } else {
          sourceEnd = indexOfAnyBut(", \t\r\n", sourceEnd);
        }
      } else {
        sourceBegin = indexOfAnyButBackward(", \t\r\n", sourceBegin) + 1;
      }
    }
    // remove node
    elements.remove(index);
    // remove source
    replaceSubstring(sourceBegin, sourceEnd - sourceBegin, "");
  }

  /**
   * Exchanges elements with given indexes inside of {@link ArrayInitializer}.
   */
  public void exchangeArrayElements(ArrayInitializer arrayInitializer, int index_1, int index_2)
      throws Exception {
    // prepare elements
    List<Expression> elements = DomGenerics.expressions(arrayInitializer);
    Expression element_1 = elements.get(index_1);
    Expression element_2 = elements.get(index_2);
    String source_1 = getSource(element_1);
    String source_2 = getSource(element_2);
    int position_1 = element_1.getStartPosition();
    int position_2 = element_2.getStartPosition();
    int length_1 = element_1.getLength();
    int length_2 = element_2.getLength();
    // exchange elements
    {
      elements.set(index_1, arrayInitializer.getAST().newSimpleName("foo_1"));
      elements.set(index_2, arrayInitializer.getAST().newSimpleName("foo_2"));
      // set source
      if (position_1 < position_2) {
        replaceSubstring(position_2, length_2, source_1);
        replaceSubstring(position_1, length_1, source_2);
        position_2 += length_2 - length_1;
      } else {
        replaceSubstring(position_1, length_1, source_2);
        replaceSubstring(position_2, length_2, source_1);
        position_1 += length_1 - length_2;
      }
      // set positions
      AstNodeUtils.moveNode(element_1, position_2);
      AstNodeUtils.moveNode(element_2, position_1);
      // set nodes
      elements.set(index_1, element_2);
      elements.set(index_2, element_1);
    }
  }
}
