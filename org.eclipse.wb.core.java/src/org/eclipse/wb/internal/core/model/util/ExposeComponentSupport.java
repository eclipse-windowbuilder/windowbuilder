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
package org.eclipse.wb.internal.core.model.util;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.variable.AbstractSimpleVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * Helper that can contributes action for exposing {@link JavaInfo} using <code>getXXX()</code>
 * method.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ExposeComponentSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Contribution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If possible, contributes "expose" action.
   *
   * @param component
   *          the {@link JavaInfo} that should be exposed.
   * @param manager
   *          the {@link IContributionManager} to add action to.
   * @param text
   *          the text to display in action.
   */
  public static void contribute(JavaInfo component, IContributionManager manager, String text) {
    // check for supported variable type
    VariableSupport variableSupport = component.getVariableSupport();
    if (!(variableSupport instanceof AbstractSimpleVariableSupport)) {
      return;
    }
    // check, may be there is already method that returns this component
    {
      AstEditor editor = component.getEditor();
      TypeDeclaration typeDeclaration = getTypeDeclaration(component);
      for (MethodDeclaration method : typeDeclaration.getMethods()) {
        List<Statement> statements = DomGenerics.statements(method);
        if (statements.size() == 1 && statements.get(0) instanceof ReturnStatement) {
          ReturnStatement returnStatement = (ReturnStatement) statements.get(0);
          // check for "return fieldName;"
          if (variableSupport.hasName()) {
            String name = variableSupport.getName();
            Expression expression = returnStatement.getExpression();
            if (expression != null && editor.getSource(expression).equals(name)) {
              return;
            }
          }
        }
      }
    }
    // OK, we can add expose action
    manager.appendToGroup(
        IContextMenuConstants.GROUP_INHERITANCE,
        new ExposeAction(component, text));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ///////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TypeDeclaration} that contains given {@link JavaInfo} creation.
   */
  private static TypeDeclaration getTypeDeclaration(JavaInfo component) {
    AstEditor editor = component.getEditor();
    return EditorState.get(editor).getFlowDescription().geTypeDeclaration();
  }

  /**
   * Exposes component using <code>getXXX()</code> method.
   *
   * @param component
   *          the component to expose.
   * @param methodName
   *          the name of method that should return component.
   * @param modifier
   *          the modifier source, such as <code>"public"</code> or <code>"protected"</code>.
   */
  private static void expose(JavaInfo component, String methodName, String modifier)
      throws Exception {
    AstEditor editor = component.getEditor();
    TypeDeclaration typeDeclaration = getTypeDeclaration(component);
    BodyDeclarationTarget methodTarget = new BodyDeclarationTarget(typeDeclaration, false);
    // add method
    String headerSource =
        MessageFormat.format(
            "{0} {1} {2}()",
            modifier,
            component.getDescription().getComponentClass().getName(),
            methodName);
    String returnSource = TemplateUtils.resolve(methodTarget, "return {0};", component);
    editor.addMethodDeclaration(headerSource, ImmutableList.of(returnSource), methodTarget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expose action
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Action} for exposing {@link JavaInfo}.
   */
  private static final class ExposeAction extends Action {
    private final JavaInfo m_component;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ExposeAction(JavaInfo component, String text) {
      m_component = component;
      setText(text);
      setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/expose/exposeComponent.png"));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void run() {
      final ExposeDialog dialog = new ExposeDialog(m_component);
      if (dialog.open() == Window.OK) {
        ExecutionUtils.run(m_component, new RunnableEx() {
          public void run() throws Exception {
            expose(m_component, dialog.getName(), dialog.isPublic() ? "public" : "protected");
          }
        });
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Expose dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Dialog for requesting parameters for exposing {@link JavaInfo}.
   */
  private static class ExposeDialog extends AbstractValidationTitleAreaDialog {
    private final JavaInfo m_component;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ExposeDialog(JavaInfo component) {
      super(DesignerPlugin.getShell(),
          DesignerPlugin.getDefault(),
          ModelMessages.ExposeComponentSupport_shellTitle,
          ModelMessages.ExposeComponentSupport_title,
          DesignerPlugin.getImage("actions/expose/expose_banner.gif"),
          ModelMessages.ExposeComponentSupport_message);
      m_component = component;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    private StringDialogField m_nameField;
    private SelectionButtonDialogFieldGroup m_modifierField;

    @Override
    protected void createControls(Composite container) {
      m_fieldsContainer = container;
      GridLayoutFactory.create(container).columns(2);
      // name
      {
        m_nameField = new StringDialogField();
        doCreateField(m_nameField, ModelMessages.ExposeComponentSupport_methodLabel);
        {
          String componentName = m_component.getVariableSupport().getComponentName();
          m_nameField.setText("get" + StringUtils.capitalize(componentName));
        }
      }
      // modifier
      {
        m_modifierField =
            new SelectionButtonDialogFieldGroup(SWT.RADIO,
                new String[]{"&public", "pro&tected"},
                1,
                SWT.SHADOW_ETCHED_IN);
        doCreateField(m_modifierField, ModelMessages.ExposeComponentSupport_modifierLabel);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String validate() {
      {
        String methodName = m_nameField.getText();
        String methodSignature = methodName + "()";
        if (AstNodeUtils.getMethodBySignature(getTypeDeclaration(m_component), methodSignature) != null) {
          return MessageFormat.format(
              ModelMessages.ExposeComponentSupport_methodAlreadyExists,
              methodSignature);
        }
      }
      // OK
      return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public String getName() {
      return m_nameField.getText();
    }

    public boolean isPublic() {
      return m_modifierField.getSelection()[0] == 0;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Utils
    //
    ////////////////////////////////////////////////////////////////////////////
    private Composite m_fieldsContainer;

    /**
     * Configures given {@link DialogField} for specific of this dialog.
     */
    protected final void doCreateField(DialogField dialogField, String labelText) {
      dialogField.setLabelText(labelText);
      dialogField.setDialogFieldListener(m_validateListener);
      DialogFieldUtils.fillControls(m_fieldsContainer, dialogField, 2, 60);
    }
  }
}
