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
package org.eclipse.wb.internal.core.editor.errors;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.controls.BrowserComposite;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.actions.SwitchAction;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.exception.NoEntryPointError;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * {@link Composite} for displaying {@link NoEntryPointError}.
 *
 * @author scheglov_ke
 * @coverage core.editor.errors
 */
public final class NoEntryPointComposite extends Composite {
  private final BrowserComposite m_browser;
  private final Label m_titleLabel;
  private final TableViewer m_viewer;
  private AstEditor m_editor;
  private TypeDeclaration m_typeDeclaration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NoEntryPointComposite(Composite parent, int style) {
    super(parent, style);
    GridLayoutFactory.create(this);
    {
      Composite titleComposite = new Composite(this, SWT.NONE);
      GridDataFactory.create(titleComposite).alignHL();
      GridLayoutFactory.create(titleComposite).columns(2).margins(10);
      {
        Label label = new Label(titleComposite, SWT.NONE);
        label.setImage(SwtResourceManager.getImage(SWT.ICON_INFORMATION));
      }
      {
        m_titleLabel = new Label(titleComposite, SWT.NONE);
        m_titleLabel.setFont(SwtResourceManager.getFont(
            getFont().getFontData()[0].getName(),
            14,
            SWT.BOLD));
      }
    }
    // Browser
    {
      m_browser = new BrowserComposite(this, SWT.NONE);
      GridDataFactory.create(m_browser).grab().fill();
    }
    // viewer
    {
      new Label(this, SWT.NONE).setText(Messages.NoEntryPointComposite_viewerLabel);
      m_viewer = new TableViewer(this, SWT.BORDER | SWT.V_SCROLL);
      Table table = m_viewer.getTable();
      GridDataFactory.create(table).hintVC(10).grabH().fill();
      // providers
      JavaElementLabelProvider labelProvider =
          new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_PARAMETERS
              | JavaElementLabelProvider.SHOW_OVERLAY_ICONS);
      m_viewer.setLabelProvider(labelProvider);
      m_viewer.setContentProvider(new ArrayContentProvider());
      // listeners
      m_viewer.addDoubleClickListener(new IDoubleClickListener() {
        public void doubleClick(DoubleClickEvent event) {
          markSelectedMethod();
        }
      });
    }
    // buttons
    {
      Composite buttonsComposite = new Composite(this, SWT.NONE);
      GridDataFactory.create(buttonsComposite).alignHR();
      GridLayoutFactory.create(buttonsComposite).columns(2).equalColumns().marginsH(0);
      {
        Button selectButton = new Button(buttonsComposite, SWT.NONE);
        GridDataFactory.create(selectButton).fillH();
        selectButton.setText(Messages.NoEntryPointComposite_useButton);
        selectButton.setImage(EnvironmentUtils.IS_MAC
            ? null
            : DesignerPlugin.getImage("actions/errors/entry_point.png"));
        selectButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            markSelectedMethod();
          }
        });
      }
      {
        Button switchButton = new Button(buttonsComposite, SWT.NONE);
        GridDataFactory.create(switchButton).fillH();
        switchButton.setText(Messages.NoEntryPointComposite_switchToCodeButton);
        switchButton.setImage(EnvironmentUtils.IS_MAC
            ? null
            : DesignerPlugin.getImage("actions/errors/switch32.png"));
        switchButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            new SwitchAction().run();
          }
        });
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link NoEntryPointError} to display.
   */
  public void setException(NoEntryPointError e) {
    m_editor = e.getEditor();
    m_typeDeclaration = e.getTypeDeclaration();
    {
      int code = ICoreExceptionConstants.PARSER_NO_ROOT_METHODS;
      ErrorEntryInfo entry = DesignerExceptionUtils.getErrorEntry(code);
      m_titleLabel.setText(entry.getTitle());
      m_browser.setText(DesignerExceptionUtils.getWarningHTML(entry));
    }
    try {
      m_viewer.setInput(getPossibleEntryPoints());
      m_viewer.getTable().setSelection(0);
    } catch (Throwable ex) {
      DesignerPlugin.log(ex);
    }
  }

  private List<IMethod> getPossibleEntryPoints() throws Exception {
    String typeName = AstNodeUtils.getFullyQualifiedName(m_typeDeclaration, false);
    IType type = m_editor.getJavaProject().findType(typeName);
    List<IMethod> methods = Lists.newArrayList(type.getMethods());
    sortMethods(methods);
    return methods;
  }

  /**
   * Sort methods so that most interesting methods, such as constructors and "create" methods,
   * become first.
   */
  private static void sortMethods(List<IMethod> methods) {
    Collections.sort(methods, new Comparator<IMethod>() {
      public int compare(IMethod o1, IMethod o2) {
        return getPriority(o1) - getPriority(o2);
      }

      private int getPriority(IMethod method) {
        try {
          if (method.isConstructor()) {
            return 0;
          }
          if (method.getElementName().startsWith("create")) {
            return 1;
          }
        } catch (Throwable e) {
        }
        return 10;
      }
    });
  }

  private void markSelectedMethod() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        markSelectedMethodEx();
      }
    });
  }

  private void markSelectedMethodEx() throws Exception {
    IMethod methodModel = GenericsUtils.first(m_viewer.getSelection());
    String methodSignature = CodeUtils.getMethodSignature(methodModel);
    MethodDeclaration method =
        AstNodeUtils.getMethodBySignature(m_typeDeclaration, methodSignature);
    m_editor.setJavadocTagText(method, "@wbp.parser.entryPoint", "");
    m_editor.commitChanges();
  }
}
