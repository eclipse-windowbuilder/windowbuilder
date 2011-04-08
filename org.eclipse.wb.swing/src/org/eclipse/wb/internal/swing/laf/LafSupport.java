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
package org.eclipse.wb.internal.swing.laf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.XmlWriter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.laf.command.AddCategoryCommand;
import org.eclipse.wb.internal.swing.laf.command.AddCommand;
import org.eclipse.wb.internal.swing.laf.command.Command;
import org.eclipse.wb.internal.swing.laf.command.EditCommand;
import org.eclipse.wb.internal.swing.laf.command.EditNameCommand;
import org.eclipse.wb.internal.swing.laf.command.MoveCategoryCommand;
import org.eclipse.wb.internal.swing.laf.command.MoveCommand;
import org.eclipse.wb.internal.swing.laf.command.RemoveCategoryCommand;
import org.eclipse.wb.internal.swing.laf.command.RemoveCommand;
import org.eclipse.wb.internal.swing.laf.command.RenameCategoryCommand;
import org.eclipse.wb.internal.swing.laf.command.SetVisibleCommand;
import org.eclipse.wb.internal.swing.laf.model.AbstractCustomLafInfo;
import org.eclipse.wb.internal.swing.laf.model.CategoryInfo;
import org.eclipse.wb.internal.swing.laf.model.LafInfo;
import org.eclipse.wb.internal.swing.laf.model.PluginLafInfo;
import org.eclipse.wb.internal.swing.laf.model.SeparatorLafInfo;
import org.eclipse.wb.internal.swing.laf.model.SystemLafInfo;
import org.eclipse.wb.internal.swing.laf.model.UndefinedLafInfo;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.preferences.laf.IPreferenceConstants;
import org.eclipse.wb.internal.swing.utils.SwingUtils;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Helper class to manage (enumerate, switching between) installed LAFs.
 * 
 * @author mitin_aa
 * @coverage swing.laf
 */
public final class LafSupport {
  // constants
  public static final String ROOT_ID = "__wbp_laf_root";
  private static final String EXTERNAL_LAF_POINT = "org.eclipse.wb.swing.lookAndFeel";
  //
  private static final QualifiedName SWING_LAF_SELECTED_STORE =
      new QualifiedName(Activator.PLUGIN_ID, "swing_laf_selected");
  private static final String SWING_LAF_SELECTED = "SWING_LAF_SELECTED";
  public static final String SET_LOOK_AND_FEEL_STRING = "setLookAndFeel(java.lang.String)";
  public static final String SET_LOOK_AND_FEEL_LAF = "setLookAndFeel(javax.swing.LookAndFeel)";
  // fields
  private static List<CategoryInfo> m_lafList;
  private static List<LafInfo> m_mruLAFList = Lists.newLinkedList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // private constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private LafSupport() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LAF list, LAF selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of {@link CategoryInfo} containing all {@link LookAndFeel}'s
   *         available.
   */
  public static List<CategoryInfo> getLAFCategoriesList() {
    createLAFList();
    return ImmutableList.copyOf(m_lafList);
  }

  /**
   * @return the {@link List} of {@link LafInfo} containing all most recently used LAF items.
   */
  public static List<LafInfo> getMRULAFList() {
    // must be mutable
    return m_mruLAFList;
  }

  /**
   * Enumerates all available {@link LafInfo} and stores it into <code>m_lafList</code>.
   */
  private static void createLAFList() {
    if (m_lafList == null) {
      m_lafList = Lists.newArrayList();
      CategoryInfo rootCategory = new CategoryInfo(ROOT_ID, "<root>");
      m_lafList.add(rootCategory);
      // add "undefined" and "current system"
      rootCategory.add(SystemLafInfo.INSTANCE);
      rootCategory.add(UndefinedLafInfo.INSTANCE);
      rootCategory.add(new SeparatorLafInfo());
      // enumerate system LAFs
      CategoryInfo jdkCategory = new CategoryInfo("JRE", "JRE");
      m_lafList.add(jdkCategory);
      LookAndFeelInfo[] systemLAFs = UIManager.getInstalledLookAndFeels();
      for (int i = 0; i < systemLAFs.length; i++) {
        LookAndFeelInfo laf = systemLAFs[i];
        jdkCategory.add(new LafInfo(laf.getName(), laf.getName(), laf.getClassName()));
      }
      // add LAFs using plugin API
      List<IConfigurationElement> categoryElements =
          ExternalFactoriesHelper.getElements(EXTERNAL_LAF_POINT, "category");
      for (IConfigurationElement categoryElement : categoryElements) {
        CategoryInfo category;
        if (ROOT_ID.equals(ExternalFactoriesHelper.getRequiredAttribute(categoryElement, "id"))) {
          category = rootCategory;
        } else {
          category = new CategoryInfo(categoryElement);
          m_lafList.add(category);
        }
        for (IConfigurationElement lafElement : categoryElement.getChildren("LookAndFeel")) {
          if (isConditionTrue(lafElement)) {
            category.add(new PluginLafInfo(lafElement));
          }
        }
      }
      // apply commands
      commands_apply();
    }
  }

