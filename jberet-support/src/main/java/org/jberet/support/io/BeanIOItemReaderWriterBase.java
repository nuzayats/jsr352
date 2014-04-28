/*
 * Copyright (c) 2014 Red Hat, Inc. and/or its affiliates.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Cheng Fang - Initial API and implementation
 */

package org.jberet.support.io;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import javax.batch.api.BatchProperty;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.naming.InitialContext;

import org.beanio.StreamFactory;

/**
 * The base class of BeanIO-based reader and writer classes: {@link org.jberet.support.io.BeanIOItemReader} and
 * {@link org.jberet.support.io.BeanIOItemWriter}.
 */
public abstract class BeanIOItemReaderWriterBase extends ItemReaderWriterBase {
    /**
     * Name of the BeanIO stream defined in BeanIO mapping file. It corresponds to the batch job property
     */
    @Inject
    @BatchProperty
    protected String streamName;

    /**
     * Location of the BeanIO mapping file, which can be a file path, a URL, or a resource loadable by the current
     * class loader.
     */
    @Inject
    @BatchProperty
    protected String streamMapping;

    /**
     * JNDI name for looking up {@code org.beanio.StreamFactory} when running in application server. When
     * {@code streamFactoryLookup} property is specified in job xml and hence injected here, {@code org.beanio.StreamFactory}
     * will be looked up with JNDI, and {@link BeanIOItemReaderWriterBase#streamMapping}, and
     * {@link org.jberet.support.io.BeanIOItemReaderWriterBase#mappingProperties}will be ignored.
     */
    @Inject
    @BatchProperty
    protected String streamFactoryLookup;

    /**
     * User properties that can be used for property substitution in BeanIO mapping file. When used with batch job
     * JSL properties, they provide dynamic BeanIO mapping attributes. For example,
     * <p>
     *     in batch job client class, set the properties values as comma-separated key-value pairs:<br/>
     *     {@code params.setProperty("mappingProperties", "zipCodeFieldName=zipCode, zipCodeFieldType=string");}
     * </p>
     * <p>
     *     in job xml file, make the properties available for {@link org.jberet.support.io.BeanIOItemReader} or
     *     {@link org.jberet.support.io.BeanIOItemWriter} via {@code @BatchProperty} injection:<br/>
     *     {@code <property name="mappingProperties" value="#{jobParameters['mappingProperties']}"/>}
     * </p>
     * <p>
     *     in BeanIO mapping file, reference the properties defined above:<br/>
     *     {@code <field name="${zipCodeFieldName}" type="${zipCodeFieldType}" length="5"/>}
     * </p>
     */
    @Inject
    @BatchProperty
    protected Map mappingProperties;

    @Inject
    protected JobContext jobContext;

    private static final WeakHashMap<StreamFactoryKey, StreamFactory> beanIOMappings =
            new WeakHashMap<StreamFactoryKey, StreamFactory>();

    static StreamFactory getStreamFactory(final String streamFactoryLookup,
                                          final StreamFactoryKey key,
                                          final Map mappingProperties) throws Exception {
        if (streamFactoryLookup != null) {
            return InitialContext.doLookup(streamFactoryLookup);
        }
        synchronized (beanIOMappings) {
            StreamFactory streamFactory = beanIOMappings.get(key);
            if (streamFactory != null) {
                return streamFactory;
            }
            final InputStream mappingInputStream = getInputStream(key.mappingFile, false);
            streamFactory = StreamFactory.newInstance();

            if (mappingProperties == null) {
                streamFactory.load(mappingInputStream);
            } else {
                final Properties p = new Properties();
                p.putAll(mappingProperties);
                streamFactory.load(mappingInputStream, p);
            }

            beanIOMappings.put(key, streamFactory);
            return streamFactory;
        }
    }

    static class StreamFactoryKey {
        private JobContext jobContext;
        private String mappingFile;

        StreamFactoryKey(final JobContext jobContext, final String mappingFile) {
            this.jobContext = jobContext;
            this.mappingFile = mappingFile;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("StreamFactoryKey{");
            sb.append("jobContext=").append(jobContext);
            sb.append(", mappingFile='").append(mappingFile).append('\'');
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final StreamFactoryKey that = (StreamFactoryKey) o;

            if (jobContext != that.jobContext) return false;
            if (!mappingFile.equals(that.mappingFile)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = jobContext.hashCode();
            result = 31 * result + mappingFile.hashCode();
            return result;
        }
    }
}