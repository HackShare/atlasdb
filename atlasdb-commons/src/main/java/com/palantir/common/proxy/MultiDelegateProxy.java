/**
 * Copyright 2015 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.common.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.AbstractInvocationHandler;

/**
 * This class will delegate functionality and return the value (or throw the exception) of
 * one of the delegates.
 * <p>
 * It will simply call the other delegates and ignore their return values and log any exceptions
 * that are thrown at the error level.  These are logged as errors because these classes shoudln't be throwing.
 * <p>
 * This class is useful if you have a Mock and you want to ensure certain methods
 * are/aren't called, but you want to use a Fake to do the work.
 * <p>
 */
public class MultiDelegateProxy<T> extends AbstractInvocationHandler {
    static private final Logger log = LoggerFactory.getLogger(MultiDelegateProxy.class);

    public static <T> T newProxyInstance(Class<T> interfaceClass, T mainDelegate, T... delegatesToCall) {
        return newProxyInstance(interfaceClass, mainDelegate, Arrays.asList(delegatesToCall));
    }

    /**
     * This will copy off all the objects in delegatesToCall.
     */
    public static <T> T newProxyInstance(Class<T> interfaceClass, T mainDelegate,
            Iterable<? extends T> delegatesToCall) {
        return newProxyInstance(interfaceClass, mainDelegate,
            Suppliers.ofInstance(ImmutableList.copyOf(delegatesToCall)));
    }

    /**
     * For each method invoked on the returned object, {@link Supplier#get()} will be called
     * and iterated over.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(Class<T> interfaceClass, T mainDelegate,
            Supplier<? extends Iterable<? extends T>> dynamicDelegatesToCall) {
        return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(),
            new Class<?>[] {interfaceClass}, new MultiDelegateProxy<T>(mainDelegate, dynamicDelegatesToCall));
    }

    final T delegate;
    final Supplier<? extends Iterable<? extends T>> othersToCall;

    private MultiDelegateProxy(T delegate, Supplier<? extends Iterable<? extends T>> toCall) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.othersToCall = toCall;
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            for (T t : othersToCall.get()) {
                try {
                    method.invoke(t, args);
                } catch (InvocationTargetException e) {
                    log.error("Failed when calling within proxy. This isn't allowed.", e);
                    if (e.getCause() instanceof Error) {
                        throw e;
                    }
                }
            }
            return method.invoke(delegate, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
