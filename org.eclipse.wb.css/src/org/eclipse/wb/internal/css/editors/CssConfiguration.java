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

import org.eclipse.wb.internal.css.Activator;
import org.eclipse.wb.internal.css.editors.scanners.CommentScanner;
import org.eclipse.wb.internal.css.editors.scanners.DefaultScanner;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * Configuration for CSS source viewer.
 * 
 * @author scheglov_ke
 * @coverage CSS.editor
 */
public class CssConfiguration extends SourceViewerConfiguration {
  private final TokenManager m_tokenManager = Activator.getDefault().getTokenManager();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
    return CssPartitionScanner.getContentTypes();
  }

  @Override
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
    PresentationReconciler reconciler = new PresentationReconciler();
    {
      DefaultDamagerRepairer dr = new MultilineDamagerRepairer(new DefaultScanner(m_tokenManager));
      addDamagerRepairer(reconciler, dr, IDocument.DEFAULT_CONTENT_TYPE);
    }
    {
      DefaultDamagerRepairer dr = new MultilineDamagerRepairer(new CommentScanner(m_tokenManager));
      addDamagerRepairer(reconciler, dr, CssPartitionScanner.COMMENT_TYPE);
    }
    return reconciler;
  }

  /**
   * Adds given {@link DefaultDamagerRepairer} as damager/repairer with given content type.
   */
  private static void addDamagerRepairer(PresentationReconciler reconciler,
      DefaultDamagerRepairer dr,
      String contentType) {
    reconciler.setDamager(dr, contentType);
    reconciler.setRepairer(dr, contentType);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Content assist
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
    /*ContentAssistant assistant = new ContentAssistant();
     assistant.setContentAssistProcessor(new PropertiesAssistant(), IDocument.DEFAULT_CONTENT_TYPE);
     assistant.enableAutoActivation(true);
     assistant.enableAutoInsert(true);
     return assistant;*/
    return null;
  }
}
