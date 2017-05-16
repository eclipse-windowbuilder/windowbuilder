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
package org.eclipse.wb.internal.core.model.generic;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationObjectFactories;
import org.eclipse.wb.core.model.association.AssociationObjectFactory;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.util.predicate.ExpressionPredicate;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Factory for accessing {@link FlowContainer} for {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public final class FlowContainerFactory {
  private final JavaInfo m_javaInfo;
  private final boolean m_forCanvas;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FlowContainerFactory(JavaInfo javaInfo, boolean forCanvas) {
    m_javaInfo = javaInfo;
    m_forCanvas = forCanvas;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<FlowContainer> get() {
    List<FlowContainer> containers = Lists.newArrayList();
    addConfigurableContainers(containers);
    return containers;
  }

  private void addConfigurableContainers(List<FlowContainer> containers) {
    List<FlowContainerConfiguration> configurations = getConfigurations();
    for (FlowContainerConfiguration configuration : configurations) {
      FlowContainer container = new FlowContainerConfigurable(m_javaInfo, configuration);
      containers.add(container);
    }
  }

  public List<FlowContainerConfiguration> getConfigurations() {
    return ExecutionUtils.runObject(new RunnableObjectEx<List<FlowContainerConfiguration>>() {
      public List<FlowContainerConfiguration> runObject() throws Exception {
        return getConfigurationsEx();
      }
    }, "Exception during reading flow container configurations for %s", m_javaInfo);
  }

  private List<FlowContainerConfiguration> getConfigurationsEx() {
    List<FlowContainerConfiguration> configurations = Lists.newArrayList();
    for (String prefix : getConfigurationPrefixes()) {
      FlowContainerConfiguration configuration = createConfiguration(prefix);
      configurations.add(configuration);
    }
    return configurations;
  }

  private List<String> getConfigurationPrefixes() {
    List<String> prefixes = Lists.newArrayList();
    addConfigurationPrefixes(prefixes, "flowContainer");
    if (m_forCanvas) {
      addConfigurationPrefixes(prefixes, "flowContainer.canvas");
    } else {
      addConfigurationPrefixes(prefixes, "flowContainer.tree");
    }
    return prefixes;
  }

  private void addConfigurationPrefixes(List<String> prefixes, String basePrefix) {
    for (int i = 0; i < 10; i++) {
      String prefix = basePrefix + (i == 0 ? "" : "." + i);
      if ("true".equals(getParameter(prefix))) {
        prefixes.add(prefix);
      }
    }
  }

  private FlowContainerConfiguration createConfiguration(String prefix) {
    return new FlowContainerConfiguration(getHorizontalPredicate(prefix, true),
        getRtlPredicate(prefix, true),
        getAssociation(prefix),
        getComponentValidator(prefix),
        getReferenceValidator(prefix),
        getGroupName(prefix));
  }

  private ContainerObjectValidator getComponentValidator(String prefix) {
    // try to find "component-validator"
    {
      String validatorExpression = getParameter(prefix + ".component-validator");
      if (validatorExpression != null) {
        return ContainerObjectValidators.forComponentExpression(validatorExpression);
      }
    }
    // component should be list of types
    {
      String componentString = getComponentString(prefix);
      Assert.isNotNull(componentString, "No 'component' validator.");
      String[] componentTypes = StringUtils.split(componentString);
      return ContainerObjectValidators.forList(componentTypes);
    }
  }

  private String getComponentString(String prefix) {
    String componentString = getParameter(prefix + ".component");
    // if no "component", try to get from "defaultComponent"
    if (componentString == null) {
      componentString = getParameter("flowContainer.defaultComponent");
    }
    // return what we have
    return componentString;
  }

  private ContainerObjectValidator getReferenceValidator(String prefix) {
    // try to find "reference-validator"
    {
      String validatorExpression = getParameter(prefix + ".reference-validator");
      if (validatorExpression != null) {
        return ContainerObjectValidators.forReferenceExpression(validatorExpression);
      }
    }
    // reference should be list of types
    {
      String referenceString = getReferenceString(prefix);
      Assert.isNotNull(referenceString, "No 'reference' validator.");
      String[] referenceTypes = StringUtils.split(referenceString);
      return ContainerObjectValidators.forList(referenceTypes);
    }
  }

  private String getReferenceString(String prefix) {
    String referenceString = getParameter(prefix + ".reference");
    // if no specific "reference" for this container, use same as "component"
    if (referenceString == null) {
      referenceString = getParameter(prefix + ".component");
    }
    // try to get from "defaultReference"
    if (referenceString == null) {
      referenceString = getParameter("flowContainer.defaultReference");
    }
    // return what we have
    return referenceString;
  }

  private Predicate<Object> getHorizontalPredicate(String prefix, boolean def) {
    String horizontalString = getParameter(prefix + ".horizontal");
    if (horizontalString == null) {
      return Predicates.alwaysTrue();
    }
    return new ExpressionPredicate<Object>(horizontalString);
  }

  private Predicate<Object> getRtlPredicate(String prefix, boolean def) {
    String rtlString = getParameter(prefix + ".rtl");
    if (rtlString == null) {
      return Predicates.alwaysFalse();
    }
    return new ExpressionPredicate<Object>(rtlString);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Association
  //
  ////////////////////////////////////////////////////////////////////////////
  private AssociationObjectFactory getAssociation(String prefix) {
    String associationString = getParameter(prefix + ".association");
    if (associationString == null) {
      return AssociationObjectFactories.no();
    }
    return getAssociation_invocationChild(associationString);
  }

  private static AssociationObjectFactory getAssociation_invocationChild(String associationString) {
    associationString = StringUtils.removeStart(associationString, "invocationChild ");
    Assert.isTrue(
        associationString.startsWith("%parent%."),
        "Association 'invocationChild' should start with %%parent%%., but '%s' found.",
        associationString);
    return AssociationObjectFactories.invocationChild(associationString, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private String getGroupName(String prefix) {
    return getParameter(prefix + ".group");
  }

  private String getParameter(String name) {
    return JavaInfoUtils.getParameter(m_javaInfo, name);
  }
}
