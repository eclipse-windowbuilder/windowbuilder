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

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.SubtypesScope;
import org.eclipse.wb.internal.core.utils.ui.MenuManagerEx;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import java.util.List;

/**
 * Helper for morphing for one component class to another.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.util
 */
public abstract class AbstractMorphingSupport<T extends ObjectInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final String m_toolkitClassName;
  protected final T m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected AbstractMorphingSupport(String toolkitClassName, T component) {
    m_toolkitClassName = toolkitClassName;
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract IJavaProject getJavaProject();

  protected abstract ClassLoader getClassLoader();

  protected abstract Class<?> getComponentClass();

  protected abstract List<MorphingTargetDescription> getMorphingTargets();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contribution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add "Morph" sub-menu to context {@link IContributionManager}.
   *
   * @return the "Morph" sub-menu manager.
   */
  protected static MenuManagerEx createMorphManager(IContributionManager manager) {
    MenuManagerEx morphManager = new MenuManagerEx(ModelMessages.MorphingSupport_managerText);
    morphManager.setImage(DesignerPlugin.getImage("actions/morph/morph2.png"));
    manager.appendToGroup(IContextMenuConstants.GROUP_INHERITANCE, morphManager);
    return morphManager;
  }

  /**
   * Contributes "morph" actions.
   *
   * @param morphingSupport
   *          the {@link AbstractMorphingSupport<T>} that should be contributed.
   * @param manager
   *          the {@link IContributionManager} to add action to.
   */
  public static <T extends ObjectInfo> void contribute(AbstractMorphingSupport<T> morphingSupport,
      IContributionManager manager) throws Exception {
    MenuManagerEx morphManager = createMorphManager(manager);
    // add known morphing targets
    for (MorphingTargetDescription target : morphingSupport.getMorphingTargets()) {
      morphManager.add(morphingSupport.new MorphTargetAction(target));
    }
    // add special actions
    morphManager.add(new Separator());
    {
      String baseClassName = morphingSupport.getComponentClass().getName();
      Action action = morphingSupport.new MorphSubclassAction(baseClassName);
      action.setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/morph/subclass.gif"));
      action.setText(ModelMessages.MorphingSupport_subclassAction);
      morphManager.add(action);
    }
    {
      Action action = morphingSupport.new MorphSubclassAction(morphingSupport.m_toolkitClassName);
      action.setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/morph/other.gif"));
      action.setText(ModelMessages.MorphingSupport_otherAction);
      morphManager.add(action);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Validates if given {@link MorphingTargetDescription} can be used.
   *
   * @return the error message or <code>null</code>.
   */
  protected String validate(MorphingTargetDescription target) throws Exception {
    return null;
  }

  /**
   * Performs morphing to given {@link MorphingTargetDescription}.
   */
  protected void morph(MorphingTargetDescription target) throws Exception {
    T newComponent = morph_create(target);
    morph_replace(newComponent);
    morph_properties(newComponent);
    morph_children(newComponent);
    morph_source(newComponent);
    morph_finish(newComponent);
  }

  protected abstract T morph_create(MorphingTargetDescription target) throws Exception;

  protected abstract void morph_replace(T newComponent) throws Exception;

  protected abstract void morph_properties(T newComponent) throws Exception;

  protected abstract void morph_children(T newComponent) throws Exception;

  protected abstract void morph_source(T newComponent) throws Exception;

  protected abstract void morph_finish(T newComponent) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract ImageDescriptor getTargetImageDescriptor(MorphingTargetDescription target)
      throws Exception;

  protected abstract String getTargetText(MorphingTargetDescription target) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // MorphTargetAction
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Abstract {@link Action} for morphing component.
   */
  protected abstract class MorphAction extends Action {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Object - make "singleton"
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public final int hashCode() {
      return 0;
    }

    @Override
    public final boolean equals(Object obj) {
      if (obj instanceof AbstractMorphingSupport.MorphTargetAction) {
        return true;
      }
      return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public final void run() {
      // prepare target
      final MorphingTargetDescription target;
      try {
        target = getTarget();
        // no target
        if (target == null) {
          return;
        }
        // validate
        {
          String message = validate(target);
          if (message != null) {
            MessageDialog.openError(
                DesignerPlugin.getShell(),
                ModelMessages.MorphingSupport_incompatibleTargetTitle,
                message);
            return;
          }
        }
      } catch (Throwable e) {
        DesignerPlugin.log(e);
        return;
      }
      // do morph
      {
        RunnableEx runnable = new RunnableEx() {
          public void run() throws Exception {
            morph(target);
          }
        };
        ObjectInfo rootObject = m_component.getRoot();
        if (m_component != rootObject) {
          ExecutionUtils.run(rootObject, runnable);
        } else {
          ExecutionUtils.runLog(runnable);
        }
      }
    }

    /**
     * @return the target to morph to, or <code>null</code> if user canceled selection.
     */
    protected abstract MorphingTargetDescription getTarget() throws Exception;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // MorphTargetAction
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Action} for morphing component into existing {@link MorphingTargetDescription}.
   */
  protected class MorphTargetAction extends MorphAction {
    private final MorphingTargetDescription m_target;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MorphTargetAction(MorphingTargetDescription target) throws Exception {
      m_target = target;
      setImageDescriptor(getTargetImageDescriptor(target));
      setText(getTargetText(target));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected MorphingTargetDescription getTarget() throws Exception {
      return m_target;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // MorphSubclassAction
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Action} for morphing component into user selected subclass.
   */
  protected class MorphSubclassAction extends MorphAction {
    private final String m_baseClassName;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MorphSubclassAction(String baseClassName) {
      m_baseClassName = baseClassName;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected MorphingTargetDescription getTarget() throws Exception {
      // prepare scope
      IJavaSearchScope scope;
      {
        IJavaProject project = getJavaProject();
        IType componentType = project.findType(m_baseClassName);
        scope = new SubtypesScope(componentType);
      }
      // prepare dialog
      SelectionDialog dialog;
      {
        Shell shell = DesignerPlugin.getShell();
        ProgressMonitorDialog context = new ProgressMonitorDialog(shell);
        dialog =
            JavaUI.createTypeDialog(
                shell,
                context,
                scope,
                IJavaElementSearchConstants.CONSIDER_CLASSES,
                false);
        dialog.setTitle(ModelMessages.MorphingSupport_chooseTitle);
        dialog.setMessage(ModelMessages.MorphingSupport_chooseMessage);
      }
      // open dialog
      if (dialog.open() == Window.OK) {
        IType type = (IType) dialog.getResult()[0];
        String typeName = type.getFullyQualifiedName();
        Class<?> targetClass = getClassLoader().loadClass(typeName);
        return new MorphingTargetDescription(targetClass, null);
      }
      // no target
      return null;
    }
  }
}
