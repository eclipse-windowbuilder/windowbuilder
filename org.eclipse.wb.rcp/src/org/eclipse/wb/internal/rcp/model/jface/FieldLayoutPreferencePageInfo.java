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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.core.controls.jface.preference.FieldLayoutPreferencePage;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyInvocationVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.support.ContainerSupport;

import org.apache.commons.lang.StringUtils;

/**
 * Model for {@link FieldLayoutPreferencePage}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class FieldLayoutPreferencePageInfo extends PreferencePageInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldLayoutPreferencePageInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // don't show FieldEditor's on design canvas (simplify layout of container Composite's)
    addBroadcastListener(new ObjectInfoChildGraphical() {
      public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
        if (object instanceof FieldEditorInfo) {
          visible[0] = false;
        }
      }
    });
    // convert FieldEditor's into block
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void variable_emptyMaterializeBefore(EmptyVariableSupport variableSupport)
          throws Exception {
        if (variableSupport.getJavaInfo() instanceof FieldEditorInfo) {
          FieldEditorInfo fieldEditor = (FieldEditorInfo) variableSupport.getJavaInfo();
          FieldEditorPreferencePageInfo.convertStatementToBlock(fieldEditor);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is invoked when we try to drop {@link FieldEditorInfo} on some container that
   * requires {@link CompositeInfo}. So, we should return {@link CompositeInfo} that will later add
   * {@link FieldEditorInfo} on itself.
   * 
   * @return the {@link CompositeInfo} wrapper for {@link FieldEditorInfo}.
   */
  public CompositeInfo schedule_CREATE(final FieldEditorInfo editor) throws Exception {
    CompositeInfo composite = (CompositeInfo) editor.getArbitraryValue(CompositeInfo.class);
    if (composite == null) {
      composite =
          (CompositeInfo) JavaInfoUtils.createJavaInfo(
              getEditor(),
              ContainerSupport.getCompositeClass(),
              new ConstructorCreationSupport());
      composite.markNoLayout();
      editor.putArbitraryValue(CompositeInfo.class, composite);
      // schedule FieldEditor creation
      final CompositeInfo wrapperComposite = composite;
      addBroadcastListener(new JavaEventListener() {
        @Override
        public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
          if (child == wrapperComposite) {
            JavaInfoUtils.add(
                editor,
                new EmptyInvocationVariableSupport(editor, "addField(%child%)", 0),
                PureFlatStatementGenerator.INSTANCE,
                AssociationObjects.empty(),
                wrapperComposite,
                null);
            removeBroadcastListener(this);
          }
        }

        @Override
        public void associationTemplate(JavaInfo component, String[] source) throws Exception {
          if (component == editor) {
            source[0] =
                StringUtils.replace(
                    source[0],
                    "%parentComposite%",
                    TemplateUtils.getExpression(wrapperComposite));
          }
        }
      });
    }
    return composite;
  }
}
