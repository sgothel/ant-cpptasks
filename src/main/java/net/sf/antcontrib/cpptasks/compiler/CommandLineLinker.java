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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.CUtil;
import net.sf.antcontrib.cpptasks.LinkerDef;
import net.sf.antcontrib.cpptasks.ProcessorDef;
import net.sf.antcontrib.cpptasks.ProcessorParam;
import net.sf.antcontrib.cpptasks.types.CommandLineArgument;
import net.sf.antcontrib.cpptasks.types.LibrarySet;
import net.sf.antcontrib.cpptasks.TargetDef;
import net.sf.antcontrib.cpptasks.VersionInfo;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Environment;


/**
 * An abstract Linker implementation that performs the link via an external
 * command.
 *
 * @author Adam Murdoch
 */
public abstract class CommandLineLinker extends AbstractLinker
{
    private String command;
    private final Environment env = null;
    private String identifier;
    private final String identifierArg;
    private final boolean isLibtool, isXcoderun;
    private String[] librarySets;
    private final CommandLineLinker libtoolLinker;
    private final boolean newEnvironment = false;
    private final String outputSuffix;
    private final boolean isGCC;
    private final boolean isCLANG;


    /** Creates a comand line linker invocation
     * @param isXCoderun TODO*/
    public CommandLineLinker(final String command,
        final String identifierArg,
        final String[] extensions,
        final String[] ignoredExtensions, final String outputSuffix,
        final boolean isXCoderun, final boolean isLibtool, final CommandLineLinker libtoolLinker)
    {
        super(extensions, ignoredExtensions);
        this.command = command;
        this.identifierArg = identifierArg;
        this.outputSuffix = outputSuffix;
        this.isLibtool = isLibtool;
        this.isXcoderun = isXCoderun;
        this.libtoolLinker = libtoolLinker;
        isGCC = "gcc".equals(command);
        isCLANG = "clang".equals(command);
    }
    public CommandLineLinker(final CommandLineLinker ld, final boolean isXCoderun) {
        super(ld);
        this.command = ld.command;
        this.identifierArg = ld.identifierArg;
        this.outputSuffix = ld.outputSuffix;
        this.isLibtool = ld.isLibtool;
        this.isXcoderun = isXCoderun;
        this.libtoolLinker = ld.libtoolLinker;
        isGCC = "gcc".equals(command);
        isCLANG = "clang".equals(command);
    }
    protected abstract void addBase(long base, Vector args);

    protected abstract void addFixed(Boolean fixed, Vector args);

    abstract protected void addImpliedArgs(boolean debug,
      LinkType linkType, Vector args);
    protected abstract void addIncremental(boolean incremental, Vector args);

      //
      //  Windows processors handle these through file list
      //
    protected String[] addLibrarySets(final CCTask task, final LibrarySet[] libsets, final Vector preargs,
        final Vector midargs, final Vector endargs) {
        return null;
    }
    protected abstract void addMap(boolean map, Vector args);
    protected abstract void addStack(int stack, Vector args);
    protected abstract void addEntry(String entry, Vector args);