  /**
   * @return <code>true</code> if "condition" attribute is empty or evaluates to <code>true</code>.
   */
  private static boolean isConditionTrue(IConfigurationElement element) {
    String condition = element.getAttribute("condition");
    // no condition
    if (StringUtils.isEmpty(condition)) {
      return true;
    }
    // evaluate condition
    Map<String, Object> variables = Maps.newHashMap();
    {
      variables.put("isWindows", EnvironmentUtils.IS_WINDOWS);
    }
    Object result = ScriptUtils.evaluate(condition, variables);
    return result instanceof Boolean ? (Boolean) result : false;
  }

  /**
   * Causes LAF list to be re-created on next list request.
   */
  public static void reloadLAFList() {
    m_lafList = null;
  }

  /**
   * Removes all applied {@link Command}'s.
   */
  public static void resetToDefaults() {
    m_commands.clear();
    commands_write();
    reloadLAFList();
    // restore possibly changed name of special LAFs
    SystemLafInfo.INSTANCE.setName(SystemLafInfo.SYSTEM_LAF_NAME);
    UndefinedLafInfo.INSTANCE.setName(UndefinedLafInfo.UNDEFINED_LAF_NAME);
  }

  /**
   * Returns the {@link LafInfo} which selected currently in active editor. First it tries to get
   * the selected LAF from {@link JavaInfo}'s arbitrary value. Next it tries to get LAF from
   * <code>main()</code> method. Then tries to get the LAF from underlying resource of CU of
   * <code>javaInfo</code>. At last if nothing found it returns the default LAF as it defined in
   * preferences.
   * 
   * @return the {@link LafInfo} which selected currently in active editor.
   */
  public static LafInfo getSelectedLAF(JavaInfo javaInfo) {
    // ensure LAF list created
    createLAFList();
    LafInfo selectedLafInfo = null;
    // first check selected in JavaInfo's arbitrary value
    selectedLafInfo = lookupLAFByID(getLAFArbitraryID(javaInfo));
    if (selectedLafInfo != null) {
      return selectedLafInfo;
    }
    // then look into main() if any.
    selectedLafInfo = getLAFFromMain(javaInfo);
    if (selectedLafInfo != null) {
      setLAFArbitraryID(javaInfo, selectedLafInfo.getID());
      return selectedLafInfo;
    }
    // still nothing, look in persistent store
    try {
      selectedLafInfo = lookupLAFByID(getLAFPersistentID(javaInfo));
      if (selectedLafInfo != null) {
        return selectedLafInfo;
      }
    } catch (Throwable e) {
      // just ignore, proceed with default
    }
    // nothing found, return default
    return getDefaultLAF();
  }

  /**
   * @return the default {@link LafInfo} taken from preferences. Returns settings default LAF if
   *         nothing found.
   */
  public static LafInfo getDefaultLAF() {
    LafInfo lafInfo =
        lookupLAFByID_ensureList(getPreferenceStore().getString(IPreferenceConstants.P_DEFAULT_LAF));
    return lafInfo == null ? getSettingsDefaultLAF() : lafInfo;
  }

  /**
   * @return the {@link LafInfo} which should be set as "default LAF" by default in preferences.
   *         Returns system default LAF if nothing found.
   */
  public static LafInfo getSettingsDefaultLAF() {
    LafInfo lafInfo =
        lookupLAFByID_ensureList(getPreferenceStore().getDefaultString(
            IPreferenceConstants.P_DEFAULT_LAF));
    return lafInfo == null ? getSystemDefaultLAF() : lafInfo;
  }

