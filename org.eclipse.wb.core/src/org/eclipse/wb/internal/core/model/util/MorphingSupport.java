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

import com.google.common.base.Function;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationUtils;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ComponentPresentation;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentPresentationHelper;
import org.eclipse.wb.internal.core.model.variable.AbstractSimpleVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.SubtypesScope;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;
import org.eclipse.wb.internal.core.utils.ui.MenuManagerEx;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import java.util.List;
import java.util.Set;

/**
 * Helper for morphing {@link JavaInfo} for one component class to another.
 * 
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class MorphingSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Contribution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If possible, contributes "morph" actions.
   * 
   * @param toolkitClassName
   *          the name of base class for "Other..." action, for example
   *          <code>"org.eclipse.swt.widgets.Control"</code> as SWT.
   * @param component
   *          the {@link JavaInfo} that should be morphed.
   * @param manager
   *          the {@link IContributionManager} to add action to.
   */
  public static void contribute(String toolkitClassName,
      JavaInfo component,
      IContributionManager manager) throws Exception {
    // check for supported variable type
    if (!(component.getVariableSupport() instanceof AbstractSimpleVariableSupport)) {
      return;
    }
    // add "Morph" sub-menu
    MenuManagerEx morphManager;
    {
      morphManager = new MenuManagerEx("Morph");
      morphManager.setImage(DesignerPlugin.getImage("actions/morph/morph2.png"));
      manager.appendToGroup(IContextMenuConstants.GROUP_INHERITANCE, morphManager);
    }
    // add "morph" actions
    new MorphingSupport(toolkitClassName, component).contribute(morphManager);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_toolkitClassName;
  private final JavaInfo m_component;
  private final AstEditor m_editor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private MorphingSupport(String toolkitClassName, JavaInfo component) {
    m_toolkitClassName = toolkitClassName;
    m_component = component;
    m_editor = m_component.getEditor();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contribution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes "morph" actions.
   * 
   * @param manager
   *          the {@link IContributionManager} to add action to.
   */
  private void contribute(IContributionManager morphManager) throws Exception {
    // add known morphing targets
    for (MorphingTargetDescription target : m_component.getDescription().getMorphingTargets()) {
      morphManager.add(new MorphTargetAction(target));
    }
    // add special actions
    morphManager.add(new Separator());
    {
      String baseClassName = m_component.getDescription().getComponentClass().getName();
      MorphSubclassAction action = new MorphSubclassAction(baseClassName);
      action.setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/morph/subclass.gif"));
      action.setText("&Subclass...");
      morphManager.add(action);
    }
    {
      MorphSubclassAction action = new MorphSubclassAction(m_toolkitClassName);
      action.setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/morph/other.gif"));
      action.setText("&Other...");
      morphManager.add(action);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utility access
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void morph(String toolkitClassName,
      JavaInfo component,
      MorphingTargetDescription target) throws Exception {
    new MorphingSupport(toolkitClassName, component).morph(target);
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
  private String validate(MorphingTargetDescription target) throws Exception {
    // prepare signatures of methods used for children association
    Set<String> associationSignatures = Sets.newHashSet();
    for (JavaInfo child : m_component.getChildrenJava()) {
      if (child.getAssociation() instanceof InvocationChildAssociation) {
        InvocationChildAssociation association =
            (InvocationChildAssociation) child.getAssociation();
        String signature = association.getDescription().getSignature();
        associationSignatures.add(signature);
      }
    }
    // check that target has methods used for associations
    {
      Class<?> targetClass = target.getComponentClass();
      ComponentDescription targetDescription =
          ComponentDescriptionHelper.getDescription(m_editor, targetClass);
      for (String associationSignature : associationSignatures) {
        MethodDescription method = targetDescription.getMethod(associationSignature);
        // check that method exists
        if (method == null) {
          return "Source container uses "
              + associationSignature
              + " for children association."
              + " But target component does not have such method."
              + " Morphing must be done manually in the Source view.";
        }
        // check that method is association
        boolean hasChild = false;
        List<ParameterDescription> parameters = method.getParameters();
        for (ParameterDescription parameter : parameters) {
          hasChild |= parameter.isChild();
        }
        if (!hasChild) {
          return "Source container uses "
              + associationSignature
              + " for children association."
              + " But target component does not use this method for association."
              + " Morphing must be done manually in the Source view.";
        }
      }
    }
    // OK
    return null;
  }

  /**
   * Performs morphing to given {@link MorphingTargetDescription}.
   */
  private void morph(MorphingTargetDescription target) throws Exception {
    if (m_component.getCreationSupport() instanceof ConstructorCreationSupport
        || m_component.getCreationSupport() instanceof StaticFactoryCreationSupport
        || m_component.getCreationSupport() instanceof InstanceFactoryCreationSupport) {
      ComponentDescription newDescription =
          ComponentDescriptionHelper.getDescription(m_editor, target.getComponentClass());
      // prepare new component
      CreationSupport newCreationSupport;
      JavaInfo newComponent;
      {
        newCreationSupport = getTargetCreationSupport(target);
        newComponent = JavaInfoUtils.createJavaInfo(m_editor, newDescription, newCreationSupport);
      }
      //
      m_component.getBroadcastJava().replaceChildBefore(
          m_component.getParentJava(),
          m_component,
          newComponent);
      // replace component in parent (following operations may require parent)
      m_component.getParent().replaceChild(m_component, newComponent);
      // move related nodes
      for (ASTNode node : m_component.getRelatedNodes()) {
        // check if method invocation can exist in new component
        if (node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
          MethodInvocation invocation = (MethodInvocation) node.getParent();
          String signature = AstNodeUtils.getMethodSignature(invocation);
          if (newDescription.getMethod(signature) == null) {
            m_editor.removeEnclosingStatement(invocation);
            continue;
          }
        }
        // check if assignment can exist in new component
        if (node.getLocationInParent() == QualifiedName.QUALIFIER_PROPERTY) {
          QualifiedName fieldAccess = (QualifiedName) node.getParent();
          if (fieldAccess.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
            String fieldName = fieldAccess.getName().getIdentifier();
            if (ReflectionUtils.getFieldByName(target.getComponentClass(), fieldName) == null) {
              m_editor.removeEnclosingStatement(node);
              continue;
            }
          }
        }
        // OK, we can add this related node
        newComponent.addRelatedNode(node);
      }
      // move children
      for (JavaInfo javaChild : m_component.getChildrenJava()) {
        newComponent.addChild(javaChild);
      }
      // reuse variable
      {
        // replace type in variable
        {
          AbstractSimpleVariableSupport variable =
              (AbstractSimpleVariableSupport) m_component.getVariableSupport();
          variable.setType(target.getComponentClass().getName());
        }
        // possible new variable was generated, use it
        m_component.getVariableSupport().moveTo(newComponent);
      }
      // replace creation
      {
        CreationSupport oldCreationSupport = m_component.getCreationSupport();
        Expression oldCreationExpression = (Expression) oldCreationSupport.getNode();
        StatementTarget statementTarget =
            new StatementTarget(AstNodeUtils.getEnclosingStatement(oldCreationExpression), true);
        // prepare new creation expression
        Expression newCreationExpression;
        {
          String source = newCreationSupport.add_getSource(null);
          source = AssociationUtils.replaceTemplates(newComponent, source, statementTarget);
          newCreationExpression = m_editor.replaceExpression(oldCreationExpression, source);
        }
        // set new creation expression
        newCreationSupport.add_setSourceExpression(newCreationExpression);
        newComponent.addRelatedNode(newCreationExpression);
      }
      // set association
      {
        Association oldAssociation = m_component.getAssociation();
        Association newAssociation = oldAssociation.getCopy();
        newComponent.setAssociation(newAssociation);
      }
      //
      m_component.getBroadcastJava().replaceChildAfter(
          m_component.getParentJava(),
          m_component,
          newComponent);
    }
  }

  private ConstructorCreationSupport getTargetCreationSupport(MorphingTargetDescription target) {
    // try constructor matching
    if (target.getCreationId() == null) {
      CreationSupport creation = m_component.getCreationSupport();
      if (creation instanceof ConstructorCreationSupport) {
        ConstructorCreationSupport constructorCreation = (ConstructorCreationSupport) creation;
        String signature = constructorCreation.getDescription().getSignature();
        final Class<?> targetClass = target.getComponentClass();
        if (ReflectionUtils.getConstructorBySignature(targetClass, signature) != null) {
          final ClassInstanceCreation creationNode = constructorCreation.getCreation();
          String source = m_editor.getExternalSource(creationNode, new Function<ASTNode, String>() {
            public String apply(ASTNode from) {
              if (from == creationNode.getType()) {
                return targetClass.getName();
              }
              return null;
            }
          });
          return ConstructorCreationSupport.forSource(source);
        }
      }
    }
    // use specified creation id
    return new ConstructorCreationSupport(target.getCreationId(), false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MorphTargetAction
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Abstract {@link Action} for morphing component.
   */
  private abstract class MorphAction extends Action {
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
      if (obj instanceof MorphTargetAction) {
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
            MessageDialog.openError(DesignerPlugin.getShell(), "Incompatible morph target", message);
            return;
          }
        }
      } catch (Throwable e) {
        DesignerPlugin.log(e);
        return;
      }
      // do morph
      ExecutionUtils.run(m_component.getRootJava(), new RunnableEx() {
        public void run() throws Exception {
          morph(target);
        }
      });
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
  private class MorphTargetAction extends MorphAction {
    private final MorphingTargetDescription m_target;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MorphTargetAction(MorphingTargetDescription target) throws Exception {
      m_target = target;
      ComponentPresentation presentation =
          ComponentPresentationHelper.getPresentation(
              m_editor,
              target.getComponentClass().getName(),
              target.getCreationId());
      setImageDescriptor(new ImageImageDescriptor(presentation.getIcon()));
      setText(presentation.getName());
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
  private class MorphSubclassAction extends MorphAction {
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
        IJavaProject project = m_editor.getJavaProject();
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
        dialog.setTitle("Open type");
        dialog.setMessage("Select a type (? = any character, * - any String):");
      }
      // open dialog
      if (dialog.open() == Window.OK) {
        IType type = (IType) dialog.getResult()[0];
        String typeName = type.getFullyQualifiedName();
        Class<?> targetClass = EditorState.get(m_editor).getEditorLoader().loadClass(typeName);
        return new MorphingTargetDescription(targetClass, null);
      }
      // no target
      return null;
    }
  }
}