    @Override
    protected LinkerConfiguration createConfiguration(
      final CCTask task,
      final LinkType linkType,
      final ProcessorDef[] baseDefs, final LinkerDef specificDef, final TargetDef targetPlatform,
	  final VersionInfo versionInfo) {

      final Vector preargs = new Vector();
      final Vector midargs = new Vector();
      final Vector endargs = new Vector();
      final Vector[] args = new Vector[] { preargs, midargs, endargs };

      final LinkerDef[] defaultProviders = new LinkerDef[baseDefs.length+1];
      defaultProviders[0] = specificDef;
      for(int i = 0; i < baseDefs.length; i++) {
        defaultProviders[i+1] = (LinkerDef) baseDefs[i];
      }
      //
      //   add command line arguments inherited from <cc> element
      //     any "extends" and finally the specific CompilerDef
      CommandLineArgument[] commandArgs;
      for(int i = defaultProviders.length-1; i >= 0; i--) {
        commandArgs = defaultProviders[i].getActiveProcessorArgs();
        for(int j = 0; j < commandArgs.length; j++) {
          args[commandArgs[j].getLocation()].
                addElement(commandArgs[j].getValue());
        }
      }

        final Vector params = new Vector();
        //
        //   add command line arguments inherited from <cc> element
        //     any "extends" and finally the specific CompilerDef
        ProcessorParam[] paramArray;
        for (int i = defaultProviders.length - 1; i >= 0; i--) {
            paramArray = defaultProviders[i].getActiveProcessorParams();
            for (int j = 0; j < paramArray.length; j++) {
                params.add(paramArray[j]);
            }
        }

        paramArray = (ProcessorParam[])(params.toArray(new ProcessorParam[params.size()]));

        final boolean debug = specificDef.getDebug(baseDefs,0);


      final String startupObject = getStartupObject(linkType);

      addImpliedArgs(debug, linkType, preargs);
      addIncremental(specificDef.getIncremental(defaultProviders,1), preargs);
      addFixed(specificDef.getFixed(defaultProviders,1), preargs);
      addMap(specificDef.getMap(defaultProviders,1), preargs);
      addBase(specificDef.getBase(defaultProviders,1), preargs);
      addStack(specificDef.getStack(defaultProviders,1), preargs);
      addEntry(specificDef.getEntry(defaultProviders, 1), preargs);

      String[] libnames = null;
      final LibrarySet[] libsets = specificDef.getActiveLibrarySets(defaultProviders,1);
      if (libsets.length > 0) {
        libnames = addLibrarySets(task, libsets, preargs, midargs, endargs);
      }

      final StringBuffer buf = new StringBuffer(getIdentifier());
      for (int i = 0; i < 3; i++) {
        final Enumeration argenum = args[i].elements();
        while (argenum.hasMoreElements()) {
           buf.append(' ');
           buf.append(argenum.nextElement().toString());
        }
      }
      final String configId = buf.toString();

      final String[][] options = new String[][] {
        new String[args[0].size() + args[1].size()],
        new String[args[2].size()] };
      args[0].copyInto(options[0]);
      final int offset = args[0].size();
      for (int i = 0; i < args[1].size(); i++) {
        options[0][i+offset] = (String) args[1].elementAt(i);
      }
      args[2].copyInto(options[1]);


      final boolean rebuild = specificDef.getRebuild(baseDefs,0);
      final boolean map = specificDef.getMap(defaultProviders,1);

      //task.log("libnames:"+libnames.length, Project.MSG_VERBOSE);
      return new CommandLineLinkerConfiguration(this,configId,options,
              paramArray,
              rebuild,map, debug,libnames, startupObject);
    }

    /**
     * Allows drived linker to decorate linker option.
     * Override by GccLinker to prepend a "-Wl," to
     * pass option to through gcc to linker.
     *
     * @param buf buffer that may be used and abused in the decoration process,
     * must not be null.
     * @param arg linker argument
     */
    protected String decorateLinkerOption(final StringBuffer buf, final String arg) {
      return arg;
    }

    protected final String getCommand() {
      return command;
    }
    protected abstract String getCommandFileSwitch(String commandFile);


     @Override
    public String getIdentifier() {
      if(identifier == null) {
        if (identifierArg == null) {
          identifier = getIdentifier(new String[] { command }, command);
        } else {
          identifier = getIdentifier(new String[] { command, identifierArg },
            command);
        }
      }
      return identifier;
    }
    public final CommandLineLinker getLibtoolLinker() {
      if (libtoolLinker != null) {
        return libtoolLinker;
      }
      return this;
    }
    protected abstract int getMaximumCommandLength();

    public String[] getOutputFileNames(final String baseName, final VersionInfo versionInfo) {
        return new String[] { baseName + outputSuffix };
    }

    protected String[] getOutputFileSwitch(final CCTask task, final String outputFile) {
        return getOutputFileSwitch(outputFile);
    }
    protected abstract String[] getOutputFileSwitch(String outputFile);
    protected String getStartupObject(final LinkType linkType) {
      return null;
    }

    protected final boolean getLibtool() {
        return isLibtool;
    }
    protected final boolean isXcodeRun() {
        return isXcoderun;
    }
    protected final boolean isGCC() {
        return isGCC;
    }
    protected final boolean isCLANG() {
        return isCLANG;
    }
    /**
     * Performs a link using a command line linker
     *
     */
    public void link(final CCTask task,
                     final File outputFile,
                     final String[] sourceFiles,
                     final CommandLineLinkerConfiguration config)
                     throws BuildException
    {
        final File parentDir = new File(outputFile.getParent());
        String parentPath;
        try {
          parentPath = parentDir.getCanonicalPath();
        } catch(final IOException ex) {
          parentPath = parentDir.getAbsolutePath();
        }
        String[] execArgs = prepareArguments(task, parentPath,outputFile.getName(),
            sourceFiles, config);
        int commandLength = 0;
        for(int i = 0; i < execArgs.length; i++) {
          commandLength += execArgs[i].length() + 1;
        }

        //
        //   if command length exceeds maximum
        //       then create a temporary
        //       file containing everything but the command name
        if(commandLength >= this.getMaximumCommandLength()) {
          try {
            execArgs = prepareResponseFile(outputFile,execArgs);
          }
          catch(final IOException ex) {
            throw new BuildException(ex);
          }
        }

        final int retval = runCommand(task,parentDir,execArgs);
        //
        //   if the process returned a failure code then
        //       throw an BuildException
        //
        if(retval != 0) {
          //
          //   construct the exception
          //
          throw new BuildException(this.getCommand() + " failed with return code " + retval, task.getLocation());
        }

    }