  /**
   * Returns system default LAF. For Linux it is 'Metal' LAF because of a bug in JVM, see
   * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6922280 For other system it is defined by
   * {@link UIManager#getSystemLookAndFeelClassName()}.
   * 
   * @return system default LAF.
   */
  public static LafInfo getSystemDefaultLAF() {
    if (EnvironmentUtils.IS_LINUX) {
      LafInfo lafInfo = lookupLAFByID_ensureList("Metal");
      return lafInfo != null ? lafInfo : UndefinedLafInfo.INSTANCE;
    } else {
      return SystemLafInfo.INSTANCE;
    }
  }

  /**
   * Selects the LAF. Stores selected LAF into both persistence's and applies in main() method if
   * needed.
   */
  public static void selectLAF(JavaInfo javaInfo, LafInfo lafInfo) throws Exception {
    Assert.isNotNull(lafInfo);
    // ensure LAF list created
    createLAFList();
    Assert.isLegal(lookupLAFByID(lafInfo.getID()) == lafInfo);
    // store selected LAF
    setLAFArbitraryID(javaInfo, lafInfo.getID());
    setLAFPersistentID(javaInfo, lafInfo.getID());
    if (getPreferenceStore().getBoolean(IPreferenceConstants.P_APPLY_IN_MAIN)) {
      IJavaProject javaProject = javaInfo.getEditor().getJavaProject();
      if (lafInfo instanceof AbstractCustomLafInfo
          && !ProjectUtils.hasType(javaProject, lafInfo.getClassName())) {
        // add LAF jar into project classpath
        ProjectUtils.addJar(javaProject, ((AbstractCustomLafInfo) lafInfo).getJarFile(), null);
      }
      lafInfo.applyInMain(javaInfo.getEditor());
    }
    // maintain MRU list
    lafInfo.increaseUsageCount();
    if (m_mruLAFList.contains(lafInfo)) {
      // remove to exclude duplicates
      m_mruLAFList.remove(lafInfo);
    }
    m_mruLAFList.add(lafInfo);
    // keep 5 items, but show only 3 with greatest usage count
    if (m_mruLAFList.size() > 5) {
      m_mruLAFList.remove(0);
    }
  }

