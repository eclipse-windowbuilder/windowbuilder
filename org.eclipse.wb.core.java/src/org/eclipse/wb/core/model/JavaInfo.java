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
package org.eclipse.wb.core.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.VisitingContext;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.core.model.broadcast.ExecutionFlowEnterFrame;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetAssociationBefore;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetVariable;
import org.eclipse.wb.core.model.broadcast.JavaInfosetObjectBefore;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.AbstractDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ConfigurablePropertyDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyFactory;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.event.EventsProperty;
import org.eclipse.wb.internal.core.model.property.hierarchy.ComponentClassProperty;
import org.eclipse.wb.internal.core.model.util.GlobalStateJava;
import org.eclipse.wb.internal.core.model.util.ImportantPropertiesDialog;
import org.eclipse.wb.internal.core.model.util.PlaceholderUtils;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.PropertyUtils2;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.parser.JavaInfoResolver;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.VisitedNodes;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract model for any Java-based model object. It has some presentation in AST.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public class JavaInfo extends ObjectInfo implements HasSourcePosition {
  private final JavaInfo m_this = this;
  /**
   * We mark components that user drops from palette with this flag to be able distinguish them from
   * objects that created as consequence of user operation.
   */
  public static final String FLAG_MANUAL_COMPONENT = "manuallyCreatedComponent";
  /**
   * When we prepare "live image", we use start/commit/edit transaction, so {@link #saveEdit()}
   * commits changes of source code into underlying {@link ICompilationUnit}, but these changes are
   * just temporary, and we don't want to have them in UNDO history. So, when component is marked
   * with this flag, we avoid committing changes.
   */
  public static final String FLAG_DONT_COMMIT_EDITOR = "don't commit ASTEditor source";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ComponentDescription m_description;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    m_editor = editor;
    m_description = description;
    m_creationSupport = creationSupport;
    // initialize
    setBroadcastSupport(EditorState.get(m_editor).getBroadcast());
    m_description.visit(this, AbstractDescription.STATE_USE);
    scheduleSendingObjectReady();
    m_creationSupport.setJavaInfo(this);
    // properties
    {
      Class<?> componentClass = description.getComponentClass();
      if (componentClass == null) {
        m_componentClassProperty = null;
        m_eventsProperty = null;
      } else {
        IJavaProject javaProject = editor.getJavaProject();
        m_componentClassProperty = new ComponentClassProperty(javaProject, componentClass);
        m_eventsProperty = new EventsProperty(this);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    // creation
    buffer.append("{");
    buffer.append(m_creationSupport.toString());
    buffer.append("}");
    // variable
    if (m_variableSupport != null) {
      buffer.append(" {");
      buffer.append(m_variableSupport.toString());
      buffer.append("}");
    }
    // nodes
    appendNodes(buffer, getRelatedNodes());
    // result
    return buffer.toString();
  }

  /**
   * Appends nodes for {@link #toString()}.
   */
  private void appendNodes(StringBuffer buffer, List<ASTNode> nodes) {
    buffer.append(" {");
    boolean first = true;
    for (ASTNode node : nodes) {
      // append separator
      if (!first) {
        buffer.append(" ");
      }
      first = false;
      // prepare node for getting source
      ASTNode sourceNode = getRelatedNodeForSource(node);
      // append wrapped source of node
      {
        String source = getEditor().getSource(sourceNode);
        // if anonymous class creation, cut body
        if (sourceNode instanceof ClassInstanceCreation) {
          ClassInstanceCreation creation = (ClassInstanceCreation) sourceNode;
          if (creation.getAnonymousClassDeclaration() != null) {
            source = StringUtils.substringBefore(source, "{").trim();
          }
        }
        // do append
        buffer.append("/");
        buffer.append(source);
        buffer.append("/");
      }
    }
    buffer.append("}");
  }

  /**
   * @return the {@link ASTNode} that should be used to display given related {@link ASTNode}.
   */
  public static ASTNode getRelatedNodeForSource(ASTNode node) {
    ASTNode sourceNode = node;
    // part of invocation
    if (node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
      sourceNode = node.getParent();
    }
    if (node.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
      sourceNode = node.getParent();
    }
    if (node.getLocationInParent() == SuperConstructorInvocation.ARGUMENTS_PROPERTY) {
      sourceNode = node.getParent();
    }
    if (node.getLocationInParent() == SuperMethodInvocation.ARGUMENTS_PROPERTY) {
      sourceNode = node.getParent();
    }
    if (node.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY) {
      sourceNode = node.getParent();
    }
    // javaInfo.foo = something
    if (node.getLocationInParent() == QualifiedName.QUALIFIER_PROPERTY) {
      QualifiedName qualifiedName = (QualifiedName) node.getParent();
      sourceNode = qualifiedName;
      if (qualifiedName.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
        sourceNode = qualifiedName.getParent();
      }
    }
    // done
    return sourceNode;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editor
  //
  ////////////////////////////////////////////////////////////////////////////
  private final AstEditor m_editor;

  /**
   * @return the current {@link AstEditor}.
   */
  public final AstEditor getEditor() {
    return m_editor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final void saveEdit() throws Exception {
    if (getArbitraryValue(FLAG_DONT_COMMIT_EDITOR) == null) {
      m_editor.commitChanges();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ComponentDescription} of this object.
   */
  public final ComponentDescription getDescription() {
    return m_description;
  }

  /**
   * @return <code>true</code> if this object can be root. For example in SWT <code>Control</code>
   *         can not be root, so we should explicitly set what can be root.
   */
  public boolean canBeRoot() {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Templates
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, String> m_templateArguments = null;

  /**
   * @return the {@link Map} of template arguments.
   */
  public Map<String, String> getTemplateArguments() {
    return m_templateArguments != null
        ? Collections.unmodifiableMap(m_templateArguments)
        : m_templateArguments;
  }

  /**
   * Associates the given value with the given template.
   */
  public void putTemplateArgument(String name, String value) {
    if (m_templateArguments == null) {
      m_templateArguments = Maps.newHashMap();
    }
    m_templateArguments.put(name, value);
  }

  /**
   * Associates the given values with the given templates.
   */
  public void putTemplateArguments(Map<String, String> templateArguments) {
    if (templateArguments != null && !templateArguments.isEmpty()) {
      for (Entry<String, String> argument : templateArguments.entrySet()) {
        putTemplateArgument(argument.getKey(), argument.getValue());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the result of {@link #getParent()} casted to the {@link JavaInfo}.
   */
  public final JavaInfo getParentJava() {
    return (JavaInfo) getParent();
  }

  /**
   * @return the result of {@link #getRoot()} casted to the {@link JavaInfo}.
   */
  public final JavaInfo getRootJava() {
    return (JavaInfo) getRoot();
  }

  /**
   * @return the list of {@link JavaInfo} children.
   */
  public final List<JavaInfo> getChildrenJava() {
    return getChildren(JavaInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Association
  //
  ////////////////////////////////////////////////////////////////////////////
  private Association m_association;

  /**
   * @return the {@link Association} between this {@link JavaInfo} and its parent.
   */
  public Association getAssociation() {
    return m_association;
  }

  /**
   * Sets the {@link Association} between this {@link JavaInfo} and its parent.
   *
   * @param association
   *          the {@link Association} instance, or <code>null</code> if existing {@link Association}
   *          should be removed.
   */
  public void setAssociation(Association association) throws Exception {
    getBroadcast(JavaInfoSetAssociationBefore.class).invoke(this, association);
    m_association = association;
    if (m_association != null) {
      m_association.setJavaInfo(this);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link JavaEventListener} for hierarchy.
   */
  public final JavaEventListener getBroadcastJava() {
    return ExecutionUtils.runObject(new RunnableObjectEx<JavaEventListener>() {
      public JavaEventListener runObject() throws Exception {
        return getBroadcast(JavaEventListener.class);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Is <code>true</code> if this {@link JavaInfo} already initialized. We check this flag in
   * {@link #setObject(Object)} and do initialization if needed. This is required for newly created
   * {@link JavaInfo
	 * }'s.
   */
  private boolean m_initialized;

  /**
   * Initializes newly created {@link JavaInfo} (it should have "live" object). This is good place
   * to create any exposed components, fetch default properties, etc. Note, that constructor is
   * <i>bad</i> place for such things because in constructors we can not call virtual methods.
   */
  protected void initialize() throws Exception {
    m_initialized = true;
    if (m_object != null) {
      createExposedChildren();
    }
    ImportantPropertiesDialog.scheduleImportantProperties(this);
    // external participators
    {
      List<IJavaInfoInitializationParticipator> participators =
          ExternalFactoriesHelper.getElementsInstances(
              IJavaInfoInitializationParticipator.class,
              "org.eclipse.wb.core.java.javaInfoInitializationParticipators",
              "participator");
      for (IJavaInfoInitializationParticipator participator : participators) {
        participator.process(this);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exposed children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds any exposed components as direct or indirect children of this {@link JavaInfo}.
   * <p>
   * Note that this method may be called several times during parse
   */
  public void createExposedChildren() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void scheduleSendingObjectReady() {
    // clean "sent" flag when dispose hierarchy
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshDispose() throws Exception {
        m_objectReady = false;
      }
    });
    //
    final String objectReadyScript = JavaInfoUtils.getParameter(this, "objectReadyValidator");
    if (objectReadyScript != null) {
      // schedule check any evaluations for determine when object is "ready"
      addBroadcastListener(new EvaluationEventListener() {
        @Override
        public void evaluateAfter(EvaluationContext context, ASTNode node) throws Exception {
          if (getObject() != null) {
            processObjectReady();
          }
        }
      });
    } else {
      // consider any new object as "ready"
      addBroadcastListener(new JavaInfoSetObjectAfter() {
        public void invoke(JavaInfo target, Object o) throws Exception {
          if (target == m_this && !m_this.isDeleted()) {
            processObjectReady();
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object-ready
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_objectReady = false;

  public final boolean isObjectReady() {
    return m_objectReady;
  }

  /**
   * Processing object-ready routine.
   */
  protected void processObjectReady() throws Exception {
    if (m_objectReady) {
      return;
    }
    // process
    String objectReadyScript = JavaInfoUtils.getParameter(this, "objectReadyValidator");
    if (StringUtils.isEmpty(objectReadyScript)) {
      processObjectReadyInternal();
    } else {
      // use script to determine when object is "ready"
      Boolean result = (Boolean) JavaInfoUtils.executeScript(m_this, objectReadyScript);
      if (result != null && result.booleanValue()) {
        processObjectReadyInternal();
      }
    }
  }

  protected final void processObjectReadyInternal() throws Exception {
    m_objectReady = true;
    m_description.visit(m_this, AbstractDescription.STATE_OBJECT_READY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<GenericPropertyImpl> m_descriptionBasedProperties;
  private List<Property> m_configurableProperties;
  private final ComponentClassProperty m_componentClassProperty;
  private final EventsProperty m_eventsProperty;

  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = Lists.newArrayList();
    // add description based properties
    if (m_descriptionBasedProperties == null) {
      m_descriptionBasedProperties = Lists.newArrayList();
      for (GenericPropertyDescription description : getDescription().getProperties()) {
        GenericPropertyImpl property = PropertyUtils2.createGenericPropertyImpl(this, description);
        m_descriptionBasedProperties.add(property);
      }
    }
    properties.addAll(m_descriptionBasedProperties);
    // add configurable properties
    addConfigurableProperties(properties);
    // add events (only if there are events)
    if (PropertyUtils.getChildren(m_eventsProperty).length != 0) {
      properties.add(m_eventsProperty);
    }
    // add properties from creation and variable
    m_creationSupport.addProperties(properties);
    m_variableSupport.addProperties(properties);
    m_association.addProperties(properties);
    // add class property
    if (m_componentClassProperty != null) {
      properties.add(m_componentClassProperty);
    }
    // add hierarchy properties
    getBroadcast(ObjectInfoAddProperties.class).invoke(this, properties);
    getBroadcast(JavaInfoAddProperties.class).invoke(this, properties);
    // remove GenericPropertyImpl's without accessors
    for (Iterator<Property> I = properties.iterator(); I.hasNext();) {
      Property property = I.next();
      if (property instanceof GenericPropertyImpl) {
        GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
        if (genericProperty.getAccessors().isEmpty()) {
          I.remove();
        }
      }
    }
    // return properties
    return properties;
  }

  /**
   * Adds properties for {@link ComponentDescription#getConfigurableProperties()}.
   */
  private void addConfigurableProperties(List<Property> properties) throws Exception {
    if (m_configurableProperties == null) {
      m_configurableProperties = Lists.newArrayList();
      for (ConfigurablePropertyDescription description : getDescription().getConfigurableProperties()) {
        String id = description.getId();
        IConfigurablePropertyFactory factory = getConfigurablePropertyFactory(id);
        Assert.isNotNull(factory, "Can not find IConfigurablePropertyFactory for %s.", id);
        // add Property
        {
          Property property = factory.create(this, description);
          Assert.isNotNull(property, "Property for for %s and %s was not created.", id, this);
          m_configurableProperties.add(property);
        }
      }
    }
    properties.addAll(m_configurableProperties);
  }

  /**
   * @return the {@link IConfigurablePropertyFactory} registered with given ID.
   */
  private static IConfigurablePropertyFactory getConfigurablePropertyFactory(String id) {
    List<IConfigurationElement> factoryElements =
        ExternalFactoriesHelper.getElements(
            "org.eclipse.wb.core.configurablePropertyFactories",
            "factory");
    for (IConfigurationElement factofyElement : factoryElements) {
      if (ExternalFactoriesHelper.getRequiredAttribute(factofyElement, "id").equals(id)) {
        return ExternalFactoriesHelper.createExecutableExtension(factofyElement, "class");
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation support
  //
  ////////////////////////////////////////////////////////////////////////////
  private CreationSupport m_creationSupport;

  /**
   * Sets new {@link CreationSupport}.<br>
   * This rare operation, for example we use it to "materialize" implicit layout.
   */
  public final void setCreationSupport(CreationSupport creationSupport) throws Exception {
    m_creationSupport = creationSupport;
    m_creationSupport.setJavaInfo(this);
  }

  /**
   * @return the current {@link CreationSupport}.
   */
  public final CreationSupport getCreationSupport() {
    return m_creationSupport;
  }

  /**
   * Specifies that given {@link Expression} is creation of this {@link JavaInfo}.
   */
  public void bindToExpression(Expression expression) {
    EditorState.get(m_editor).getJavaInfoResolver().bind(this, expression);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // HasSourcePosition
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getSourcePosition() {
    return getCreationSupport().getNode().getStartPosition();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Variable support
  //
  ////////////////////////////////////////////////////////////////////////////
  private VariableSupport m_variableSupport;

  /**
   * @return current {@link VariableSupport}. Can not return <code>null</code>.
   */
  public final VariableSupport getVariableSupport() {
    return m_variableSupport;
  }

  /**
   * Sets new {@link VariableSupport}.<br>
   * Usually this is done during parsing or when existing {@link VariableSupport} morphed into new
   * one.
   */
  public final void setVariableSupport(VariableSupport variableSupport) throws Exception {
    VariableSupport oldVariable = m_variableSupport;
    m_variableSupport = variableSupport;
    getBroadcastSupport().getListener(JavaInfoSetVariable.class).invoke(
        m_this,
        oldVariable,
        m_variableSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this);

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Related nodes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link ASTNode}s which are tightly related to this {@link JavaInfo}. Node is related if it
   * represents reference of this {@link JavaInfo}, i.e. is creation or usage as part of
   * {@link MethodInvocation} (as target expression or argument) or {@link Assignment}, etc.
   */
  private final List<ASTNode> m_nodes = Lists.newLinkedList();

  /**
   * Adds given related {@link ASTNode}.
   */
  public final void addRelatedNode(ASTNode node) {
    if (node != null && !m_nodes.contains(node)) {
      m_nodes.add(node);
    }
  }

  /**
   * Adds {@link ASTNode}'s that represent this {@link JavaInfo} in subtree of given {@link ASTNode}
   * .
   */
  public final void addRelatedNodes(ASTNode start) {
    start.accept(new ASTVisitor() {
      @Override
      public void postVisit(ASTNode node) {
        // ignore "button" in "button = new JButton()"
        if (node.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY
            || node.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
          return;
        }
        if (isRepresentedBy(node)) {
          addRelatedNode(node);
        }
      }

      @Override
      public void endVisit(MethodInvocation node) {
        // support for invocation for "this" component
        if (node.getExpression() == null && isRepresentedBy(null)) {
          addRelatedNode(node);
        }
      }
    });
  }

  /**
   * @return the {@link List} of tightly related {@link ASTNode}'s.
   */
  public final List<ASTNode> getRelatedNodes() {
    AstNodeUtils.removeDanglingNodes(m_nodes);
    return m_nodes;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean canDelete() {
    // try "canDelete" script
    {
      boolean canDeleteBoolean = ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
        public Boolean runObject() throws Exception {
          Object canDeleteObject = JavaInfoUtils.executeScriptParameter(m_this, "canDelete");
          if (canDeleteObject == null) {
            return true;
          }
          if (canDeleteObject instanceof Boolean) {
            return ((Boolean) canDeleteObject).booleanValue();
          }
          return false;
        }
      }, false);
      if (!canDeleteBoolean) {
        return false;
      }
    }
    // check if creation support can delete
    if (!m_creationSupport.canDelete()) {
      return false;
    }
    // check if association with parent can be deleted
    if (!m_association.canDelete()) {
      return false;
    }
    // ask each child
    putArbitraryValue(FLAG_DELETING, Boolean.TRUE);
    try {
      for (ObjectInfo child : getChildren()) {
        if (!child.canDelete()) {
          return false;
        }
      }
    } finally {
      removeArbitraryValue(FLAG_DELETING);
    }
    // yes, this JavaInfo can be deleted
    return true;
  }

  @Override
  public void delete() throws Exception {
    final ObjectInfo parent = getParent();
    ObjectInfo hierarchyObject = parent != null ? parent : this;
    ExecutionUtils.run(hierarchyObject, new RunnableEx() {
      public void run() throws Exception {
        putArbitraryValue(FLAG_DELETING, Boolean.TRUE);
        try {
          // broadcast "before"
          ObjectInfoDelete deleteBroadcast = getBroadcast(ObjectInfoDelete.class);
          deleteBroadcast.before(parent, m_this);
          // delete association
          m_association.remove();
          // delete creation/variable
          VariableSupport variableSupport = m_variableSupport;
          variableSupport.deleteBefore();
          m_creationSupport.delete();
          variableSupport.deleteAfter();
          // broadcast "after"
          deleteBroadcast.after(parent, m_this);
        } finally {
          removeArbitraryValue(FLAG_DELETING);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodInvocation utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link StatementTarget} to add new {@link MethodInvocation}, using
   *         {@link MethodOrder}.
   */
  protected StatementTarget getMethodInvocationTarget(String newSignature) throws Exception {
    // get target from order
    {
      MethodDescription newDescription = getDescription().getMethod(newSignature);
      if (newDescription != null) {
        return newDescription.getOrder().getTarget(this, newSignature);
      }
    }
    // default target
    return getDescription().getDefaultMethodOrder().getTarget(this, newSignature);
  }

  /**
   * @param relatedNode
   *          the {@link ASTNode} that represents this {@link JavaInfo}.
   *
   * @return the {@link MethodInvocation} of this {@link JavaInfo} where given related
   *         {@link ASTNode} is expression of this {@link MethodInvocation}. May return
   *         <code>null</code>, if given related node is not part of {@link MethodInvocation}.
   */
  public final MethodInvocation getMethodInvocation(ASTNode relatedNode) {
    if (relatedNode.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
      return (MethodInvocation) relatedNode.getParent();
    } else if (relatedNode instanceof MethodInvocation
        && ((MethodInvocation) relatedNode).getExpression() == null
        && isRepresentedBy(null)) {
      return (MethodInvocation) relatedNode;
    }
    // not a MethodInvocation part
    return null;
  }

  /**
   * @return the {@link List} of all {@link MethodInvocation} of this {@link JavaInfo}.
   */
  public final List<MethodInvocation> getMethodInvocations() {
    List<MethodInvocation> invocations = Lists.newArrayList();
    for (ASTNode node : getRelatedNodes()) {
      MethodInvocation invocation = getMethodInvocation(node);
      if (invocation != null) {
        invocations.add(invocation);
      }
    }
    return invocations;
  }

  /**
   * @return the {@link List} of {@link MethodInvocation} of this {@link JavaInfo} with given
   *         signature.
   */
  public final List<MethodInvocation> getMethodInvocations(String signature) {
    List<MethodInvocation> invocations = Lists.newArrayList();
    for (ASTNode node : getRelatedNodes()) {
      // prepare invocation from related node
      MethodInvocation invocation = getMethodInvocation(node);
      // check invocation
      if (invocation != null) {
        String methodSignature = AstNodeUtils.getMethodSignature(invocation);
        if (signature.equals(methodSignature) && isRepresentedBy(invocation.getExpression())) {
          invocations.add(invocation);
        }
      }
    }
    //
    return invocations;
  }

  /**
   * @return the {@link MethodInvocation} of this {@link JavaInfo} with given signature.
   */
  public final MethodInvocation getMethodInvocation(String signature) {
    List<MethodInvocation> invocations = getMethodInvocations(signature);
    return GenericsUtils.getFirstOrNull(invocations);
  }

  /**
   * Adds new {@link MethodInvocation} for this {@link JavaInfo}. In compare to
   * {@link #addExpressionStatement(String)} it uses {@link MethodDescription}'s to find correct
   * position for this method. For example for {@link javax.swing.JProgressBar} "setMaximum" should
   * be before "setValue".
   *
   * @return the new added {@link MethodInvocation}.
   *
   * @param signature
   *          the signature of method, to find correct position
   * @param arguments
   *          the comma separated arguments string
   */
  public final MethodInvocation addMethodInvocation(String signature, String arguments)
      throws Exception {
    StatementTarget target = getMethodInvocationTarget(signature);
    return addMethodInvocation(target, signature, arguments);
  }

  /**
   * Adds new {@link MethodInvocation} for this {@link JavaInfo} into given target.
   *
   * @return the new added {@link MethodInvocation}.
   *
   * @param target
   *          the {@link StatementTarget} that specifies where to add {@link MethodInvocation}.
   * @param signature
   *          the signature of method, to find correct position
   * @param arguments
   *          the comma separated arguments string
   */
  public final MethodInvocation addMethodInvocation(StatementTarget target,
      String signature,
      String arguments) throws Exception {
    // create invocation source
    String invocationSource;
    {
      String methodName = StringUtils.substringBefore(signature, "(");
      invocationSource = TemplateUtils.format("{0}.{1}({2})", this, methodName, arguments);
    }
    // add statement with invocation
    return (MethodInvocation) addExpressionStatement(target, invocationSource);
  }

  /**
   * Removes {@link MethodInvocation}'s of this {@link JavaInfo} with given signature.
   */
  public final void removeMethodInvocations(String signature) throws Exception {
    List<MethodInvocation> invocations = getMethodInvocations(signature);
    for (MethodInvocation invocation : invocations) {
      m_editor.removeEnclosingStatement(invocation);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return new added {@link Expression} for {@link ExpressionStatement} expression with given
   *         {@link Expression} source.
   */
  public final Expression addExpressionStatement(String expressionSource) throws Exception {
    StatementTarget target = m_variableSupport.getStatementTarget();
    return addExpressionStatement(target, expressionSource);
  }

  /**
   * @return new added {@link Expression} for {@link ExpressionStatement} expression with given
   *         {@link Expression} source.
   */
  public final Expression addExpressionStatement(StatementTarget target, String expressionSource)
      throws Exception {
    expressionSource = TemplateUtils.resolve(target, expressionSource);
    String statementSource = expressionSource + ";";
    ExpressionStatement statement =
        (ExpressionStatement) m_editor.addStatement(statementSource, target);
    Expression expression = statement.getExpression();
    addRelatedNodes(statement);
    return expression;
  }

  /**
   * See {@link AstEditor#replaceExpression(Expression, String)}. Performs also deferred
   * {@link JavaInfo} references resolving.
   */
  public final Expression replaceExpression(Expression expression, String source) throws Exception {
    Statement targetStatement = AstNodeUtils.getEnclosingStatement(expression);
    StatementTarget target = new StatementTarget(targetStatement, true);
    source = TemplateUtils.resolve(target, source);
    return getEditor().replaceExpression(expression, source);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Field Assignment utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the all {@link Assignment}'s to fields, may be empty {@link List}.
   */
  public final List<Assignment> getFieldAssignments() {
    List<Assignment> assignments = Lists.newArrayList();
    for (ASTNode node : getRelatedNodes()) {
      Expression fieldAccess = AstNodeUtils.getFieldAssignment(node);
      if (fieldAccess != null) {
        Assignment assignment = (Assignment) fieldAccess.getParent();
        assignments.add(assignment);
      }
    }
    return assignments;
  }

  /**
   * @return the {@link Assignment}'s to field, may be empty {@link List}.
   */
  public final List<Assignment> getFieldAssignments(String fieldName) {
    List<Assignment> assignments = Lists.newArrayList();
    for (Assignment assignment : getFieldAssignments()) {
      Expression fieldAccess = assignment.getLeftHandSide();
      String fieldAccessName = AstNodeUtils.getFieldAccessName(fieldAccess).getIdentifier();
      if (fieldName.equals(fieldAccessName)) {
        assignments.add(assignment);
      }
    }
    return assignments;
  }

  /**
   * @return the {@link Assignment} to field, or <code>null</code> if not found.
   */
  public final Assignment getFieldAssignment(String fieldName) {
    List<Assignment> assignments = getFieldAssignments(fieldName);
    return GenericsUtils.getFirstOrNull(assignments);
  }

  /**
   * Adds new {@link Assignment} to given field.
   */
  public final Assignment addFieldAssignment(String fieldName, String source) throws Exception {
    String assignmentSource = TemplateUtils.format("{0}.{1} = {2}", this, fieldName, source);
    return (Assignment) addExpressionStatement(assignmentSource);
  }

  /**
   * Removes {@link Assignment}'s to given field on this {@link JavaInfo}.
   */
  public final void removeFieldAssignments(String fieldName) throws Exception {
    List<Assignment> assignments = getFieldAssignments(fieldName);
    for (Assignment assignment : assignments) {
      if (!AstNodeUtils.isDanglingNode(assignment)) {
        m_editor.removeEnclosingStatement(assignment);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isRepresentedBy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> is given {@link ASTNode} represents this {@link JavaInfo}.
   */
  public boolean isRepresentedBy(ASTNode node) {
    return getChildRepresentedBy(node) == this;
  }

  /**
   * @return the {@link JavaInfo} that represents given {@link ASTNode}, or <code>null</code>.
   */
  public JavaInfo getChildRepresentedBy(ASTNode node) {
    if (node == null || node instanceof Expression) {
      JavaInfoResolver resolver = EditorState.get(m_editor).getJavaInfoResolver();
      return resolver.getJavaInfo((Expression) node);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  private JavaInfoEvaluationHelper m_evaluationHelper;

  @Override
  public void refresh_dispose() throws Exception {
    // clean object reference
    m_object = null;
    // continue
    super.refresh_dispose();
  }

  @Override
  public void refresh_beforeCreate() throws Exception {
    // mark active editor
    if (isRoot()) {
      GlobalStateJava.activate(this);
    }
    super.refresh_beforeCreate();
  }

  @Override
  public void refresh_create() throws Exception {
    ExecutionFlowDescription flowDescription = EditorState.get(m_editor).getFlowDescription();
    // prepare editor state
    final EditorState editorState;
    {
      editorState = EditorState.get(m_editor);
      editorState.getBadRefreshNodes().clear();
      editorState.setTmp_visitingContext(new VisitingContext(false));
      editorState.getTmp_InterceptedMethods().clear();
      editorState.setExecuting(true);
    }
    // visited ASTNode-s
    final VisitedNodes visitedNodes = editorState.getVisitedNodes();
    visitedNodes.clear();
    // prepare visitor for evaluation
    final EvaluationEventListener evaluationListener = getBroadcast(EvaluationEventListener.class);
    m_evaluationHelper = null;
    ExecutionFlowFrameVisitor visitor = new ExecutionFlowFrameVisitor() {
      @Override
      public void postVisit(ASTNode node) {
        visitedNodes.add(node);
        try {
          JavaInfoEvaluationHelper evaluationHelper = getEvaluationHelper(this);
          // send notifications and evaluate
          if (shouldNotifyAboutEvaluate(node)) {
            EvaluationContext context = getEvaluationHelper(this).getContext();
            evaluationListener.evaluateBefore(context, node);
          }
          evaluationHelper.evaluate(node);
          if (shouldNotifyAboutEvaluate(node)) {
            EvaluationContext context = getEvaluationHelper(this).getContext();
            evaluationListener.evaluateAfter(context, node);
          }
        } catch (Throwable e) {
          // exception during JavaInfo evaluation is serious, so fail
          if (node instanceof Expression && getChildRepresentedBy(node) != null) {
            ReflectionUtils.propagate(e);
          }
          // some other exception, log and continue
          editorState.getBadRefreshNodes().add(node, e);
        }
      }

      private boolean shouldNotifyAboutEvaluate(ASTNode node) {
        return node instanceof ClassInstanceCreation
            || node instanceof MethodInvocation
            || node instanceof Statement;
      }

      @Override
      public boolean enterFrame(final ASTNode node) {
        // send broadcast
        ExecutionUtils.runRethrow(new RunnableEx() {
          public void run() throws Exception {
            editorState.getBroadcast().getListener(ExecutionFlowEnterFrame.class).invoke(node);
          }
        });
        // MethodDeclaration
        if (node instanceof MethodDeclaration) {
          MethodDeclaration methodDeclaration = (MethodDeclaration) node;
          // don't visit local factory methods
          if (FactoryDescriptionHelper.isFactoryMethod(methodDeclaration)) {
            return false;
          }
        }
        return super.enterFrame(node);
      }

      @Override
      public void leaveFrame(final ASTNode node) {
        super.leaveFrame(node);
        // send broadcast
        ExecutionUtils.runRethrow(new RunnableEx() {
          public void run() throws Exception {
            editorState.getBroadcast().getListener(EvaluationEventListener.class).leaveFrame(node);
          }
        });
      }
    };
    // evaluate "this"
    if (!getCreationSupport().canBeEvaluated()) {
      getEvaluationHelper(visitor).evaluateJavaInfoUsingCreationSupport(this);
    }
    // visit all AST nodes on execution flow and execute nodes related to components
    ExecutionFlowUtils.visit(editorState.getTmp_visitingContext(), flowDescription, visitor);
    editorState.setExecuting(false);
    highlightVisitedNodes(visitedNodes);
  }

  private void highlightVisitedNodes(VisitedNodes visitedNodes) throws JavaModelException {
    // don't call DesignPageSite methods
    if (EnvironmentUtils.DEVELOPER_HOST && EnvironmentUtils.isTestingTime()) {
      return;
    }
    // do highlight
    DesignPageSite site = DesignPageSite.Helper.getSite(this);
    if (site != null) {
      AstEditor editor = getEditor();
      String editorSource = editor.getSource();
      String unitSource = editor.getModelUnit().getSource();
      boolean isCommitted = editorSource.equals(unitSource);
      if (isCommitted) {
        // TODO(scheglov)
//        site.highlightVisitedNodes(visitedNodes.getNodes());
      }
    }
  }

  private JavaInfoEvaluationHelper getEvaluationHelper(ExecutionFlowFrameVisitor visitor) {
    if (m_evaluationHelper == null) {
      m_evaluationHelper = new JavaInfoEvaluationHelper(m_editor, visitor) {
        @Override
        protected JavaInfo getRootJavaInfo() {
          return m_this;
        }

        @Override
        protected JavaInfo getJavaInfoRepresentedBy(Expression expression) {
          return m_this.getChildRepresentedBy(expression);
        }

        @Override
        protected void thisJavaInfoNodeProcessed(JavaInfo javaInfo, ASTNode node) throws Exception {
        }
      };
    }
    return m_evaluationHelper;
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    if (!isPlaceholder()) {
      JavaInfoUtils.executeScriptParameter(this, "refresh_afterCreate");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object m_object;

  /**
   * @return <code>true</code> if {@link #getObject()} has not actual object, but placeholder, so
   *         not all methods may be invoked.
   */
  public final boolean isPlaceholder() {
    return PlaceholderUtils.isPlaceholder(this);
  }

  /**
   * @return the {@link Object} created for this {@link JavaInfo}.
   */
  public final Object getObject() {
    return m_object;
  }

  /**
   * Sets the {@link Object} created for this {@link JavaInfo}. We create these objects externally -
   * in {@link JavaInfoEvaluationHelper} or in {@link CreationSupport}.
   */
  public void setObject(Object object) throws Exception {
    {
      Object[] objectRef = new Object[]{object};
      getBroadcast(JavaInfosetObjectBefore.class).invoke(this, objectRef);
      object = objectRef[0];
    }
    //
    m_object = object;
    if (!m_initialized) {
      if (!isPlaceholder()) {
        initialize();
      }
    }
    //
    getBroadcast(JavaInfoSetObjectAfter.class).invoke(this, object);
  }

  /**
   * @return the {@link JavaInfo} with same object as given.
   */
  public final JavaInfo getChildByObject(final Object o) {
    if (o == null) {
      return null;
    }
    final JavaInfo result[] = new JavaInfo[1];
    accept(new ObjectInfoVisitor() {
      @Override
      public boolean visit(ObjectInfo objectInfo) throws Exception {
        if (result[0] == null && objectInfo instanceof JavaInfo) {
          JavaInfo javaInfo = (JavaInfo) objectInfo;
          if (javaInfo.getObject() == o) {
            result[0] = javaInfo;
          }
        }
        return result[0] == null;
      }
    });
    return result[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link MethodInvocation} should be evaluated. For example we
   *         should evaluate <code>setXXX(value)</code> invocations, but not
   *         <code>addXXXListener(anonymousClass)</code>. By default methods from description are
   *         used.
   */
  public boolean shouldEvaluateInvocation(MethodInvocation invocation) {
    IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(invocation);
    MethodDescription methodDescription = getDescription().getMethod(methodBinding);
    return methodDescription != null && methodDescription.isExecutable();
  }
}
