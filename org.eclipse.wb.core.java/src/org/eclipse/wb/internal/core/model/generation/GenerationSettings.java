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
package org.eclipse.wb.internal.core.model.generation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreview;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.VariableSupportDescription;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Code generation settings for adding new {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation
 */
public final class GenerationSettings {
  public static final String P_DEDUCE_SETTINGS = "codeGeneration.deduceSettings";
  public static final String P_FORCED_METHOD = "codeGeneration.forcedMethod";
  private static final String P_VARIABLE_SUPPORT_ID = "codeGeneration.variableId";
  private static final String P_STATEMENT_GENERATOR_ID = "codeGeneration.statementId";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IPreferenceStore m_store;
  private final Map<String, VariableSupportDescription> m_idToVariable = Maps.newTreeMap();
  private final Map<String, StatementGeneratorDescription> m_idToStatement = Maps.newTreeMap();
  private final List<VariableSupportDescription> m_variables = Lists.newArrayList();
  private final Map<VariableSupportDescription, StatementGeneratorDescription[]> m_variableToStatements =
      Maps.newHashMap();
  private final MultiKeyMap/*<variable + statement -> GenerationPreview>*/m_previewMap =
      new MultiKeyMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenerationSettings(IPreferenceStore store) {
    m_store = store;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuring
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link VariableSupportDescription} supported for this toolkit and compatible
   * {@link StatementGeneratorDescription}'s.
   *
   * @param variable
   *          the {@link VariableSupportDescription}
   * @param statements
   *          the array of {@link StatementGeneratorDescription}'s compatible with
   *          {@link VariableSupportDescription}
   * @param previews
   *          the array of optional {@link GenerationPreview}'s for variable/statement combinations
   */
  public void addGenerators(VariableSupportDescription variable,
      StatementGeneratorDescription[] statements,
      GenerationPreview[] previews) {
    // configure default preferences
    {
      variable.configureDefaultPreferences(m_store);
      for (StatementGeneratorDescription statement : statements) {
        statement.configureDefaultPreferences(m_store);
      }
    }
    // remember in id -> description maps
    {
      m_idToVariable.put(variable.getId(), variable);
      for (StatementGeneratorDescription statement : statements) {
        m_idToStatement.put(statement.getId(), statement);
      }
    }
    // remember preview's
    {
      Assert.isTrue(statements.length == previews.length);
      for (int i = 0; i < statements.length; i++) {
        StatementGeneratorDescription statement = statements[i];
        GenerationPreview preview = previews[i];
        if (preview != null) {
          m_previewMap.put(variable, statement, preview);
        }
      }
    }
    // add
    m_variables.add(variable);
    m_variableToStatements.put(variable, statements);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Deduce
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Specifies if code generation settings should be deduced from existing code.
   */
  public void setDefaultDeduceSettings(boolean deduce) {
    m_store.setDefault(P_DEDUCE_SETTINGS, deduce);
  }

  /**
   * Specifies if code generation settings should be deduced from existing code.
   */
  public void setDeduceSettings(boolean deduce) {
    m_store.setValue(P_DEDUCE_SETTINGS, deduce);
  }

  /**
   * @return <code>true</code> if code generation settings should be deduced from existing code.
   */
  public boolean shouldDeduceSettings() {
    return m_store.getBoolean(P_DEDUCE_SETTINGS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Forced method
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setDefaultForcedMethodName(String forcedName) {
    m_store.setDefault(P_FORCED_METHOD, forcedName);
  }

  /**
   * Returns the name of "forced" method, where new statements and children should be located.
   * "Forced" methods are used by "this" components without children because sometimes placing GUI
   * code is not desirable, for example to keep constructor shot, or because there are several
   * constructors and they may call single "initialize" method.
   *
   * @return the name of "forced" method or <code>null</code> if there are no such method
   *         configured.
   */
  public String getForcedMethodName() {
    String methodName = m_store.getString(P_FORCED_METHOD);
    return StringUtils.isEmpty(methodName) ? null : methodName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Variable
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of {@link VariableSupportDescription} that can be used with this toolkit.
   */
  public VariableSupportDescription[] getVariables() {
    return m_variables.toArray(new VariableSupportDescription[m_variables.size()]);
  }

  /**
   * @return the {@link VariableSupportDescription}.
   */
  public VariableSupportDescription getVariable() {
    String id = m_store.getString(P_VARIABLE_SUPPORT_ID);
    return getVariable(id);
  }

  /**
   * Returns default {@link VariableSupportDescription} from {@link ToolkitDescription} or from type
   * specific information.
   *
   * Sometimes we have exceptions for some components, for example "text fields" should be generated
   * as fields instead of locals, so this method analyzes given {@link JavaInfo} component and can
   * return different {@link VariableSupportDescription}.
   *
   * @return the {@link VariableSupportDescription} for given {@link JavaInfo}.
   */
  public VariableSupportDescription getVariable(JavaInfo javaInfo) {
    String id = getPreferences(javaInfo).getString(P_VARIABLE_SUPPORT_ID);
    // use script for validation
    {
      String script = JavaInfoUtils.getParameter(javaInfo, "variable.validateID");
      if (script != null) {
        ClassLoader coreClassLoader = getClass().getClassLoader();
        id = (String) ScriptUtils.evaluate(coreClassLoader, script, "id", id);
      }
    }
    // if "Local", check for type specific "Field"
    if (LocalUniqueVariableDescription.ID.equals(id)) {
      if (NamesManager.shouldUseFieldInsteadOfLocal(javaInfo.getDescription())) {
        id = FieldUniqueVariableDescription.ID;
      }
    }
    // OK, get variable description
    return getVariable(id);
  }

  /**
   * Sets the {@link VariableSupportDescription}.
   */
  public void setVariable(VariableSupportDescription variable) {
    m_store.setValue(P_VARIABLE_SUPPORT_ID, variable.getId());
  }

  /**
   * @return the default {@link VariableSupportDescription}.
   */
  public VariableSupportDescription getDefaultVariable() {
    String id = m_store.getDefaultString(P_VARIABLE_SUPPORT_ID);
    return getVariable(id);
  }

  /**
   * Sets the default {@link VariableSupportDescription}.
   */
  public void setDefaultVariable(VariableSupportDescription description) {
    m_store.setDefault(P_VARIABLE_SUPPORT_ID, description.getId());
  }

  /**
   * @return the {@link VariableSupportDescription} with given id.
   */
  private VariableSupportDescription getVariable(String id) {
    VariableSupportDescription variable = m_idToVariable.get(id);
    Assert.isNotNull(variable, "Unable to find variable with id \"" + id + "\".");
    return variable;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Statement
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of {@link StatementGeneratorDescription} compatible with given
   *         {@link VariableSupportDescription}.
   */
  public StatementGeneratorDescription[] getStatements(VariableSupportDescription variable) {
    return m_variableToStatements.get(variable);
  }

  /**
   * @return the {@link StatementGeneratorDescription}.
   */
  public StatementGeneratorDescription getStatement() {
    String id = m_store.getString(P_STATEMENT_GENERATOR_ID);
    return getStatement(id);
  }

  /**
   * @return the {@link StatementGeneratorDescription} for editor of given {@link JavaInfo}.
   */
  public StatementGeneratorDescription getStatement(JavaInfo javaInfo) {
    String id = getPreferences(javaInfo).getString(P_STATEMENT_GENERATOR_ID);
    return getStatement(id);
  }

  /**
   * Sets the {@link StatementGeneratorDescription}.
   */
  public void setStatement(StatementGeneratorDescription statement) {
    m_store.setValue(P_STATEMENT_GENERATOR_ID, statement.getId());
  }

  /**
   * @return the default {@link StatementGeneratorDescription}.
   */
  public StatementGeneratorDescription getDefaultStatement() {
    String id = m_store.getDefaultString(P_STATEMENT_GENERATOR_ID);
    return getStatement(id);
  }

  /**
   * Sets the default {@link StatementGeneratorDescription}.
   */
  public void setDefaultStatement(StatementGeneratorDescription description) {
    m_store.setDefault(P_STATEMENT_GENERATOR_ID, description.getId());
  }

  /**
   * @return the {@link StatementGeneratorDescription} with given id.
   */
  private StatementGeneratorDescription getStatement(String id) {
    StatementGeneratorDescription statement = m_idToStatement.get(id);
    Assert.isNotNull(statement, "Unable to find statement with id \"" + id + "\".");
    return statement;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preview
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link GenerationPreview} for given variable/statement, or <code>null</code> if no
   *         preview registered for this combination.
   */
  public GenerationPreview getPreview(VariableSupportDescription variable,
      StatementGeneratorDescription statement) {
    return (GenerationPreview) m_previewMap.get(variable, statement);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String CODE_GENERATION_PREFERENCES_KEY = "CODE_GENERATION_PREFERENCES_KEY";

  /**
   * @return the {@link IPreferenceStore} with settings bounds to given {@link JavaInfo}. This can
   *         or editor specific settings, or global, toolkit level settings.
   */
  private IPreferenceStore getPreferences(JavaInfo javaInfo) {
    IPreferenceStore store =
        (IPreferenceStore) javaInfo.getEditor().getGlobalValue(CODE_GENERATION_PREFERENCES_KEY);
    return store != null ? store : m_store;
  }

  /**
   * Sets the editor specific code generation settings.
   */
  private void setPreferences(JavaInfo javaInfo, IPreferenceStore store) {
    javaInfo.getEditor().putGlobalValue(CODE_GENERATION_PREFERENCES_KEY, store);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Deduce
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Deduces code generation settings for all toolkits presented in hierarchy that starts in given
   * {@link JavaInfo} root.
   * <p>
   * Deducing - is determining code generation settings mostly used in existing code and use then
   * same settings for following code generation.
   */
  public static void deduce(JavaInfo root) throws Exception {
    // prepare map: settings -> components
    final Map<GenerationSettings, Set<AbstractComponentInfo>> settingsToComponents =
        Maps.newHashMap();
    root.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof AbstractComponentInfo) {
          AbstractComponentInfo component = (AbstractComponentInfo) objectInfo;
          GenerationSettings settings =
              component.getDescription().getToolkit().getGenerationSettings();
          // prepare list of components
          Set<AbstractComponentInfo> components = settingsToComponents.get(settings);
          if (components == null) {
            components = Sets.newHashSet();
            settingsToComponents.put(settings, components);
          }
          // add new component
          components.add(component);
        }
      }
    });
    // deduce settings for each toolkit
    for (Map.Entry<GenerationSettings, Set<AbstractComponentInfo>> entry : settingsToComponents.entrySet()) {
      GenerationSettings settings = entry.getKey();
      Set<AbstractComponentInfo> components = entry.getValue();
      settings.deduce(components);
    }
  }

  /**
   * Deduces code generation settings for this toolkit.
   */
  private void deduce(Set<AbstractComponentInfo> components) throws Exception {
    if (!m_store.getBoolean(P_DEDUCE_SETTINGS)) {
      return;
    }
    // use this JavaInfo
    JavaInfo javaInfo = components.iterator().next();
    // prepare preference store for editor level settings
    IPreferenceStore editorPreferences;
    {
      editorPreferences = new PreferenceStore();
      setPreferences(javaInfo, new ChainedPreferenceStore(new IPreferenceStore[]{
          editorPreferences,
          m_store}));
    }
    // do deduce
    deduceVariable(components, editorPreferences);
    deduceStatement(components, editorPreferences);
    // check that statement generator (deduced or default) is compatible with variable
    {
      VariableSupportDescription variableDescription = getVariable(javaInfo);
      StatementGeneratorDescription statementDescription = getStatement(javaInfo);
      StatementGeneratorDescription[] compatibleStatements = getStatements(variableDescription);
      if (!ArrayUtils.contains(compatibleStatements, statementDescription)) {
        editorPreferences.setValue(P_STATEMENT_GENERATOR_ID, compatibleStatements[0].getId());
      }
    }
  }

  /**
   * Deduces {@link VariableSupportDescription}.
   */
  private void deduceVariable(Set<AbstractComponentInfo> components, IPreferenceStore store) {
    // prepare statistics
    int componentCount = 0;
    Map<VariableSupportDescription, Integer> variableToCount = Maps.newHashMap();
    for (AbstractComponentInfo component : components) {
      VariableSupport variableSupport = component.getVariableSupport();
      VariableSupportDescription variableDescription = getVariableDescription(variableSupport);
      if (variableDescription != null) {
        componentCount++;
        Integer count = variableToCount.get(variableDescription);
        variableToCount.put(variableDescription, count == null ? 1 : count + 1);
      }
    }
    // deduce only if there was representative sample
    if (componentCount >= 3) {
      VariableSupportDescription variableDescription = getMaxElement(variableToCount);
      store.setValue(P_VARIABLE_SUPPORT_ID, variableDescription.getId());
    }
  }

  /**
   * Deduces {@link StatementGeneratorDescription}.
   */
  private void deduceStatement(Set<AbstractComponentInfo> components, IPreferenceStore store)
      throws Exception {
    // prepare statistics
    int componentCount = 0;
    Map<Block, Integer> blockToCount = Maps.newHashMap();
    for (AbstractComponentInfo component : components) {
      VariableSupport variableSupport = component.getVariableSupport();
      if (getVariableDescription(variableSupport) != null) {
        componentCount++;
        // prepare block
        Block block;
        {
          StatementTarget target = variableSupport.getStatementTarget();
          block = target.getBlock();
          if (block == null) {
            block = AstNodeUtils.getEnclosingBlock(target.getStatement());
          }
        }
        // update count of components in block
        Integer count = blockToCount.get(block);
        blockToCount.put(block, count == null ? 1 : count + 1);
      }
    }
    // deduce only if there was representative sample
    if (componentCount >= 3) {
      int singleCount = 0;
      int multipleCount = 0;
      for (Integer count : blockToCount.values()) {
        if (count == 1) {
          singleCount++;
        } else {
          multipleCount += count;
        }
      }
      StatementGeneratorDescription statementDescription =
          singleCount >= multipleCount
              ? BlockStatementGeneratorDescription.INSTANCE
              : FlatStatementGeneratorDescription.INSTANCE;
      store.setValue(P_STATEMENT_GENERATOR_ID, statementDescription.getId());
    }
  }

  /**
   * @return the {@link VariableSupportDescription} corresponding to given {@link VariableSupport}
   *         or <code>null</code> if no {@link VariableSupportDescription} can be found.
   */
  private VariableSupportDescription getVariableDescription(VariableSupport variableSupport) {
    for (VariableSupportDescription variableDescription : m_variables) {
      if (variableSupport.getClass() == variableDescription.getType()) {
        return variableDescription;
      }
    }
    return null;
  }

  /**
   * @return the element with maximal cardinality.
   */
  private static <T> T getMaxElement(Map<T, Integer> elementToCount) {
    Assert.isTrue(!elementToCount.isEmpty());
    T maxElement = elementToCount.keySet().iterator().next();
    int maxCardinality = elementToCount.get(maxElement);
    for (Map.Entry<T, Integer> entry : elementToCount.entrySet()) {
      if (entry.getValue() > maxCardinality) {
        maxElement = entry.getKey();
        maxCardinality = entry.getValue();
      }
    }
    return maxElement;
  }
}
