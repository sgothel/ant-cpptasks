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
package net.sf.antcontrib.cpptasks;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import net.sf.antcontrib.cpptasks.compiler.LinkerConfiguration;
import net.sf.antcontrib.cpptasks.compiler.ProcessorConfiguration;

import org.apache.tools.ant.BuildException;
/**
 * This class matches each visited file with an appropriate compiler
 *
 * @author Curt Arnold
 */
public final class TargetMatcher implements FileVisitor {
    private final LinkerConfiguration linker;
    private final Vector objectFiles;
    private final File outputDir;
    private final ProcessorConfiguration[] processors;
    private final File sourceFiles[] = new File[1];
    private final Hashtable targets;
    private final VersionInfo versionInfo;
    private final CCTask task;
    public TargetMatcher(final CCTask task, final File outputDir,
            final ProcessorConfiguration[] processors, final LinkerConfiguration linker,
            final Vector objectFiles, final Hashtable targets,
			final VersionInfo versionInfo) {
        this.task = task;
        this.outputDir = outputDir;
        this.processors = processors;
        this.targets = targets;
        this.linker = linker;
        this.objectFiles = objectFiles;
        this.versionInfo = versionInfo;
    }
    public void visit(final File parentDir, final String filename) throws BuildException {
        final File fullPath = new File(parentDir, filename);
        //
        //   see if any processor wants to bid
        //       on this one
        ProcessorConfiguration selectedCompiler = null;
        int bid = 0;
        if (processors != null) {
            for (int k = 0; k < processors.length; k++) {
                final int newBid = processors[k].bid(fullPath.toString());
                if (newBid > bid) {
                    bid = newBid;
                    selectedCompiler = processors[k];
                }
            }
        }
        //
        //   no processor interested in file
        //      log diagnostic message
        if (bid <= 0) {
            if (linker != null) {
                final int linkerbid = linker.bid(filename);
                if (linkerbid > 0) {
                    objectFiles.addElement(fullPath);
                    if (linkerbid == 1) {
                        task.log("Unrecognized file type " + fullPath.toString()
                                + " will be passed to linker");
                    }
                }
            }
        } else {
            //
            //  get output file name
            //
            final String[] outputFileNames = selectedCompiler
                    .getOutputFileNames(filename, versionInfo);
            sourceFiles[0] = fullPath;
            //
            //   if there is some output for this task
            //      (that is a source file and not an header file)
            //
            for (int i = 0; i < outputFileNames.length; i++) {
                //
                //   see if the same output file has already been registered
                //
                final TargetInfo previousTarget = (TargetInfo) targets
                        .get(outputFileNames[i]);
                if (previousTarget == null) {
                    targets.put( outputFileNames[i],
                            new TargetInfo (
                                selectedCompiler, sourceFiles, null, new File(
                                outputDir, outputFileNames[i]), outputFileNames[i],
                                selectedCompiler.getRebuild() )
                               );
                } else {
                    if (!previousTarget.getSources()[0].equals(sourceFiles[0])) {
                        final StringBuffer builder = new StringBuffer(
                                "Output filename conflict: ");
                        builder.append(outputFileNames[i]);
                        builder.append(" would be produced from ");
                        builder.append(previousTarget.getSources()[0]
                                .toString());
                        builder.append(" and ");
                        builder.append(filename);
                        throw new BuildException(builder.toString());
                    }
                }
            }
        }
    }
}
