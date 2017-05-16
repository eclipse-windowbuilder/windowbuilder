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
package org.eclipse.wb.internal.core.editor.structure;

import org.eclipse.wb.core.controls.SelfOrientingSashForm;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.structure.components.ComponentsTreePage;
import org.eclipse.wb.internal.core.editor.structure.property.ComponentsPropertiesPage;
import org.eclipse.wb.internal.gef.tree.TreeViewer;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link Composite} for displaying {@link ComponentsTreePage} and {@link ComponentsPropertiesPage}.
 *
 * @author scheglov_ke
 * @coverage core.editor.structure
 */
public final class DesignComponentsComposite extends Composite {
  private final SelfOrientingSashForm m_sashForm;
  private final ComponentsTreePage m_treePage;
  private final ComponentsPropertiesPage m_propertiesPage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignComponentsComposite(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout());
    m_sashForm = new SelfOrientingSashForm(this, SWT.NONE);
    // create pages
    {
      // tree
      {
        PageSiteComposite siteComposite = new PageSiteComposite(m_sashForm, SWT.BORDER);
        siteComposite.setTitleText(Messages.DesignComponentsComposite_componentsTitle);
        siteComposite.setTitleImage(DesignerPlugin.getImage("structure/components_view.gif"));
        m_treePage = new ComponentsTreePage();
        siteComposite.setPage(m_treePage);
      }
      // properties
      {
        PageSiteComposite siteComposite = new PageSiteComposite(m_sashForm, SWT.BORDER);
        siteComposite.setTitleText(Messages.DesignComponentsComposite_propertiesTitle);
        siteComposite.setTitleImage(DesignerPlugin.getImage("structure/properties_view.gif"));
        m_propertiesPage = new ComponentsPropertiesPage();
        siteComposite.setPage(m_propertiesPage);
      }
    }
    // add listeners
    m_treePage.getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        m_propertiesPage.setSelection(selection);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TreeViewer} used to display components tree.
   */
  public TreeViewer getTreeViewer() {
    return m_treePage.getTreeViewer();
  }

  /**
   * Sets information about editor.
   */
  public void setInput(IEditPartViewer editPartViewer, ObjectInfo rootObject) {
    m_treePage.setInput(editPartViewer, rootObject);
  }
}