  /**
   * Applies the selected LAF in Swing via UIManager.
   * 
   * @param lafInfo
   *          the {@link LafInfo} to be applied.
   */
  public static void applySelectedLAF(final LafInfo lafInfo) {
    try {
      SwingUtils.runLaterAndWait(new RunnableEx() {
        public void run() throws Exception {
          LookAndFeel lookAndFeelInstance = lafInfo.getLookAndFeelInstance();
          UIManager.put("ClassLoader", lookAndFeelInstance.getClass().getClassLoader());
          UIManager.setLookAndFeel(lookAndFeelInstance);
        }
      });
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils, Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Traverses through LAF list and looks up for LAF with given <code>id</code>.
   * 
   * @param id
   *          the id of look-and-feel. Can be <code>null</code>.
   */
  private static LafInfo lookupLAFByID(String id) {
    if (id == null) {
      return null;
    }
    for (CategoryInfo lafCategory : m_lafList) {
      LafInfo lookupResult = lafCategory.lookupByID(id);
      if (lookupResult != null) {
        return lookupResult;
      }
    }
    return null;
  }

  /**
   * Ensures the LAF list to be created and traverses through LAF list and looks up for LAF with
   * given <code>id</code>.
   * 
   * @param id
   *          the id of look-and-feel. Can NOT be <code>null</code>.
   * @return the found look-and-feel or <code>null</code> if nothing found.
   */
  private static LafInfo lookupLAFByID_ensureList(String id) {
    // ensure LAF list created
    createLAFList();
    Assert.isNotNull(id);
    return lookupLAFByID(id);
  }

  /**
   * Ensures the LAF list to be created and traverses through LAF categories and looks up for
   * category with given <code>id</code>.
   * 
   * @param id
   *          the id of category. Can NOT be <code>null</code>.
   * @return the found category or <code>null</code>.
   */
  private static CategoryInfo lookupCategoryByID_ensureList(String id) {
    // ensure LAF list created
    createLAFList();
    Assert.isNotNull(id);
    for (CategoryInfo lafCategory : m_lafList) {
      if (id.equals(lafCategory.getID())) {
        return lafCategory;
      }
    }
    // nothing found
    return null;
  }

  /**
   * @return {@link IPreferenceStore} for Swing Toolkit.
   */
  private static IPreferenceStore getPreferenceStore() {
    return ToolkitProvider.DESCRIPTION.getPreferences();
  }

  /**
   * Stores the selected LAF ID into underlying resource of this CU.
   */
  private static void setLAFPersistentID(JavaInfo javaInfo, String id) throws Exception {
    IResource resource = javaInfo.getEditor().getModelUnit().getUnderlyingResource();
    resource.setPersistentProperty(SWING_LAF_SELECTED_STORE, id);
  }

  /**
   * Load the selected LAF ID from underlying resource of this CU.
   */
  private static String getLAFPersistentID(JavaInfo javaInfo) throws Exception {
    IResource resource = javaInfo.getEditor().getModelUnit().getUnderlyingResource();
    return resource.getPersistentProperty(SWING_LAF_SELECTED_STORE);
  }

  /**
   * Stores the selected LAF ID into JavaInfo's arbitrary value.
   */
  private static void setLAFArbitraryID(JavaInfo javaInfo, String id) {
    javaInfo.putArbitraryValue(SWING_LAF_SELECTED, id);
  }

  /**
   * Load the selected LAF id from JavaInfo's arbitrary value.
   */
  private static String getLAFArbitraryID(JavaInfo javaInfo) {
    return (String) javaInfo.getArbitraryValue(SWING_LAF_SELECTED);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Get LAF installed in main()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Looks up for <code>main()</code> method and searches the installed LAF by searching
   * <code>setLookAndFeel</code> method invocation. If found, stores LAF id in passed
   * <code>javaInfo</code> 's underlying resource under <code>SWING_LAF_SELECTED</code> persistent
   * key.
   * 
   * @return {@link LafInfo} which represents installed LAF or <code>null</code> if no
   *         <code>main</code> method found of no <code>setLookAndFeel</code> method invocation
   *         found.
   */
  private static LafInfo getLAFFromMain(final JavaInfo javaInfo) {
    AstEditor editor = javaInfo.getEditor();
    MethodDeclaration mainMethod = getMainMethod(editor);
    if (mainMethod == null) {
      // no main method 
      return null;
    }
    // look up for setLookAndFeel method
    MethodInvocation setLookAndFeelMethod = getSetLookAndFeelMethod(mainMethod);
    if (setLookAndFeelMethod == null) {
      return null;
    }
    String methodSignature = AstNodeUtils.getMethodSignature(setLookAndFeelMethod);
    try {
      String className;
      // evaluate what we've got
      EvaluationContext context =
          new EvaluationContext(EditorState.get(editor).getEditorLoader(),
              new ExecutionFlowDescription(mainMethod));
      final Object evaluateObject =
          AstEvaluationEngine.evaluate(context, DomGenerics.arguments(setLookAndFeelMethod).get(0));
      // it can be String or LookAndFeel only
      if (SET_LOOK_AND_FEEL_LAF.equals(methodSignature)) {
        LookAndFeel laf = (LookAndFeel) evaluateObject;
        className = laf.getClass().getName();
      } else {
        className = (String) evaluateObject;
      }
      // find in known LAFs list by class name
      for (CategoryInfo categoryInfo : m_lafList) {
        LafInfo lafInfo = categoryInfo.lookupByClassName(className);
        if (lafInfo != null) {
          return lafInfo;
        }
      }
    } catch (Throwable e) {
      EditorState.get(editor).addWarning(
          new EditorWarning(ModelMessages.LafSupport_errCanParse_setLookAndFeel, e));
    }
    return null;
  }

  /**
   * Searches for main method declaration and looks for any
   * UIManager.setLookAndFeel(java.lang.String) and
   * UIManager.setLookAndFeel(javax.swing.LookAndFeel) method invocations in it.
   * 
   * @return any of UIManager.setLookAndFeel(java.lang.String) or
   *         UIManager.setLookAndFeel(javax.swing.LookAndFeel) {@link MethodInvocation} instance if
   *         any. Otherwise returns <code>null</code>.
   */
  public static MethodInvocation getSetLookAndFeelMethod(MethodDeclaration mainMethod) {
    // look up for setLookAndFeel method
    final MethodInvocation setLAFMethodInvocation[] = new MethodInvocation[1];
    mainMethod.accept(new ASTVisitor() {
      @Override
      public boolean visit(final MethodInvocation node) {
        // look for UIManager.setLookAndFeel(java.lang.String) and UIManager.setLookAndFeel(javax.swing.LookAndFeel)
        String methodSignature = AstNodeUtils.getMethodSignature(node);
        if (isUIManagerInvocation(node)
            && (SET_LOOK_AND_FEEL_LAF.equals(methodSignature) || SET_LOOK_AND_FEEL_STRING.equals(methodSignature))) {
          setLAFMethodInvocation[0] = node;
        }
        // not interested in children
        return false;
      }

      /**
       * @return <code>true</code> if method invocation expression is type of UIManager.
       */
      private boolean isUIManagerInvocation(MethodInvocation node) {
        return node.getExpression() != null
            && AstNodeUtils.getFullyQualifiedName(node.getExpression(), false).equals(
                "javax.swing.UIManager");
      }
    });
    return setLAFMethodInvocation[0];
  }

  /**
   * Returns the {@link MethodDeclaration} for main method of primary type of the compilation unit
   * for given <code>editor</code>. Returns <code>null</code> if no main method found.
   * 
   * @return the {@link MethodDeclaration} for main method of primary type of the compilation unit
   *         for given <code>editor</code> or <code>null</code> if no main method found.
   */
  public static MethodDeclaration getMainMethod(AstEditor editor) {
    IType primaryType = CodeUtils.findPrimaryType(editor.getModelUnit());
    if (primaryType != null) {
      TypeDeclaration typeDeclaration =
          AstNodeUtils.getTypeByName(editor.getAstUnit(), primaryType.getElementName());
      return AstNodeUtils.getMethodBySignature(typeDeclaration, "main(java.lang.String[])");
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this <code>category</code> is root category.
   */
  public static boolean isRootCategory(CategoryInfo category) {
    return LafSupport.ROOT_ID.equals(category.getID());
  }

  /**
   * Ensures the LAF list to be created and traverses through LAF categories and looks up for
   * category with given <code>id</code>.
   * 
   * @param id
   *          the id of category. Can NOT be <code>null</code>.
   * @return the found category or assertion error if not found.
   */
  public static CategoryInfo getCategory(String id) {
    return lookupCategoryByID_ensureList(id);
  }

  /**
   * Ensures the LAF list to be created and traverses through LAF list and looks up for LAF with
   * given <code>id</code>.
   * 
   * @param id
   *          the id of look-and-feel. Can NOT be <code>null</code>.
   * @return the found look-and-feel or <code>null</code> if nothing found.
   */
  public static LafInfo getLookAndFeel(String id) {
    return lookupLAFByID_ensureList(id);
  }

  /**
   * Removes the category specified by <code>id</code> if any. Note that root category cannot be
   * removed.
   * 
   * @param id
   *          the id of category. Can NOT be <code>null</code>.
   */
  public static void removeLAFCategory(String id) {
    Assert.isNotNull(id);
    CategoryInfo category = getCategory(id);
    if (category == null || isRootCategory(category)) {
      return;
    }
    // remove from MRU list
    for (LafInfo lafInfo : category.getLAFList()) {
      m_mruLAFList.remove(lafInfo);
    }
    m_lafList.remove(category);
  }

  /**
   * Removes the LookAndFeel specified by <code>lafInfo</code> if any. Note: don't remove from
   * category directly.
   * 
   * @param lafInfo
   *          the instance of {@link LafInfo} to remove. Can't be <code>null</code>.
   */
  public static void removeLookAndFeel(LafInfo lafInfo) {
    Assert.isNotNull(lafInfo);
    lafInfo.getCategory().remove(lafInfo);
    m_mruLAFList.remove(lafInfo);
  }

  /**
   * Adds the new category with <code>id</code> and <code>name</code>.
   * 
   * @param id
   *          the id of category. Can not be <code>null</code>.
   * @param name
   *          the name of category. Can not be <code>null</code>.
   */
  public static void addLAFCategory(String id, String name) {
    Assert.isNotNull(id);
    Assert.isNotNull(name);
    CategoryInfo category = new CategoryInfo(id, name);
    m_lafList.add(category);
  }

  /**
   * Moves given category within another categories. Root category can not be moved.
   * 
   * @param moveCategoryID
   *          the id of moving category. Can not be <code>null</code>.
   * @param nextCategoryID
   *          the id of category before which the moving category would be placed. Can be
   *          <code>null</code>.
   */
  public static void moveLAFCategory(String moveCategoryID, String nextCategoryID) {
    Assert.isNotNull(moveCategoryID);
    CategoryInfo category = LafSupport.getCategory(moveCategoryID);
    if (category == null || isRootCategory(category)) {
      return;
    }
    removeLAFCategory(moveCategoryID);
    CategoryInfo nextCategory;
    if (nextCategoryID != null && (nextCategory = LafSupport.getCategory(nextCategoryID)) != null) {
      int index = m_lafList.indexOf(nextCategory);
      m_lafList.add(index, category);
    } else {
      m_lafList.add(category);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final List<Class<? extends Command>> m_commandClasses = Lists.newArrayList();
  static {
    m_commandClasses.add(SetVisibleCommand.class);
    m_commandClasses.add(AddCategoryCommand.class);
    m_commandClasses.add(RenameCategoryCommand.class);
    m_commandClasses.add(MoveCategoryCommand.class);
    m_commandClasses.add(RemoveCategoryCommand.class);
    m_commandClasses.add(AddCommand.class);
    m_commandClasses.add(EditNameCommand.class);
    m_commandClasses.add(EditCommand.class);
    m_commandClasses.add(MoveCommand.class);
    m_commandClasses.add(RemoveCommand.class);
  }
  private static Map<String, Class<? extends Command>> m_idToCommandClass;
  private static List<Command> m_commands;

  /**
   * Applies commands for modifying list of LAFs.
   */
  private static void commands_apply() {
    try {
      // prepare mapping: id -> command class
      if (m_idToCommandClass == null) {
        m_idToCommandClass = Maps.newTreeMap();
        for (Class<? extends Command> commandClass : m_commandClasses) {
          String id = (String) commandClass.getField("ID").get(null);
          m_idToCommandClass.put(id, commandClass);
        }
      }
      // read commands
      m_commands = Lists.newArrayList();
      File commandsFile = commands_getFile();
      if (commandsFile.exists()) {
        FileInputStream inputStream = new FileInputStream(commandsFile);
        try {
          SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
          parser.parse(inputStream, new DefaultHandler() {
            @Override
            public void startElement(String uri,
                String localName,
                String name,
                Attributes attributes) {
              try {
                // prepare command class
                Class<? extends Command> commandClass;
                {
                  commandClass = m_idToCommandClass.get(name);
                  if (commandClass == null) {
                    return;
                  }
                }
                // create command
                Command command;
                {
                  Constructor<? extends Command> constructor =
                      commandClass.getConstructor(new Class[]{Attributes.class});
                  command = constructor.newInstance(new Object[]{attributes});
                }
                // add command
                commands_addExecute(command);
              } catch (Throwable e) {
              }
            }
          });
        } finally {
          inputStream.close();
        }
      }
    } catch (Throwable e) {
    }
  }

  /**
   * Adds given {@link Command} to the list (and executes it).
   */
  private static void commands_addExecute(Command command) {
    try {
      command.execute();
      commands_add(command);
    } catch (Throwable e) {
    }
  }

  /**
   * Adds given {@link Command} to the list and writes commands.
   */
  public static void commands_add(Command command) {
    command.addToCommandList(m_commands);
  }

  /**
   * Stores current {@link Command}'s {@link List}.
   */
  public static void commands_write() {
    try {
      File commandsFile = commands_getFile();
      XmlWriter xmlWriter = new XmlWriter(commandsFile);
      try {
        xmlWriter.openTag("commands");
        // write separate commands
        for (Command command : m_commands) {
          command.write(xmlWriter);
        }
        // close
        xmlWriter.closeTag();
      } finally {
        xmlWriter.close();
      }
    } catch (Throwable e) {
    }
  }

  /**
   * @return the {@link File} with {@link Command}'s.
   */
  private static File commands_getFile() {
    File stateDirectory = Activator.getDefault().getStateLocation().toFile();
    stateDirectory.mkdirs();
    return new File(stateDirectory, "lookAndFeel.commands");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LAF changing listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private static List<ILookAndFeelsChangeListener> m_lafChangingListeners = Lists.newArrayList();

  public static void fireLookAndFeelsChanged() {
    for (ILookAndFeelsChangeListener listener : m_lafChangingListeners) {
      listener.lookAndFeelsListChanged();
    }
  }

  public static void addLookAndFeelsChangeListener(ILookAndFeelsChangeListener listener) {
    m_lafChangingListeners.add(listener);
  }

  public static void removeLookAndFeelsChangeListener(ILookAndFeelsChangeListener listener) {
    m_lafChangingListeners.remove(listener);
  }

  /**
   * Listener interface for changing LookAndFeel preferences.
   * 
   * @author mitin_aa
   */
  public interface ILookAndFeelsChangeListener {
    /**
     * Called when LAF list modification occurred.
     */
    void lookAndFeelsListChanged();
  }
}
