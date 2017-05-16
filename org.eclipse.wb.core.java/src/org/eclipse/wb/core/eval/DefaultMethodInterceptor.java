package org.eclipse.wb.core.eval;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * {@link MethodInterceptor} which returns default value for anonymous methods.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public class DefaultMethodInterceptor implements MethodInterceptor {
  public static final DefaultMethodInterceptor INSTANCE = new DefaultMethodInterceptor();

  public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
      throws Throwable {
    if (ReflectionUtils.isAbstract(method)) {
      return ReflectionUtils.getDefaultValue(method.getReturnType());
    } else {
      return proxy.invokeSuper(obj, args);
    }
  }
}