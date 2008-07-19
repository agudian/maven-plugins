package org.apache.maven.plugin.javadoc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Locale;

import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;

/**
 * Generates documentation for the <code>Java Test code</code> in an <b>aggregator</b> project using the standard
 * <a href="http://java.sun.com/j2se/javadoc/">Javadoc Tool</a>.
 * <br/>
 * <b>Note</b>: the <code>aggregate</code> parameter is always set to <code>true</code>.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 * @goal test-aggregate
 * @aggregator
 * @since 2.5
 */
public class AggregatorTestJavadocReport
    extends TestJavadocReport
{
    /** {@inheritDoc} */
    public void generate( Sink sink, Locale locale )
        throws MavenReportException
    {
        // operate always in aggregation mode
        aggregate = true;

        super.generate( sink, locale );
    }
}
