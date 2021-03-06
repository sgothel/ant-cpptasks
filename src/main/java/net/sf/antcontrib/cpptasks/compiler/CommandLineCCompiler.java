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

import net.sf.antcontrib.cpptasks.parser.CParser;
import net.sf.antcontrib.cpptasks.parser.Parser;

import org.apache.tools.ant.types.Environment;
/**
 * An abstract Compiler implementation which uses an external program to
 * perform the compile.
 *
 * @author Adam Murdoch
 */
public abstract class CommandLineCCompiler extends CommandLineCompiler {
    protected CommandLineCCompiler(String command, String identifierArg,
            String[] sourceExtensions, String[] headerExtensions,
            String outputSuffix, boolean isXcoderun,
            boolean libtool, CommandLineCCompiler libtoolCompiler,
            boolean newEnvironment, Environment env) {
        super(command, identifierArg, sourceExtensions, headerExtensions,
                outputSuffix, isXcoderun, libtool, libtoolCompiler, newEnvironment, env);
    }
    protected CommandLineCCompiler(CommandLineCCompiler cc, boolean isXcoderun) {
        super(cc, isXcoderun);
    }
    protected Parser createParser(File source) {
        return new CParser();
    }
}
