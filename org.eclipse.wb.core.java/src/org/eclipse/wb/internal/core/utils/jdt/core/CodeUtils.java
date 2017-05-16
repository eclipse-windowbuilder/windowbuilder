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
package org.eclipse.wb.internal.core.utils.jdt.core;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.pde.ReflectivePDE;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.osgi.service.resolver.BundleDescription;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains different utilities for working with Java model elements.
 *
 * @author scheglov_ke
 * @coverage core.util.jdt
 */
public class CodeUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private CodeUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parses given {@link ICompilationUnit} (model) into {@link CompilationUnit} (ast).
   */
  public static CompilationUnit parseCompilationUnit(ICompilationUnit unit) throws Exception {
    String source = unit.getBuffer().getContents();
    source = clearHiddenCode(source);
    return parseCompilationUnit(source, unit.getJavaProject(), unit.getElementName());
  }

  /**
   * Parses given Java source into {@link CompilationUnit}.
   */
  public static CompilationUnit parseCompilationUnit(String source,
      IJavaProject javaProject,
      String unitName) {
    org.eclipse.jdt.core.dom.ASTParser parser =
        org.eclipse.jdt.core.dom.ASTParser.newParser(AST.JLS3);
    parser.setSource(source.toCharArray());
    parser.setProject(javaProject);
    parser.setCompilerOptions(ProjectUtils.getOptions(javaProject));
    parser.setUnitName(unitName);
    parser.setResolveBindings(true);
    return (CompilationUnit) parser.createAST(null);
  }

  /**
   * Replaces in given Java source hidden parts of code with spaces.
   *
   * @return the cleared Java source.
   */
  private static String clearHiddenCode(String source) throws Exception {
    // remove hidden code blocks
    {
      String beginTag = clearHiddenCode_getTag(IPreferenceConstants.P_CODE_HIDE_BEGIN);
      String endTag = clearHiddenCode_getTag(IPreferenceConstants.P_CODE_HIDE_END);
      while (true) {
        int beginIndex = source.indexOf(beginTag);
        int endIndex = source.indexOf(endTag);
        // validate begin/end indexes
        if (beginIndex == -1 && endIndex == -1) {
          break;
        }
        if (beginIndex == -1 && endIndex != -1) {
          throw new IllegalStateException("Unexpected state - no hide start and hide stop found.");
        }
        if (beginIndex != -1 && endIndex == -1) {
          throw new IllegalStateException("Unexpected state - no hide stop and hide start found.");
        }
        if (beginIndex >= endIndex) {
          throw new IllegalStateException("Unexpected state - hide start after hide stop.");
        }
        // do replace, line by line
        {
          Document document = new Document(source);
          int beginLine = document.getLineOfOffset(beginIndex);
          int endLine = document.getLineOfOffset(endIndex);
          for (int line = beginLine; line <= endLine; line++) {
            IRegion info = document.getLineInformation(line);
            int beginOffset = line == beginLine ? beginIndex : info.getOffset();
            //int endOffset = line == endLine ? endIndex : info.getOffset() + info.getLength();
            int endOffset = info.getOffset() + info.getLength();
            // replace inside of single line
            int length = endOffset - beginOffset;
            document.replace(beginOffset, length, StringUtils.repeat(" ", length));
          }
          // get updated source
          source = document.get();
        }
      }
    }
    // remove single line hidden code
    {
      String lineTag = clearHiddenCode_getTag(IPreferenceConstants.P_CODE_HIDE_LINE);
      while (true) {
        // find hide comment
        int hideIndex = source.indexOf(lineTag);
        if (hideIndex == -1) {
          break;
        }
        // replace with whitespace using Document
        Document document = new Document(source);
        IRegion lineInformation = document.getLineInformationOfOffset(hideIndex);
        document.replace(
            lineInformation.getOffset(),
            lineInformation.getLength(),
            StringUtils.repeat(" ", lineInformation.getLength()));
        // get updated source
        source = document.get();
      }
    }
    return source;
  }

  private static String clearHiddenCode_getTag(String name) {
    IPreferenceStore preferences = DesignerPlugin.getPreferences();
    String tag = preferences.getString(name);
    tag = tag.trim();
    // remove leading "//" prefix to search even "// $hide" - formatted line comments
    tag = StringUtils.removeStart(tag, "//");
    return tag;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Search
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return primary {@link IType} in given {@link ICompilationUnit}, i.e. type with name of unit.
   *         Can return <code>null</code> if no such type found.
   */
  public static IType findPrimaryType(ICompilationUnit compilationUnit) {
    String unitName = compilationUnit.getElementName();
    String typeName = StringUtils.chomp(unitName, ".java");
    IType primaryType = compilationUnit.getType(typeName);
    if (primaryType.exists()) {
      return primaryType;
    }
    return null;
  }

  /**
   * @return {@link IType} associated with given {@link IJavaElement}.
   */
  public static IType getType(IJavaElement element) throws JavaModelException {
    if (element instanceof IType) {
      return (IType) element;
    } else if (element instanceof IMember) {
      return ((IMember) element).getDeclaringType();
    } else if (element instanceof ICompilationUnit) {
      return ((ICompilationUnit) element).findPrimaryType();
    } else if (element instanceof IClassFile) {
      return ((IClassFile) element).getType();
    }
    return null;
  }

  /**
   * @return references of given {@link IType} in {@link IJavaProject}.
   */
  public static List<IJavaElement> searchReferences(IType type) throws Exception {
    IJavaSearchScope scope = prepareSearchScope(type);
    return searchReferences(scope, type);
  }

  /**
   * @return references of given {@link IField} in {@link IJavaProject}.
   */
  public static List<IJavaElement> searchReferences(IField field) throws Exception {
    IJavaSearchScope scope = prepareSearchScope(field);
    return searchReferences(scope, field);
  }

  /**
   * @return references of given {@link IField} in given {@link IJavaSearchScope}.
   */
  public static List<IJavaElement> searchReferences(IJavaSearchScope scope, IType type)
      throws Exception {
    return searchReferences(scope, (IJavaElement) type);
  }

  /**
   * @return references of given {@link IJavaElement} in given {@link IJavaSearchScope}.
   */
  private static List<IJavaElement> searchReferences(IJavaSearchScope scope, IJavaElement element)
      throws Exception {
    final List<IJavaElement> references = Lists.newArrayList();
    SearchRequestor requestor = new SearchRequestor() {
      @Override
      public void acceptSearchMatch(SearchMatch match) {
        if (match instanceof ReferenceMatch) {
          ReferenceMatch refMatch = (ReferenceMatch) match;
          IJavaElement matchElement = (IJavaElement) refMatch.getElement();
          {
            IJavaElement localElement = refMatch.getLocalElement();
            if (localElement != null) {
              matchElement = localElement;
            }
          }
          references.add(matchElement);
        }
      }
    };
    // do search
    SearchPattern pattern = SearchPattern.createPattern(element, IJavaSearchConstants.REFERENCES);
    SearchEngine searchEngine = new SearchEngine();
    searchEngine.search(
        pattern,
        new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
        scope,
        requestor,
        new NullProgressMonitor());
    // done
    return references;
  }

  /**
   * @return the {@link IJavaSearchScope} for full {@link IJavaProject}.
   */
  private static IJavaSearchScope prepareSearchScope(IJavaElement element) {
    IJavaProject javaProject = element.getJavaProject();
    return SearchEngine.createJavaSearchScope(
        new IJavaElement[]{javaProject},
        IJavaSearchScope.SOURCES);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Strings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the short name of fully qualified class name, or same name for simple type name.
   *
   * <pre>
	 * CodeUtils.getShortClass("javax.swing.JPanel")  = "JPanel"
	 * CodeUtils.getShortClass("test.MyPanel$Inner")  = "Inner"
	 * CodeUtils.getShortClass("boolean")             = "boolean"
	 * </pre>
   *
   * @param className
   *          the fully qualified class name.
   *
   * @return the short name of given class name.
   */
  public static String getShortClass(String className) {
    int index = StringUtils.lastIndexOfAny(className, new String[]{".", "$"});
    if (index != -1) {
      return className.substring(index + 1);
    }
    return className;
  }

  /**
   * @return the name of package for given fully qualified class name.
   */
  public static String getPackage(String className) {
    int lastDotIndex = className.lastIndexOf('.');
    if (lastDotIndex == -1) {
      return "";
    } else {
      return className.substring(0, lastDotIndex);
    }
  }

  /**
   * @return <code>true</code> if two classes are in same.
   */
  public static boolean isSamePackage(String className_1, String className_2) {
    String package_1 = getPackage(className_1);
    String package_2 = getPackage(className_2);
    return package_1.equals(package_2);
  }

  /**
   * @return the array of {@link String}'s that contains given strings plus on additional string.
   *
   * @param baseStrings
   *          optional base array of strings (can be <code>null</code>)
   * @param add
   *          additional string to add
   */
  public static String[] join(String[] baseStrings, String add) {
    // simple case - no base strings
    if (baseStrings == null) {
      return new String[]{add};
    }
    // complex case
    String[] strings = new String[baseStrings.length + 1];
    System.arraycopy(baseStrings, 0, strings, 0, baseStrings.length);
    strings[baseStrings.length] = add;
    return strings;
  }

  /**
   * @return the array of {@link String}'s that contains strings from two given arrays.
   */
  public static String[] join(String[] strings_1, String[] strings_2) {
    // check if one of the arrays is null
    if (strings_1 == null) {
      return strings_2;
    }
    if (strings_2 == null) {
      return strings_1;
    }
    // do join
    String[] strings = new String[strings_1.length + strings_2.length];
    System.arraycopy(strings_1, 0, strings, 0, strings_1.length);
    System.arraycopy(strings_2, 0, strings, strings_1.length, strings_2.length);
    return strings;
  }

  /**
   * @return the array of {@link String}'s that contains strings from two given arrays.
   */
  public static String[] join(String[] strings_1, String[] strings_2, String[] strings_3) {
    String[] strings_1_2 = join(strings_1, strings_2);
    return join(strings_1_2, strings_3);
  }

  /**
   * @return the source as single {@link String}, lines joined using "\n".
   */
  public static String getSource(String... lines) {
    return StringUtils.join(lines, "\n");
  }

  /**
   * Generates unique name, checking name in following order:
   *
   * <ul>
   * <li>baseName;</li>
   * <li>baseName_1;</li>
   * <li>baseName_2;</li>
   * <li>etc...</li>
   * <ul>
   *
   * @param baseName
   *          the base name.
   * @param validator
   *          the {@link Predicate} to check uniqueness.
   * @return the unique name based on given base name.
   */
  public static String generateUniqueName(String baseName, Predicate<String> validator) {
    for (int index = 0;; index++) {
      // prepare name
      String name = baseName;
      if (index != 0) {
        name += "_" + index;
      }
      // if name is unique, return it
      if (validator.apply(name)) {
        return name;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class loader
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ProjectClassLoader} for given {@link IJavaProject}.
   */
  public static ProjectClassLoader getProjectClassLoader(IJavaProject project) throws Exception {
    ClassLoader parentClassLoader = CodeUtils.class.getClassLoader();
    return ProjectClassLoader.create(parentClassLoader, project);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JDT model utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given <code>type</code> is successor of
   *         <code>superTypeToCheck</code>.
   */
  public static boolean isSuccessorOf(IType type, String superTypeName) throws JavaModelException {
    ITypeHierarchy supertypeHierarchy = type.newSupertypeHierarchy(null);
    for (IType superType : supertypeHierarchy.getAllTypes()) {
      if (superType.getFullyQualifiedName().equals(superTypeName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return <code>true</code> if given <code>type</code> is successor of
   *         <code>superTypeToCheck</code>.
   */
  public static boolean isSuccessorOf(IType type, IType superType) throws JavaModelException {
    return isSuccessorOf(type, superType.getFullyQualifiedName());
  }

  /**
   * @return the {@link IMethod} in super class for given {@link IMethod}.
   */
  public static IMethod findSuperMethod(IMethod method) throws Exception {
    String signature = getMethodSignature(method);
    IType type = method.getDeclaringType();
    return findMethodInSuperTypes(type, signature);
  }

  /**
   * @return the {@link IMethod} for given {@link IMethodBinding} or <code>null</code> if not found.
   */
  public static IMethod findMethod(IJavaProject project, IMethodBinding methodBinding)
      throws Exception {
    String declaringTypeName =
        AstNodeUtils.getFullyQualifiedName(methodBinding.getDeclaringClass(), false);
    String signature = AstNodeUtils.getMethodSignature(methodBinding);
    return findMethod(project, declaringTypeName, signature);
  }

  /**
   * @return the {@link IMethod} with given signature or <code>null</code> if not found.
   */
  public static IMethod findMethod(IJavaProject project, String typeName, String signature)
      throws Exception {
    IType type = project.findType(typeName);
    return findMethod(type, signature);
  }

  /**
   * @return the {@link IMethod} with given signature or <code>null</code> if not found.
   */
  public static IMethod findMethod(IType type, String signature) throws Exception {
    if (type == null) {
      return null;
    }
    // check single type
    {
      IMethod method = findMethodSingleType(type, signature);
      if (method != null) {
        return method;
      }
    }
    // check super types
    return findMethodInSuperTypes(type, signature);
  }

  /**
   * @return the {@link IMethod} with given signature in super types of given {@link IType}.
   */
  private static IMethod findMethodInSuperTypes(IType type, String signature)
      throws JavaModelException {
    // check each type for method
    ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
    IType[] allSupertypes = hierarchy.getAllSupertypes(type);
    for (int i = allSupertypes.length - 1; i >= 0; i--) {
      IType superType = allSupertypes[i];
      IMethod superMethod = findMethodSingleType(superType, signature);
      if (superMethod != null) {
        return superMethod;
      }
    }
    // not found
    return null;
  }

  /**
   * @return the {@link IMethod} with given signature exactly in given {@link IType} or
   *         <code>null</code>.
   */
  public static IMethod findMethodSingleType(IType type, String signature)
      throws JavaModelException {
    String name = StringUtils.substringBefore(signature, "(");
    boolean wantConstructor = "<init>".equals(name);
    for (IMethod method : type.getMethods()) {
      if (method.getElementName().equals(name) || wantConstructor && method.isConstructor()) {
        if (getMethodSignature(method).equals(signature)) {
          return method;
        }
      }
    }
    // not found
    return null;
  }

  /**
   * @param signatures
   *          the array with signatures, can have <code>null</code> elements.
   *
   * @return the {@link IMethod}'s for given method signatures, with <code>null</code> elements if
   *         corresponding signature is <code>null</code>.
   */
  public static List<IMethod> findMethods(IType type, List<String> signatures)
      throws JavaModelException {
    IMethod[] methods = findMethods(type, signatures.toArray(new String[signatures.size()]));
    return Lists.newArrayList(methods);
  }

  /**
   * @param signatures
   *          the array with signatures, can have <code>null</code> elements.
   *
   * @return the array of {@link IMethod} for given array of method signatures, with
   *         <code>null</code> elements if corresponding signature is <code>null</code>.
   */
  public static IMethod[] findMethods(IType type, String[] signatures) throws JavaModelException {
    // prepare map: signature -> IMethod
    Map<String, IMethod> signatureToMethod = Maps.newTreeMap();
    for (IMethod method : type.getMethods()) {
      signatureToMethod.put(getMethodSignature(method), method);
    }
    // fill methods array
    IMethod[] methods = new IMethod[signatures.length];
    for (int i = 0; i < signatures.length; i++) {
      String signature = signatures[i];
      if (signature != null) {
        methods[i] = signatureToMethod.get(signature);
      }
    }
    return methods;
  }

  /**
   * @return the signature for given {@link IMethod}.
   */
  public static String getMethodSignature(IMethod method) throws JavaModelException {
    Assert.isNotNull(method);
    StringBuffer buffer = new StringBuffer();
    // name
    if (method.isConstructor()) {
      buffer.append("<init>");
    } else {
      buffer.append(method.getElementName());
    }
    // parameters
    buffer.append('(');
    {
      IType contextType = method.getDeclaringType();
      String[] parameterTypes = method.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++) {
        String shortParameterType = parameterTypes[i];
        if (i != 0) {
          buffer.append(',');
        }
        String parameterType = getResolvedTypeName(contextType, shortParameterType);
        buffer.append(parameterType);
      }
    }
    buffer.append(')');
    // return result
    return buffer.toString();
  }

  /**
   * Resolves the given type name within the context of given {@link IType} (depending on the type
   * hierarchy and its imports).
   */
  public static String getResolvedTypeName(IType context, String typeSignature)
      throws JavaModelException {
    // remove generic
    if (typeSignature.indexOf(Signature.C_GENERIC_START) != -1) {
      typeSignature = Signature.getTypeErasure(typeSignature);
    }
    //
    int arrayCount = Signature.getArrayCount(typeSignature);
    char type = typeSignature.charAt(arrayCount);
    if (type == Signature.C_UNRESOLVED || type == Signature.C_TYPE_VARIABLE) {
      int semi = typeSignature.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
      String name = typeSignature.substring(arrayCount + 1, semi);
      name = getResolvedTypeName_resolveTypeVariable(context, name);
      // resolve type
      String[][] resolvedNames = context.resolveType(name);
      if (resolvedNames != null && resolvedNames.length > 0) {
        return concatenateName(resolvedNames[0][0], resolvedNames[0][1])
            + StringUtils.repeat("[]", arrayCount);
      }
      // can not resolve
      return null;
    }
    // resolved
    {
      String name = Signature.toString(typeSignature);
      name = getResolvedTypeName_resolveTypeVariable(context, name);
      // done
      return name;
    }
  }

  /**
   * If given name is name of generic {@link TypeVariable}, returns its bounds.
   */
  private static String getResolvedTypeName_resolveTypeVariable(IType context, String name)
      throws JavaModelException {
    ITypeParameter typeParameter = context.getTypeParameter(name);
    if (typeParameter.exists()) {
      String[] bounds = typeParameter.getBounds();
      if (bounds.length != 0) {
        name = bounds[0];
      } else {
        name = "java.lang.Object";
      }
    }
    return name;
  }

  /**
   * Concatenates two names. Uses a dot for separation. Both strings can be empty or
   * <code>null</code>.
   *
   * @param name1
   *          the first name
   * @param name2
   *          the second name
   * @return the concatenated name
   */
  private static String concatenateName(String name1, String name2) {
    StringBuffer buf = new StringBuffer();
    if (name1 != null && name1.length() > 0) {
      buf.append(name1);
    }
    if (name2 != null && name2.length() > 0) {
      if (buf.length() > 0) {
        buf.append('.');
      }
      buf.append(name2);
    }
    return buf.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JDT IField utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IField} with given name or <code>null</code> if not found.
   */
  public static IField findField(IJavaProject project, String typeName, String fieldName)
      throws Exception {
    IType type = project.findType(typeName);
    if (type == null) {
      return null;
    }
    // check single type
    {
      IField field = findFieldSingleType(type, fieldName);
      if (field != null) {
        return field;
      }
    }
    // check super types
    {
      ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
      IType[] allSupertypes = hierarchy.getAllSupertypes(type);
      for (int i = allSupertypes.length - 1; i >= 0; i--) {
        IType superType = allSupertypes[i];
        IField field = findFieldSingleType(superType, fieldName);
        if (field != null) {
          return field;
        }
      }
    }
    // not found
    return null;
  }

  /**
   * @return the {@link IField} with given name exactly in given {@link IType} or <code>null</code>.
   */
  private static IField findFieldSingleType(IType type, String fieldName) throws JavaModelException {
    for (IField field : type.getFields()) {
      if (field.getElementName().equals(fieldName)) {
        return field;
      }
    }
    // not found
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source containers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array {@link IContainer}'s for all source directories of given {@link IJavaProject}
   *         .
   */
  public static List<IContainer> getSourceContainers(IJavaProject javaProject,
      boolean includeRequiredProjects) throws Exception {
    List<IContainer> containers = Lists.newArrayList();
    addSourceContainers(
        containers,
        Sets.<IJavaProject>newHashSet(),
        javaProject,
        includeRequiredProjects);
    return containers;
  }

  /**
   * Adds {@link IContainer}'s for all source directories of given {@link IJavaProject} and its
   * required projects.
   */
  private static void addSourceContainers(List<IContainer> containers,
      Set<IJavaProject> visitedProjects,
      IJavaProject javaProject,
      boolean includeRequiredProjects) throws Exception {
    // check for existence
    if (!javaProject.exists()) {
      return;
    }
    // check for recursion
    if (visitedProjects.contains(javaProject)) {
      return;
    }
    visitedProjects.add(javaProject);
    // prepare information
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = javaProject.getProject();
    // add source folders
    for (IClasspathEntry entry : javaProject.getResolvedClasspath(true)) {
      if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
        IContainer container = (IContainer) root.findMember(entry.getPath());
        if (container != null) {
          containers.add(container);
        }
      }
    }
    // source folders of required projects
    if (includeRequiredProjects) {
      for (String requiredProjectName : javaProject.getRequiredProjectNames()) {
        addSourceContainers(containers, visitedProjects, requiredProjectName);
      }
    }
    // source folders for fragments
    if (includeRequiredProjects) {
      Object model = ReflectivePDE.findModel(project);
      if (model != null) {
        BundleDescription bundleDescription = ReflectivePDE.getPluginModelBundleDescription(model);
        if (bundleDescription != null) {
          BundleDescription[] fragments = bundleDescription.getFragments();
          for (BundleDescription fragment : fragments) {
            String fragmentProjectName = fragment.getSymbolicName();
            addSourceContainers(containers, visitedProjects, fragmentProjectName);
          }
        }
      }
    }
  }

  /**
   * Adds {@link IContainer}'s for all source directories of given {@link IJavaProject} and its
   * required projects.
   */
  private static void addSourceContainers(List<IContainer> containers,
      Set<IJavaProject> visitedProjects,
      String projectName) throws Exception {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject(projectName);
    IJavaProject javaProject = JavaCore.create(project);
    addSourceContainers(containers, visitedProjects, javaProject, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPackageFragmentRoot
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IPackageFragmentRoot} that is parent of given {@link IJavaElement} or first
   *         {@link IPackageFragmentRoot} of {@link IJavaProject}.
   */
  public static IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element)
      throws JavaModelException {
    if (element != null) {
      // try to find valid parent IPackageFragmentRoot
      {
        IPackageFragmentRoot root =
            (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        if (root != null && root.getKind() == IPackageFragmentRoot.K_SOURCE) {
          return root;
        }
      }
      // use IPackageFragmentRoot of IJavaProject
      {
        IJavaProject javaProject = element.getJavaProject();
        if (javaProject != null && javaProject.exists()) {
          for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
              return root;
            }
          }
        }
      }
    }
    // invalid element
    return null;
  }
}
