package net.sf.antcontrib.cpptasks.gcc;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import net.sf.antcontrib.cpptasks.CUtil;
import net.sf.antcontrib.cpptasks.compiler.LinkType;
import net.sf.antcontrib.cpptasks.compiler.Linker;

public abstract class GnuLinker extends AbstractLdLinker {

    static String[] darwinLinkerOptions = new String[]{"-arch", "-weak_framework", "-lazy_framework", "-weak_library" };

    protected final boolean isGCC;
    protected File[] libDirs;

    public GnuLinker(String command, String identifierArg, String[] extensions,
            String[] ignoredExtensions, String outputPrefix,
            String outputSuffix, boolean isXCoderun, boolean isLibtool,
            AbstractLdLinker libtoolLinker) {
        super(command, identifierArg, extensions, ignoredExtensions,
                outputPrefix, outputSuffix, isXCoderun, isLibtool,
                libtoolLinker);
        isGCC = "gcc".equals(command);
    }

    public GnuLinker(AbstractLdLinker ld, boolean isXCoderun) {
        super(ld, isXCoderun);
        isGCC = "gcc".equals(getCommand());
    }

    protected abstract String[] getStaticLinkerOptions();
    protected abstract GnuLinker getStaticDllLinker();
    protected abstract GnuLinker getStaticDllClangLinker();
    protected abstract GnuLinker getStaticArLinker();
    protected abstract GnuLinker getStaticArClangLinker();
    protected abstract GnuLinker getStaticClangInstance();
    protected abstract GnuLinker getStaticXcodeClangInstance();
    protected abstract GnuLinker getStaticMachBundleLinker();
    protected abstract GnuLinker getStaticMachClangBundleLinker();
    protected abstract GnuLinker getStaticXcodeMachClangBundleLinker();
    protected abstract GnuLinker getStaticMachDllLinker();
    protected abstract GnuLinker getStaticMachDllClangLinker();
    protected abstract GnuLinker getStaticXcodeMachDllClangLinker();
    protected abstract GnuLinker getStaticMachArLinker();
    protected abstract GnuLinker getStaticMachArClangLinker();
    protected abstract GnuLinker getStaticXcodeMachArClangLinker();
    protected abstract GnuLinker getStaticInstance();

    @Override
    protected void addImpliedArgs(boolean debug, LinkType linkType, Vector args) {
        super.addImpliedArgs(debug, linkType, args);
        if (getIdentifier().indexOf("mingw") >= 0) {
            if (linkType.isSubsystemConsole()) {
                args.addElement("-mconsole");
            }
            if (linkType.isSubsystemGUI()) {
                args.addElement("-mwindows");
            }
        }
    }

    /**
     * Allows drived linker to decorate linker option. Override by GccLinker to
     * prepend a "-Wl," to pass option to through gcc to linker.
     *
     * @param buf
     *            buffer that may be used and abused in the decoration process,
     *            must not be null.
     * @param arg
     *            linker argument
     */
    @Override
    public String decorateLinkerOption(StringBuffer buf, String arg) {
        if (arg.startsWith("--sysroot")) {
          return arg;
        }
        if (arg.startsWith("-nostdlib")) {
          return arg;
        }
        String decoratedArg = arg;
        if (arg.length() > 1 && arg.charAt(0) == '-') {
            switch (arg.charAt(1)) {
                //
                //   passed automatically by GCC
                //
                case 'g' :
                case 'f' :
                case 'F' :
                /* Darwin */
                case 'm' :
                case 'O' :
                case 'W' :
                case 'l' :
                case 'L' :
                case 'u' :
                case 'v' :
                    break;
                default :
                    boolean known = false;
                    HashSet<String> allLinkerOptions = new HashSet<String>();
                    allLinkerOptions.addAll(Arrays.asList(getStaticLinkerOptions()));
                    if (isDarwin()) {
                        allLinkerOptions.addAll(Arrays.asList(darwinLinkerOptions));
                    }
                    known = allLinkerOptions.contains(arg);

                    if (!known) {
                        buf.setLength(0);
                        buf.append("-Wl,");
                        buf.append(arg);
                        decoratedArg = buf.toString();
                    }
                    break;
            }
        }
        return decoratedArg;
    }

    @Override
    public Linker getLinker(final LinkType type) {
        if ( type.isStaticLibrary() && !type.getUseHighlevelTool() ) {
            return GccLibrarian.getInstance(); // uses 'ar', which is 'gcc' agnostic
        }
        if (type.isStaticLibrary()) {
            if (isDarwin()) {
                return isGCC ? getStaticMachArLinker() : ( getXcodeRun() ? getStaticXcodeMachArClangLinker() : getStaticMachArClangLinker() );
            } else {
                return isGCC ? getStaticArLinker() : getStaticArClangLinker();
            }
        }
        if (type.isPluginModule()) {
            if (isDarwin()) {
                return isGCC ? getStaticMachBundleLinker() : ( getXcodeRun() ? getStaticXcodeMachClangBundleLinker() : getStaticMachClangBundleLinker() );
            } else {
                return isGCC ? getStaticDllLinker() : getStaticDllClangLinker();
            }
        }
        if (type.isSharedLibrary()) {
            if (isDarwin()) {
                return isGCC ? getStaticMachDllLinker() : ( getXcodeRun() ? getStaticXcodeMachDllClangLinker() : getStaticMachDllClangLinker() );
            } else {
                return isGCC ? getStaticDllLinker() : getStaticDllClangLinker();
            }
        }
        return isGCC ? getStaticInstance() : ( getXcodeRun() ? getStaticXcodeClangInstance() : getStaticClangInstance() ) ;
    }

}