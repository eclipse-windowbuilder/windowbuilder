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
package org.eclipse.wb.internal.css.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * Partition scanner for CSS.
 * 
 * @author scheglov_ke
 * @coverage CSS.editor
 */
public class CssPartitionScanner extends RuleBasedPartitionScanner {
  public final static String DEFAULT_TYPE = IDocument.DEFAULT_CONTENT_TYPE;
  public final static String COMMENT_TYPE = "__css_comment";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CssPartitionScanner() {
    // comment rule
    IPredicateRule commentRule;
    {
      Token commentToken = new Token(COMMENT_TYPE);
      commentRule = new MultiLineRule("/*", "*/", commentToken, (char) 0, true);
    }
    // set rules
    setPredicateRules(new IPredicateRule[]{commentRule});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the content types for which partitions are created.
   */
  public static String[] getContentTypes() {
    return new String[]{DEFAULT_TYPE, COMMENT_TYPE};
  }

  /**
   * Configures partitioning for given {@link IDocument}.
   */
  public static void configurePartitions(IDocument document) {
    IDocumentPartitioner partitioner =
        new FastPartitioner(new CssPartitionScanner(), getContentTypes());
    document.setDocumentPartitioner(partitioner);
    partitioner.connect(document);
  }
}
