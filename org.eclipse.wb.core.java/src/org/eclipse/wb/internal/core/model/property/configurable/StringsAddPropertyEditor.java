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
package org.eclipse.wb.internal.core.model.property.configurable;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.JavaProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.dialogs.StringsDialog;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.window.Window;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * {@link PropertyEditor} for editing set of invocations like <code>addItem(text)</code>.
 * <p>
 * For example in GWT <code>ListBox</code> has only <code>addItem(String)</code> methods, and no
 * method to set all items as single invocation, like <code>setItems(String[])</code>. So, to edit
 * items, we need some artificial {@link Property} with this editor.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public final class StringsAddPropertyEditor extends TextDialogPropertyEditor
    implements
      IConfigurablePropertyObject {
  private String m_addMethodSignature;
  private String[] m_removeMethodsSignatures;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    String[] items = getItems(property);
    return "[" + StringUtils.join(items, ",") + "]";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Items
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the items specified in value of given {@link Property}.
   */
  String[] getItems(Property _property) throws Exception {
    JavaInfo javaInfo = ((JavaProperty) _property).getJavaInfo();
    // prepare "add" invocations
    List<MethodInvocation> invocations;
    {
      invocations = javaInfo.getMethodInvocations(m_addMethodSignature);
      ExecutionFlowDescription flowDescription =
          EditorState.get(javaInfo.getEditor()).getFlowDescription();
      JavaInfoUtils.sortNodesByFlow(flowDescription, false, invocations);
    }
    // fill items
    List<String> items = Lists.newArrayList();
    for (MethodInvocation invocation : invocations) {
      Expression itemExpression = DomGenerics.arguments(invocation).get(0);
      String item = (String) JavaInfoEvaluationHelper.getValue(itemExpression);
      items.add(item);
    }
    // return as array
    return items.toArray(new String[items.size()]);
  }

  /**
   * Sets new items into given {@link Property}.
   */
  void setItems(Property _property, final String[] items) throws Exception {
    final JavaInfo javaInfo = ((JavaProperty) _property).getJavaInfo();
    ExecutionUtils.run(javaInfo, new RunnableEx() {
      public void run() throws Exception {
        setItems0(items, javaInfo);
      }
    });
  }

  private void setItems0(String[] items, JavaInfo javaInfo) throws Exception {
    // remove existing invocations
    javaInfo.removeMethodInvocations(m_addMethodSignature);
    for (String removeMethodSignature : m_removeMethodsSignatures) {
      javaInfo.removeMethodInvocations(removeMethodSignature);
    }
    // add "add" invocations
    for (int i = items.length - 1; i >= 0; i--) {
      String item = items[i];
      String itemSource = StringConverter.INSTANCE.toJavaSource(javaInfo, item);
      javaInfo.addMethodInvocation(m_addMethodSignature, itemSource);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    StringsDialog itemsDialog =
        new StringsDialog(DesignerPlugin.getShell(),
            DesignerPlugin.getDefault(),
            property.getTitle(),
            "String items:",
            "Each line in the above text field represents a single item.");
    itemsDialog.setItems(getItems(property));
    // open dialog
    if (itemsDialog.open() == Window.OK) {
      String[] items = itemsDialog.getItems();
      setItems(property, items);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
    {
      String addMethodName = (String) parameters.get("addMethod");
      Assert.isNotNull(addMethodName, "No 'addMethod' parameter in %s.", parameters);
      m_addMethodSignature = addMethodName + "(java.lang.String)";
    }
    {
      String removeMethodsString = (String) parameters.get("removeMethods");
      if (removeMethodsString != null) {
        m_removeMethodsSignatures = StringUtils.split(removeMethodsString);
      } else {
        m_removeMethodsSignatures = ArrayUtils.EMPTY_STRING_ARRAY;
      }
    }
  }
}
