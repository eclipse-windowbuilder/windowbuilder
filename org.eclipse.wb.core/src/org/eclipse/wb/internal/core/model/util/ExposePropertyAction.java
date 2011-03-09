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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.AccessorUtils;
import org.eclipse.wb.internal.core.model.property.accessor.IExposableExpressionAccessor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * {@link Action} for exposing {@link JavaInfo} property using <code>getXXX()</code> and
 * <code>setXXX()</code> method in top level class.
 * 
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ExposePropertyAction extends Action {
  private GenericPropertyImpl m_property;
  private IExposableExpressionAccessor m_exposableAccessor;
  private String m_propertyTypeName;
  private JavaInfo m_javaInfo;
  private AstEditor m_editor;
  private TypeDeclaration m_typeDeclaration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExposePropertyAction() {
    setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/expose/exposeProperty.png"));
    setText("Expose property...");
    setToolTipText("Expose property...");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the selected {@link Property} that should be exposed (if possible).
   */
  public void setProperty(Property property) throws Exception {
    m_property = null;
    m_exposableAccessor = null;
    // try to find IExposableExpressionAccessor in GenericPropertyImpl
    if (property instanceof GenericPropertyImpl) {
      m_property = (GenericPropertyImpl) property;
      m_exposableAccessor = AccessorUtils.getExposableExpressionAccessor(property);
    }
    // update state
    if (m_exposableAccessor != null) {
      setEnabled(true);
      m_javaInfo = m_property.getJavaInfo();
      m_editor = m_javaInfo.getEditor();
      m_typeDeclaration = JavaInfoUtils.getTypeDeclaration(m_javaInfo);
      m_propertyTypeName =
          ReflectionUtils.getFullyQualifiedName(
              m_exposableAccessor.getValueClass(m_javaInfo),
              false);
    } else {
      setEnabled(false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void run() {
    final ExposeDialog dialog = new ExposeDialog();
    if (dialog.open() == Window.OK) {
      ExecutionUtils.run(m_javaInfo, new RunnableEx() {
        public void run() throws Exception {
          expose(dialog.isPublic());
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_exposedName;
  private String m_exposedGetter;
  private String m_exposedSetter;
  private String m_exposedSetterParameter;

  /**
   * Sets the currently entered by user name of exposed property.<br>
   * This automatically updates all values that depend on it, and used in both {@link ExposeDialog}
   * and this action.
   */
  private void setExposedName(String exposedName) {
    m_exposedName = exposedName;
    m_exposedGetter = "get" + StringUtils.capitalize(m_exposedName);
    m_exposedSetter = "set" + StringUtils.capitalize(m_exposedName);
    {
      final List<VariableDeclaration> variables =
          AstNodeUtils.getVariableDeclarationsAll(m_editor.getAstUnit());
      m_exposedSetterParameter =
          CodeUtils.generateUniqueName(m_property.getTitle(), new Predicate<String>() {
            public boolean apply(String name) {
              for (VariableDeclaration variable : variables) {
                if (variable.getName().getIdentifier().equals(name)) {
                  return false;
                }
              }
              return true;
            }
          });
    }
  }

  /**
   * Validates the currently entered by user name of exposed property.
   * 
   * @return the error message, or <code>null</code> if given name is valid.
   */
  private String validate(String exposedName) {
    setExposedName(exposedName);
    // check for valid identifier
    {
      IStatus status = JavaConventions.validateIdentifier(m_exposedName);
      if (status.getSeverity() == IStatus.ERROR) {
        return status.getMessage();
      }
    }
    // check for existing getter
    {
      String signature = m_exposedGetter + "()";
      if (AstNodeUtils.getMethodBySignature(m_typeDeclaration, signature) != null) {
        return "Method \"" + signature + "\" already exists.";
      }
    }
    // check for existing setter
    {
      String signature = m_exposedSetter + "(" + m_propertyTypeName + ")";
      if (AstNodeUtils.getMethodBySignature(m_typeDeclaration, signature) != null) {
        return "Method \"" + signature + "\" already exists.";
      }
    }
    // OK
    return null;
  }

  /**
   * Prepares source for preview.
   * 
   * @param isPublic
   *          is <code>true</code> if <code>public</code> modifier should be used and
   *          <code>false</code> for <code>protected</code> modifier.
   */
  private String getPreviewSource(boolean isPublic) throws Exception {
    String modifierSource = isPublic ? "public" : "protected";
    String propertyTypeName = CodeUtils.getShortClass(m_propertyTypeName);
    String accessExpression = m_javaInfo.getVariableSupport().getName() + ".";
    // prepare source
    String source = "";
    {
      source += "...\n";
      // getter
      {
        source +=
            MessageFormat.format(
                "\t{0} {1} {2}() '{'\n",
                modifierSource,
                propertyTypeName,
                m_exposedGetter);
        source +=
            MessageFormat.format(
                "\t\treturn {0}{1};\n",
                accessExpression,
                m_exposableAccessor.getGetterCode(m_javaInfo));
        source += "\t}\n";
      }
      // setter
      {
        source +=
            MessageFormat.format(
                "\t{0} void {1}({2} {3}) '{'\n",
                modifierSource,
                m_exposedSetter,
                propertyTypeName,
                m_exposedSetterParameter);
        source +=
            MessageFormat.format(
                "\t\t{0}{1};\n",
                accessExpression,
                m_exposableAccessor.getSetterCode(m_javaInfo, m_exposedSetterParameter));
        source += "\t}\n";
      }
      // end
      source += "...\n";
    }
    // final result
    return source;
  }

  /**
   * Generates getter/setter for exposing property.
   */
  private void expose(boolean isPublic) throws Exception {
    BodyDeclarationTarget methodTarget = new BodyDeclarationTarget(m_typeDeclaration, false);
    String modifierSource = isPublic ? "public" : "protected";
    // getter
    {
      String header = modifierSource + " " + m_propertyTypeName + " " + m_exposedGetter + "()";
      String body =
          TemplateUtils.resolve(
              methodTarget,
              "return {0}.{1};",
              m_javaInfo,
              m_exposableAccessor.getGetterCode(m_javaInfo));
      m_editor.addMethodDeclaration(header, ImmutableList.of(body), methodTarget);
    }
    // setter
    {
      String header =
          MessageFormat.format(
              "{0} void {1}({2} {3})",
              modifierSource,
              m_exposedSetter,
              m_propertyTypeName,
              m_exposedSetterParameter);
      String body =
          TemplateUtils.resolve(
              methodTarget,
              "{0}.{1};",
              m_javaInfo,
              m_exposableAccessor.getSetterCode(m_javaInfo, m_exposedSetterParameter));
      m_editor.addMethodDeclaration(header, ImmutableList.of(body), methodTarget);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expose dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Dialog for requesting parameters for exposing {@link AbstractComponentInfo}.
   */
  private class ExposeDialog extends AbstractValidationTitleAreaDialog {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ExposeDialog() {
      super(DesignerPlugin.getShell(),
          DesignerPlugin.getDefault(),
          "Expose property",
          "Expose component property as property of top-level class.",
          DesignerPlugin.getImage("actions/expose/expose_banner.gif"),
          "Enter the name of top-level property.");
      setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    private StringDialogField m_nameField;
    private SelectionButtonDialogFieldGroup m_modifierField;
    private SourceViewer m_previewViewer;

    @Override
    protected void createControls(Composite container) {
      m_fieldsContainer = container;
      GridLayoutFactory.create(container).columns(2);
      // name
      {
        m_nameField = new StringDialogField();
        doCreateField(m_nameField, "Property name:");
        // initial name
        String exposedName =
            m_javaInfo.getVariableSupport().getComponentName()
                + StringUtils.capitalize(m_property.getTitle());
        m_nameField.setText(exposedName);
      }
      // modifier
      {
        m_modifierField =
            new SelectionButtonDialogFieldGroup(SWT.RADIO,
                new String[]{"&public", "pro&tected"},
                1,
                SWT.SHADOW_ETCHED_IN);
        doCreateField(m_modifierField, "Modifier");
      }
      // preview
      {
        new Label(container, SWT.NONE).setText("Preview:");
        m_previewViewer = JdtUiUtils.createJavaSourceViewer(container, SWT.BORDER | SWT.V_SCROLL);
        GridDataFactory.create(m_previewViewer.getControl()).spanH(2).hintVC(9).grab().fill();
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String validate() throws Exception {
      // validate
      {
        String message = ExposePropertyAction.this.validate(m_nameField.getText());
        if (message != null) {
          JdtUiUtils.setJavaSourceForViewer(m_previewViewer, "No preview");
          return message;
        }
      }
      // update preview
      {
        boolean isPublic = isPublic();
        JdtUiUtils.setJavaSourceForViewer(m_previewViewer, getPreviewSource(isPublic));
      }
      // OK
      return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
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
      DialogFieldUtils.fillControls(m_fieldsContainer, dialogField, 2, 40);
    }
  }
}
