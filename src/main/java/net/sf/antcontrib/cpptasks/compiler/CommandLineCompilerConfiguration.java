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
import java.io.File;

import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.CompilerParam;
import net.sf.antcontrib.cpptasks.DependencyInfo;
import net.sf.antcontrib.cpptasks.ProcessorParam;
import net.sf.antcontrib.cpptasks.VersionInfo;

import org.apache.tools.ant.BuildException;
/**
 * A configuration for a C++ compiler
 *
 * @author Curt Arnold
 */
public final class CommandLineCompilerConfiguration
        implements
            CompilerConfiguration {
    private/* final */String[] args;
    private final /* final */CommandLineCompiler compiler;
    private final String[] endArgs;
    //
    //    include path from environment variable not
    //       explicitly stated in Ant script
    private/* final */File[] envIncludePath;
    private String[] exceptFiles;
    private final /* final */String identifier;
    private/* final */File[] includePath;
    private final /* final */String includePathIdentifier;
    private final boolean isPrecompiledHeaderGeneration;
    private/* final */ProcessorParam[] params;
    private final /* final */boolean rebuild;
    private/* final */File[] sysIncludePath;
    public CommandLineCompilerConfiguration(final CommandLineCompiler compiler,
            final String identifier, final File[] includePath, final File[] sysIncludePath,
            final File[] envIncludePath, final String includePathIdentifier, final String[] args,
            final ProcessorParam[] params, final boolean rebuild, final String[] endArgs) {
        if (compiler == null) {
            throw new NullPointerException("compiler");
        }
        if (identifier == null) {
            throw new NullPointerException("identifier");
        }
        if (includePathIdentifier == null) {
            throw new NullPointerException("includePathIdentifier");
        }
        if (args == null) {
            this.args = new String[0];
        } else {
            this.args = args.clone();
        }
        if (includePath == null) {
            this.includePath = new File[0];
        } else {
            this.includePath = includePath.clone();
        }
        if (sysIncludePath == null) {
            this.sysIncludePath = new File[0];
        } else {
            this.sysIncludePath = sysIncludePath.clone();
        }
        if (envIncludePath == null) {
            this.envIncludePath = new File[0];
        } else {
            this.envIncludePath = envIncludePath.clone();
        }
        this.compiler = compiler;
        this.params = params.clone();
        this.rebuild = rebuild;
        this.identifier = identifier;
        this.includePathIdentifier = includePathIdentifier;
        this.endArgs = endArgs.clone();
        exceptFiles = null;
        isPrecompiledHeaderGeneration = false;
    }
    public CommandLineCompilerConfiguration(
            final CommandLineCompilerConfiguration base, final String[] additionalArgs,
            final String[] exceptFiles, final boolean isPrecompileHeaderGeneration) {
        compiler = base.compiler;
        identifier = base.identifier;
        rebuild = base.rebuild;
        includePath = base.includePath.clone();
        sysIncludePath = base.sysIncludePath.clone();
        endArgs = base.endArgs.clone();
        envIncludePath = base.envIncludePath.clone();
        includePathIdentifier = base.includePathIdentifier;
        if (exceptFiles != null) {
            this.exceptFiles = exceptFiles.clone();
        }
        this.isPrecompiledHeaderGeneration = isPrecompileHeaderGeneration;
        args = new String[base.args.length + additionalArgs.length];
        for (int i = 0; i < base.args.length; i++) {
            args[i] = base.args[i];
        }
        int index = base.args.length;
        for (int i = 0; i < additionalArgs.length; i++) {
            args[index++] = additionalArgs[i];
        }
    }
    @Override
    public int bid(final String inputFile) {
        final int compilerBid = compiler.bid(inputFile);
        if (compilerBid > 0 && exceptFiles != null) {
            for (int i = 0; i < exceptFiles.length; i++) {
                if (inputFile.equals(exceptFiles[i])) {
                    return 0;
                }
            }
        }
        return compilerBid;
    }
    @Override
    public void compile(final CCTask task, final File outputDir, final String[] sourceFiles,
            final boolean relentless, final ProgressMonitor monitor) throws BuildException {
        if (monitor != null) {
            monitor.start(this);
        }
        try {
            compiler.compile(task, outputDir, sourceFiles, args, endArgs,
                    relentless, this, monitor);
            if (monitor != null) {
                monitor.finish(this, true);
            }
        } catch (final BuildException ex) {
            if (monitor != null) {
                monitor.finish(this, false);
            }
            throw ex;
        }
    }
    /**
     *
     * This method may be used to get two distinct compiler configurations, one
     * for compiling the specified file and producing a precompiled header
     * file, and a second for compiling other files using the precompiled
     * header file.
     *
     * The last (preferrably only) include directive in the prototype file will
     * be used to mark the boundary between pre-compiled and normally compiled
     * headers.
     *
     * @param prototype
     *            A source file (for example, stdafx.cpp) that is used to build
     *            the precompiled header file. @returns null if precompiled
     *            headers are not supported or a two element array containing
     *            the precompiled header generation configuration and the
     *            consuming configuration
     *
     */
    @Override
    public CompilerConfiguration[] createPrecompileConfigurations(
            final File prototype, final String[] nonPrecompiledFiles) {
        if (compiler instanceof PrecompilingCompiler) {
            return ((PrecompilingCompiler) compiler)
                    .createPrecompileConfigurations(this, prototype,
                            nonPrecompiledFiles);
        }
        return null;
    }
    /**
     * Returns a string representation of this configuration. Should be
     * canonical so that equivalent configurations will have equivalent string
     * representations
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }
    @Override
    public String getIncludePathIdentifier() {
        return includePathIdentifier;
    }
    @Override
    public final String[] getOutputFileNames(final String inputFile, final VersionInfo versionInfo) {
        return compiler.getOutputFileNames(inputFile, versionInfo);
    }
    @Override
    public CompilerParam getParam(final String name) {
        for (int i = 0; i < params.length; i++) {
            if (name.equals(params[i].getName()))
                return (CompilerParam) params[i];
        }
        return null;
    }
    @Override
    public ProcessorParam[] getParams() {
        return params;
    }
    @Override
    public boolean getRebuild() {
        return rebuild;
    }
    @Override
    public boolean isPrecompileGeneration() {
        return isPrecompiledHeaderGeneration;
    }
    @Override
    public DependencyInfo parseIncludes(final CCTask task, final File baseDir, final File source) {
        return compiler.parseIncludes(task, source, includePath,
                sysIncludePath, envIncludePath, baseDir,
                getIncludePathIdentifier());
    }
    @Override
    public String toString() {
        return identifier;
    }
    public String[] getPreArguments() {
    	return args.clone();
    }
    public String[] getEndArguments() {
    	return endArgs.clone();
    }
    public File[] getIncludePath() {
    	return includePath.clone();
    }
    public Compiler getCompiler() {
    	return compiler;
    }
    public String getCommand() {
    	return compiler.getCommand();
    }
}
