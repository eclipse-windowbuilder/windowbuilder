/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.jdt.core;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.internal.corext.javadoc.JavaDocCommentReader;

import java.io.Reader;

/**
 * Helper needed get the content of a Javadoc comment.
 *
 * @since 3.1
 * @coverage core.util.jdt
 */
public class JavadocContentAccess {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private JavadocContentAccess() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Reader} for an {@link IMember}'s Javadoc comment content from the source
   *         attachment.
   */
  public static Reader getContentReader(IMember member, boolean allowInherited) throws Exception {
    // check current type
    {
      IBuffer buffer =
          member.isBinary()
              ? member.getClassFile().getBuffer()
              : member.getCompilationUnit().getBuffer();
      // no source attachment found
      if (buffer == null) {
        return null;
      }
      //
      ISourceRange range = member.getSourceRange();
      int start = range.getOffset();
      int length = range.getLength();
      if (length > 0 && buffer.getChar(start) == '/') {
        // prepare scanner
        IScanner scanner;
        {
          scanner = ToolFactory.createScanner(true, false, false, false);
          scanner.setSource(buffer.getCharacters());
          scanner.resetTo(start, start + length - 1);
        }
        // find last JavaDoc comment
        {
          int docOffset = -1;
          int docEnd = -1;
          {
            int terminal = scanner.getNextToken();
            while (org.eclipse.jdt.internal.corext.dom.TokenScanner.isComment(terminal)) {
              if (terminal == ITerminalSymbols.TokenNameCOMMENT_JAVADOC) {
                docOffset = scanner.getCurrentTokenStartPosition();
                docEnd = scanner.getCurrentTokenEndPosition() + 1;
              }
              terminal = scanner.getNextToken();
            }
          }
          // if comment found, return it
          if (docOffset != -1) {
            return new JavaDocCommentReader(buffer, docOffset, docEnd);
          }
        }
      }
    }
    // check inherited
    if (allowInherited && member.getElementType() == IJavaElement.METHOD) {
      IMethod method = (IMethod) member;
      IMethod superMethod = CodeUtils.findSuperMethod(method);
      if (superMethod != null) {
        return getContentReader(superMethod, allowInherited);
      }
    }
    // not found
    return null;
  }
}