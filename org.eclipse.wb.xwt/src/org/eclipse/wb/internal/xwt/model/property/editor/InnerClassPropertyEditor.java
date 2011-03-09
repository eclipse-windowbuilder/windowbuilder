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
package org.eclipse.wb.internal.xwt.model.property.editor;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Point;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * {@link PropertyEditor} for creating or selecting {@link Class}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.property.editor
 */
public final class InnerClassPropertyEditor extends TextDialogPropertyEditor
    implements
      IConfigurablePropertyObject {
  private String m_baseName;
  private String m_className;
  private String m_source;
  private boolean m_disabled;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    // existing class
    {
      String className = getClassName(genericProperty);
      if (className != null) {
        return CodeUtils.getShortClass(className);
      }
    }
    // no expression
    return "<double click>";
  }

  private static String getClassName(GenericProperty genericProperty) {
    DocumentElement objectElement = genericProperty.getObject().getElement();
    String attributeName = genericProperty.getTitle();
    DocumentElement propertyElement =
        objectElement.getChild(objectElement.getTag() + "." + attributeName, false);
    if (propertyElement == null) {
      return null;
    }
    DocumentElement classElement = propertyElement.getChildAt(0);
    String packageNS;
    String shortClassName;
    {
      String tag = classElement.getTag();
      int index = tag.indexOf(':');
      packageNS = tag.substring(0, index);
      shortClassName = tag.substring(index + 1);
    }
    String uri = NamespacesHelper.getURI(objectElement, packageNS);
    String packageName = StringUtils.removeStart(uri, "clr-namespace:");
    return packageName + "." + shortClassName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean activate(PropertyTable propertyTable, Property property, Point location)
      throws Exception {
    // activate using keyboard
    if (location == null) {
      openClass(property);
    }
    // don't activate
    return false;
  }

  @Override
  protected void openDialog(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    XmlObjectInfo object = genericProperty.getObject();
    IJavaProject javaProject = object.getContext().getJavaProject();
    // open dialog
    IType type = JdtUiUtils.selectClassType(DesignerPlugin.getShell(), javaProject);
    if (type != null) {
      setClassName(genericProperty, type.getFullyQualifiedName());
    }
  }

  @Override
  public void doubleClick(Property property, Point location) throws Exception {
    openClass(property);
  }

  /**
   * If there is class, open it, else create new inner class and open.
   */
  private void openClass(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    if (m_disabled) {
      return;
    }
    // open existing
    {
      String className = getClassName(genericProperty);
      if (className != null) {
        IJavaProject javaProject = genericProperty.getObject().getContext().getJavaProject();
        IType type = javaProject.findType(className);
        JavaUI.openInEditor(type);
        return;
      }
    }
    // generate new 
    {
      createNewClass(genericProperty);
      openClass(property);
    }
  }

  private void createNewClass(GenericProperty genericProperty) throws Exception {
    XmlObjectInfo object = genericProperty.getObject();
    //
    IPackageFragment packageFragment = getPackageFragment(object);
    String packageName = packageFragment.getElementName();
    String newName = generateUniqueTypeName(packageFragment, m_baseName);
    // prepare source
    String source = m_source;
    source = updateSourceIfAnonymous(source);
    source = StringUtils.replace(source, "${name}", newName);
    source = StringUtils.replace(source, "private class", "public class");
    source = StringUtils.replace(source, "private static class", "public class");
    source = "package " + packageName + ";\r\n" + source;
    // create class
    {
      String unitName = newName + ".java";
      ICompilationUnit unit = packageFragment.createCompilationUnit(unitName, source, false, null);
      // resolve imports
      {
        AstEditor astEditor = new AstEditor(unit);
        astEditor.resolveImports(DomGenerics.types(astEditor.getAstUnit()).get(0));
        astEditor.commitChanges();
      }
      // done
      unit.save(null, false);
      ProjectUtils.waitForAutoBuild();
    }
    // use class in XML
    {
      String newClassName = packageName + "." + newName;
      setClassName(genericProperty, newClassName);
    }
  }

  private String updateSourceIfAnonymous(String source) {
    if (source.startsWith("new ")) {
      int superBegin = "new ".length();
      int superEnd = source.indexOf('(');
      int classOpen = source.indexOf('{');
      String superName = source.substring(superBegin, superEnd);
      source = "public class ${name} extends " + superName + " " + source.substring(classOpen);
    }
    return source;
  }

  private static void setClassName(GenericProperty genericProperty, String newClassName) {
    XmlObjectInfo object = genericProperty.getObject();
    String packageName = CodeUtils.getPackage(newClassName);
    String newName = CodeUtils.getShortClass(newClassName);
    // use class in XML
    DocumentElement objectElement = object.getElement();
    DocumentElement propertyElement = new DocumentElement();
    propertyElement.setTag(objectElement.getTag() + "." + genericProperty.getTitle());
    objectElement.addChild(propertyElement);
    {
      String newClassTag = getPackageNamespace(object, packageName) + ":" + newName;
      DocumentElement newClassElement = new DocumentElement();
      newClassElement.setTag(newClassTag);
      propertyElement.addChild(newClassElement);
    }
    // update context
    ExecutionUtils.refresh(object);
  }

  private static IPackageFragment getPackageFragment(XmlObjectInfo object) {
    IFolder parentFolder = (IFolder) object.getContext().getFile().getParent();
    return (IPackageFragment) JavaCore.create(parentFolder);
  }

  private static String generateUniqueTypeName(IPackageFragment packageFragment, String baseName) {
    int index = 1;
    while (true) {
      String name = baseName + "_" + index;
      if (!packageFragment.getCompilationUnit(name + ".java").exists()) {
        return name;
      }
      index++;
    }
  }

  private static String getPackageNamespace(XmlObjectInfo object, String packageName) {
    DocumentElement element = object.getCreationSupport().getElement();
    return NamespacesHelper.ensureName(element, "clr-namespace:" + packageName, "p");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorContext context, Map<String, Object> parameters) throws Exception {
    // base class name
    {
      m_baseName = (String) parameters.get("name");
      Assert.isNotNull(m_baseName, "'name' attribute required.");
    }
    // class
    {
      m_className = (String) parameters.get("class");
      Assert.isNotNull(m_className, "'class' attribute required.");
    }
    // template source
    {
      m_source = (String) parameters.get("source");
      Assert.isNotNull(m_source, "'source' attribute required.");
      m_source = m_source.trim();
    }
    // disabling
    m_disabled = "true".equals(parameters.get("disableInXML"));
  }
}
