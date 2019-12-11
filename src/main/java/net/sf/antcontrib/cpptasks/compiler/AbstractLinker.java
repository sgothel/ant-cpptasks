/*
 *
 * Copyright 2001-2004 The Ant-Contrib project
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
import java.io.File;
import java.io.IOException;

import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.LinkerDef;
import net.sf.antcontrib.cpptasks.ProcessorDef;
import net.sf.antcontrib.cpptasks.TargetDef;
import net.sf.antcontrib.cpptasks.TargetMatcher;
import net.sf.antcontrib.cpptasks.VersionInfo;

import org.apache.tools.ant.types.Environment;
/**
 * An abstract Linker implementation.
 *
 * @author Adam Murdoch
 */
public abstract class AbstractLinker extends AbstractProcessor
        implements
            Linker {
    public AbstractLinker(final String[] objExtensions, final String[] ignoredExtensions) {
        super(objExtensions, ignoredExtensions);
    }
    public AbstractLinker(final AbstractLinker ld) {
        super(ld);
    }
    /**
     * Returns the bid of the processor for the file.
     *
     * A linker will bid 1 on any unrecognized file type.
     *
     * @param inputFile
     *            filename of input file
     * @return bid for the file, 0 indicates no interest, 1 indicates that the
     *         processor recognizes the file but doesn't process it (header
     *         files, for example), 100 indicates strong interest
     */
    @Override
    public int bid(final String inputFile) {
        final int bid = super.bid(inputFile);
        switch (bid) {
            //
            //  unrecognized extension, take the file
            //
            case 0 :
                return 1;
            //
            //   discard the ignored extensions
            //
            case 1 :
                return 0;
        }
        return bid;
    }
    @Override
    public Processor changeEnvironment(final boolean newEnvironment, final Environment env) {
        return this;
    }
    abstract protected LinkerConfiguration createConfiguration(CCTask task,
            LinkType linkType, ProcessorDef[] baseConfigs,
            LinkerDef specificConfig, TargetDef targetPlatform,
			VersionInfo versionInfo);
    @Override
    public ProcessorConfiguration createConfiguration(final CCTask task,
            final LinkType linkType, final ProcessorDef[] baseConfigs,
            final ProcessorDef specificConfig,
			final TargetDef targetPlatform,
			final VersionInfo versionInfo) {
        if (specificConfig == null) {
            throw new NullPointerException("specificConfig");
        }
        return createConfiguration(task, linkType, baseConfigs,
                (LinkerDef) specificConfig, targetPlatform, versionInfo);
    }
    @Override
    public String getLibraryKey(final File libfile) {
        return libfile.getName();
    }
    /**
     * This implementation for a linker instance returns a unique output file
     * as determined via {@link #getOutputFileName(String, VersionInfo)}.
     */
    @Override
    public final String[] getOutputFileNames(final String baseName, final VersionInfo versionInfo) {
        return new String[] { getOutputFileName(baseName, versionInfo) };
    }
    /**
     * Unique output file for linker, also being used to fill a one element array
     * of {@link #getOutputFileNames(String, VersionInfo)} implementation.
     * @param inputFile the input file basename
     * @param versionInfo the version number
     * @return the unique output filename
     * @see #getOutputFileNames(String, VersionInfo)
     */
    public abstract String getOutputFileName(String fileName, VersionInfo versionInfo);


    /**
     * Adds source or object files to the bidded fileset to
     * support version information.
     *
     * @param versionInfo version information
     * @param linkType link type
     * @param isDebug true if debug build
     * @param outputFile name of generated executable
     * @param objDir directory for generated files
     * @param matcher bidded fileset
     */
	@Override
    public void addVersionFiles(final VersionInfo versionInfo,
			final LinkType linkType,
			final File outputFile,
			final boolean isDebug,
			final File objDir,
			final TargetMatcher matcher) throws IOException {
		if (versionInfo == null) {
			throw new NullPointerException("versionInfo");
		}
		if (linkType == null) {
			throw new NullPointerException("linkType");
		}
		if (outputFile == null) {
			throw new NullPointerException("outputFile");
		}
		if (objDir == null) {
			throw new NullPointerException("objDir");
		}
	}

}
