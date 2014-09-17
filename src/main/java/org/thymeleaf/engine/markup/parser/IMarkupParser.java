/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
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
package org.thymeleaf.engine.markup.parser;

import java.io.Reader;

import org.thymeleaf.engine.markup.MarkupEngineConfiguration;
import org.thymeleaf.engine.markup.handler.IMarkupHandler;

/**
 *
 * @author Daniel Fern&aacute;ndez
 * @since 3.0.0
 * 
 */
public interface IMarkupParser {

    
    public void parseTemplate(final MarkupEngineConfiguration configuration,
                              final IMarkupHandler handler,
                              final String documentName,
                              final Reader reader);

    public void parseTemplate(final MarkupEngineConfiguration configuration,
                              final IMarkupHandler handler,
                              final String documentName,
                              final Reader reader,
                              final int lineOffset, final int colOffset);

    public void parseFragment(final MarkupEngineConfiguration configuration,
                              final IMarkupHandler handler,
                              final String fragment);

    public void parseFragment(final MarkupEngineConfiguration configuration,
                              final IMarkupHandler handler,
                              final String fragment,
                              final int lineOffset, final int colOffset);

}