    /**
     * Prepares argument list for exec command.  Will return null
     * if command line would exceed allowable command line buffer.
     *
     * @param task compilation task.
     * @param outputFile linker output file
     * @param sourceFiles linker input files (.obj, .o, .res)
     * @param config linker configuration
     * @return arguments for runTask
     */
    protected String[] prepareArguments(
        final CCTask task,
        final String outputDir,
        final String outputFile,
        final String[] sourceFiles,
        final CommandLineLinkerConfiguration config) {

        final String[] preargs = config.getPreArguments();
        final String[] endargs = config.getEndArguments();
        final String outputSwitch[] =  getOutputFileSwitch(task, outputFile);
        int allArgsCount = preargs.length + 1 + outputSwitch.length +
                sourceFiles.length + endargs.length;
        if (isLibtool) {
          allArgsCount++;
        }
        if(isXcoderun) {
          allArgsCount++;
        }
        final String[] allArgs = new String[allArgsCount];
        int index = 0;
        if (isLibtool) {
          allArgs[index++] = "libtool";
        }
        if(isXcoderun) {
          allArgs[index++] = "xcrun";
        }
        allArgs[index++] = this.getCommand();
        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < preargs.length; i++) {
          allArgs[index++] = decorateLinkerOption(buf, preargs[i]);
        }
        for (int i = 0; i < outputSwitch.length; i++) {
          allArgs[index++] = outputSwitch[i];
        }
        for (int i = 0; i < sourceFiles.length; i++) {
          allArgs[index++] = prepareFilename(buf,outputDir,sourceFiles[i]);
        }
        for (int i = 0; i < endargs.length; i++) {
          allArgs[index++] = decorateLinkerOption(buf, endargs[i]);
        }
        return allArgs;
    }

    /**
     * Processes filename into argument form
     *
     */
    protected String prepareFilename(final StringBuffer buf,
      final String outputDir, final String sourceFile) {
      final String relativePath = CUtil.getRelativePath(outputDir,
        new File(sourceFile));
      return quoteFilename(buf,relativePath);
    }

    /**
     * Prepares argument list to execute the linker using a
     * response file.
     *
     * @param outputFile linker output file
     * @param args output of prepareArguments
     * @return arguments for runTask
     */
    protected String[] prepareResponseFile(final File outputFile,final String[] args) throws IOException
    {
        final String baseName = outputFile.getName();
        final File commandFile = new File(outputFile.getParent(),baseName + ".rsp");
        final FileWriter writer = new FileWriter(commandFile);
        int execArgCount = 1;
        if (isLibtool) {
          execArgCount++;
        }
        if(isXcoderun) {
          execArgCount++;
        }
        final String[] execArgs = new String[execArgCount+1];
        for (int i = 0; i < execArgCount; i++) {
          execArgs[i] = args[i];
        }
        execArgs[execArgCount] = getCommandFileSwitch(commandFile.toString());
        for(int i = execArgCount; i < args.length; i++) {
        	//
        	//   if embedded space and not quoted then
        	//       quote argument
          if (args[i].indexOf(" ") >= 0 && args[i].charAt(0) != '\"') {
          	writer.write('\"');
          	writer.write(args[i]);
          	writer.write("\"\n");
          } else {
          	writer.write(args[i]);
            writer.write('\n');
          }
        }
        writer.close();
        return execArgs;
    }


    protected String quoteFilename(final StringBuffer buf,final String filename) {
      if(filename.indexOf(' ') >= 0) {
        buf.setLength(0);
        buf.append('\"');
        buf.append(filename);
        buf.append('\"');
        return buf.toString();
      }
      return filename;
    }

    /**
     * This method is exposed so test classes can overload
     * and test the arguments without actually spawning the
     * compiler
     */
    protected int runCommand(final CCTask task, final File workingDir,final String[] cmdline)
      throws BuildException {
      return CUtil.runCommand(task,workingDir,cmdline, newEnvironment, env);
    }

    protected final void setCommand(final String command) {
        this.command = command;
    }

}
