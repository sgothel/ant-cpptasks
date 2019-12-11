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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;
import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.CUtil;
import net.sf.antcontrib.cpptasks.CompilerDef;
import net.sf.antcontrib.cpptasks.DependencyInfo;
import net.sf.antcontrib.cpptasks.ProcessorDef;
import net.sf.antcontrib.cpptasks.parser.Parser;
import net.sf.antcontrib.cpptasks.TargetDef;
import net.sf.antcontrib.cpptasks.VersionInfo;

/**
 * An abstract compiler implementation.
 *
 * @author Adam Murdoch
 * @author Curt Arnold
 */
public abstract class AbstractCompiler extends AbstractProcessor
        implements
            Compiler {
    private static final String[] emptyIncludeArray = new String[0];
    private final String outputSuffix;
    protected AbstractCompiler(final String[] sourceExtensions,
            final String[] headerExtensions, final String outputSuffix) {
        super(sourceExtensions, headerExtensions);
        this.outputSuffix = outputSuffix;
    }
    protected AbstractCompiler(final AbstractCompiler cc) {
        super(cc);
        this.outputSuffix = cc.outputSuffix;
    }
    /**
     * Checks file name to see if parse should be attempted
     *
     * Default implementation returns false for files with extensions '.dll',
     * 'tlb', '.res'
     *
     */
    protected boolean canParse(final File sourceFile) {
        final String sourceName = sourceFile.toString();
        final int lastPeriod = sourceName.lastIndexOf('.');
        if (lastPeriod >= 0 && lastPeriod == sourceName.length() - 4) {
            final String ext = sourceName.substring(lastPeriod).toUpperCase();
            if (ext.equals(".DLL") || ext.equals(".TLB") || ext.equals(".RES")) {
                return false;
            }
        }
        return true;
    }
    abstract protected CompilerConfiguration createConfiguration(CCTask task,
            LinkType linkType, ProcessorDef[] baseConfigs,
            CompilerDef specificConfig, TargetDef targetPlatform,
			VersionInfo versionInfo);
    @Override
    public ProcessorConfiguration createConfiguration(final CCTask task,
            final LinkType linkType, final ProcessorDef[] baseConfigs,
            final ProcessorDef specificConfig, final TargetDef targetPlatform,
			final VersionInfo versionInfo) {
        if (specificConfig == null) {
            throw new NullPointerException("specificConfig");
        }
        return createConfiguration(task, linkType, baseConfigs,
                (CompilerDef) specificConfig, targetPlatform, versionInfo);
    }
    abstract protected Parser createParser(File sourceFile);
    protected String getBaseOutputName(final String inputFile) {
        int lastSlash = inputFile.lastIndexOf('/');
        final int lastReverse = inputFile.lastIndexOf('\\');
        final int lastSep = inputFile.lastIndexOf(File.separatorChar);
        if (lastReverse > lastSlash) {
            lastSlash = lastReverse;
        }
        if (lastSep > lastSlash) {
            lastSlash = lastSep;
        }
        int lastPeriod = inputFile.lastIndexOf('.');
        if (lastPeriod < 0) {
            lastPeriod = inputFile.length();
        }
        return inputFile.substring(lastSlash + 1, lastPeriod);
    }
    @Override
    public String[] getOutputFileNames(final String inputFile, final VersionInfo versionInfo) {
        //
        //  if a recognized input file
        //
        if (bid(inputFile) > 1) {
            final String baseName = getBaseOutputName(inputFile);
            return new String[] { baseName + outputSuffix };
        }
        return new String[0];
    }
    /**
     * Returns dependency info for the specified source file
     *
     * @param task
     *            task for any diagnostic output
     * @param source
     *            file to be parsed
     * @param includePath
     *            include path to be used to resolve included files
     *
     * @param sysIncludePath
     *            sysinclude path from build file, files resolved using
     *            sysInclude path will not participate in dependency analysis
     *
     * @param envIncludePath
     *            include path from environment variable, files resolved with
     *            envIncludePath will not participate in dependency analysis
     *
     * @param baseDir
     *            used to produce relative paths in DependencyInfo
     * @param includePathIdentifier
     *            used to distinguish DependencyInfo's from different include
     *            path settings
     *
     */
    public final DependencyInfo parseIncludes(final CCTask task, final File source,
            final File[] includePath, final File[] sysIncludePath, final File[] envIncludePath,
            final File baseDir, final String includePathIdentifier) {
        //
        //  if any of the include files can not be identified
        //      change the sourceLastModified to Long.MAX_VALUE to
        //      force recompilation of anything that depends on it
        long sourceLastModified = source.lastModified();
        final File[] sourcePath = new File[1];
        sourcePath[0] = new File(source.getParent());
        final Vector onIncludePath = new Vector();
        final Vector onSysIncludePath = new Vector();
        String baseDirPath;
        try {
            baseDirPath = baseDir.getCanonicalPath();
        } catch (final IOException ex) {
            baseDirPath = baseDir.toString();
        }
        final String relativeSource = CUtil.getRelativePath(baseDirPath, source);
        String[] includes = emptyIncludeArray;
        if (canParse(source)) {
            final Parser parser = createParser(source);
            try {
                final Reader reader = new BufferedReader(new FileReader(source));
                parser.parse(reader);
                includes = parser.getIncludes();
            } catch (final IOException ex) {
                task.log("Error parsing " + source.toString() + ":"
                        + ex.toString());
                includes = new String[0];
            }
        }
        for (int i = 0; i < includes.length; i++) {
            final String includeName = includes[i];
            if (!resolveInclude(includeName, sourcePath, onIncludePath)) {
                if (!resolveInclude(includeName, includePath, onIncludePath)) {
                    if (!resolveInclude(includeName, sysIncludePath,
                            onSysIncludePath)) {
                        if (!resolveInclude(includeName, envIncludePath,
                                onSysIncludePath)) {
                            //
                            //  this should be enough to require us to reparse
                            //     the file with the missing include for dependency
                            //     information without forcing a rebuild
                            sourceLastModified += 2 * CUtil.FILETIME_EPSILON;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < onIncludePath.size(); i++) {
            final String relativeInclude = CUtil.getRelativePath(baseDirPath,
                    (File) onIncludePath.elementAt(i));
            onIncludePath.setElementAt(relativeInclude, i);
        }
        for (int i = 0; i < onSysIncludePath.size(); i++) {
            final String relativeInclude = CUtil.getRelativePath(baseDirPath,
                    (File) onSysIncludePath.elementAt(i));
            onSysIncludePath.setElementAt(relativeInclude, i);
        }
        return new DependencyInfo(includePathIdentifier, relativeSource,
                sourceLastModified, onIncludePath, onSysIncludePath);
    }
    protected boolean resolveInclude(final String includeName, final File[] includePath,
            final Vector onThisPath) {
        for (int i = 0; i < includePath.length; i++) {
            final File includeFile = new File(includePath[i], includeName);
            if (includeFile.exists()) {
                onThisPath.addElement(includeFile);
                return true;
            }
        }
        return false;
    }
}
