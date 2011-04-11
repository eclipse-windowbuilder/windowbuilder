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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeBeanAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.SimpleClassUiContentProvider;

import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;

import java.util.List;

/**
 * Abstract model for all <code>JFace</code> tree viewer observable content provider.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class ObservableCollectionTreeContentProviderInfo
    extends
      ObservableCollectionContentProviderInfo {
  protected ObservableFactoryInfo m_factoryInfo;
  protected TreeStructureAdvisorInfo m_advisorInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObservableCollectionTreeContentProviderInfo(String className,
      ObservableFactoryInfo factoryInfo,
      TreeStructureAdvisorInfo advisorInfo) {
    super(className);
    m_factoryInfo = factoryInfo;
    m_advisorInfo = advisorInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final ObservableFactoryInfo getFactoryInfo() {
    return m_factoryInfo;
  }

  public final void setFactoryInfo(ObservableFactoryInfo factoryInfo) {
    m_factoryInfo = factoryInfo;
  }

  public final TreeStructureAdvisorInfo getAdvisorInfo() {
    return m_advisorInfo;
  }

  public final void setAdvisorInfo(TreeStructureAdvisorInfo advisorInfo) {
    m_advisorInfo = advisorInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link IUiContentProvider} content providers for edit this model.
   */
  public final void createContentProviders(List<IUiContentProvider> providers,
      DatabindingsProvider provider,
      boolean useClear) {
    ChooseClassConfiguration configuration = new ChooseClassConfiguration();
    configuration.setDialogFieldLabel(Messages.ObservableCollectionTreeContentProviderInfo_chooseLabel);
    configure(configuration, useClear);
    configuration.setConstructorParameters(new Class[]{
        IObservableFactory.class,
        TreeStructureAdvisor.class});
    configuration.setEmptyClassErrorMessage(Messages.ObservableCollectionTreeContentProviderInfo_chooseEmptyMessage);
    configuration.setErrorMessagePrefix(Messages.ObservableCollectionTreeContentProviderInfo_chooseMessagePrefix);
    providers.add(new SimpleClassUiContentProvider(configuration, this));
  }

  /**
   * Create configuration for edit this model.
   */
  protected abstract void configure(ChooseClassConfiguration configuration, boolean useClear);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getPresentationText() throws Exception {
    String text = super.getPresentationText();
    if (m_advisorInfo instanceof TreeBeanAdvisorInfo) {
      text += "[" + m_advisorInfo.getPresentationText() + "]";
    }
    return text;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    // prepare variable
    if (getVariableIdentifier() == null) {
      setVariableIdentifier(generationSupport.generateLocalName("treeContentProvider"));
    }
    // add factory code
    m_factoryInfo.addSourceCode(lines, generationSupport);
    // add advisor code
    m_advisorInfo.addSourceCode(lines, generationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void accept(AstObjectInfoVisitor visitor) throws Exception {
    super.accept(visitor);
    // visit to factory
    m_factoryInfo.accept(visitor);
    // visit to advisor
    m_advisorInfo.accept(visitor);
  }
}