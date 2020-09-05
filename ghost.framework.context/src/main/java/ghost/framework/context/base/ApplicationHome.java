package ghost.framework.context.base;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @Author: 郭树灿{guo-w541}
 * @link: 手机:13715848993, QQ 27048384
 * @Description:
 * @Date: 0:29 2019/5/24
 */
public class ApplicationHome {
    private final File source;
    private final File dir;
    public ApplicationHome(Class<?> sourceClass) {
        this.source = this.findSource(sourceClass != null ? sourceClass : this.getStartClass());
        this.dir = this.findHomeDir(this.source);
    }

    private Class<?> getStartClass() {
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            return this.getStartClass(classLoader.getResources("META-INF/MANIFEST.MF"));
        } catch (Exception e) {
            return null;
        }
    }

    private Class<?> getStartClass(Enumeration<URL> manifestResources) {
        while (manifestResources.hasMoreElements()) {
            try {
                InputStream inputStream = (manifestResources.nextElement()).openStream();
                Class<?> c = null;
                try {
                    Manifest manifest = new Manifest(inputStream);
                    String startClass = manifest.getMainAttributes().getValue("Start-Class");
                    if (startClass == null) {
                        continue;
                    }
                    c = Class.forName(startClass, false, this.getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
                return c;
            } catch (Exception e) {
            }
        }
        return null;
    }

    private File findSource(Class<?> sourceClass) {
        try {
            ProtectionDomain domain = sourceClass != null ? sourceClass.getProtectionDomain() : null;
            CodeSource codeSource = domain != null ? domain.getCodeSource() : null;
            URL location = codeSource != null ? codeSource.getLocation() : null;
            File source = location != null ? this.findSource(location) : null;
            return source != null && source.exists() && !this.isUnitTest() ? source.getAbsoluteFile() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isUnitTest() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            for(int i = stackTrace.length - 1; i >= 0; --i) {
                if (stackTrace[i].getClassName().startsWith("org.junit.")) {
                    return true;
                }
            }
        } catch (Exception e) {
        }

        return false;
    }

    private File findSource(URL location) throws IOException {
        URLConnection connection = location.openConnection();
        return connection instanceof JarURLConnection ? this.getRootJarFile(((JarURLConnection)connection).getJarFile()) : new File(location.getPath());
    }

    private File getRootJarFile(JarFile jarFile) {
        String name = jarFile.getName();
        int separator = name.indexOf("!/");
        if (separator > 0) {
            name = name.substring(0, separator);
        }

        return new File(name);
    }

    private File findHomeDir(File source) {
        File homeDir = source != null ? source : this.findDefaultHomeDir();
        if (homeDir.isFile()) {
            homeDir = homeDir.getParentFile();
        }

        homeDir = homeDir.exists() ? homeDir : new File(".");
        return homeDir.getAbsoluteFile();
    }

    private File findDefaultHomeDir() {
        String userDir = System.getProperty("user.dir");
        return new File(StringUtils.isEmpty(userDir) ? userDir : ".");
    }

    public File getSource() {
        return this.source;
    }

    public File getDir() {
        return this.dir;
    }

    public String toString() {
        return this.getDir().toString();
    }
}
