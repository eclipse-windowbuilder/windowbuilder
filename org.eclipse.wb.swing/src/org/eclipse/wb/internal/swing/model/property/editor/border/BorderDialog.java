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
package org.eclipse.wb.internal.swing.model.property.editor.border;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.AbstractBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.BevelBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.CompoundBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.DefaultBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.EmptyBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.EtchedBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.LineBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.MatteBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.NoBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.SoftBevelBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.SwingBorderComposite;
import org.eclipse.wb.internal.swing.model.property.editor.border.pages.TitledBorderComposite;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;

import javax.swing.border.Border;

import swingintegration.example.EmbeddedSwingComposite2;

/**
 * Dialog for {@link Border} editing.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class BorderDialog extends ResizableDialog {
  private final AstEditor m_editor;
  private boolean m_borderModified;
  private Border m_border;
  private String m_source;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderDialog(Shell parentShell, AstEditor editor) {
    super(parentShell, Activator.getDefault());
    setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
    m_editor = editor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets flag, that {@link Border} is modified.
   * 
   * @param borderModified
   *          is <code>false</code>, if {@link Border} is not set, so <code>(default)</code> page
   *          should be used.
   */
  public void setBorderModified(boolean borderModified) {
    m_borderModified = borderModified;
  }

  /**
   * Sets the {@link Border} to edit.
   */
  public void setBorder(Border border) {
    m_border = border;
  }

  /**
   * @return the updated {@link Border}.
   */
  public Border getBorder() {
    return m_border;
  }

  /**
   * @return the Java source for selected {@link Border}.
   */
  public String getBorderSource() {
    return m_source;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private BorderPreviewCanvas m_previewCanvas;
  private Combo m_typeCombo;
  private Group m_pagesComposite;
  private StackLayout m_pagesLayout;
  private final java.util.List<AbstractBorderComposite> m_pages = Lists.newArrayList();

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    GridLayoutFactory.create(area).margins(10);
    // create Border type combo
    {
      Group typeGroup = new Group(area, SWT.NONE);
      GridDataFactory.create(typeGroup).grabH().fillH();
      GridLayoutFactory.create(typeGroup);
      typeGroup.setText(ModelMessages.BorderDialog_type);
      {
        m_typeCombo = new Combo(typeGroup, SWT.READ_ONLY);
        GridDataFactory.create(m_typeCombo).grab().fill();
        m_typeCombo.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            int index = m_typeCombo.getSelectionIndex();
            m_pagesLayout.topControl = m_pages.get(index);
            m_pagesComposite.layout();
            // show Border from new page
            borderUpdated();
          }
        });
      }
    }
    // pages
    {
      m_pagesComposite = new Group(area, SWT.NONE);
      GridDataFactory.create(m_pagesComposite).spanH(2).grab().fill();
      m_pagesComposite.setText(ModelMessages.BorderDialog_properties);
      // create pages
      addPages(m_pagesComposite);
      m_typeCombo.setVisibleItemCount(m_typeCombo.getItemCount());
      // set layout
      m_pagesLayout = new StackLayout();
      m_pagesLayout.topControl = m_pages.get(0);
      m_pagesComposite.setLayout(m_pagesLayout);
    }
    // create preview
    {
      Group previewGroup = new Group(area, SWT.NONE);
      GridDataFactory.create(previewGroup).spanH(2).grabH().fillH();
      GridLayoutFactory.create(previewGroup);
      previewGroup.setText(ModelMessages.BorderDialog_preview);
      //
      if (EmbeddedSwingComposite2.canUseAwt()) {
        m_previewCanvas = new BorderPreviewCanvas(previewGroup, SWT.NONE);
        GridDataFactory.create(m_previewCanvas).grab().fill().hintV(100);
      }
    }
    //
    updateGUI();
    return area;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(ModelMessages.BorderDialog_title);
  }

  @Override
  protected void okPressed() {
    m_source = getCurrentBorderSource();
    super.okPressed();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates GUI using current {@link Border}.
   */
  private void updateGUI() {
    // select page
    {
      m_pagesLayout.topControl = m_pages.get(0);
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          for (AbstractBorderComposite page : m_pages) {
            boolean understands = page.setBorder(m_border);
            if (understands && m_borderModified) {
              m_pagesLayout.topControl = page;
            }
          }
        }
      });
      m_pagesComposite.layout();
      // select in "type" combo
      m_typeCombo.select(m_pages.indexOf(m_pagesLayout.topControl));
    }
    // update preview
    if (m_previewCanvas != null) {
      m_previewCanvas.setBorder(m_border);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link AbstractBorderComposite}.
   */
  protected final void addPage(AbstractBorderComposite page) {
    page.initialize(this, m_editor);
    m_pages.add(page);
    m_typeCombo.add(page.getTitle());
  }

  /**
   * Adds {@link AbstractBorderComposite}'s.
   */
  protected void addPages(Composite parent) {
    addPage(new DefaultBorderComposite(parent));
    addPage(new NoBorderComposite(parent));
    addPage(new BevelBorderComposite(parent));
    addPage(new CompoundBorderComposite(parent));
    addPage(new EmptyBorderComposite(parent));
    addPage(new EtchedBorderComposite(parent));
    addPage(new LineBorderComposite(parent));
    addPage(new MatteBorderComposite(parent));
    addPage(new SoftBevelBorderComposite(parent));
    addPage(new TitledBorderComposite(parent));
    addPage(new SwingBorderComposite(parent));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the source of {@link Border} from currently selected {@link AbstractBorderComposite}.
   */
  private String getCurrentBorderSource() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        int index = m_typeCombo.getSelectionIndex();
        return m_pages.get(index).getSource();
      }
    }, null);
  }

  /**
   * This method is invoked by {@link AbstractBorderComposite} to notify that something was changed,
   * so we need to update {@link BorderPreviewCanvas}.
   */
  public void borderUpdated() {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        borderUpdatedEx();
      }
    });
  }

  /**
   * Implementation of {@link #borderUpdated()}, that throws {@link Exception}.
   */
  private void borderUpdatedEx() throws Exception {
    ICompilationUnit unit = m_editor.getModelUnit().getWorkingCopy(new NullProgressMonitor());
    try {
      updateBorderEx(unit);
    } finally {
      unit.discardWorkingCopy();
    }
  }

  private void updateBorderEx(ICompilationUnit unit) throws Exception {
    // prepare Border expression
    Expression borderExpression;
    {
      String borderSource = getCurrentBorderSource();
      unit.getBuffer().setContents(
          StringUtils.join(new String[]{
              "class __Foo {",
              "  private javax.swing.border.Border border = " + borderSource,
              "}"}, "\n"));
      AstEditor editor = new AstEditor(unit);
      FieldDeclaration fieldDeclaration =
          DomGenerics.types(editor.getAstUnit()).get(0).getFields()[0];
      borderExpression = DomGenerics.fragments(fieldDeclaration).get(0).getInitializer();
    }
    // evaluate Border expression
    {
      ClassLoader classLoader = EditorState.get(m_editor).getEditorLoader();
      EvaluationContext context =
          new EvaluationContext(classLoader, new ExecutionFlowDescription());
      m_border = (Border) AstEvaluationEngine.evaluate(context, borderExpression);
    }
    // set new Border
    m_previewCanvas.setBorder(m_border);
  }
}
