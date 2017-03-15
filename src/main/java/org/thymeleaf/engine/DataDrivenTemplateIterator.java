/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2016, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * <p>
 *   Throttled implementation of {@link Iterator}, meant to be queried in scenarios when an iterated
 *   context variable is allowed to be in control of the engine's throttling (i.e. the engine's execution
 *   is <em>data-driven</em>).
 * </p>
 * <p>
 *   A common scenario for this would be reactive systems executing the template engine as a part of a
 *   flow obtaining data from a data source, so that as the data is obtained, a part of the template is output
 *   containing that part of the data.
 * </p>
 * <p>
 *   This class is meant for <strong>internal use only</strong> from the diverse integrations of Thymeleaf in
 *   reactive architectures. There is normally no reason why a user would have to use this class directly.
 * </p>
 *
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 3.0.0
 *
 */
public final class DataDrivenTemplateIterator implements Iterator<Object> {

    private final List<Object> values;
    private IThrottledTemplateWriterControl writerControl;
    private ISSEThrottledTemplateWriterControl sseControl;
    private boolean inStep;
    private boolean feedingComplete;
    private boolean queried;


    public DataDrivenTemplateIterator() {

        super();
        this.values = new ArrayList<Object>(10);
        this.writerControl = null;
        this.sseControl = null;
        this.inStep = false;
        this.feedingComplete = false;
        this.queried = false;

    }


    public void setWriterControl(final IThrottledTemplateWriterControl writerControl) {
        this.writerControl = writerControl;
        if (writerControl instanceof ISSEThrottledTemplateWriterControl) {
            this.sseControl = (ISSEThrottledTemplateWriterControl) this.writerControl;
        } else {
            this.sseControl = null;
        }
    }


    @Override
    public boolean hasNext() {
        this.queried = true;
        return !this.values.isEmpty();
    }


    @Override
    public Object next() {

        this.queried = true;

        if (this.values.isEmpty()) {
            throw new NoSuchElementException();
        }

        final Object value = this.values.get(0);
        this.values.remove(0);
        return value;

    }


    public void startIteration() {
        this.inStep = true;
        if (this.sseControl != null) {
            this.sseControl.startEvent(null, "data");
        }
    }


    public void finishIteration() {
        finishStep();
    }


    /**
     * <p>
     *   Returns whether this data driven iterator has been actually queried, i.e., whether its {@link #hasNext()} or
     *   {@link #next()} methods have been called at least once.
     * </p>
     * <p>
     *   This indicates if the template has actually reached a point at which this iterator has been already
     *   needed or not. The typical use of this is to be able to switch between the "head" and the "data/buffer" phase.
     * </p>
     *
     * @return <tt>true</tt> if this iterator has been queried, <tt>false</tt> if not.
     *
     * @since 3.0.3
     */
    public boolean hasBeenQueried() {
        return this.queried;
    }


    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() is not supported in Throttled Iterator");
    }



    boolean isPaused() {
        this.queried = true;
        return this.values.isEmpty() && !this.feedingComplete;
    }


    public boolean continueBufferExecution() {
        return !this.values.isEmpty();
    }


    public void feedBuffer(final List<Object> newElements) {
        this.values.addAll(newElements);
    }



    public void startHead() {
        this.inStep = true;
        if (this.sseControl != null) {
            this.sseControl.startEvent(null, "head");
        }
    }

    public void feedingComplete() {
        this.feedingComplete = true;
    }


    public void startTail() {
        this.inStep = true;
        if (this.sseControl != null) {
            this.sseControl.startEvent(null, "tail");
        }
    }


    public void finishStep() {
        if (!this.inStep) {
            return;
        }
        this.inStep = false;
        if (this.sseControl != null) {
            try {
                this.sseControl.endEvent();
            } catch (final IOException e) {
                throw new TemplateProcessingException("Cannot signal end of SSE event", e);
            }
        }
    }


    public boolean isStepOutputFinished() {
        if (this.inStep) {
            return false;
        }
        if (this.writerControl != null) {
            try {
                return !this.writerControl.isOverflown();
            } catch (final IOException e) {
                throw new TemplateProcessingException("Cannot signal end of SSE event", e);
            }
        }
        // We just don't know, so we will not worry about overflow
        return true;
    }



}
