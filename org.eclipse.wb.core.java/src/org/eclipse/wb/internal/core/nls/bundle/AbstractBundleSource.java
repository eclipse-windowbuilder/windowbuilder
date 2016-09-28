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
package org.eclipse.wb.internal.core.nls.bundle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.edit.EditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.IKeyGeneratorStrategy;
import org.eclipse.wb.internal.core.nls.model.IKeyRenameStrategy;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstVisitorEx;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract source for NLS information, based on *.properties files.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public abstract class AbstractBundleSource extends AbstractSource {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Static fields
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static final String NLS_EXPRESSION_INFO = "NLS_EXPRESSION_INFO";
  private final HashMap<String, String> m_keyToValue = new HashMap<String, String>();
  /**
   * Usual key generator for *.properties files based sources.
   */
  public static final IKeyGeneratorStrategy KEY_GENERATOR = new IKeyGeneratorStrategy() {
    public final String generateBaseKey(JavaInfo component, GenericProperty property) {
      String typeName = getTypeName(component);
      String componentName = component.getVariableSupport().getComponentName();
      return typeName + "." + componentName + "." + property.getTitle();
    }
  };
  /**
   * Usual key rename strategy.
   */
  public static final IKeyRenameStrategy KEY_RENAME = new IKeyRenameStrategy() {
    public String getNewKey(String oldName, String newName, String oldKey) {
      if (oldName != null && oldKey.contains(oldName)) {
        String newKey = oldKey;
        newKey = StringUtils.replace(newKey, "." + oldName + ".", "." + newName + ".");
        newKey = StringUtils.replace(newKey, "_" + oldName + "_", "_" + newName + "_");
        {
          String prefix = oldName + ".";
          if (newKey.startsWith(prefix)) {
            newKey = newName + "." + newKey.substring(prefix.length());
          }
        }
        {
          String prefix = oldName + "_";
          if (newKey.startsWith(prefix)) {
            newKey = newName + "_" + newKey.substring(prefix.length());
          }
        }
        return newKey;
      }
      return oldKey;
    }
  };
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final String m_bundleName;
  private final IFolder m_bundlesFolder;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractBundleSource(JavaInfo root, String bundleName) throws Exception {
    super(root);
    m_bundleName = bundleName;
    // prepare bundles folder
    {
      IFile[] bundleFiles = getBundleFiles();
      if (bundleFiles.length == 0) {
        throw new IllegalStateException("At least one bundle file expected for: " + m_bundleName);
      }
      m_bundlesFolder = (IFolder) bundleFiles[0].getParent();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Comments in bundle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return some comments line that should be used in .properties file.
   *
   *         We use this line later during finding "possible" sources, so this line should contain
   *         enough information for source to detect that this bundle is its one, plus may be some
   *         additional information (for example field name in case of "ResourceBundle in field").
   */
  protected abstract String getBundleComment();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the value for given key in current locale or "parent" locale.
   */
  protected final String getValue(String key) throws Exception {
    // prepare locale
    LocaleInfo localeInfo = getLocaleInfo();
    // prepare value
    String value = getValue(key, localeInfo);
    // if value if null, may be we don't have such bundle or key in bundle, search in parent
    if (value == null) {
      LocaleInfo[] locales = getLocales();
      // try to find key in parent locales, for example for 'ru_RU' we try first 'ru', then default locale
      while (value == null && !localeInfo.isDefault()) {
        localeInfo = localeInfo.getParent(locales);
        return getValue(key, localeInfo);
      }
    }
    //
    return value;
  }

  /**
   * @return the value for given key and locale. Can return <code>null</code> if there are no bundle
   *         for such locale or no such key in bundle.
   */
  private String getValue(String key, LocaleInfo localeInfo) throws Exception {
    BundleInfo bundleInfo = getBundleInfo(localeInfo);
    return bundleInfo == null ? null : bundleInfo.getValue(key);
  }

  /**
   * Set value of key in bundle for current locale.
   */
  protected final void setValueInBundle(String key, String value) throws Exception {
    LocaleInfo localeInfo = getLocaleInfo();
    BundleInfo bundleInfo = getBundleInfo(localeInfo);
    if (bundleInfo != null) {
      bundleInfo.setValue(key, value);
      saveBundle(bundleInfo);
    }
  }

  @Override
  public final String getKey(Expression expression) throws Exception {
    return getBasicExpressionInfo(expression).m_key;
  }

  @Override
  public boolean isExternallyChanged() throws Exception {
    LocaleInfo localeInfo = getLocaleInfo();
    BundleInfo bundleInfo = getBundleInfo(localeInfo);
    return bundleInfo.isExternallyChanged();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Auto externalize support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void externalize(JavaInfo component, GenericProperty property, String value)
      throws Exception {
    // prepare key
    String baseKey = null;
    String key = null;
    IPreferenceStore preferences = component.getDescription().getToolkit().getPreferences();
    if (preferences.getBoolean(IPreferenceConstants.P_NLS_KEY_AS_STRING_VALUE_ONLY)) {
      baseKey = shrinkText(value);
    } else {
      baseKey = getKeyGeneratorStrategy().generateBaseKey(component, property);
      if (preferences.getBoolean(IPreferenceConstants.P_NLS_KEY_HAS_STRING_VALUE)) {
        baseKey += "_" + shrinkText(value);
      }
    }
    key = AbstractSource.generateUniqueKey(m_keyToValue, baseKey, value);
    m_keyToValue.put(key, value);
    // externalize with prepared key
    apply_externalizeProperty(property, key);
  }

  @Override
  public final void useKey(GenericProperty property, String key) throws Exception {
    ensureStringLiteral(property);
    // replace direct string expression with externalization expression
    BasicExpressionInfo expressionInfo = apply_externalize_replaceExpression(property, key);
    // ensure that all works correctly
    Assert.isNotNull(
        expressionInfo,
        "Not-null expression information expected for newly externalized property.");
    // mark expression as externalized using this source
    onKeyAdd(property.getJavaInfo(), key);
    NlsSupport.setSource(expressionInfo.m_expression, this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final IEditableSource getEditable() throws Exception {
    EditableSource editableSource = new EditableSource();
    editableSource.setShortTitle(getTitle());
    editableSource.setLongTitle(getTitle() + " (" + getTypeTitle() + ")");
    //
    LocaleInfo[] locales = getLocales();
    for (int i = 0; i < locales.length; i++) {
      LocaleInfo locale = locales[i];
      BundleInfo bundle = getBundleInfo(locale);
      editableSource.add(locale, bundle.getMap());
    }
    //
    editableSource.setFormKeys(getFormKeys());
    editableSource.setKeyToComponentsSupport(getKeyToComponentsSupport().getCopy(false));
    editableSource.setKeyGeneratorStrategy(getKeyGeneratorStrategy());
    return editableSource;
  }

  /**
   * @return the {@link IKeyGeneratorStrategy}. Usually we return {@link #KEY_GENERATOR}, but some
   *         sources can use other generators.
   */
  protected abstract IKeyGeneratorStrategy getKeyGeneratorStrategy();

  /**
   * @return the {@link IKeyRenameStrategy}, usually standard {@link #KEY_RENAME}.
   */
  @Override
  public IKeyRenameStrategy getKeyRenameStrategy() {
    return KEY_RENAME;
  }

  @Override
  public final void apply_renameKeys(final Map<String, String> oldToNew) throws Exception {
    final AstEditor editor = m_root.getEditor();
    // allow subclasses do preparing work
    apply_renameKeys_pre(oldToNew);
    // prepare list of expression information objects
    final List<BasicExpressionInfo> expressionInformationList = Lists.newArrayList();
    editor.getAstUnit().accept(new ASTVisitor() {
      @Override
      public void postVisit(ASTNode node) {
        if (node instanceof Expression) {
          Expression expression = (Expression) node;
          // check that we have expression information with this source
          BasicExpressionInfo expressionInfo = getBasicExpressionInfo(expression);
          if (expressionInfo == null) {
            return;
          }
          // remember information
          expressionInformationList.add(expressionInfo);
        }
      }
    });
    // replace keys in expressions
    for (BasicExpressionInfo expressionInfo : expressionInformationList) {
      // replace key
      String newKey = oldToNew.get(expressionInfo.m_key);
      if (newKey != null) {
        // replace key expression
        Expression newKeyExpression =
            apply_renameKey_replaceKeyExpression(editor, expressionInfo.m_keyExpression, newKey);
        // change expression information
        expressionInfo.m_keyExpression = newKeyExpression;
        expressionInfo.m_key = newKey;
      }
    }
    // update collections
    for (Map.Entry<String, String> entry : oldToNew.entrySet()) {
      String oldKey = entry.getKey();
      String newKey = entry.getValue();
      onKeyRename(oldKey, newKey);
    }
  }

  /**
   * Replaces existing key {@link Expression} with new one, corresponding to new given new key.
   *
   * @return the new {@link Expression} for key.
   */
  protected abstract Expression apply_renameKey_replaceKeyExpression(AstEditor editor,
      Expression keyExpression,
      String newKey) throws Exception;

  /**
   * This method is invoked before keys rename in compilation unit.
   *
   * In most sources we don't need it. But for sources that should change some external compilation
   * units, we need composite rename method.
   */
  protected void apply_renameKeys_pre(Map<String, String> oldToNew) throws Exception {
  }

  @Override
  public final void apply_setValues(LocaleInfo locale, Map<String, String> values)
      throws Exception {
    BundleInfo bundleInfo = getBundleInfo(locale);
    bundleInfo.setMap(values);
    saveBundle(bundleInfo);
  }

  @Override
  public void apply_addLocale(LocaleInfo locale, Map<String, String> values) throws Exception {
    String shortBundleName = CodeUtils.getShortClass(m_bundleName);
    // create empty file for bundle
    {
      String bundleFileName = shortBundleName + "_" + locale.getLocale().toString() + ".properties";
      createPropertyBundleFile(m_bundlesFolder, bundleFileName, getCharsetForBundleFiles());
    }
    // change values in bundle
    BundleInfo bundleInfo = getBundleInfo(locale);
    bundleInfo.setMap(values);
    saveBundle(bundleInfo);
  }

  /**
   * In usual *.properties files encoding is ISO-8859-1. But in GWT we can use UTF-8, so we need
   * this method to specify default encoding for new bundle files.
   */
  protected String getCharsetForBundleFiles() {
    return null;
  }

  @Override
  public void apply_removeLocale(LocaleInfo locale) throws Exception {
    BundleInfo bundleInfo = getBundleInfo(locale);
    bundleInfo.getFile().delete(true, new NullProgressMonitor());
    m_localeToBundleMap.remove(locale);
  }

  @Override
  public void apply_addKey(String key) throws Exception {
  }

  @Override
  public final void apply_externalizeProperty(GenericProperty property, String key)
      throws Exception {
    apply_addKey(key);
    useKey(property, key);
  }

  /**
   * Replace given expression with externalized code.
   */
  protected abstract BasicExpressionInfo apply_externalize_replaceExpression(
      GenericProperty property, String key) throws Exception;

  @Override
  public final void apply_internalizeKeys(final Set<String> keys) throws Exception {
    // internalize keys in expressions
    final AstEditor editor = m_root.getEditor();
    editor.getAstUnit().accept(new AstVisitorEx() {
      @Override
      public void postVisitEx(ASTNode node) throws Exception {
        if (node instanceof Expression) {
          Expression expression = (Expression) node;
          // check that we have expression information
          BasicExpressionInfo expressionInfo = getBasicExpressionInfo(expression);
          if (expressionInfo == null) {
            return;
          }
          // check for remove
          if (keys.contains(expressionInfo.m_key)) {
            // remove // comments for key and may be something else
            apply_removeNonNLSComments(expressionInfo);
            // replace NLS expression with current value StringLiteral
            String currentValue = getValue(expression);
            String code = StringConverter.INSTANCE.toJavaSource(m_root, currentValue);
            editor.replaceExpression(expression, code);
          }
        }
      }
    });
    // update collections
    for (String key : keys) {
      onKeyInternalize(key);
    }
    // allow subclasses do more work
    apply_internalizeKeys_post(keys);
  }

  /**
   * This method is invoked during keys internalizing.
   *
   * In most sources we don't need it. But for sources that should change some external compilation
   * units, we need composite internalize method.
   */
  protected void apply_internalizeKeys_post(Set<String> keys) throws Exception {
  }

  /**
   * This method allows subclasses remove any other NON-NLS comments that they can use. It is used
   * during internalizing keys. For example "classic" Eclipse NLS style can have default value, so
   * we should remove comments for it.
   */
  protected void apply_removeNonNLSComments(BasicExpressionInfo basicExpressionInfo)
      throws Exception {
  }

  /**
   * Replace given {@link Expression} with new {@link Expression} with given code and return
   * getExpressionInfo() for this new expression.
   */
  protected final Object replaceExpression_getInfo(Expression expression, String code)
      throws Exception {
    // replace expression
    Expression newExpression = m_root.getEditor().replaceExpression(expression, code);
    // call "private static getExpressionInfo()"
    {
      Method getExpressionInfoMethod = getClass().getDeclaredMethod(
          "getExpressionInfo",
          new Class[]{JavaInfo.class, Expression.class});
      getExpressionInfoMethod.setAccessible(true);
      // side effect of this invocation is that ExpressionInfo placed in newExpression
      return getExpressionInfoMethod.invoke(null, new Object[]{m_root, newExpression});
    }
  }

  /**
   * If given expression is {@link StringLiteral}, adds "//$NON-NLS-xxx" comment for it.
   */
  protected final void addNonNLSComment(Expression stringLiteralExpression) throws Exception {
    AstEditor editor = m_root.getEditor();
    if (stringLiteralExpression instanceof StringLiteral) {
      int index = editor.getStringLiteralNumberOnLine((StringLiteral) stringLiteralExpression);
      editor.addEndOfLineComment(
          stringLiteralExpression.getStartPosition(),
          " //$NON-NLS-" + (1 + index) + "$");
    }
  }

  /**
   * Remove "//$NON-NLS-xxx" comment for given {@link StringLiteral}.
   */
  protected final void removeNonNLSComment(StringLiteral literal) throws Exception {
    AstEditor editor = m_root.getEditor();
    if (literal != null) {
      int index = editor.getStringLiteralNumberOnLine(literal);
      if (index != -1) {
        String comment = "//$NON-NLS-" + (1 + index) + "$";
        editor.removeEndOfLineComment(literal.getStartPosition(), comment);
      }
    }
  }

  /**
   * Creates *.properties file with given name and encoding in given package.
   */
  protected static void createPropertyBundleFile(IPackageFragment targetPackage,
      String propertyFileName,
      String charset) throws Exception {
    IFolder folder = (IFolder) targetPackage.getUnderlyingResource();
    createPropertyBundleFile(folder, propertyFileName, charset);
  }

  /**
   * Creates *.properties file with given name and encoding in given folder.
   */
  protected static void createPropertyBundleFile(IFolder folder,
      String propertyFileName,
      String charset) throws Exception {
    IFile propertyFile = createFileIfDoesNotExist(folder, propertyFileName, "");
    if (propertyFile != null) {
      if (charset != null) {
        propertyFile.setCharset(charset, null);
      }
    }
  }

  /**
   * Creates file with given name and contents in given package.
   *
   * @return new {@link IFile} or <code>null</code> if file already exists
   */
  protected static IFile createFileIfDoesNotExist(IFolder folder, String fileName, String contents)
      throws Exception {
    IFile file = folder.getFile(fileName);
    if (!file.exists()) {
      file.create(new ByteArrayInputStream(contents.getBytes()), true, new NullProgressMonitor());
      return file;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Replace
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void replace_toStringLiteral(GenericProperty property, String value)
      throws Exception {
    Expression expression = property.getExpression();
    BasicExpressionInfo expressionInfo = getBasicExpressionInfo(expression);
    // notify that key is not used by this expression
    getKeyToComponentsSupport().remove(property.getJavaInfo(), expressionInfo.m_key);
    // remove // comments for key and may be something else
    apply_removeNonNLSComments(expressionInfo);
    // replace NLS expression with StringLiteral of given value
    String code = StringConverter.INSTANCE.toJavaSource(m_root, value);
    m_root.getEditor().replaceExpression(expression, code);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getTitle() throws Exception {
    return m_bundleName;
  }

  @Override
  public final LocaleInfo[] getLocales() throws Exception {
    IFile[] bundleFiles = getBundleFiles();
    return getLocales(bundleFiles);
  }

  private LocaleInfo[] getLocales(IFile[] bundleFiles) {
    Set<LocaleInfo> locales = Sets.newHashSet();
    for (int i = 0; i < bundleFiles.length; i++) {
      IFile file = bundleFiles[i];
      locales.add(BundleInfo.getLocale(m_bundleName, file));
    }
    return locales.toArray(new LocaleInfo[locales.size()]);
  }

  @Override
  public Set<String> getKeys() throws Exception {
    Set<String> keys = Sets.newHashSet();
    for (LocaleInfo locale : getLocales()) {
      BundleInfo bundle = getBundleInfo(locale);
      keys.addAll(bundle.getKeys());
    }
    return keys;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Basic expression information object
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Basic information about expression.
   */
  protected static class BasicExpressionInfo {
    private final Expression m_expression;
    public Expression m_keyExpression;
    public String m_key;

    public BasicExpressionInfo(Expression expression, Expression keyExpression, String key) {
      m_expression = expression;
      m_keyExpression = keyExpression;
      m_key = key;
    }
  }

  /**
   * Get information from expression. Ensure that this information comes from current source.
   */
  protected final BasicExpressionInfo getBasicExpressionInfo(Expression expression) {
    AbstractSource expressionSource = NlsSupport.getSource(expression);
    if (expressionSource == this) {
      return (BasicExpressionInfo) expression.getProperty(NLS_EXPRESSION_INFO);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bundle access
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<LocaleInfo, BundleInfo> m_localeToBundleMap = Maps.newHashMap();

  /**
   * Return existing or new information about resource bundle for given locale.
   */
  protected final BundleInfo getBundleInfo(LocaleInfo localeInfo) throws Exception {
    BundleInfo bundleInfo = m_localeToBundleMap.get(localeInfo);
    if (bundleInfo == null) {
      IPropertiesAccessor propertiesAccessor = getPropertiesAccessor();
      IFile[] files = getBundleFiles();
      bundleInfo = BundleInfo.createBundle(propertiesAccessor, m_bundleName, localeInfo, files);
      m_localeToBundleMap.put(localeInfo, bundleInfo);
    }
    return bundleInfo;
  }

  /**
   * @return {@link IPropertiesAccessor} that should be used to read bundles.
   */
  protected IPropertiesAccessor getPropertiesAccessor() {
    return StandardPropertiesAccessor.INSTANCE;
  }

  /**
   * Save given bundle.
   */
  private void saveBundle(BundleInfo bundleInfo) throws Exception {
    bundleInfo.save(getBundleComment());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // *.properties files
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return list of IResource's that represent bundle files (*.properties) for given bundle name.
   */
  protected final IFile[] getBundleFiles() throws Exception {
    List<IFile> bundleFiles = Lists.newArrayList();
    String bundlePath = m_bundleName.replace('.', '/');
    String bundleFileName = new Path(bundlePath).lastSegment();
    // iterate over all source containers
    for (IContainer container : getSourceContainers()) {
      // prepare container children
      IResource[] resources = new IResource[0];
      {
        IPath folderPath = new Path(bundlePath).removeLastSegments(1);
        IFolder folder = container.getFolder(folderPath);
        if (folder.exists()) {
          resources = folder.members();
        }
      }
      // find properties files for given bundle name
      for (IResource resource : resources) {
        String fileName = resource.getName();
        if (fileName.endsWith(".properties")) {
          if (fileName.startsWith(bundleFileName + "_")
              || fileName.startsWith(bundleFileName + ".")) {
            if (!bundleFiles.contains(resource)) {
              bundleFiles.add((IFile) resource);
            }
          }
        }
      }
    }
    // return resources as array
    return bundleFiles.toArray(new IFile[bundleFiles.size()]);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source containers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns IContainer's for all source directories of current Java project.
   */
  private List<IContainer> getSourceContainers() throws Exception {
    return CodeUtils.getSourceContainers(m_javaProject, true);
  }
}
