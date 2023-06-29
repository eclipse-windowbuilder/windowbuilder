package org.eclipse.wb.core.eval;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * {@link InvocationHandler} which returns default value for anonymous methods.
 * This handler may only be used for abstract method which have been matched via
 * {@link ElementMatchers#isAbstract}. In order to delegate any other call to
 * the {@code super} method, use {@link ElementMatchers#any()} in combination
 * with {@link SuperMethodCall#Instance} <b>prior</b> to using this handler.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public class DefaultMethodInterceptor implements InvocationHandler {
	public static final InvocationHandlerAdapter INSTANCE = InvocationHandlerAdapter.of(new DefaultMethodInterceptor());

	@Override
	public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
		return ReflectionUtils.getDefaultValue(method.getReturnType());
	}
}