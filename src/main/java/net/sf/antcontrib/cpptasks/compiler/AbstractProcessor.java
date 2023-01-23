/*
 *
 * Copyright 2002-2004 The Ant-Contrib project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.sf.antcontrib.cpptasks.compiler;
import org.apache.tools.ant.types.Environment;

import net.sf.antcontrib.cpptasks.OSType;
import net.sf.antcontrib.cpptasks.Platform;

/**
 * An abstract processor (compiler/linker) implementation.
 *
 * @author Curt Arnold
 */
public abstract class AbstractProcessor implements Processor, Cloneable {
    /**
     * default bid for a file name that the processor recognizes but does not
     * process and does not want to fall through to the linker
     */
    public final static int DEFAULT_DISCARD_BID = 1;
    /**
     * default bid for a file name that the processor desires to process
     */
    public final static int DEFAULT_PROCESS_BID = 100;
    /**
     * Determines the identification of a command line processor by capture the
     * first line of its output for a specific command.
     *
     * @param command
     *            array of command line arguments starting with executable
     *            name. For example, { "cl" }
     * @param fallback
     *            start of identifier if there is an error in executing the
     *            command
     * @return identifier for the processor
     */
    protected static String getIdentifier(final String[] command, final String fallback) {
        String identifier = fallback;
        try {
            final String[] cmdout = CaptureStreamHandler.run(command);
            if (cmdout.length > 0) {
                identifier = cmdout[0];
            }
        } catch (final Throwable ex) {
            identifier = fallback + ":" + ex.toString();
        }
        return identifier;
    }
    private final String[] headerExtensions;
    private final String[] sourceExtensions;
    protected AbstractProcessor(final String[] sourceExtensions,
            final String[] headerExtensions) {
        this.sourceExtensions = sourceExtensions.clone();
        this.headerExtensions = headerExtensions.clone();
    }
    protected AbstractProcessor(final AbstractProcessor ap) {
        this.sourceExtensions = ap.sourceExtensions.clone();
        this.headerExtensions = ap.headerExtensions.clone();
    }
    /**
     * Returns the bid of the processor for the file.
     *
     * @param inputFile
     *            filename of input file
     * @return bid for the file, 0 indicates no interest, 1 indicates that the
     *         processor recognizes the file but doesn't process it (header
     *         files, for example), 100 indicates strong interest
     */
    @Override
    public int bid(final String inputFile) {
        final String lower = inputFile.toLowerCase();
        for (int i = 0; i < sourceExtensions.length; i++) {
            if (lower.endsWith(sourceExtensions[i])) {
                return DEFAULT_PROCESS_BID;
            }
        }
        for (int i = 0; i < headerExtensions.length; i++) {
            if (lower.endsWith(headerExtensions[i])) {
                return DEFAULT_DISCARD_BID;
            }
        }
        return 0;
    }
    @Override
    public Processor changeEnvironment(final boolean newEnvironment, final Environment env) {
        return this;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    public String[] getHeaderExtensions() {
        return this.headerExtensions.clone();
    }
    @Override
    abstract public String getIdentifier();

    public String[] getSourceExtensions() {
        return this.sourceExtensions.clone();
    }

    /**
     * Returns true if the target operating system is Mac OS X or Darwin.
     *
     * @return boolean
     */
    protected boolean isDarwin() {
        return OSType.MACOS == Platform.OS_TYPE;
    }

    /**
     * Returns true if the target operating system is MS Windows.
     *
     * @return boolean
     */
    protected boolean isWindows() {
        return OSType.WINDOWS == Platform.OS_TYPE;
    }

    @Override
    public final String toString() {
        return getIdentifier();
    }
}
