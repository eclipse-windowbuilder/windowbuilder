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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el;

import org.eclipse.wb.internal.swing.databinding.Messages;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

/**
 * {@link SourceViewerConfiguration} for highlighting {@code EL} properties.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class EvalutionLanguageConfiguration extends SourceViewerConfiguration {
  private static final String EXPRESSION_TYPE = "__expression_type_";
  private final ElPropertyUiConfiguration m_configuration;
  private final IBeanPropertiesSupport m_propertiesSupport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EvalutionLanguageConfiguration(SourceViewer sourceViewer,
      IDocument document,
      ElPropertyUiConfiguration configuration,
      IBeanPropertiesSupport propertiesSupport) {
    m_configuration = configuration;
    m_propertiesSupport = propertiesSupport;
    sourceViewer.configure(this);
    configureDocument(document);
    sourceViewer.setDocument(document);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Document
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void configureDocument(IDocument document) {
    // token
    IToken token = new Token(EXPRESSION_TYPE);
    IPredicateRule[] rules = new IPredicateRule[2];
    rules[0] = new MultiLineRule("${", "}", token);
    rules[1] = new MultiLineRule("#{", "}", token);
    // scanner
    RuleBasedPartitionScanner partitionScanner = new RuleBasedPartitionScanner();
    partitionScanner.setPredicateRules(rules);
    IDocumentPartitioner partitioner =
        new FastPartitioner(partitionScanner, new String[]{EXPRESSION_TYPE});
    // configure document
    partitioner.connect(document);
    document.setDocumentPartitioner(partitioner);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SourceViewerConfiguration
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
    return new String[]{IDocument.DEFAULT_CONTENT_TYPE, EXPRESSION_TYPE};
  }

  @Override
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
    PresentationReconciler reconciler = new PresentationReconciler();
    // EL language rules
    configureExpressionType(reconciler, sourceViewer);
    // text rules
    configureDefaultType(reconciler);
    return reconciler;
  }

  private void configureExpressionType(PresentationReconciler reconciler, ISourceViewer sourceViewer) {
    RuleBasedScanner scanner = new RuleBasedScanner();
    scanner.setDefaultReturnToken(new Token(new TextAttribute(m_configuration.getPropertiesColor())));
    // EL language rules
    IRule keywordsRule = new KeywordsRule(sourceViewer, m_configuration);
    IRule operatorsRule = new OperatorsRule(m_configuration);
    IRule numbersRule = new NumbersRule(m_configuration);
    scanner.setRules(new IRule[]{keywordsRule, operatorsRule, numbersRule});
    // repairer
    DefaultDamagerRepairer repairer = new DefaultDamagerRepairer(scanner);
    reconciler.setDamager(repairer, EXPRESSION_TYPE);
    reconciler.setRepairer(repairer, EXPRESSION_TYPE);
  }

  private void configureDefaultType(PresentationReconciler reconciler) {
    // scanner
    RuleBasedScanner scanner = new RuleBasedScanner();
    scanner.setDefaultReturnToken(new Token(new TextAttribute(m_configuration.getStringsColor())));
    // repairer
    DefaultDamagerRepairer repairer = new DefaultDamagerRepairer(scanner);
    reconciler.setDamager(repairer, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setRepairer(repairer, IDocument.DEFAULT_CONTENT_TYPE);
  }

  @Override
  public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
    final ContentAssistant contentAssistant = new ContentAssistant();
    // sets processors
    contentAssistant.setContentAssistProcessor(
        new ContentAssistProcessor(m_propertiesSupport, true),
        IDocument.DEFAULT_CONTENT_TYPE);
    contentAssistant.setContentAssistProcessor(new ContentAssistProcessor(m_propertiesSupport,
        false), EXPRESSION_TYPE);
    // configure
    contentAssistant.enableAutoActivation(true);
    contentAssistant.enableAutoInsert(true);
    contentAssistant.setAutoActivationDelay(200);
    contentAssistant.setStatusLineVisible(true);
    contentAssistant.setStatusMessage(Messages.EvalutionLanguageConfiguration_contentAssistMessage);
    // CTRL + SPACE completion
    sourceViewer.getTextWidget().addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.character == ' ' && (e.stateMask & SWT.CTRL) != 0) {
          contentAssistant.showPossibleCompletions();
        }
      }
    });
    return contentAssistant;
  }
}