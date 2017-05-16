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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationUtils;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ModelMessages;
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
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.resource.ImageDescriptor;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

/**
 * Helper for morphing {@link JavaInfo} for one component class to another.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.util
 */
public abstract class MorphingSupport<T extends JavaInfo> extends AbstractMorphingSupport<T> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final AstEditor m_editor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected MorphingSupport(String toolkitClassName, T component) {
    super(toolkitClassName, component);
    m_editor = m_component.getEditor();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected IJavaProject getJavaProject() {
    return m_editor.getJavaProject();
  }

  @Override
  protected ClassLoader getClassLoader() {
    return EditorState.get(m_editor).getEditorLoader();
  }

  @Override
  protected Class<?> getComponentClass() {
    return m_component.getDescription().getComponentClass();
  }

  @Override
  protected List<MorphingTargetDescription> getMorphingTargets() {
    return m_component.getDescription().getMorphingTargets();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTargetText(MorphingTargetDescription target) throws Exception {
    return getComponentPresentation(target).getName();
  }

  @Override
  protected ImageDescriptor getTargetImageDescriptor(MorphingTargetDescription target)
      throws Exception {
    ComponentPresentation presentation = getComponentPresentation(target);
    return new ImageImageDescriptor(presentation.getIcon());
  }

  private ComponentPresentation getComponentPresentation(MorphingTargetDescription target)
      throws Exception {
    return ComponentPresentationHelper.getPresentation(
        m_editor,
        target.getComponentClass().getName(),
        target.getCreationId());
  }

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
    // add "morph" actions
    MorphingSupport<JavaInfo> morphingSupport =
        new MorphingSupport<JavaInfo>(toolkitClassName, component) {
        };
    contribute(morphingSupport, manager);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String validate(MorphingTargetDescription target) throws Exception {
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
          return MessageFormat.format(
              ModelMessages.MorphingSupport_validateNoAssociationMethod,
              associationSignature);
        }
        // check that method is association
        boolean hasChild = false;
        List<ParameterDescription> parameters = method.getParameters();
        for (ParameterDescription parameter : parameters) {
          hasChild |= parameter.isChild();
        }
        if (!hasChild) {
          return ModelMessages.MorphingSupport_validateAssociationMethod_1
              + associationSignature
              + ModelMessages.MorphingSupport_validateAssociationMethod_2
              + ModelMessages.MorphingSupport_validateAssociationMethod_3
              + ModelMessages.MorphingSupport_validateAssociationMethod_4;
        }
      }
    }
    // OK
    return super.validate(target);
  }

  @Override
  protected void morph(MorphingTargetDescription target) throws Exception {
    if (m_component.getCreationSupport() instanceof ConstructorCreationSupport
        || m_component.getCreationSupport() instanceof StaticFactoryCreationSupport
        || m_component.getCreationSupport() instanceof InstanceFactoryCreationSupport) {
      super.morph(target);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected T morph_create(MorphingTargetDescription target) throws Exception {
    ComponentDescription newDescription =
        ComponentDescriptionHelper.getDescription(m_editor, target.getComponentClass());
    CreationSupport newCreationSupport = getTargetCreationSupport(target);
    return (T) JavaInfoUtils.createJavaInfo(m_editor, newDescription, newCreationSupport);
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

  @Override
  protected void morph_replace(T newComponent) throws Exception {
    m_component.getBroadcastJava().replaceChildBefore(
        m_component.getParentJava(),
        m_component,
        newComponent);
    // replace component in parent (following operations may require parent)
    m_component.getParent().replaceChild(m_component, newComponent);
  }

  @Override
  protected void morph_properties(T newComponent) throws Exception {
    ComponentDescription newComponentDescription = newComponent.getDescription();
    // move related nodes
    for (ASTNode node : m_component.getRelatedNodes()) {
      // check if method invocation can exist in new component
      if (node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
        MethodInvocation invocation = (MethodInvocation) node.getParent();
        String signature = AstNodeUtils.getMethodSignature(invocation);
        if (newComponentDescription.getMethod(signature) == null) {
          m_editor.removeEnclosingStatement(invocation);
          continue;
        }
      }
      // check if assignment can exist in new component
      if (node.getLocationInParent() == QualifiedName.QUALIFIER_PROPERTY) {
        QualifiedName fieldAccess = (QualifiedName) node.getParent();
        if (fieldAccess.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
          String fieldName = fieldAccess.getName().getIdentifier();
          if (ReflectionUtils.getFieldByName(newComponentDescription.getComponentClass(), fieldName) == null) {
            m_editor.removeEnclosingStatement(node);
            continue;
          }
        }
      }
      // OK, we can add this related node
      newComponent.addRelatedNode(node);
    }
  }

  @Override
  protected void morph_children(T newComponent) throws Exception {
    // move children
    for (JavaInfo javaChild : m_component.getChildrenJava()) {
      newComponent.addChild(javaChild);
    }
  }

  @Override
  protected void morph_source(T newComponent) throws Exception {
    // reuse variable
    {
      // replace type in variable
      {
        ComponentDescription newComponentDescription = newComponent.getDescription();
        AbstractSimpleVariableSupport variable =
            (AbstractSimpleVariableSupport) m_component.getVariableSupport();
        variable.setType(newComponentDescription.getComponentClass().getName());
      }
      // possible new variable was generated, use it
      m_component.getVariableSupport().moveTo(newComponent);
    }
    // replace creation
    {
      CreationSupport oldCreationSupport = m_component.getCreationSupport();
      CreationSupport newCreationSupport = newComponent.getCreationSupport();
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
  }

  @Override
  protected void morph_finish(T newComponent) throws Exception {
    m_component.getBroadcastJava().replaceChildAfter(
        m_component.getParentJava(),
        m_component,
        newComponent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utility access
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String validate(String toolkitClassName,
      JavaInfo component,
      MorphingTargetDescription target) throws Exception {
    MorphingSupport<JavaInfo> morphingSupport =
        new MorphingSupport<JavaInfo>(toolkitClassName, component) {
        };
    return morphingSupport.validate(target);
  }

  public static void morph(String toolkitClassName,
      JavaInfo component,
      MorphingTargetDescription target) throws Exception {
    MorphingSupport<JavaInfo> morphingSupport =
        new MorphingSupport<JavaInfo>(toolkitClassName, component) {
        };
    morphingSupport.morph(target);
  }
}
