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
package org.eclipse.wb.internal.core.parser;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.WrapperMethodInfo;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.association.FactoryParentAssociation;
import org.eclipse.wb.core.model.association.ImplicitFactoryArgumentAssociation;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CastedSuperInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.InvocationChainCreationSupport;
import org.eclipse.wb.internal.core.model.creation.SuperInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.AbstractInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProviderFactory;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanInfo;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.exception.MultipleConstructorsError;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.BundleClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.IByteCodeProcessor;
import org.eclipse.wb.internal.core.utils.reflect.IClassLoaderInitializer;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * Abstract implementation of {@link IParseFactory}.
 *
 * @author scheglov_ke
 * @coverage core.model.parser
 */
public abstract class AbstractParseFactory implements IParseFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IParseFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public ParseRootContext getRootContext(AstEditor editor,
      TypeDeclaration typeDeclaration,
      ITypeBinding typeBinding) throws Exception {
    return null;
  }

  public JavaInfo create(AstEditor editor, Expression expression) throws Exception {
    // (Type) super.someMethodInvocation()
    if (expression instanceof CastExpression) {
      CastExpression castExpression = (CastExpression) expression;
      if (castExpression.getExpression() instanceof SuperMethodInvocation) {
        // prepare component Class
        Class<?> componentClass;
        {
          String componentClassName = AstNodeUtils.getFullyQualifiedName(expression, true);
          componentClass = EditorState.get(editor).getEditorLoader().loadClass(componentClassName);
        }
        // create JavaInfo
        return JavaInfoUtils.createJavaInfo(
            editor,
            componentClass,
            new CastedSuperInvocationCreationSupport(castExpression));
      }
    }
    // super.someMethodInvocation()
    if (expression instanceof SuperMethodInvocation
        && expression.getLocationInParent() != CastExpression.EXPRESSION_PROPERTY) {
      SuperMethodInvocation invocation = (SuperMethodInvocation) expression;
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(invocation);
      if (isToolkitObject(editor, typeBinding)) {
        // prepare component Class
        Class<?> componentClass;
        {
          String componentClassName = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
          componentClass = EditorState.get(editor).getEditorLoader().loadClass(componentClassName);
        }
        // create JavaInfo
        return JavaInfoUtils.createJavaInfo(
            editor,
            componentClass,
            new SuperInvocationCreationSupport(invocation));
      }
    }
    // can not create model
    return null;
  }

  public JavaInfo create(final AstEditor editor,
      ClassInstanceCreation creation,
      final IMethodBinding methodBinding,
      ITypeBinding typeBinding,
      Expression[] arguments,
      JavaInfo[] argumentInfos) throws Exception {
    final Class<?> creationClass = getClass(editor, typeBinding);
    if (creationClass == null) {
      return null;
    }
    // non-visual bean
    {
      NonVisualBeanInfo nonVisualInfo = NonVisualBeanContainerInfo.getNonVisualInfo(creation);
      if (nonVisualInfo != null) {
        CreationSupport creationSupport = new ConstructorCreationSupport(creation);
        JavaInfo javaInfo = JavaInfoUtils.createJavaInfo(editor, creationClass, creationSupport);
        nonVisualInfo.setJavaInfo(javaInfo);
        return javaInfo;
      }
    }
    // instance
    {
      JavaInfo javaInfo =
          createInstance(
              editor,
              typeBinding,
              new RunnableObjectEx<AbstractInvocationDescription>() {
                public AbstractInvocationDescription runObject() throws Exception {
                  ComponentDescription description =
                      ComponentDescriptionHelper.getDescription(editor, creationClass);
                  return description.getConstructor(methodBinding);
                }
              },
              argumentInfos,
              new ConstructorCreationSupport(creation));
      if (javaInfo != null) {
        return javaInfo;
      }
    }
    // instance factory
    if (InstanceFactoryInfo.isFactory(editor, creationClass)) {
      CreationSupport creationSupport = new ConstructorCreationSupport(creation);
      return InstanceFactoryInfo.createFactory(editor, creationClass, creationSupport);
    }
    // can not create model
    return null;
  }

  public JavaInfo create(AstEditor editor,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      Expression[] arguments,
      JavaInfo expressionInfo,
      JavaInfo[] argumentInfos,
      IJavaInfoParseResolver javaInfoResolver) throws Exception {
    // check for "implicit factory" component creation
    if (expressionInfo != null) {
      MethodDescription methodDescription =
          expressionInfo.getDescription().getMethod(methodBinding);
      if (methodDescription != null && methodDescription.hasTrueTag("implicitFactory")) {
        JavaInfo javaInfo =
            createImplicitFactory(
                editor,
                invocation,
                expressionInfo,
                argumentInfos,
                methodDescription);
        if (javaInfo != null) {
          return javaInfo;
        }
      }
    }
    // check for "invocation chain" pattern
    {
      // prepare signatures chain
      String allSignatures = "";
      MethodInvocation currentInvocation = invocation;
      while (true) {
        allSignatures =
            AstNodeUtils.getMethodSignature(currentInvocation)
                + (allSignatures.length() != 0 ? "." : "")
                + allSignatures;
        if (currentInvocation.getExpression() instanceof MethodInvocation) {
          currentInvocation = (MethodInvocation) currentInvocation.getExpression();
        } else {
          break;
        }
      }
      // use last Expression as host JavaInfo
      JavaInfo hostJavaInfo =
          currentInvocation == invocation
              ? expressionInfo
              : javaInfoResolver.getJavaInfo(currentInvocation.getExpression());
      if (hostJavaInfo != null) {
        String componentClassName =
            JavaInfoUtils.getParameter(hostJavaInfo, "invocationChain: " + allSignatures);
        if (componentClassName != null) {
          // if no class name, use method return type
          if (componentClassName.length() == 0) {
            componentClassName =
                AstNodeUtils.getFullyQualifiedName(methodBinding.getReturnType(), true);
          }
          // create JavaInfo
          CreationSupport creationSupport =
              new InvocationChainCreationSupport(invocation, allSignatures);
          JavaInfo javaInfo =
              JavaInfoUtils.createJavaInfo(editor, componentClassName, creationSupport);
          javaInfo.setAssociation(new EmptyAssociation());
          hostJavaInfo.addChild(javaInfo);
          return javaInfo;
        }
      }
    }
    // check for internal "void factory" component creation
    if (expressionInfo != null) {
      MethodDescription methodDescription =
          expressionInfo.getDescription().getMethod(methodBinding);
      if (methodDescription != null) {
        String voidCreationSupportClassName =
            methodDescription.getTag("voidFactory.creationSupport");
        String voidComponentClassName = methodDescription.getTag("voidFactory.componentClass");
        if (!StringUtils.isEmpty(voidCreationSupportClassName)
            && !StringUtils.isEmpty(voidComponentClassName)) {
          if (!createVoidFactory_hasRequiredJavaInfo(methodDescription, argumentInfos)) {
            return null;
          }
          // prepare CreationSupport
          CreationSupport creationSupport;
          {
            Class<?> voidCreationSupportClass =
                DescriptionHelper.loadModelClass(voidCreationSupportClassName);
            Constructor<?> constructor =
                ReflectionUtils.getConstructor(
                    voidCreationSupportClass,
                    JavaInfo.class,
                    MethodDescription.class,
                    MethodInvocation.class,
                    JavaInfo[].class);
            Assert.isNotNull(constructor, "Can not find constructor "
                + voidCreationSupportClassName
                + "(JavaInfo,MethodDescription,MethodInvocation,JavaInfo[])");
            creationSupport =
                (CreationSupport) constructor.newInstance(
                    expressionInfo,
                    methodDescription,
                    invocation,
                    argumentInfos);
          }
          Class<?> componentClass =
              EditorState.get(editor).getEditorLoader().loadClass(voidComponentClassName);
          //
          JavaInfo javaInfo =
              createVoidFactory(
                  editor,
                  invocation,
                  expressionInfo,
                  argumentInfos,
                  methodDescription,
                  componentClass,
                  creationSupport);
          if (javaInfo != null) {
            return javaInfo;
          }
        }
      }
    }
    // check for "instance factory" component creation
    if (expressionInfo instanceof InstanceFactoryInfo) {
      InstanceFactoryInfo factoryInfo = (InstanceFactoryInfo) expressionInfo;
      FactoryMethodDescription description = getFactory(editor, methodBinding, false);
      if (description != null) {
        JavaInfo javaInfo =
            createInstanceFactory(
                editor,
                invocation,
                methodBinding,
                argumentInfos,
                factoryInfo,
                description);
        if (javaInfo != null) {
          return javaInfo;
        }
      }
    }
    // check for "static factory" component creation
    if (AstNodeUtils.isStatic(methodBinding)) {
      FactoryMethodDescription description = getFactory(editor, methodBinding, true);
      if (description != null) {
        JavaInfo javaInfo =
            createStaticFactory(editor, invocation, methodBinding, argumentInfos, description);
        if (javaInfo != null) {
          return javaInfo;
        }
      }
    }
    // no JavaInfo for MethodInvocation
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Class} loaded using {@link ClassLoader} of given {@link AstEditor}. May
   *         return <code>null</code> if class can not be loaded.
   */
  protected static Class<?> getClass(AstEditor editor, ITypeBinding typeBinding) {
    try {
      ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
      String componentClassName = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
      Class<?> clazz = classLoader.loadClass(componentClassName);
      // we don't need inner Class
      if (clazz.getEnclosingClass() != null
          && !java.lang.reflect.Modifier.isStatic(clazz.getModifiers())) {
        return null;
      }
      // good Class
      return clazz;
    } catch (Throwable e) {
      return null;
    }
  }

  /**
   * @return the {@link Class} loaded using {@link ClassLoader} of given {@link AstEditor}. We use
   *         this method to load "super" {@link Class}, so if any exception happens, we consider it
   *         as serious and throw fatal exception.
   */
  protected static Class<?> getSuperClass(AstEditor editor, ITypeBinding typeBinding) {
    String componentClassName = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
    try {
      ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
      return classLoader.loadClass(componentClassName);
    } catch (Throwable e) {
      throw new DesignerException(ICoreExceptionConstants.PARSER_NO_SUPER_CLASS,
          e,
          componentClassName);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Other
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link ITypeBinding} is Toolkit object.
   */
  public boolean isToolkitObject(AstEditor editor, ITypeBinding typeBinding) throws Exception {
    return false;
  }

  /**
   * We don't support parsing static/instance factories. So, we should fail and show explicit
   * warning.
   */
  protected static void failIfFactory(AstEditor editor,
      TypeDeclaration typeDeclaration,
      ITypeBinding typeBinding) throws Exception {
    String typeName = AstNodeUtils.getFullyQualifiedName(typeDeclaration, true);
    if (FactoryDescriptionHelper.isFactoryClass(editor, typeName)) {
      throw new DesignerException(ICoreExceptionConstants.PARSER_FACTORY_NOT_SUPPORTED);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the existing constructor or newly added default constructor.
   */
  protected static MethodDeclaration getConstructor(AstEditor editor,
      TypeDeclaration typeDeclaration) throws Exception {
    MethodDeclaration constructor;
    try {
      constructor = ExecutionFlowUtils.getExecutionFlowConstructor(typeDeclaration);
    } catch (MultipleConstructorsError e) {
      throw new MultipleConstructorsError(editor, typeDeclaration);
    }
    // if no constructor, add default
    if (constructor == null) {
      String header = "public " + typeDeclaration.getName().getIdentifier() + "()";
      constructor =
          editor.addMethodDeclaration(
              header,
              Collections.<String>emptyList(),
              new BodyDeclarationTarget(typeDeclaration, true));
      constructor.setConstructor(true);
      // commit changes into compilation unit
      editor.commitChanges();
      // we should also save changes, else IMethod's will have invalid source ranges
      editor.getModelUnit().save(null, true);
    }
    return constructor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Factories
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link FactoryMethodDescription} for given {@link IMethodBinding} or <code>null</code>
   *         if there are no valid factory.
   */
  public static FactoryMethodDescription getFactory(AstEditor editor,
      IMethodBinding methodBinding,
      boolean forStatic) throws Exception {
    // prepare factory class
    Class<?> factoryClass;
    {
      ITypeBinding declaringClass = methodBinding.getDeclaringClass();
      factoryClass = getClass(editor, declaringClass);
      if (factoryClass == null) {
        return null;
      }
    }
    // prepare method signature
    String methodSignature = AstNodeUtils.getMethodSignature(methodBinding);
    // get static factory description
    FactoryMethodDescription description =
        FactoryDescriptionHelper.getDescription(editor, factoryClass, methodSignature, forStatic);
    if (description != null && description.isFactory()) {
      return description;
    }
    // no valid factory
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creating JavaInfo for various Classes
  //
  ////////////////////////////////////////////////////////////////////////////
  private JavaInfo createInstance(AstEditor editor,
      ITypeBinding typeBinding,
      RunnableObjectEx<AbstractInvocationDescription> methodDescriptionProvider,
      JavaInfo[] argumentInfos,
      CreationSupport creationSupport) throws Exception {
    Class<?> objectClass = getClass(editor, typeBinding);
    if (objectClass == null) {
      return null;
    }
    // instance factory
    if (InstanceFactoryInfo.isFactory(editor, objectClass)) {
      return InstanceFactoryInfo.createFactory(editor, objectClass, creationSupport);
    }
    // toolkit
    if (isToolkitObject(editor, typeBinding)) {
      return JavaInfoUtils.createJavaInfo(editor, objectClass, creationSupport);
    }
    // wrappers
    if (WrapperMethodInfo.isWrapper(editor, objectClass)) {
      WrapperMethodInfo wrapperJavaInfo =
          (WrapperMethodInfo) JavaInfoUtils.createJavaInfo(editor, objectClass, creationSupport);
      AbstractInvocationDescription methodDescription = methodDescriptionProvider.runObject();
      wrapperJavaInfo.getWrapper().configureWrapper(methodDescription, argumentInfos);
      wrapperJavaInfo.setAssociation(new ConstructorParentAssociation());
      return wrapperJavaInfo;
    }
    // unknown type
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creating JavaInfo for various Factories
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Processing void implicit factory
   */
  protected JavaInfo createVoidFactory(AstEditor editor,
      MethodInvocation invocation,
      JavaInfo expressionInfo,
      JavaInfo[] argumentInfos,
      MethodDescription methodDescription,
      Class<?> componentClass,
      CreationSupport creationSupport) throws Exception {
    JavaInfo javaInfo = JavaInfoUtils.createJavaInfo(editor, componentClass, creationSupport);
    javaInfo.setVariableSupport(new VoidInvocationVariableSupport(javaInfo));
    javaInfo.setAssociation(new InvocationVoidAssociation(invocation));
    expressionInfo.addChild(javaInfo);
    return javaInfo;
  }

  /**
   * If parameter should be {@link JavaInfo}, this should be specified in
   * {@link ParameterDescription} using "voidFactory.requiredJavaInfo" tag.
   */
  private static boolean createVoidFactory_hasRequiredJavaInfo(MethodDescription methodDescription,
      JavaInfo[] argumentInfos) {
    List<ParameterDescription> parameters = methodDescription.getParameters();
    for (ParameterDescription parameter : parameters) {
      if (parameter.hasTrueTag("voidFactory.requiredJavaInfo")) {
        int index = parameter.getIndex();
        if (argumentInfos[index] == null) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Processing static factory
   */
  protected JavaInfo createStaticFactory(AstEditor editor,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      JavaInfo[] argumentInfos,
      FactoryMethodDescription factoryMethodDescription) throws Exception {
    JavaInfo javaInfo =
        createStaticFactory_JavaInfo(
            editor,
            invocation,
            methodBinding,
            argumentInfos,
            factoryMethodDescription);
    // check for parent
    if (javaInfo != null) {
      for (ParameterDescription parameter : factoryMethodDescription.getParameters()) {
        if (parameter.isParent()) {
          JavaInfo parent = argumentInfos[parameter.getIndex()];
          if (parent != null) {
            javaInfo.setAssociation(new FactoryParentAssociation(invocation));
            if (javaInfo.getParent() == null) {
              parent.addChild(javaInfo);
            }
          } else {
            String message = "No parent model for " + invocation;
            EditorState.get(editor).addWarning(new EditorWarning(message));
          }
        }
      }
    }
    // done
    return javaInfo;
  }

  private JavaInfo createStaticFactory_JavaInfo(AstEditor editor,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      JavaInfo[] argumentInfos,
      FactoryMethodDescription factoryMethodDescription) throws Exception {
    Class<?> objectClass = factoryMethodDescription.getReturnClass();
    CreationSupport creationSupport =
        new StaticFactoryCreationSupport(factoryMethodDescription, invocation);
    // instance factory
    if (InstanceFactoryInfo.isFactory(editor, objectClass)) {
      return InstanceFactoryInfo.createFactory(editor, objectClass, creationSupport);
    }
    // toolkit
    if (isToolkitObject(editor, methodBinding.getReturnType())) {
      ComponentDescription componentDescription =
          ComponentDescriptionHelper.getDescription(editor, factoryMethodDescription);
      return JavaInfoUtils.createJavaInfo(editor, componentDescription, creationSupport);
    }
    // wrappers
    if (WrapperMethodInfo.isWrapper(editor, objectClass)) {
      WrapperMethodInfo wrapperJavaInfo =
          (WrapperMethodInfo) JavaInfoUtils.createJavaInfo(editor, objectClass, creationSupport);
      wrapperJavaInfo.getWrapper().configureWrapper(factoryMethodDescription, argumentInfos);
      return wrapperJavaInfo;
    }
    // check for non-visual bean
    NonVisualBeanInfo nonVisualInfo = NonVisualBeanContainerInfo.getNonVisualInfo(invocation);
    if (nonVisualInfo != null) {
      JavaInfo javaInfo = JavaInfoUtils.createJavaInfo(editor, objectClass, creationSupport);
      nonVisualInfo.setJavaInfo(javaInfo);
      return javaInfo;
    }
    // unknown
    return null;
  }

  /**
   * Processing instance factory
   *
   * @param methodBinding
   */
  protected JavaInfo createInstanceFactory(AstEditor editor,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      JavaInfo[] argumentInfos,
      InstanceFactoryInfo factoryInfo,
      FactoryMethodDescription description) throws Exception {
    Class<?> returnClass = description.getReturnClass();
    // ignore interfaces
    if (returnClass.isInterface()) {
      return null;
    }
    // create JavaInfo
    JavaInfo javaInfo =
        JavaInfoUtils.createJavaInfo(
            editor,
            returnClass,
            new InstanceFactoryCreationSupport(factoryInfo, description, invocation));
    // try to associate with parent
    for (ParameterDescription parameter : description.getParameters()) {
      if (parameter.isParent()) {
        JavaInfo parameterJavaInfo = argumentInfos[parameter.getIndex()];
        if (parameterJavaInfo != null) {
          javaInfo.setAssociation(new FactoryParentAssociation(invocation));
          parameterJavaInfo.addChild(javaInfo);
        }
      }
    }
    // check for non-visual bean
    NonVisualBeanInfo nonVisualInfo = NonVisualBeanContainerInfo.getNonVisualInfo(invocation);
    if (nonVisualInfo != null) {
      nonVisualInfo.setJavaInfo(javaInfo);
    }
    // component created using instance factory
    return javaInfo;
  }

  /**
   * Processing implicit factory
   */
  protected JavaInfo createImplicitFactory(AstEditor editor,
      MethodInvocation invocation,
      JavaInfo expressionInfo,
      JavaInfo[] argumentInfos,
      MethodDescription methodDescription) throws Exception {
    JavaInfo javaInfo =
        JavaInfoUtils.createJavaInfo(
            editor,
            methodDescription.getReturnClass(),
            new ImplicitFactoryCreationSupport(methodDescription, invocation));
    //  try to find parent
    for (ParameterDescription parameter : methodDescription.getParameters()) {
      JavaInfo parameterJavaInfo = argumentInfos[parameter.getIndex()];
      if (parameter.isParent()) {
        javaInfo.setAssociation(new FactoryParentAssociation(invocation));
        parameterJavaInfo.addChild(javaInfo);
      }
      if (parameter.hasTrueTag("implicitFactory.child")) {
        Assert.isNotNull(
            parameterJavaInfo,
            "Argument %d in %s %s should be not-null JavaInfo.",
            parameter.getIndex(),
            methodDescription,
            invocation);
        parameterJavaInfo.setAssociation(new ImplicitFactoryArgumentAssociation(invocation,
            javaInfo));
        javaInfo.addChild(parameterJavaInfo);
      }
    }
    // if no association yet, associate with "expression"
    if (javaInfo.getParent() == null) {
      javaInfo.setAssociation(new InvocationVoidAssociation(invocation));
      expressionInfo.addChild(javaInfo);
    }
    return javaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the toolkit id.
   */
  protected abstract String getToolkitId();

  /**
   * Creates complex {@link ClassLoader} for given {@link AstEditor}.
   */
  protected void initializeClassLoader(AstEditor editor) throws Exception {
    EditorState editorState = EditorState.get(editor);
    if (editorState.isInitialized()) {
      return;
    }
    String toolkitId = getToolkitId();
    // set ClassLoader
    ClassLoader classLoader = getClassLoader(editor);
    editorState.initialize(toolkitId, classLoader);
    initializeClassLoader(classLoader, toolkitId);
    // configure
    IJavaProject javaProject = editor.getJavaProject();
    runUserConfigurationScripts(editorState, javaProject);
    configureDescriptionVersionsProviders(editorState, javaProject);
  }

  /**
   * @return the {@link ClassLoader} to use for given {@link AstEditor}.
   */
  protected ClassLoader getClassLoader(AstEditor editor) throws Exception {
    CompositeClassLoader parentClassLoader = createClassLoader_parent(editor);
    initializeClassLoader_parent(editor, parentClassLoader);
    ClassLoader projectClassLoader = createClassLoader_project(editor, parentClassLoader);
    return createCompositeLoader(projectClassLoader, getToolkitId());
  }

  /**
   * Create {@link ClassLoader}'s that should be in parent of "project" {@link ClassLoader}.
   */
  protected CompositeClassLoader createClassLoader_parent(AstEditor editor) throws Exception {
    return new CompositeClassLoader();
  }

  protected void initializeClassLoader_parent(AstEditor editor,
      CompositeClassLoader parentClassLoader) throws Exception {
    // add required
    parentClassLoader.add(
        new BundleClassLoader("org.eclipse.wb.runtime"),
        ImmutableList.of("net.sf.cglib."));
    // add class loaders for "classLoader-bundle" contributions
    List<IConfigurationElement> toolkitElements =
        DescriptionHelper.getToolkitElements(getToolkitId());
    for (IConfigurationElement toolkitElement : toolkitElements) {
      IConfigurationElement[] contributorElements =
          toolkitElement.getChildren("classLoader-bundle");
      for (IConfigurationElement contributorElement : contributorElements) {
        Bundle bundle = getExistingBundle(contributorElement);
        List<String> namespaces = getBundleClassLoaderNamespaces(contributorElement);
        ClassLoader classLoader = new BundleClassLoader(bundle);
        parentClassLoader.add(classLoader, namespaces);
      }
    }
  }

  private static List<String> getBundleClassLoaderNamespaces(IConfigurationElement contributorElement) {
    String namespacesString = contributorElement.getAttribute("namespaces");
    if (namespacesString != null) {
      return ImmutableList.copyOf(StringUtils.split(namespacesString));
    }
    return null;
  }

  /**
   * Create "project" {@link ClassLoader}.
   */
  protected ClassLoader createClassLoader_project(AstEditor editor,
      CompositeClassLoader parentClassLoader) throws Exception {
    return ProjectClassLoader.create(parentClassLoader, editor.getJavaProject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the composite {@link ClassLoader} based on given main {@link ClassLoader} with addition
   *         of {@link BundleClassLoader}'s and {@link IByteCodeProcessor}'s.
   */
  protected static CompositeClassLoader createCompositeLoader(ClassLoader mainClassLoader,
      String toolkitId) throws Exception {
    List<IConfigurationElement> toolkitElements = DescriptionHelper.getToolkitElements(toolkitId);
    CompositeClassLoader compositeClassLoader = new CompositeClassLoader();
    // add project class loader
    compositeClassLoader.add(mainClassLoader, null);
    // add processors for "classPath-byteCode-processor"
    if (mainClassLoader instanceof ProjectClassLoader) {
      ProjectClassLoader projectClassLoader = (ProjectClassLoader) mainClassLoader;
      for (IConfigurationElement toolkitElement : toolkitElements) {
        IConfigurationElement[] contributorElements =
            toolkitElement.getChildren("classPath-byteCode-processor");
        for (IConfigurationElement contributorElement : contributorElements) {
          IByteCodeProcessor processor =
              (IByteCodeProcessor) contributorElement.createExecutableExtension("processor");
          projectClassLoader.add(processor);
        }
      }
    }
    // add class loaders for "classLoader-library" contributions
    for (IConfigurationElement toolkitElement : toolkitElements) {
      IConfigurationElement[] contributorElements =
          toolkitElement.getChildren("classLoader-library");
      if (contributorElements.length != 0) {
        URL[] urls = new URL[contributorElements.length];
        for (int i = 0; i < contributorElements.length; i++) {
          IConfigurationElement contributorElement = contributorElements[i];
          Bundle bundle = getExistingBundle(contributorElement);
          // prepare URL for JAR
          String jarPath = contributorElement.getAttribute("jar");
          URL jarEntry = bundle.getEntry(jarPath);
          Assert.isNotNull(jarEntry, "Unable to find %s in %s", jarPath, bundle.getSymbolicName());
          urls[i] = FileLocator.toFileURL(jarEntry);
        }
        // add ClassLoader with all libraries
        compositeClassLoader.add(new URLClassLoader(urls, mainClassLoader), null);
      }
    }
    // return final ClassLoader
    return compositeClassLoader;
  }

  /**
   * @return {@link Bundle} specified in attribute "bundle".
   */
  private static Bundle getExistingBundle(IConfigurationElement contributorElement) {
    String bundleId = ExternalFactoriesHelper.getRequiredAttribute(contributorElement, "bundle");
    return getExistingBundle(bundleId);
  }

  private static Bundle getExistingBundle(String bundleId) {
    Bundle bundle = Platform.getBundle(bundleId);
    Assert.isNotNull(bundle, "Unable to find Bundle %s", bundleId);
    return bundle;
  }

  /**
   * Initializes {@link ClassLoader} with registered {@link IClassLoaderInitializer}'s.
   */
  protected static void initializeClassLoader(ClassLoader classLoader, String toolkitId)
      throws Exception {
    List<IConfigurationElement> elements =
        ExternalFactoriesHelper.getElements(
            "org.eclipse.wb.core.java.classLoaderInitializers",
            "initializer");
    for (IConfigurationElement element : elements) {
      if (ExternalFactoriesHelper.getRequiredAttribute(element, "toolkit").equals(toolkitId)) {
        IClassLoaderInitializer initializer =
            ExternalFactoriesHelper.createExecutableExtension(element, "class");
        initializer.initialize(classLoader);
      }
    }
  }

  /**
   * Deinitializes {@link ClassLoader} with registered {@link IClassLoaderInitializer}'s.
   */
  public static void deinitializeClassLoader(ClassLoader classLoader, String toolkitId)
      throws Exception {
    List<IConfigurationElement> elements =
        ExternalFactoriesHelper.getElements(
            "org.eclipse.wb.core.java.classLoaderInitializers",
            "initializer");
    for (IConfigurationElement element : elements) {
      if (ExternalFactoriesHelper.getRequiredAttribute(element, "toolkit").equals(toolkitId)) {
        IClassLoaderInitializer initializer =
            ExternalFactoriesHelper.createExecutableExtension(element, "class");
        initializer.deinitialize(classLoader);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionVersionsProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Installs {@link IDescriptionVersionsProvider}'s.
   */
  protected static void configureDescriptionVersionsProviders(EditorState editorState,
      IJavaProject javaProject) throws Exception {
    ClassLoader classLoader = editorState.getEditorLoader();
    List<IDescriptionVersionsProviderFactory> factories =
        ExternalFactoriesHelper.getElementsInstances(
            IDescriptionVersionsProviderFactory.class,
            "org.eclipse.wb.core.descriptionVersionsProviderFactories",
            "factory");
    for (IDescriptionVersionsProviderFactory factory : factories) {
      // versions
      editorState.addVersions(factory.getVersions(javaProject, classLoader));
      // version providers
      {
        IDescriptionVersionsProvider provider = factory.getProvider(javaProject, classLoader);
        if (provider != null) {
          editorState.addDescriptionVersionsProvider(provider);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration scripts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Runs all <code>wbp-meta/ConfigureClassLoader.mvel</code> scripts to configure
   * {@link ClassLoader}.
   */
  protected static void runUserConfigurationScripts(EditorState editorState,
      IJavaProject javaProject) throws Exception {
    ClassLoader classLoader = editorState.getEditorLoader();
    List<IFile> scriptFiles =
        ProjectUtils.findFiles(javaProject, "wbp-meta/ConfigureClassLoader.mvel");
    for (IFile scriptFile : scriptFiles) {
      String script = IOUtils2.readString(scriptFile);
      try {
        ScriptUtils.evaluate(classLoader, script);
      } catch (Throwable e) {
        String message =
            MessageFormat.format(
                "Can not execute {0}\n{1}",
                scriptFile.getFullPath().toPortableString(),
                script);
        editorState.addWarning(new EditorWarning(message, e));
      }
    }
  }
}
