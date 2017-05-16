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
package org.eclipse.wb.internal.core.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.VisitingContext;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.IWrapperInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.CompoundAssociation;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.core.model.association.InvocationChildArrayAssociation;
import org.eclipse.wb.core.model.association.InvocationChildEllipsisAssociation;
import org.eclipse.wb.core.model.association.UnknownAssociation;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ExposedFieldCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.creation.IExposedCreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.creation.IWrapperControlCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.creation.WrapperMethodControlCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.AbstractExplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ExposingRule;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.GenerationUtils;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.order.ComponentOrder;
import org.eclipse.wb.internal.core.model.order.ComponentOrderFirst;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ExposedFieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ExposedPropertyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.lang.ArrayUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Utilities for {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public class JavaInfoUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private JavaInfoUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TypeDeclaration} that contains given {@link JavaInfo}.
   */
  public static TypeDeclaration getTypeDeclaration(JavaInfo javaInfo) {
    return AstNodeUtils.getEnclosingType(javaInfo.getCreationSupport().getNode());
  }

  /**
   * @return the {@link MethodDeclaration} that contains given {@link JavaInfo}.
   */
  public static MethodDeclaration getMethodDeclaration(JavaInfo javaInfo) {
    return AstNodeUtils.getEnclosingMethod(javaInfo.getCreationSupport().getNode());
  }

  /**
   * @return <code>true</code> if given {@link IField} is declared in top-level
   *         {@link TypeDeclaration} of {@link JavaInfo}, directly or by one of its implemented
   *         interfaces.
   */
  public static boolean isLocalField(JavaInfo javaInfo, IField field) throws Exception {
    // prepare binding of top-level type
    ITypeBinding typeBinding;
    {
      TypeDeclaration typeDeclaration = getTypeDeclaration(javaInfo);
      typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
    }
    // do check
    String declaringTypeName = field.getDeclaringType().getFullyQualifiedName();
    return AstNodeUtils.isSuccessorOf(typeBinding, declaringTypeName);
  }

  /**
   * @return <code>true</code> if given {@link JavaInfo} created implicitly, with its host.
   */
  public static boolean isImplicitlyCreated(JavaInfo javaInfo) {
    return javaInfo.getCreationSupport() instanceof IImplicitCreationSupport;
  }

  /**
   * Schedules save of {@link ICompilationUnit} of given {@link JavaInfo}.<br>
   * We should save {@link ICompilationUnit} after edit operation finished.
   */
  public static void scheduleSave(final JavaInfo info) {
    DesignerPlugin.getStandardDisplay().asyncExec(new Runnable() {
      public void run() {
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            info.getEditor().getModelUnit().getBuffer().save(null, false);
          }
        });
      }
    });
  }

  /**
   * Schedules opening given {@link ASTNode} in source view.
   */
  public static void scheduleOpenNode(final JavaInfo javaInfo, final ASTNode node) {
    // Do in async because at this time changes may be not committed from ASTEditor to compilation unit.
    // This happens because this method is invoked in internal start/endEdit cycle.
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        IDesignPageSite site = IDesignPageSite.Helper.getSite(javaInfo);
        site.openSourcePosition(node.getStartPosition());
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Assertions
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void assertIsNotDeleted(JavaInfo javaInfo) {
    Assert.isTrue(!javaInfo.isDeleted(), "Component is already deleted: %s", javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Permissions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link JavaInfo} can be moved inside of its parent in some
   *         way. Usually we just ask {@link CreationSupport#canReorder()}, however for example for
   *         absolute layout we can not reorder exposed components, but we can move them (change
   *         bounds).
   */
  public static boolean canMove(final JavaInfo javaInfo) {
    // check, may be we can obtain external permission
    {
      final boolean[] forceMoveEnable = new boolean[1];
      final boolean[] forceMoveDisable = new boolean[1];
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          javaInfo.getBroadcastJava().canMove(javaInfo, forceMoveEnable, forceMoveDisable);
        }
      });
      if (forceMoveEnable[0]) {
        return true;
      }
      if (forceMoveDisable[0]) {
        return false;
      }
    }
    // ask CreationSupport
    return javaInfo.getCreationSupport().canReorder();
  }

  /**
   * @return <code>true</code> if given {@link JavaInfo} can be moved into different parent. Here we
   *         just ask {@link CreationSupport#canReparent()} and have this method only for symmetry
   *         with {@link #canMove(JavaInfo)}.
   */
  public static boolean canReparent(JavaInfo javaInfo) {
    return javaInfo.getCreationSupport().canReparent() && javaInfo.getAssociation().canDelete();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_PARAMETER_PREFIX = "Instance-level parameter: ";

  /**
   * Sets the {@link JavaInfo} instance level parameter value, can be accesses later using
   * {@link #getParameter(JavaInfo, String)}.
   */
  public static void setParameter(JavaInfo javaInfo, String name, String value) {
    String key = KEY_PARAMETER_PREFIX + name;
    javaInfo.putArbitraryValue(key, value);
  }

  /**
   * Returns the value of {@link JavaInfo} parameter.<br>
   * Usually returns just {@link ComponentDescription#getParameter(String)}, but factory components
   * we should also check this parameters in {@link FactoryMethodDescription}.
   *
   * @return the value of {@link JavaInfo} parameter.
   */
  public static String getParameter(JavaInfo javaInfo, String name) {
    // try to key from JavaInfo instance
    {
      String key = KEY_PARAMETER_PREFIX + name;
      String value = (String) javaInfo.getArbitraryValue(key);
      if (value != null) {
        return value;
      }
    }
    // try to get from FactoryMethodDescription
    if (javaInfo.getCreationSupport() instanceof AbstractExplicitFactoryCreationSupport) {
      AbstractExplicitFactoryCreationSupport factoryCreationSupport =
          (AbstractExplicitFactoryCreationSupport) javaInfo.getCreationSupport();
      FactoryMethodDescription factoryMethodDescription = factoryCreationSupport.getDescription();
      String value = factoryMethodDescription.getParameter(name);
      if (value != null) {
        return value;
      }
    }
    // get from ComponentDescription
    return javaInfo.getDescription().getParameter(name);
  }

  /**
   * @return mapped {@link JavaInfo} parameters.
   */
  public static Map<String, String> getParameters(JavaInfo javaInfo) {
    Map<String, String> parameters = Maps.newHashMap();
    parameters.putAll(extractArbitraryParameters(javaInfo));
    parameters.putAll(javaInfo.getDescription().getParameters());
    return parameters;
  }

  /**
   * @return the {@link Map} of parameters set using {@link #setParameter(JavaInfo, String, String)}
   *         .
   */
  private static Map<String, String> extractArbitraryParameters(JavaInfo javaInfo) {
    Map<String, String> parameters = Maps.newHashMap();
    for (Entry<Object, Object> arbitrary : javaInfo.getArbitraries().entrySet()) {
      Object key = arbitrary.getKey();
      Object value = arbitrary.getValue();
      if (key instanceof String && value instanceof String) {
        String stringKey = (String) key;
        if (stringKey.startsWith(KEY_PARAMETER_PREFIX)) {
          parameters.put(stringKey.substring(KEY_PARAMETER_PREFIX.length()), (String) value);
        }
      }
    }
    return parameters;
  }

  /**
   * Checks if {@link JavaInfo} has parameter with value <code>"true"</code>.
   */
  public static boolean hasTrueParameter(JavaInfo javaInfo, String name) {
    String parameter = getParameter(javaInfo, name);
    return "true".equals(parameter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Script
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If given {@link JavaInfo} has parameter with script, execute it with "model" and "object"
   * variables.
   */
  public static Object executeScriptParameter(JavaInfo javaInfo, String scriptName)
      throws Exception {
    String script = getParameter(javaInfo, scriptName);
    if (script != null) {
      return executeScript(javaInfo, script);
    }
    return null;
  }

  /**
   * Execute script with "model" and "object" variables.
   */
  public static Object executeScript(JavaInfo javaInfo, String script) throws Exception {
    ClassLoader classLoader = JavaInfoUtils.getClassLoader(javaInfo);
    Map<String, Object> variables = Maps.newHashMap();
    variables.put("model", javaInfo);
    variables.put("object", javaInfo.getObject());
    return ScriptUtils.evaluate(classLoader, script, variables);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return new {@link JavaInfo} for given component {@link Class} name and {@link CreationSupport}
   *         .
   */
  public static JavaInfo createJavaInfo(AstEditor editor,
      String componentClassName,
      CreationSupport creationSupport) throws Exception {
    Class<?> componentClass =
        EditorState.get(editor).getEditorLoader().loadClass(componentClassName);
    return createJavaInfo(editor, componentClass, creationSupport);
  }

  /**
   * @return new {@link JavaInfo} for given component {@link Class} and {@link CreationSupport}.
   */
  public static JavaInfo createJavaInfo(AstEditor editor,
      Class<?> componentClass,
      CreationSupport creationSupport) throws Exception {
    // prepare description
    ComponentDescription componentDescription =
        ComponentDescriptionHelper.getDescription(editor, componentClass);
    // create model
    return createJavaInfo(editor, componentDescription, creationSupport);
  }

  /**
   * @return new {@link JavaInfo} for given {@link ComponentDescription} and {@link CreationSupport}
   *         .
   */
  public static JavaInfo createJavaInfo(AstEditor editor,
      ComponentDescription componentDescription,
      CreationSupport creationSupport) throws Exception {
    // prepare constructor of model
    Constructor<?> modelConstructor;
    {
      Class<?> modelClass = componentDescription.getModelClass();
      modelConstructor =
          modelClass.getConstructor(new Class[]{
              AstEditor.class,
              ComponentDescription.class,
              CreationSupport.class});
    }
    // create model
    JavaInfo javaInfo =
        (JavaInfo) modelConstructor.newInstance(new Object[]{
            editor,
            componentDescription,
            creationSupport});
    ObjectInfoUtils.setNewId(javaInfo);
    return javaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exposed children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new exposed {@link JavaInfo}'s.
   */
  public static void addExposedChildren(JavaInfo host, Class<?>[] exposedTypes) throws Exception {
    if (hasTrueParameter(host, "noExposedChildren")) {
      return;
    }
    addExposedChildred_Method(host, exposedTypes);
    addExposedChildred_Field(host, exposedTypes);
    // reorder added components
    buildExposedChildrenHierarchy(host);
  }

  /**
   * Adds new {@link JavaInfo}'s exposed using {@link PropertyDescriptor}'s.
   */
  private static void addExposedChildred_Method(JavaInfo host, Class<?>[] exposedTypes)
      throws Exception {
    Object hostObject = host.getObject();
    Assert.isNotNull(hostObject);
    // prepare property descriptors
    boolean includeProtected = shouldExposeProtectedMembers(host);
    List<PropertyDescriptor> propertyDescriptors = host.getDescription().getPropertyDescriptors();
    // check all PropertyDescriptor's
    for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
      Method setMethod = ReflectionUtils.getWriteMethod(propertyDescriptor);
      // prepare "getter"
      Method getMethod = ReflectionUtils.getReadMethod(propertyDescriptor);
      if (getMethod == null) {
        continue;
      }
      if (!includeProtected && ReflectionUtils.isProtected(getMethod)) {
        continue;
      }
      // check for return type
      {
        Class<?> returnType = getMethod.getReturnType();
        if (!isExposedType(exposedTypes, returnType)) {
          continue;
        }
      }
      // check if this method can be used for exposing child
      if (!isEnabledExposeMethod(host, getMethod)) {
        continue;
      }
      // check that methods returns not "null"
      Object methodObject = null;
      {
        try {
          methodObject = getMethod.invoke(hostObject);
          if (methodObject == null) {
            continue;
          }
        } catch (Exception e) {
          continue;
        }
      }
      // check that component returned by method is bound to parent object
      if (!isExposeDisconnectedComponent(host, getMethod) && !areParentChild(host, methodObject)) {
        continue;
      }
      // check for infinite recursion
      if (isExposedRecursive(host, methodObject)) {
        continue;
      }
      // OK, add component
      addChildExposedByMethod(host, getMethod, setMethod, methodObject);
    }
  }

  /**
   * Adds new {@link JavaInfo} exposed by given {@link Method} as child of given <code>host</code>.
   */
  public static JavaInfo addChildExposedByMethod(JavaInfo host, String getMethodName)
      throws Exception {
    // prepare "host" object
    Object hostObject = host.getObject();
    Assert.isNotNull(hostObject);
    // prepare "method" object
    Method getMethod = ReflectionUtils.getMethod(hostObject.getClass(), getMethodName);
    Object methodObject = getMethod.invoke(hostObject);
    Assert.isNotNull(methodObject);
    // add exposed child
    return addChildExposedByMethod(host, getMethod, null, methodObject);
  }

  /**
   * Adds new {@link JavaInfo} exposed by given {@link Method} as child of given <code>host</code>.
   */
  private static JavaInfo addChildExposedByMethod(JavaInfo host,
      Method getMethod,
      Method setMethod,
      Object methodObject) throws Exception {
    // check if already exposed
    if (isAlreadyExposedMethod(host, getMethod)) {
      return null;
    }
    // prepare elements
    AstEditor editor = host.getEditor();
    boolean direct = getParentObject(methodObject) == host.getObject();
    CreationSupport creationSupport =
        new ExposedPropertyCreationSupport(host, getMethod, setMethod, direct);
    // create child JavaInfo
    JavaInfo methodJavaInfo;
    if (InstanceFactoryInfo.isFactory(editor, getMethod.getReturnType())) {
      methodJavaInfo =
          InstanceFactoryInfo.createFactory(editor, getMethod.getReturnType(), creationSupport);
    } else {
      ComponentDescription componentDescription =
          ComponentDescriptionHelper.getDescription(editor, host.getDescription(), getMethod);
      methodJavaInfo = createJavaInfo(editor, componentDescription, creationSupport);
    }
    // configure JavaInfo
    methodJavaInfo.setVariableSupport(new ExposedPropertyVariableSupport(methodJavaInfo,
        host,
        getMethod));
    methodJavaInfo.setAssociation(new ImplicitObjectAssociation(host));
    // add new child
    addExposedJavaInfo(host, methodJavaInfo);
    // initialize
    methodJavaInfo.setObject(methodObject);
    return methodJavaInfo;
  }

  /**
   * @return <code>true</code> if given getter is already used to create exposed {@link JavaInfo},
   *         so should be ignored. If not used yet, we mark it as used and return <code>false</code>
   *         .
   */
  @SuppressWarnings("unchecked")
  private static boolean isAlreadyExposedMethod(JavaInfo host, Method getMethod) {
    String key = "JavaInfoUtils.alreadyExposed.Method";
    Set<Method> alreadyExposed = (Set<Method>) host.getArbitraryValue(key);
    if (alreadyExposed == null) {
      alreadyExposed = Sets.newHashSet();
      host.putArbitraryValue(key, alreadyExposed);
    }
    if (alreadyExposed.contains(getMethod)) {
      return true;
    }
    alreadyExposed.add(getMethod);
    return false;
  }

  /**
   * Adds new {@link JavaInfo}'s exposed using {@link PropertyDescriptor}'s.
   */
  private static void addExposedChildred_Field(JavaInfo host, Class<?>[] exposedTypes)
      throws Exception {
    Object hostObject = host.getObject();
    Assert.isNotNull(hostObject);
    // check all Field's
    boolean includeProtected = shouldExposeProtectedMembers(host);
    List<Field> fields = ReflectionUtils.getFields(hostObject.getClass());
    for (Field field : fields) {
      // check modifier
      if (ReflectionUtils.isPrivate(field) || ReflectionUtils.isPackagePrivate(field)) {
        continue;
      }
      if (ReflectionUtils.isProtected(field) && !includeProtected) {
        continue;
      }
      // check for type
      {
        Class<?> fieldType = field.getType();
        if (!isExposedType(exposedTypes, fieldType)) {
          continue;
        }
      }
      // check if this field can be used for exposing child
      if (!isEnabledExposeField(host, field)) {
        continue;
      }
      // check that methods returns not "null"
      Object fieldObject;
      {
        fieldObject = field.get(hostObject);
        if (fieldObject == null) {
          continue;
        }
      }
      // check that component returned by method is bound to parent object
      if (!areParentChild(host, fieldObject)) {
        continue;
      }
      // check for infinite recursion
      if (isExposedRecursive(host, fieldObject)) {
        continue;
      }
      // OK, add component
      addChildExposedByField(host, field, fieldObject);
    }
  }

  /**
   * Adds new {@link JavaInfo} exposed by given {@link Field} as child of given <code>host</code>.
   */
  private static JavaInfo addChildExposedByField(JavaInfo host, Field field, Object fieldObject)
      throws Exception {
    // check if already exposed
    if (isAlreadyExposedField(host, field)) {
      return null;
    }
    // prepare elements
    AstEditor editor = host.getEditor();
    boolean direct = getParentObject(fieldObject) == host.getObject();
    CreationSupport creationSupport = new ExposedFieldCreationSupport(host, field, direct);
    // create child JavaInfo
    JavaInfo fieldJavaInfo;
    ComponentDescription componentDescription =
        ComponentDescriptionHelper.getDescription(editor, field.getType());
    fieldJavaInfo = createJavaInfo(editor, componentDescription, creationSupport);
    // configure JavaInfo
    fieldJavaInfo.setVariableSupport(new ExposedFieldVariableSupport(fieldJavaInfo, host, field));
    fieldJavaInfo.setAssociation(new ImplicitObjectAssociation(host));
    // add new child
    addExposedJavaInfo(host, fieldJavaInfo);
    // initialize
    fieldJavaInfo.setObject(fieldObject);
    return fieldJavaInfo;
  }

  /**
   * @return <code>true</code> if given {@link Field} is already used to create exposed
   *         {@link JavaInfo}, so should be ignored. If not used yet, we mark it as used and return
   *         <code>false</code>.
   */
  @SuppressWarnings("unchecked")
  private static boolean isAlreadyExposedField(JavaInfo host, Field field) {
    String key = "JavaInfoUtils.alreadyExposed.Field";
    Set<Field> alreadyExposed = (Set<Field>) host.getArbitraryValue(key);
    if (alreadyExposed == null) {
      alreadyExposed = Sets.newHashSet();
      host.putArbitraryValue(key, alreadyExposed);
    }
    if (alreadyExposed.contains(field)) {
      return true;
    }
    alreadyExposed.add(field);
    return false;
  }

  /**
   * Adds exposed {@link JavaInfo} to its host.
   */
  private static void addExposedJavaInfo(JavaInfo host, JavaInfo exposed) throws Exception {
    for (HierarchyProvider provider : getHierarchyProviders()) {
      provider.add(host, exposed);
      if (exposed.getParent() != null) {
        break;
      }
    }
    if (exposed.getParent() == null) {
      host.addChild(exposed);
    }
  }

  /**
   * @return <code>true</code> if given {@link Method} can expose child.
   */
  private static boolean isEnabledExposeMethod(JavaInfo host, Method getMethod) {
    // check filters
    for (ExposingRule rule : host.getDescription().getExposingRules()) {
      Boolean filter = rule.filter(getMethod);
      if (filter != null) {
        if (filter.booleanValue()) {
          return true;
        } else {
          return false;
        }
      }
    }
    // if method of "this" JavaInfo overrides this method, ignore it to avoid binary execution flow
    if (host.getCreationSupport() instanceof ThisCreationSupport) {
      TypeDeclaration typeDeclaration = getTypeDeclaration(host);
      String signature = ReflectionUtils.getMethodSignature(getMethod);
      if (AstNodeUtils.getMethodBySignature(typeDeclaration, signature) != null) {
        return false;
      }
    }
    // if no rule that forbids this method, then allow exposing
    return true;
  }

  /**
   * @return <code>true</code> if given {@link Field} can expose child.
   */
  private static boolean isEnabledExposeField(JavaInfo host, Field field) {
    // check filters
    for (ExposingRule rule : host.getDescription().getExposingRules()) {
      Boolean filter = rule.filter(field);
      if (filter != null) {
        if (filter.booleanValue()) {
          return true;
        } else {
          return false;
        }
      }
    }
    // if no rule that forbids this field, then allow exposing
    return true;
  }

  /**
   * @return <code>true</code> if current form can see protected members of "host".
   */
  private static boolean shouldExposeProtectedMembers(JavaInfo host) {
    // if subclass
    if (host.getCreationSupport() instanceof ThisCreationSupport) {
      return true;
    }
    // if same package
    {
      String unitPackageName = AstNodeUtils.getPackageName(host.getEditor().getAstUnit());
      String hostPackageName =
          CodeUtils.getPackage(host.getDescription().getComponentClass().getName());
      if (unitPackageName.equals(hostPackageName)) {
        return true;
      }
    }
    // no
    return false;
  }

  /**
   * @return <code>true</code> if given {@link Class} is assignable to one of the given exposed
   *         types.
   */
  private static boolean isExposedType(Class<?>[] exposedTypes, Class<?> candidate) {
    for (Class<?> exposedType : exposedTypes) {
      if (exposedType.isAssignableFrom(candidate)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return <code>true</code> if given {@link Method} should expose component even if this
   *         component is not connected to the host object.
   */
  private static boolean isExposeDisconnectedComponent(JavaInfo host, Method getMethod)
      throws Exception {
    String signature = ReflectionUtils.getMethodSignature(getMethod);
    MethodDescription methodDescription = host.getDescription().getMethod(signature);
    return methodDescription != null && methodDescription.hasTrueTag("exposeDisconnectedComponent");
  }

  /**
   * @return <code>true</code> if given {@link Object} will cause infinite recursive exposing.
   */
  private static boolean isExposedRecursive(JavaInfo host, Object methodObject) {
    return host.getObject() == methodObject;
  }

  /**
   * @return <code>true</code> if given {@link JavaInfo} is not direct child of its parent, i.e. it
   *         is child of some inner (not exposed) container.
   */
  public static boolean isIndirectlyExposed(JavaInfo javaInfo) {
    CreationSupport creationSupport = javaInfo.getCreationSupport();
    if (creationSupport instanceof IExposedCreationSupport) {
      return !((IExposedCreationSupport) creationSupport).isDirect();
    }
    if (creationSupport instanceof IWrapperControlCreationSupport) {
      JavaInfo wrapper = ((IWrapperControlCreationSupport) creationSupport).getWrapperInfo();
      return isIndirectlyExposed(wrapper);
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sorting: exposed
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Interface for retrieving parent/child information using widget instances of specific toolkit.
   * Used to build hierarchy of exposed children.
   *
   * @author scheglov_ke
   * @author mitin_aa
   */
  public static class HierarchyProvider {
    /**
     * @return all aliases of given component object (including given object), may be
     *         <code>null</code>, if no aliases.
     */
    public Object[] getAliases(Object object) throws Exception {
      return null;
    }

    /**
     * @return the parent object for given object.
     */
    public Object getParentObject(Object object) throws Exception {
      return null;
    }

    /**
     * @return the children of given toolkit object.
     *
     * @param object
     *          the object to get children from
     */
    public Object[] getChildrenObjects(Object object) throws Exception {
      return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    /**
     * Can associate exposed {@link JavaInfo} with its host.
     * <p>
     * For example in RCP we need to create intermediate Control for exposed Viewer.
     */
    public void add(JavaInfo host, JavaInfo exposed) throws Exception {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Build exposed children hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * For custom component it is possible that exposed component is a child of other exposed
   * component, but from custom component point of view this looks as two independent components.
   * This method restores such logical hierarchy.
   */
  private static void buildExposedChildrenHierarchy(JavaInfo host) throws Exception {
    // prepare map (object -> child)
    final Map<Object, JavaInfo> objectToChild = Maps.newHashMap();
    for (JavaInfo child : host.getChildrenJava()) {
      objectToChild.put(child.getObject(), child);
    }
    // prepare sorted list of children, so we will add them to the logical parents in correct order
    final List<JavaInfo> sortedChildren;
    {
      sortedChildren = Lists.newArrayList();
      fillChildren(sortedChildren, host.getObject(), objectToChild);
    }
    // sort children in host JavaInfo (to reorder children that have host as logical parent)
    // only exposed children should be sorted
    Collections.sort(host.getChildren(), new Comparator<ObjectInfo>() {
      public int compare(ObjectInfo o1, ObjectInfo o2) {
        // ignore not exposed
        {
          boolean exposed_1 = isExposed(o1);
          boolean exposed_2 = isExposed(o2);
          if (!exposed_1 || !exposed_2) {
            return 0;
          }
        }
        // two exposed
        return sortedChildren.indexOf(o1) - sortedChildren.indexOf(o2);
      }

      private boolean isExposed(ObjectInfo o) {
        if (o instanceof JavaInfo) {
          JavaInfo javaInfo = (JavaInfo) o;
          return javaInfo.getCreationSupport() instanceof IExposedCreationSupport;
        }
        return false;
      }
    });
    // find logical parent for each exposed child
    for (JavaInfo child : sortedChildren) {
      if (child.getCreationSupport() instanceof IExposedCreationSupport) {
        // go up along the hierarchy
        Object parentObject = getParentObject(child.getObject());
        for (; parentObject != null; parentObject = getParentObject(parentObject)) {
          JavaInfo parent = objectToChild.get(parentObject);
          if (parent != null) {
            // move child to the logical parent
            host.removeChild(child);
            parent.addChild(child);
            // OK, we found logical parent
            break;
          }
        }
      }
    }
  }

  /**
   * @return the parent object of given object according to toolkit which object belongs to
   *
   * @param childObject
   *          the object to get parent for
   */
  private static Object getParentObject(Object childObject) throws Exception {
    for (HierarchyProvider provider : getHierarchyProviders()) {
      Object parentObject = provider.getParentObject(childObject);
      if (parentObject != null) {
        return parentObject;
      }
    }
    return null;
  }

  /**
   * @return <code>true</code> if given objects are parent/child.
   */
  private static boolean areParentChild(JavaInfo host, Object child) throws Exception {
    Object[] requiredParents = getComponentObjects(host);
    for (Object requiredParent : requiredParents) {
      // check by parent
      if (areParentChild(requiredParent, child)) {
        return true;
      }
      // check in children
      for (HierarchyProvider provider : getHierarchyProviders()) {
        Object[] children = provider.getChildrenObjects(requiredParent);
        if (ArrayUtils.contains(children, child)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @return <code>true</code> if given objects are parent/child.
   */
  private static boolean areParentChild(Object requiredParent, Object child) throws Exception {
    for (Object parent = child; parent != null; parent = getParentObject(parent)) {
      if (parent == requiredParent) {
        return true;
      }
    }
    return false;
  }

  /**
   * Traverse hierarchy starting from given objects and fill {@link List} of {@link JavaInfo} models
   * in top-down order.
   *
   * @param children
   *          the {@link List} to add ordered {@link JavaInfo} models
   * @param object
   *          the current object to fill models from
   * @param objectToChild
   *          the {@link Map} to convert objects into models
   */
  private static void fillChildren(List<JavaInfo> children,
      Object object,
      Map<Object, JavaInfo> objectToChild) throws Exception {
    // add model for current object
    {
      JavaInfo child = objectToChild.get(object);
      if (child != null) {
        children.add(child);
      }
    }
    // prepare children objects
    List<Object> childrenObjects = Lists.newArrayList();
    for (HierarchyProvider provider : getHierarchyProviders()) {
      Collections.addAll(childrenObjects, provider.getChildrenObjects(object));
    }
    // process children objects
    for (Object childOfChild : childrenObjects) {
      fillChildren(children, childOfChild, objectToChild);
    }
  }

  /**
   * @return the instances of {@link HierarchyProvider}.
   */
  private static List<HierarchyProvider> getHierarchyProviders() {
    return ExternalFactoriesHelper.getElementsInstances(
        HierarchyProvider.class,
        "org.eclipse.wb.core.componentsHierarchyProviders",
        "provider");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Binary hierarchy binding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Binds any not bound yet components to the given root {@link JavaInfo} or any of its children.
   */
  public static void bindBinaryComponents(List<JavaInfo> components) throws Exception {
    List<JavaInfo> reverseComponents = ImmutableList.copyOf(components).reverse();
    // prepare map (object -> JavaInfo)
    final Map<Object, JavaInfo> objectToModel;
    {
      objectToModel = Maps.newHashMap();
      for (JavaInfo component : components) {
        component.accept(new ObjectInfoVisitor() {
          @Override
          public void endVisit(ObjectInfo objectInfo) throws Exception {
            if (objectInfo instanceof JavaInfo) {
              JavaInfo javaInfo = (JavaInfo) objectInfo;
              for (Object componentObject : getComponentObjects(javaInfo)) {
                objectToModel.put(componentObject, javaInfo);
              }
            }
          }
        });
      }
    }
    // prepare toolkit objects for ALL hierarchies
    List<Object> objects;
    {
      objects = Lists.newArrayList();
      for (JavaInfo component : components) {
        if (component.getParent() == null) {
          for (Object componentObject : getComponentObjects(component)) {
            Assert.isNotNull(componentObject, "No object for component: %s", component);
            fillToolkitObjects(objects, componentObject);
          }
        }
      }
    }
    // bind not bound components
    for (JavaInfo component : components) {
      if (component.getParent() == null) {
        bindBinaryComponent_toParent(objectToModel, objects, component);
      }
    }
    // check still not bound component, it may be alias for some other component
    for (JavaInfo component : reverseComponents) {
      if (component.getParent() == null) {
        bindBinaryComponent_asAlias(components, component);
      }
    }
    // check still not bound component, flow to depth
    for (JavaInfo component : reverseComponents) {
      if (component.getParent() == null
          && component.getDescription().hasTrueParameter("bindBinary.toDepth")) {
        bindBinaryComponent_toDepth(objectToModel, component);
      }
    }
  }

  /**
   * Binds single binary component to parent.
   */
  private static void bindBinaryComponent_toParent(Map<Object, JavaInfo> objectToModel,
      List<Object> objects,
      JavaInfo component) throws Exception {
    for (Object object : getComponentObjects(component)) {
      int objectIndex = objects.indexOf(object);
      // go up along the hierarchy
      Object parentObject = getParentObject(object);
      for (; parentObject != null; parentObject = getParentObject(parentObject)) {
        JavaInfo parent = objectToModel.get(parentObject);
        if (parent != null) {
          boolean added = false;
          // try to add before some existing "child"
          nextChild : for (JavaInfo child : parent.getChildrenJava()) {
            for (Object childObject : getComponentObjects(child)) {
              int childIndex = objects.indexOf(childObject);
              if (objectIndex < childIndex) {
                parent.addChild(component, child);
                added = true;
                break nextChild;
              }
            }
          }
          // if not added yet, add as the last "child"
          if (!added) {
            parent.addChild(component);
          }
          // set association
          component.setAssociation(new UnknownAssociation());
          // OK, we found logical parent
          return;
        }
      }
    }
  }

  /**
   * Binds single binary component to its alias (but not to itself).
   */
  private static void bindBinaryComponent_asAlias(List<JavaInfo> components, JavaInfo component)
      throws Exception {
    for (Object object : getComponentObjects(component)) {
      for (JavaInfo otherComponent : components) {
        for (Object otherObject : getComponentObjects(otherComponent)) {
          boolean otherIsSameOrChild =
              otherComponent == component || component.isParentOf(otherComponent);
          if (otherObject == object && !otherIsSameOrChild) {
            List<AbstractComponentInfo> children =
                otherComponent.getChildren(AbstractComponentInfo.class);
            JavaInfo nextChild = !children.isEmpty() ? children.get(0) : null;
            otherComponent.addChild(component, nextChild);
            component.setAssociation(new UnknownAssociation());
            return;
          }
        }
      }
    }
  }

  /**
   * @return {@link Object}'s that represent given component. Usually this is just single object,
   *         but in GWT components has two presentations: <code>UIObject</code> and its
   *         <code>Element</code>.
   */
  private static Object[] getComponentObjects(JavaInfo component) throws Exception {
    Object object = getComponentObject0(component);
    if (object == null) {
      return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }
    for (HierarchyProvider provider : getHierarchyProviders()) {
      Object[] objects = provider.getAliases(object);
      if (objects != null) {
        return objects;
      }
    }
    return new Object[]{object};
  }

  /**
   * @return the {@link AbstractComponentInfo#getComponentObject()} or {@link JavaInfo#getObject()}.
   */
  private static Object getComponentObject0(JavaInfo component) {
    return component instanceof AbstractComponentInfo
        ? ((AbstractComponentInfo) component).getComponentObject()
        : component.getObject();
  }

  /**
   * Traverse hierarchy starting from given objects and fill {@link List} with toolkit
   * {@link Object}'s in top-down order.
   *
   * @param objects
   *          the {@link List} to add ordered toolkit objects.
   * @param object
   *          the current toolkit object.
   */
  private static void fillToolkitObjects(List<Object> objects, Object object) throws Exception {
    // add current object
    objects.add(object);
    // prepare children objects
    List<Object> children = Lists.newArrayList();
    for (HierarchyProvider provider : getHierarchyProviders()) {
      Collections.addAll(children, provider.getChildrenObjects(object));
    }
    // process children objects
    for (Object child : children) {
      fillToolkitObjects(objects, child);
    }
  }

  /**
   * Binds single binary component to parent using flow from root to depth.
   */
  private static void bindBinaryComponent_toDepth(Map<Object, JavaInfo> objectToModel,
      JavaInfo component) throws Exception {
    Object[] objects = getComponentObjects(component);
    for (Object object : objects) {
      bindBinaryComponent_toDepth(objectToModel, component, object);
    }
  }

  private static void bindBinaryComponent_toDepth(Map<Object, JavaInfo> objectToModel,
      JavaInfo parentInfo,
      Object object) throws Exception {
    for (HierarchyProvider provider : getHierarchyProviders()) {
      Object[] children = provider.getChildrenObjects(object);
      for (Object child : children) {
        JavaInfo childInfo = objectToModel.get(child);
        if (childInfo != null) {
          if (childInfo.getParent() == null) {
            parentInfo.addChild(childInfo);
            childInfo.setAssociation(new UnknownAssociation());
          }
          bindBinaryComponent_toDepth(objectToModel, childInfo, child);
        } else {
          bindBinaryComponent_toDepth(objectToModel, parentInfo, child);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding new JavaInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new component to container before some existing component.
   *
   * @param component
   *          the component to add.
   * @param associationObject
   *          the container {@link AssociationObject}.
   * @param container
   *          the parent for adding component.
   * @param nextComponent
   *          the component before which new component should be added, may be <code>null</code>, so
   *          new component will be added as last child of parent.
   */
  public static void add(JavaInfo component,
      AssociationObject associationObject,
      JavaInfo container,
      JavaInfo nextComponent) throws Exception {
    VariableSupport variableSupport = GenerationUtils.getVariableSupport(component);
    StatementGenerator statementGenerator = GenerationUtils.getStatementGenerator(component);
    add(component, variableSupport, statementGenerator, associationObject, container, nextComponent);
  }

  /**
   * Adds new component to container into given {@link StatementTarget}.
   *
   * @param component
   *          the component to add.
   * @param associationObject
   *          the container {@link AssociationObject}.
   * @param container
   *          the parent for adding component.
   * @param target
   *          the {@link StatementTarget} to add component to.
   */
  public static void addTarget(JavaInfo component,
      AssociationObject associationObject,
      JavaInfo container,
      StatementTarget target) throws Exception {
    addTarget(component, associationObject, container, null, target);
  }

  public static void addTarget(JavaInfo component,
      AssociationObject associationObject,
      JavaInfo container,
      JavaInfo nextComponent,
      StatementTarget target) throws Exception {
    VariableSupport variableSupport = GenerationUtils.getVariableSupport(component);
    StatementGenerator statementGenerator = GenerationUtils.getStatementGenerator(component);
    add(
        component,
        variableSupport,
        statementGenerator,
        associationObject,
        container,
        nextComponent,
        target);
  }

  /**
   * Adds new component to container before some existing component.
   *
   * @param component
   *          the component to add.
   * @param variableSupport
   *          the {@link VariableSupport} for new component.
   * @param statementGenerator
   *          the {@link StatementGenerator} for new component.
   * @param associationObject
   *          the container {@link AssociationObject}.
   * @param container
   *          the parent for adding component.
   * @param nextComponent
   *          the component before which new component should be added, may be <code>null</code>, so
   *          new component will be added as last child of parent.
   */
  public static void add(JavaInfo component,
      VariableSupport variableSupport,
      StatementGenerator statementGenerator,
      AssociationObject associationObject,
      JavaInfo container,
      JavaInfo nextComponent) throws Exception {
    nextComponent = getNextComponent_useComponentOrder(component, container, nextComponent);
    StatementTarget target = getTarget(container, component, nextComponent);
    add(
        component,
        variableSupport,
        statementGenerator,
        associationObject,
        container,
        nextComponent,
        target);
  }

  /**
   * @return the actual "nextComponent" that should be used to conform "component"
   *         {@link ComponentOrder} and {@link ComponentOrder}-s of existing children.
   */
  private static JavaInfo getNextComponent_useComponentOrder(JavaInfo component,
      JavaInfo container,
      JavaInfo nextComponent) throws Exception {
    ComponentOrder order = component.getDescription().getOrder();
    // may be this "component" wants to be first
    if (nextComponent == null) {
      nextComponent = order.getNextComponent_whenLast(component, container);
    }
    // may be other child wants to be last
    if (nextComponent == null) {
      for (JavaInfo parentChild : container.getChildrenJava()) {
        if (!parentChild.getDescription().getOrder().canBeBefore(component)) {
          return parentChild;
        }
      }
    }
    // no changes
    return nextComponent;
  }

  /**
   * Adds new component to container as first child.
   *
   * @param component
   *          the component to add.
   * @param associationObject
   *          the container {@link AssociationObject}.
   * @param container
   *          the parent for adding component.
   */
  public static void addFirst(JavaInfo component,
      AssociationObject associationObject,
      JavaInfo container) throws Exception {
    ComponentOrder order = ComponentOrderFirst.INSTANCE;
    JavaInfo nextComponent = order.getNextComponent_whenLast(component, container);
    StatementTarget target = getTarget(container, component, nextComponent);
    //
    VariableSupport variableSupport = GenerationUtils.getVariableSupport(component);
    StatementGenerator statementGenerator = GenerationUtils.getStatementGenerator(component);
    add(
        component,
        variableSupport,
        statementGenerator,
        associationObject,
        container,
        nextComponent,
        target);
  }

  /**
   * Adds new component to container before some existing component.
   *
   * @param component
   *          the component to add.
   * @param variableSupport
   *          the {@link VariableSupport} for new component.
   * @param statementGenerator
   *          the {@link StatementGenerator} for new component.
   * @param associationObject
   *          the container {@link AssociationObject}.
   * @param container
   *          the parent for adding component.
   * @param nextComponent
   *          the component before which new component should be added, may be <code>null</code>, so
   *          new component will be added as last child of parent.
   * @param target
   *          the {@link StatementTarget} where {@link Statement}'s (more precise - association) of
   *          new component should be added.
   */
  public static void add(JavaInfo component,
      VariableSupport variableSupport,
      StatementGenerator statementGenerator,
      AssociationObject associationObject,
      JavaInfo container,
      JavaInfo nextComponent,
      StatementTarget target) throws Exception {
    container.getBroadcastJava().addBefore(container, component);
    // setup hierarchy
    container.addChild(component, nextComponent);
    // set variable support
    component.setVariableSupport(variableSupport);
    // add component and association using StatementGenerator
    Association association = createAssociation(container, component, associationObject);
    statementGenerator.add(component, target, association);
    //
    container.getBroadcastJava().addAfter(container, component);
  }

  /**
   * When container does not have special {@link Association}, then only {@link Association} from
   * component should be used.
   * <p>
   * When container has special {@link Association} and it is required, then {@link Association}
   * from component should be mixed with it in {@link CompoundAssociation}.
   */
  private static Association createAssociation(JavaInfo container,
      JavaInfo component,
      AssociationObject associationObject) throws Exception {
    associationObject = getNotNullAssociationObject(associationObject);
    Association componentAssociation = component.getCreationSupport().getAssociation();
    Association containerAssociation = associationObject.getAssociation();
    // try to mix with container association
    if (componentAssociation != null) {
      if (associationObject.isRequired()) {
        return new CompoundAssociation(componentAssociation, containerAssociation);
      }
      return componentAssociation;
    }
    // use container association
    Assert.isNotNull(
        containerAssociation,
        "No special container association and no component creation association for %s and %s",
        container,
        component);
    return containerAssociation;
  }

  /**
   * @return the given not null {@link AssociationObject} or {@link AssociationObject} with
   *         <code>null</code> as {@link Association}.
   */
  private static AssociationObject getNotNullAssociationObject(AssociationObject associationObject) {
    if (associationObject == null) {
      associationObject = new AssociationObject(null, false);
    }
    return associationObject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Internal interface for providing {@link StatementTarget} during move.
   */
  public interface IMoveTargetProvider {
    void add() throws Exception;

    void move() throws Exception;

    StatementTarget getTarget() throws Exception;
  }

  /**
   * Moves component to new container.<br>
   * Move can be inner (i.e. move association) or adding from other container.
   *
   * @param component
   *          the component to move.
   * @param associationObject
   *          the container {@link AssociationObject}.
   * @param newParent
   *          the parent for adding component, may be old parent (so, we just move inside it), or
   *          new one, so we reparent component.
   * @param nextComponent
   *          the component before which new component should be added, may be <code>null</code>, so
   *          new component will be added as last child of parent.
   */
  public static void move(JavaInfo component,
      AssociationObject associationObject,
      JavaInfo newParent,
      JavaInfo nextComponent) throws Exception {
    // pre-condition: move component before itself, ignore
    if (nextComponent == component) {
      return;
    }
    // do move
    nextComponent = getNextComponent_useComponentOrder(component, newParent, nextComponent);
    move0(component, associationObject, newParent, nextComponent);
  }

  private static void move0(final JavaInfo component,
      final AssociationObject associationObject,
      final JavaInfo newParent,
      final JavaInfo nextComponent) throws Exception {
    IMoveTargetProvider targetProvider = new IMoveTargetProvider() {
      public void add() throws Exception {
        newParent.addChild(component, nextComponent);
      }

      public void move() throws Exception {
        newParent.moveChild(component, nextComponent);
      }

      public StatementTarget getTarget() throws Exception {
        return JavaInfoUtils.getTarget(newParent, component, nextComponent);
      }
    };
    moveProvider(component, associationObject, newParent, targetProvider);
  }

  /**
   * Moves component: inside of same container, or to new container.
   */
  public static void moveTarget(final JavaInfo component,
      AssociationObject associationObject,
      final JavaInfo newParent,
      final JavaInfo nextComponent,
      final StatementTarget target) throws Exception {
    IMoveTargetProvider targetProvider = new IMoveTargetProvider() {
      public void add() throws Exception {
        newParent.addChild(component, nextComponent);
      }

      public void move() throws Exception {
        newParent.moveChild(component, nextComponent);
      }

      public StatementTarget getTarget() throws Exception {
        return target;
      }
    };
    moveProvider(component, associationObject, newParent, targetProvider);
  }

  /**
   * Moves component to new container.<br>
   * Move can be inner (i.e. move association) or adding from other container.
   *
   * @param component
   *          the component to move.
   * @param associationObject
   *          the container {@link AssociationObject}.
   * @param newParent
   *          the parent for adding component, may be old parent (so, we just move inside it), or
   *          new one, so we reparent component.
   * @param targetProvider
   *          the {@link IMoveTargetProvider} that is used to separate move operation from target
   *          {@link Statement} and position in {@link JavaInfo} children.
   */
  public static void moveProvider(JavaInfo component,
      AssociationObject associationObject,
      JavaInfo newParent,
      IMoveTargetProvider targetProvider) throws Exception {
    Association oldAssociation = component.getAssociation();
    associationObject = getNotNullAssociationObject(associationObject);
    Association newAssociation = associationObject.getAssociation();
    boolean newAssociationRequired = associationObject.isRequired();
    //
    ObjectInfo oldParent = component.getParent();
    boolean isReparenting = oldParent != newParent;
    newParent.getBroadcastJava().moveBefore0(component, oldParent, newParent);
    newParent.getBroadcastJava().moveBefore(component, oldParent, newParent);
    // remove association
    // we do this now to avoid possible using association node as target
    if (isReparenting || newAssociationRequired) {
      materializeVariable(component);
      oldAssociation.remove();
    } else if (oldAssociation instanceof InvocationChildArrayAssociation
        || oldAssociation instanceof InvocationChildEllipsisAssociation) {
      // support array
      VariableSupport variableSupport = component.getVariableSupport();
      if (variableSupport instanceof EmptyVariableSupport) {
        ((EmptyVariableSupport) variableSupport).materialize();
      }
      oldAssociation.remove();
      newAssociationRequired = true;
    }
    // move component ASTNode's, prepare statements target
    StatementTarget componentTarget;
    {
      componentTarget = targetProvider.getTarget();
      component.getVariableSupport().ensureInstanceReadyAt(componentTarget);
    }
    // move/reparent component
    if (isReparenting) {
      oldParent.removeChild(component);
      targetProvider.add();
    } else {
      targetProvider.move();
    }
    // now, when all moving are done, prepare association target
    StatementTarget associationTarget =
        component.getVariableSupport().getAssociationTarget(componentTarget);
    // update association
    if (isReparenting || newAssociationRequired) {
      if (component.getAssociation() == null) {
        newAssociation.add(component, associationTarget, null);
      } else {
        // check, may be we need to add one more association
        if (newAssociation != null) {
          CompoundAssociation compoundAssociation =
              new CompoundAssociation(component.getAssociation());
          compoundAssociation.add(newAssociation);
          component.setAssociation(compoundAssociation);
        }
        // ask association about update to reflect new parent
        component.getAssociation().setParent(newParent);
      }
    } else {
      // for "lazy creation" - move association
      // for other variables - it will be moved with other component statements
      if (component.getVariableSupport() instanceof LazyVariableSupport) {
        component.getAssociation().move(associationTarget);
      }
    }
    // end move operation
    newParent.getBroadcastJava().moveAfter(component, oldParent, newParent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return new {@link StatementTarget} where {@link Statement}'s for new {@link JavaInfo} should
   *         be added to place them before given <code>nextChild</code>.
   *
   * @param parent
   *          the parent to which {@link JavaInfo} should be added.
   * @param child
   *          the child that will be added.
   * @param nextChild
   *          the {@link JavaInfo} before which next {@link JavaInfo} should be added, can be
   *          <code>null</code> that means that new {@link JavaInfo} should be added as last child
   *          of parent.
   */
  public static StatementTarget getTarget(JavaInfo parent, JavaInfo child, JavaInfo nextChild)
      throws Exception {
    return new ChildTargetCalculator(parent, child, nextChild).getTarget();
  }

  /**
   * @return new {@link StatementTarget} where {@link Statement}'s should be added to place them
   *         before given <code>nextChild</code>.
   *
   * @param parent
   *          the parent to which {@link JavaInfo} should be added.
   * @param nextChild
   *          the {@link JavaInfo} before which next {@link JavaInfo} should be added, can be
   *          <code>null</code> that means that new {@link JavaInfo} should be added as last child
   *          of parent.
   */
  public static StatementTarget getTarget(JavaInfo parent, JavaInfo nextChild) throws Exception {
    return getTarget(parent, null, nextChild);
  }

  /**
   * @return new {@link StatementTarget} where {@link Statement}'s should be added.
   *
   * @param parent
   *          the parent to which {@link JavaInfo} should be added.
   */
  public static StatementTarget getTarget(JavaInfo parent) throws Exception {
    return getTarget(parent, null, null);
  }

  /**
   * Ensures that given {@link JavaInfo} has some "real" {@link VariableSupport}. For example we
   * materialize {@link EmptyVariableSupport} because we may need to reference creation/association
   * {@link Statement}.
   */
  public static void materializeVariable(JavaInfo javaInfo) throws Exception {
    if (javaInfo.getVariableSupport() instanceof EmptyVariableSupport) {
      EmptyVariableSupport variableSupport = (EmptyVariableSupport) javaInfo.getVariableSupport();
      variableSupport.materialize();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation sequence
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sorts {@link JavaInfo}'s by the time when they are created on execution flow.
   */
  public static void sortComponentsByFlow(final List<JavaInfo> components) {
    if (components.isEmpty()) {
      return;
    }
    // prepare ExecutionFlowDescription
    ExecutionFlowDescription flowDescription;
    {
      JavaInfo someComponent = components.get(0);
      flowDescription = getState(someComponent).getFlowDescription();
    }
    // prepare new List, with components in execution flow order
    final List<JavaInfo> sortedComponents = Lists.newArrayList();
    ExecutionFlowUtils.visit(
        new VisitingContext(true),
        flowDescription,
        new ExecutionFlowFrameVisitor() {
          @Override
          public void postVisit(ASTNode node) {
            for (JavaInfo component : components) {
              if (component.getCreationSupport().getNode() == node) {
                sortedComponents.add(component);
                break;
              }
            }
          }
        });
    // sort original List
    Collections.sort(components, new Comparator<JavaInfo>() {
      public int compare(JavaInfo o1, JavaInfo o2) {
        int index_1 = sortedComponents.indexOf(o1);
        int index_2 = sortedComponents.indexOf(o2);
        return index_1 - index_2;
      }
    });
  }

  /**
   * Sorts {@link ASTNode}'s by the time when they are visited on execution flow. {@link ASTNode}'s
   * not included into execution flow should be removed.
   *
   * @param onEnter
   *          is <code>true</code> if {@link ASTNode} considered as visited on enter, use
   *          <code>false</code> to consider as visited on exit.
   */
  public static void sortNodesByFlow(ExecutionFlowDescription flowDescription,
      final boolean onEnter,
      final List<? extends ASTNode> nodes) {
    final List<ASTNode> sortedNodes = Lists.newArrayList();
    ExecutionFlowUtils.visit(
        new VisitingContext(true),
        flowDescription,
        new ExecutionFlowFrameVisitor() {
          @Override
          public boolean enterFrame(ASTNode node) {
            if (onEnter) {
              addSortedNode(node);
            }
            return super.enterFrame(node);
          }

          @Override
          public void leaveFrame(ASTNode node) {
            if (!onEnter) {
              addSortedNode(node);
            }
          }

          @Override
          public void preVisit(ASTNode node) {
            if (onEnter) {
              addSortedNode(node);
            }
          }

          @Override
          public void postVisit(ASTNode node) {
            if (!onEnter) {
              addSortedNode(node);
            }
          }

          private void addSortedNode(ASTNode node) {
            if (nodes.contains(node)) {
              sortedNodes.add(node);
            }
          }
        });
    // remove nodes that are not visited on execution flow
    for (Iterator<? extends ASTNode> I = nodes.iterator(); I.hasNext();) {
      ASTNode node = I.next();
      if (!sortedNodes.contains(node)) {
        I.remove();
      }
    }
    // sort original List
    Collections.sort(nodes, new Comparator<ASTNode>() {
      public int compare(ASTNode o1, ASTNode o2) {
        int index_1 = sortedNodes.indexOf(o1);
        int index_2 = sortedNodes.indexOf(o2);
        return index_1 - index_2;
      }
    });
  }

  /**
   * @return <code>true</code> if {@link JavaInfo} is already created at/before location specified
   *         by {@link NodeTarget}.
   */
  public static boolean isCreatedAtTarget(JavaInfo javaInfo, NodeTarget target) {
    // special case: JavaInfo with constructor as node
    {
      ASTNode node = javaInfo.getCreationSupport().getNode();
      if (node instanceof MethodDeclaration) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) node;
        if (methodDeclaration.isConstructor()) {
          return true;
        }
      }
    }
    // StatementTarget
    {
      StatementTarget statementTarget = target.getStatementTarget();
      if (statementTarget != null) {
        return isCreatedAt(javaInfo, statementTarget);
      }
    }
    // BodyDeclarationTarget
    {
      BodyDeclarationTarget bodyDeclarationTarget = target.getBodyDeclarationTarget();
      return isCreatedAt(javaInfo, bodyDeclarationTarget);
    }
  }

  /**
   * @return <code>true</code> if {@link JavaInfo} is already created at location specified by
   *         {@link StatementTarget}.
   */
  private static boolean isCreatedAt(JavaInfo javaInfo, StatementTarget target) {
    ExecutionFlowDescription flowDescription = getState(javaInfo).getFlowDescription();
    ASTNode javaInfoNode = javaInfo.getCreationSupport().getNode();
    // Statement
    {
      Statement statement = target.getStatement();
      if (statement != null) {
        List<ASTNode> nodes = Lists.newArrayList(statement, javaInfoNode);
        sortNodesByFlow(flowDescription, target.isBefore(), nodes);
        return nodes.get(0) == javaInfoNode;
      }
    }
    // Block
    {
      Block block = target.getBlock();
      List<ASTNode> nodes = Lists.newArrayList(block, javaInfoNode);
      sortNodesByFlow(flowDescription, target.isBefore(), nodes);
      return nodes.get(0) == javaInfoNode;
    }
  }

  /**
   * @return <code>true</code> if {@link JavaInfo} is already created at location specified by
   *         {@link BodyDeclarationTarget}.
   */
  private static boolean isCreatedAt(JavaInfo javaInfo, BodyDeclarationTarget target) {
    ExecutionFlowDescription flowDescription = getState(javaInfo).getFlowDescription();
    ASTNode javaInfoNode = javaInfo.getCreationSupport().getNode();
    // BodyDeclaration
    {
      BodyDeclaration bodyDeclaration = target.getDeclaration();
      if (bodyDeclaration != null) {
        List<ASTNode> nodes = Lists.newArrayList(bodyDeclaration, javaInfoNode);
        sortNodesByFlow(flowDescription, target.isBefore(), nodes);
        return nodes.get(0) == javaInfoNode;
      }
    }
    // TypeDeclaration
    {
      TypeDeclaration typeDeclaration = target.getType();
      List<ASTNode> nodes = Lists.newArrayList(typeDeclaration, javaInfoNode);
      sortNodesByFlow(flowDescription, target.isBefore(), nodes);
      return nodes.get(0) == javaInfoNode;
    }
  }

  /**
   * @return the {@link StatementTarget} such that all given {@link JavaInfo} are created at this
   *         target, so can be referenced.
   */
  public static StatementTarget getStatementTarget_whenAllCreated(List<? extends JavaInfo> components)
      throws Exception {
    Assert.isTrue(!components.isEmpty(), "Can not provide target for empty components list.");
    // prepare target after last component
    NodeTarget nodeTarget_afterLastComponent;
    {
      List<JavaInfo> componentsCopy = Lists.newArrayList(components);
      sortComponentsByFlow(componentsCopy);
      JavaInfo lastComponent = componentsCopy.get(componentsCopy.size() - 1);
      nodeTarget_afterLastComponent = getNodeTarget_afterCreation(lastComponent);
    }
    // convert NodeTarget into StatementTarget
    StatementTarget statementTarget = nodeTarget_afterLastComponent.getStatementTarget();
    if (statementTarget != null) {
      return statementTarget;
    }
    // probably all components created in fields, so use "this" target
    JavaInfo rootJava = components.get(0).getRootJava();
    if (rootJava.getCreationSupport() instanceof ThisCreationSupport) {
      return rootJava.getVariableSupport().getStatementTarget();
    }
    // use "flow" method
    ExecutionFlowDescription flowDescription = getState(rootJava).getFlowDescription();
    MethodDeclaration startMethod = flowDescription.getStartMethods().get(0);
    return new StatementTarget(startMethod, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // NodeTarget
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link NodeTarget} directly before creation of given {@link JavaInfo}.
   */
  public static NodeTarget getNodeTarget_beforeCreation(JavaInfo javaInfo) throws Exception {
    return getNodeTarget_relativeCreation(javaInfo, true);
  }

  /**
   * @return the {@link NodeTarget} directly after creation of given {@link JavaInfo}.
   */
  public static NodeTarget getNodeTarget_afterCreation(JavaInfo javaInfo) throws Exception {
    return getNodeTarget_relativeCreation(javaInfo, false);
  }

  /**
   * @return the {@link NodeTarget} relative creation of given {@link JavaInfo}.
   */
  private static NodeTarget getNodeTarget_relativeCreation(JavaInfo javaInfo, boolean before)
      throws Exception {
    CreationSupport creationSupport = javaInfo.getCreationSupport();
    // Special support for control of wrapper/viewer.
    {
      if (creationSupport instanceof WrapperMethodControlCreationSupport) {
        StatementTarget statementTarget = javaInfo.getVariableSupport().getStatementTarget();
        if (statementTarget != null) {
          return new NodeTarget(statementTarget);
        }
      }
    }
    // general case
    ASTNode node = creationSupport.getNode();
    {
      Statement statement = AstNodeUtils.getEnclosingStatement(node);
      if (statement != null) {
        return new NodeTarget(new StatementTarget(statement, before));
      }
    }
    {
      BodyDeclaration bodyDeclaration = AstNodeUtils.getEnclosingNode(node, BodyDeclaration.class);
      Assert.isNotNull(
          bodyDeclaration,
          "No Statement and no BodyDeclaration for %s %s in %s",
          javaInfo,
          node,
          node.getRoot());
      return new NodeTarget(new BodyDeclarationTarget(bodyDeclaration, before));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Schedules invocation of {@link IJavaInfoRendering#render()} when execution flow leaves
   * constructor.
   * <p>
   * This method should specify that this rendering is performed for some active {@link Statement}
   * in {@link ExecutionFlowDescription}. We use constructor body as such {@link Statement}.
   *
   * @see IJavaInfoRendering IJavaInfoRendering for more information.
   */
  public static void scheduleSpecialRendering(JavaInfo javaInfo) {
    if (javaInfo instanceof IJavaInfoRendering) {
      scheduleSpecialRendering(javaInfo, (IJavaInfoRendering) javaInfo);
    }
  }

  /**
   * Schedules invocation of {@link IJavaInfoRendering#render()} when execution flow leaves
   * constructor.
   * <p>
   * This method should specify that this rendering is performed for some active {@link Statement}
   * in {@link ExecutionFlowDescription}. We use constructor body as such {@link Statement}.
   *
   * @see IJavaInfoRendering IJavaInfoRendering for more information.
   */
  public static void scheduleSpecialRendering(JavaInfo javaInfo, final IJavaInfoRendering rendering) {
    if (!(javaInfo.getCreationSupport() instanceof ThisCreationSupport)) {
      return;
    }
    // prepare JavaInfo elements
    final EditorState editorState = getState(javaInfo);
    final MethodDeclaration constructor =
        (MethodDeclaration) javaInfo.getCreationSupport().getNode();
    final Statement statement = constructor.getBody();
    // add rendering listener
    javaInfo.addBroadcastListener(new EvaluationEventListener() {
      @Override
      public void leaveFrame(ASTNode node) throws Exception {
        if (node == constructor) {
          ExecutionFlowDescription flowDescription = editorState.getFlowDescription();
          flowDescription.enterStatement(statement);
          try {
            rendering.render();
          } finally {
            flowDescription.leaveStatement(statement);
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Wrappers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sometimes real component is created using some wrapper, for example as {@link Viewer} creates
   * {@link Control}. We should drop {@link Viewer} in code, but layout policies should see that we
   * drop {@link Control}. So we should "extract" {@link Control} {@link JavaInfo} from
   * {@link Viewer} {@link JavaInfo}.
   *
   * @return the wrapped {@link JavaInfo} or original {@link JavaInfo} if there are no wrapping.
   */
  public static JavaInfo getWrapped(JavaInfo original) throws Exception {
    if (original instanceof IWrapperInfo) {
      IWrapperInfo wrapperInfo = (IWrapperInfo) original;
      return wrapperInfo.getWrapper().getWrappedInfo();
    }
    return original;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Deleting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Deletes children and related nodes of given {@link JavaInfo}.<br>
   *
   * @param removeFromParent
   *          is <code>true</code>, also removes from from parent. Usually it is <code>true</code>,
   *          but exposed components can not be really deleted, because they belong to its host, so
   *          only children and related nodes can be removed.
   */
  public static void deleteJavaInfo(JavaInfo javaInfo, boolean removeFromParent) throws Exception {
    // delete children
    List<ObjectInfo> children = ImmutableList.copyOf(javaInfo.getChildren());
    for (ObjectInfo child : children) {
      // There are cases when children of some parent are "linked", so one deletes other on delete.
      // So, we should check, may be child is already deleted.
      if (!child.isDeleted()) {
        child.delete();
      }
    }
    // remove statements of related nodes
    for (ASTNode node : javaInfo.getRelatedNodes()) {
      // we can have several related nodes in same Statement, so may be already removed
      if (AstNodeUtils.isDanglingNode(node)) {
        continue;
      }
      // may be creation node, when we don't want to remove it
      if (!removeFromParent && node == javaInfo.getCreationSupport().getNode()) {
        continue;
      }
      // do remove
      Statement statement = AstNodeUtils.getEnclosingStatement(node);
      if (statement != null) {
        javaInfo.getEditor().removeStatement(statement);
      }
    }
    // remove from parent
    if (removeFromParent) {
      javaInfo.getParent().removeChild(javaInfo);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reparse on dependency change
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String DEPENDENCY_KEY = "JavaInfo.dependencies";

  /**
   * @return <code>true</code> if dependency information for hierarchy was prepared.
   */
  public static boolean hasDependencyInformation(JavaInfo javaInfo) throws Exception {
    return javaInfo.getEditor().getGlobalValue(DEPENDENCY_KEY) != null;
  }

  /**
   * @return <code>true</code> if one of the referenced types was changed.
   */
  @SuppressWarnings("unchecked")
  public static boolean isDependencyChanged(JavaInfo javaInfo) throws Exception {
    Map<IResource, Long> dependencies =
        (Map<IResource, Long>) javaInfo.getEditor().getGlobalValue(DEPENDENCY_KEY);
    if (dependencies != null) {
      for (Map.Entry<IResource, Long> entry : dependencies.entrySet()) {
        if (entry.getKey().getModificationStamp() != entry.getValue()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Remembers dependency information for given {@link JavaInfo}, i.e. time stamps for referenced
   * types.
   */
  public static void rememberDependency(JavaInfo javaInfo) throws Exception {
    AstEditor editor = javaInfo.getEditor();
    // prepare dependencies
    Map<IResource, Long> dependencies = Maps.newHashMap();
    addDependencies(dependencies, Sets.<String>newTreeSet(), editor.getModelUnit(), 0);
    // don't use this compilation unit resource
    dependencies.remove(editor.getModelUnit().getResource());
    // remember dependencies
    editor.putGlobalValue(DEPENDENCY_KEY, dependencies);
  }

  /**
   * Adds dependencies for given {@link ICompilationUnit}.
   */
  private static void addDependencies(final Map<IResource, Long> dependencies,
      final Set<String> checkedTypes,
      final ICompilationUnit modelUnit,
      final int level) throws Exception {
    if (level < 5 && dependencies.size() < 100) {
      final IJavaProject javaProject = modelUnit.getJavaProject();
      // add current resource
      {
        IResource resource = modelUnit.getResource();
        dependencies.put(resource, resource.getModificationStamp());
      }
      // add references
      CompilationUnit astUnit = CodeUtils.parseCompilationUnit(modelUnit);
      astUnit.accept(new ASTVisitor() {
        @Override
        public void endVisit(QualifiedName node) {
          addNewType(node.resolveTypeBinding());
        }

        @Override
        public void endVisit(SimpleName node) {
          addNewType(node.resolveTypeBinding());
        }

        private void addNewType(final ITypeBinding binding) {
          if (binding == null) {
            return;
          }
          ExecutionUtils.runIgnore(new RunnableEx() {
            public void run() throws Exception {
              String typeName = AstNodeUtils.getFullyQualifiedName(binding, false);
              if (typeName.indexOf('.') != -1 && !checkedTypes.contains(typeName)) {
                checkedTypes.add(typeName);
                IType type = javaProject.findType(typeName);
                if (type != null && !type.isBinary()) {
                  addDependencies(dependencies, checkedTypes, type.getCompilationUnit(), level + 1);
                }
              }
            }
          });
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditorState
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link EditorState} instance for given {@link JavaInfo}.
   */
  public static EditorState getState(JavaInfo javaInfo) {
    return EditorState.get(javaInfo.getEditor());
  }

  /**
   * @return the {@link ClassLoader} for given {@link JavaInfo}.
   */
  public static ClassLoader getClassLoader(JavaInfo javaInfo) {
    return getState(javaInfo).getEditorLoader();
  }
}
