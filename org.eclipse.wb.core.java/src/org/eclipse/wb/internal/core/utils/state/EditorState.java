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
package org.eclipse.wb.internal.core.utils.state;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.VisitingContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.parser.AbstractParseFactory;
import org.eclipse.wb.internal.core.parser.JavaInfoResolver;
import org.eclipse.wb.internal.core.utils.IDisposable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.collections.map.MultiKeyMap;

import java.beans.PropertyEditorManager;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * The editor-wide state. It contains information about current editor.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public final class EditorState {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Global
  //
  ////////////////////////////////////////////////////////////////////////////
  private static JavaInfo m_activeJavaInfo;

  /**
   * @return some {@link JavaInfo} of active editor.
   */
  public static JavaInfo getActiveJavaInfo() {
    return m_activeJavaInfo;
  }

  /**
   * Sets some {@link JavaInfo} of active editor.
   * <p>
   * We use this {@link JavaInfo} to understand context in which Designer operates, for example
   * RCP/eRCP uses same model classes for widgets, but access for bounds/image/etc is specific to
   * the toolkit.
   */
  public static void setActiveJavaInfo(JavaInfo rootInfo) {
    m_activeJavaInfo = rootInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_disposed;
  private final AstEditor m_editor;
  private final VisitedNodes m_visitedNodes;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_EDITOR_STATE = "KEY_EDITOR_STATE";

  /**
   * @return the {@link EditorState} instance for given {@link AstEditor}.
   */
  public static EditorState get(AstEditor editor) {
    EditorState state = (EditorState) editor.getGlobalValue(KEY_EDITOR_STATE);
    if (state == null) {
      state = new EditorState(editor);
      editor.putGlobalValue(KEY_EDITOR_STATE, state);
    }
    return state;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private EditorState(AstEditor editor) {
    m_editor = editor;
    m_visitedNodes = new VisitedNodes();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcast
  //
  ////////////////////////////////////////////////////////////////////////////
  private BroadcastSupport m_broadcastSupport;

  /**
   * @return the {@link BroadcastSupport} for this editor.
   */
  public BroadcastSupport getBroadcast() {
    if (m_broadcastSupport == null) {
      m_broadcastSupport = new BroadcastSupport();
    }
    return m_broadcastSupport;
  }

  /**
   * Sets the {@link BroadcastSupport} for this editor.<br>
   *
   * Usually we don't need this method, however when we create "live image", we should use separate
   * {@link BroadcastSupport}.
   */
  public void setBroadcastSupport(BroadcastSupport broadcastSupport) {
    m_broadcastSupport = broadcastSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AstEditor} for unit under design.
   */
  public AstEditor getEditor() {
    return m_editor;
  }

  /**
   * @return the holder for visited nodes.
   */
  public VisitedNodes getVisitedNodes() {
    return m_visitedNodes;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JavaInfo Resolver
  //
  ////////////////////////////////////////////////////////////////////////////
  private JavaInfoResolver m_javaInfoResolver;

  /**
   * @return the {@link JavaInfoResolver} for this editor.
   */
  public JavaInfoResolver getJavaInfoResolver() {
    return m_javaInfoResolver;
  }

  /**
   * Sets the {@link JavaInfoResolver} for this editor.
   */
  public void setJavaInfoResolver(JavaInfoResolver javaInfoResolver) {
    m_javaInfoResolver = javaInfoResolver;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dispose
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if hierarchy was disposed.
   */
  public boolean isDisposed() {
    return m_disposed;
  }

  /**
   * Disposes any resource allocated globally for current editor.
   */
  public void dispose() {
    // dispose Image's
    {
      for (Image image : m_disposableImages) {
        if (image != null && !image.isDisposed()) {
          image.dispose();
        }
      }
      m_disposableImages.clear();
    }
    // dispose the IDisposable's
    {
      for (IDisposable disposable : m_disposableList) {
        disposable.dispose();
      }
      m_disposableList.clear();
    }
    // clear Java internals
    dispose_PropertyEditorManager();
    dispose_UIManager();
    // dispose class loader
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        AbstractParseFactory.deinitializeClassLoader(m_editorLoader, m_toolkitId);
      }
    });
    // done
    m_disposed = true;
  }

  /**
   * Removes cached {@link Class}-s to prevent memory leak and {@link ClassCastException}'s.
   * <p>
   * We can not use <code>entrySet()</code> method, because of bug - it does not call
   * <code>super()</code>.
   */
  private void dispose_UIManager() {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        UIDefaults defaults = UIManager.getDefaults();
        Collection<Object> values = defaults.values();
        for (Iterator<?> I = values.iterator(); I.hasNext();) {
          Object value = I.next();
          if (value instanceof Class<?>) {
            if (isLoadedFrom((Class<?>) value)) {
              I.remove();
            }
          }
        }
      }
    });
  }

  /**
   * Clear {@link PropertyEditorManager} cache to prevent {@link Class} and {@link ClassLoader}
   * leaks.
   */
  private void dispose_PropertyEditorManager() {
    ExecutionUtils.runIgnore(new RunnableEx() {
      @SuppressWarnings("rawtypes")
      public void run() throws Exception {
        Map registry =
            (Map) ReflectionUtils.getFieldObject(PropertyEditorManager.class, "registry");
        if (registry != null) {
          for (Iterator I = registry.entrySet().iterator(); I.hasNext();) {
            Map.Entry entry = (Map.Entry) I.next();
            Class editorClass = (Class) entry.getValue();
            if (isLoadedFrom(editorClass)) {
              I.remove();
            }
          }
        }
      }
    });
  }

  /**
   * @return <code>true</code> if given {@link Class} was loaded using this {@link EditorState}.
   */
  public boolean isLoadedFrom(Class<?> clazz) {
    return isLoadedFrom(clazz, m_editorLoader);
  }

  /**
   * @return <code>true</code> if given {@link Class} was loaded using this {@link EditorState}.
   */
  private static boolean isLoadedFrom(Class<?> clazz, ClassLoader loader) {
    // check for this ClassLoader
    if (clazz.getClassLoader() == loader) {
      return true;
    }
    // check parts of CompositeClassLoader
    if (loader instanceof CompositeClassLoader) {
      CompositeClassLoader compositeClassLoader = (CompositeClassLoader) loader;
      for (Iterator<?> I = compositeClassLoader.getClassLoaders().iterator(); I.hasNext();) {
        ClassLoader classLoader = (ClassLoader) I.next();
        if (isLoadedFrom(clazz, classLoader)) {
          return true;
        }
      }
    }
    // generic case
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExecutionFlowDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  private ExecutionFlowDescription m_flowDescription;

  /**
   * @return the {@link ExecutionFlowDescription} of current editor.
   */
  public ExecutionFlowDescription getFlowDescription() {
    return m_flowDescription;
  }

  /**
   * Sets new {@link ExecutionFlowDescription}, can be called only one time.
   */
  public void setFlowDescription(ExecutionFlowDescription flowDescription) {
    Assert.isNotNull(flowDescription);
    m_flowDescription = flowDescription;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Executing flag
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_isExecuting;

  /**
   * @return <code>true</code> if we visit/execute AST now. Some methods may be intercepted during
   *         fetching, we don't need them.
   */
  public boolean isExecuting() {
    return m_isExecuting;
  }

  /**
   * Specifies if in visit/execute AST.
   */
  public void setExecuting(boolean isExecuting) {
    m_isExecuting = isExecuting;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live component" flag
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_isLiveComponent;

  /**
   * @return <code>true</code> if model hierarchy is in process of "live component" operation.
   */
  public boolean isLiveComponent() {
    return m_isLiveComponent;
  }

  /**
   * Specifies if model hierarchy is in process of "live component".
   */
  public void setLiveComponent(boolean isLiveComponent) {
    m_isLiveComponent = isLiveComponent;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Temporary parse/execution time values
  //
  ////////////////////////////////////////////////////////////////////////////
  private VisitingContext m_tmp_visitingContext;
  private List<JavaInfo> m_tmp_Components;
  private final Set<MethodDeclaration> m_tmp_InterceptedMethods = Sets.newHashSet();

  /**
   * Sets the parse/execution time {@link VisitingContext}.
   */
  public void setTmp_visitingContext(VisitingContext tmp_visitingContext) {
    m_tmp_visitingContext = tmp_visitingContext;
  }

  /**
   * @return the parse/execution time {@link VisitingContext}.
   */
  public VisitingContext getTmp_visitingContext() {
    return m_tmp_visitingContext;
  }

  /**
   * Sets the parse time components {@link List}.
   */
  public void setTmp_Components(List<JavaInfo> components) {
    m_tmp_Components = components;
  }

  /**
   * @return the parse time components {@link List}.
   */
  public List<JavaInfo> getTmp_Components() {
    return m_tmp_Components;
  }

  /**
   * @return the {@link MethodDeclaration} that were already intercepted during parsing.
   */
  public Set<MethodDeclaration> getTmp_InterceptedMethods() {
    return m_tmp_InterceptedMethods;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_toolkitId;
  private ClassLoader m_editorLoader;

  /**
   * @return the id of "main" toolkit, i.e. toolkit that created {@link ClassLoader}.
   */
  public String getToolkitId() {
    return m_toolkitId;
  }

  /**
   * @return the {@link ClassLoader} for editor.
   */
  public ClassLoader getEditorLoader() {
    return m_editorLoader;
  }

  /**
   * @return <code>true</code> if this {@link EditorState} was already initialized.
   */
  public boolean isInitialized() {
    return m_editorLoader != null;
  }

  /**
   * Specifies that this {@link EditorState} created for some toolkit and given {@link ClassLoader}
   * should be used to load classes.
   *
   * @param toolkitId
   *          the id of "main" toolkit.
   * @param loader
   *          the {@link ClassLoader} to load classes from project.
   */
  public void initialize(String toolkitId, ClassLoader loader) {
    m_toolkitId = toolkitId;
    // class loader can be set only one time
    Assert.isNull(m_editorLoader);
    // check new class loader and set
    Assert.isNotNull(loader);
    m_editorLoader = loader;
    GlobalState.setClassLoader(loader);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionVersionsProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, Object> m_versions = Maps.newHashMap();
  private final List<IDescriptionVersionsProvider> m_descriptionVersionsProviders =
      Lists.newArrayList();

  /**
   * @return the {@link Map} of versions for toolkit in this {@link EditorState}.
   */
  public Map<String, Object> getVersions() {
    return m_versions;
  }

  /**
   * @see IDescriptionVersionsProvider
   */
  public List<IDescriptionVersionsProvider> getDescriptionVersionsProviders() {
    return m_descriptionVersionsProviders;
  }

  public void addVersions(Map<String, ?> versions) {
    m_versions.putAll(versions);
  }

  public void addDescriptionVersionsProvider(IDescriptionVersionsProvider provider) {
    Assert.isNotNull(provider);
    if (!m_descriptionVersionsProviders.contains(provider)) {
      m_descriptionVersionsProviders.add(provider);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Static FactoryMethodDescription's cache
  //
  ////////////////////////////////////////////////////////////////////////////
  private final MultiKeyMap/*<Class,boolean,Map<String, FactoryMethodDescription>>*/m_staticSignatures =
      new MultiKeyMap();

  /**
   * @return the cached map: signature -> {@link FactoryMethodDescription}.
   *
   * @param factoryClass
   *          the name of class that contains factory method
   */
  @SuppressWarnings("unchecked")
  public Map<String, FactoryMethodDescription> getFactorySignatures(Class<?> factoryClass,
      boolean forStatic) {
    return (Map<String, FactoryMethodDescription>) m_staticSignatures.get(factoryClass, forStatic);
  }

  /**
   * Sets the cached map: signature -> {@link FactoryMethodDescription}.
   *
   * @param factoryClass
   *          the class with factory methods
   * @param signaturesMap
   *          the map: signature -> {@link FactoryMethodDescription}
   */
  public void putFactorySignatures(Class<?> factoryClass,
      boolean forStatic,
      Map<String, FactoryMethodDescription> signaturesMap) {
    m_staticSignatures.put(factoryClass, forStatic, signaturesMap);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Icons
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<Image> m_disposableImages = Lists.newArrayList();

  /**
   * Adds {@link Image} for further disposing during disposing the editor.
   */
  public void addDisposableImage(Image image) {
    if (image != null && !m_disposableImages.contains(image)) {
      m_disposableImages.add(image);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handling of IDisposable's
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<IDisposable> m_disposableList = Lists.newArrayList();

  /**
   * Remembers the instances of {@link IDisposable} for further disposing during disposing the
   * editor.
   */
  public void addDisposable(IDisposable disposable) {
    if (disposable != null && !m_disposableList.contains(disposable)) {
      m_disposableList.add(disposable);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BadNodeInformation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Information about "bad" {@link ASTNode} and exception that caused marking it as "bad".
   *
   * @author scheglov_ke
   */
  public static final class BadNodeInformation {
    private final ASTNode m_node;
    private final Throwable m_exception;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public BadNodeInformation(ASTNode node, Throwable exception) {
      Assert.isNotNull(node);
      m_node = node;
      m_exception = exception;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the "bad" {@link ASTNode}.
     */
    public ASTNode getNode() {
      return m_node;
    }

    /**
     * @return the cause exception.
     */
    public Throwable getException() {
      return m_exception;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // BadNodesCollection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Collection of {@link BadNodeInformation}.
   */
  public static final class BadNodesCollection {
    private final List<BadNodeInformation> m_nodes = Lists.newArrayList();

    /**
     * Removes all nodes.
     */
    public void clear() {
      m_nodes.clear();
    }

    /**
     * Adds new bad {@link ASTNode} and its exception.
     */
    public void add(ASTNode node, Throwable e) {
      m_nodes.add(new BadNodeInformation(node, e));
    }

    /**
     * @return <code>true</code> is there are no bad nodes.
     */
    public boolean isEmpty() {
      return m_nodes.isEmpty();
    }

    /**
     * @return the {@link List} of {@link BadNodeInformation}.
     */
    public List<BadNodeInformation> nodes() {
      return m_nodes;
    }

    public void print() {
      for (BadNodeInformation node : m_nodes) {
        System.out.flush();
        System.err.flush();
        System.out.println("------------------ bad node ------------------");
        System.out.println(node.getNode());
        System.out.flush();
        System.err.flush();
        node.getException().printStackTrace();
        System.out.flush();
        System.err.flush();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bad nodes
  //
  ////////////////////////////////////////////////////////////////////////////
  private final BadNodesCollection m_badParserNodes = new BadNodesCollection();
  private final BadNodesCollection m_badRefreshNodes = new BadNodesCollection();

  /**
   * @return the {@link BadNodesCollection} for parser.
   */
  public BadNodesCollection getBadParserNodes() {
    return m_badParserNodes;
  }

  /**
   * @return the {@link BadNodesCollection} for refresh process.
   */
  public BadNodesCollection getBadRefreshNodes() {
    return m_badRefreshNodes;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Warnings
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<EditorWarning> m_warnings = Lists.newArrayList();

  /**
   * @return the {@link List} of {@link EditorWarning}'s.
   */
  public List<EditorWarning> getWarnings() {
    return m_warnings;
  }

  /**
   * Adds new {@link EditorWarning}.
   */
  public void addWarning(EditorWarning warning) {
    m_warnings.add(warning);
  }
}
