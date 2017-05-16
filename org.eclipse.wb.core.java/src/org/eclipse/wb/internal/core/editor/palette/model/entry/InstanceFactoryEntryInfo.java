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
package org.eclipse.wb.internal.core.editor.palette.model.entry;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import java.util.List;

/**
 * Implementation of {@link EntryInfo} for "instance-factory-method" contribution.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class InstanceFactoryEntryInfo extends FactoryEntryInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public InstanceFactoryEntryInfo() {
  }

  public InstanceFactoryEntryInfo(CategoryInfo categoryInfo,
      String factoryClassName,
      AttributesProvider attributes) {
    super(categoryInfo, factoryClassName, attributes);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "InstanceFactoryMethod(class='"
        + m_factoryClassName
        + "' signature='"
        + m_methodSignature
        + "')";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isStaticFactory() {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ToolEntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Tool createTool() throws Exception {
    if (!ensureComponentDescription()) {
      return null;
    }
    // prepare instance factory
    final InstanceFactoryInfo factoryInfo;
    {
      factoryInfo = getFactory();
      if (factoryInfo == null) {
        return null;
      }
    }
    // prepare creation factory
    ICreationFactory creationFactory = new ICreationFactory() {
      private JavaInfo m_javaInfo;

      public void activate() throws Exception {
        CreationSupport creationSupport =
            new InstanceFactoryCreationSupport(factoryInfo, m_methodDescription);
        m_javaInfo = createJavaInfo(creationSupport);
        m_javaInfo = JavaInfoUtils.getWrapped(m_javaInfo);
        m_javaInfo.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
      }

      public Object getNewObject() {
        return m_javaInfo;
      }
    };
    // return tool
    return new CreationTool(creationFactory);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the single {@link InstanceFactoryInfo} - existing or newly added.
   */
  private InstanceFactoryInfo getFactory() throws Exception {
    List<InstanceFactoryInfo> factories =
        InstanceFactoryInfo.getFactories(m_rootJavaInfo, m_factoryClass);
    // single factory
    if (factories.size() == 1) {
      return factories.get(0);
    }
    // no factories
    if (factories.size() == 0) {
      final InstanceFactoryInfo[] result = new InstanceFactoryInfo[1];
      ExecutionUtils.run(m_rootJavaInfo, new RunnableEx() {
        public void run() throws Exception {
          result[0] = InstanceFactoryInfo.add(m_rootJavaInfo, m_factoryClass);
        }
      });
      return result[0];
    }
    // more than one factory
    {
      // prepare dialog
      ElementListSelectionDialog dialog;
      {
        Shell shell = DesignerPlugin.getShell();
        dialog = new ElementListSelectionDialog(shell, ObjectsLabelProvider.INSTANCE);
        dialog.setTitle(Messages.InstanceFactoryEntryInfo_selectFactoryTitle);
        dialog.setMessage(Messages.InstanceFactoryEntryInfo_selectFactoryMessage);
      }
      // open dialog
      dialog.setElements(factories.toArray());
      dialog.open();
      return (InstanceFactoryInfo) dialog.getFirstResult();
    }
  }
}
