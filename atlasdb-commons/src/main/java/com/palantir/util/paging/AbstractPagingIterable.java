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
package com.palantir.util.paging;

import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.palantir.common.base.Throwables;

/**
 * This class only requires two methods {@link #getFirstPage()} and
 * {@link #getNextPage(BasicResultsPage)} to be implmented to make an iterable.
 *
 * @param <P> page type
 * @param <T> data type
 *
 * @author carrino
 */
public abstract class AbstractPagingIterable<T, P extends BasicResultsPage<T>> implements Iterable<T> {
    @Override
    public PagingIterator<T, P> iterator() {
        return new PagingIterator<T, P>(this);
    }

    /**
     *
     * @author manthony
     *
     * @param <T>
     * @param <P>
     */
    public static class PagingIterator<T, P extends BasicResultsPage<T>> extends AbstractIterator<T> {
        private final AbstractPagingIterable<T, P> iterable;
        private P currentPage = null;
        private Iterator<T> currentIterator = null;
        private volatile P lastPageProcessedInFull = null;

        protected PagingIterator(AbstractPagingIterable<T, P> iterable) {
            this.iterable = iterable;
        }

        @Override
        protected T computeNext() {
            if (currentPage == null) {
                try {
                    setNextPage(iterable.getFirstPage());
                } catch (Exception e) {
                    throw Throwables.throwUncheckedException(e);
                }
            }
            while (true) {
                if (currentIterator.hasNext()) {
                    return currentIterator.next();
                }

                lastPageProcessedInFull = currentPage;

                if (!currentPage.moreResultsAvailable()) {
                    return endOfData();
                }

                try {
                    setNextPage(iterable.getNextPage(currentPage));
                } catch(Exception e) {
                    throw Throwables.throwUncheckedException(e);
                }
            }
        }

        private void setNextPage(P page) {
            Preconditions.checkNotNull(page);
            currentPage = page;
            currentIterator = page.getResults().iterator();
        }

        /**
         * As this iterator pages through all the results this call will
         * get you the token you can use to get more data at a later date.
         * <p>
         * This value will be null until this iterator has passed the first page.
         * <p>
         * This value may be used even if this iterator throws an Exception.
         *
         * @return
         */
        public P getLastFullPageProcessed() {
            return lastPageProcessedInFull;
        }
    }

    protected abstract P getFirstPage() throws Exception;

    protected abstract P getNextPage(P previous) throws Exception;

}
