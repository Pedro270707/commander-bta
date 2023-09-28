package net.pedroricardo.commander;

import com.b100.utils.FileUtils;
import com.b100.utils.interfaces.Condition;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.minecraft.core.util.helper.ReflectionHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CommanderReflectionHelper {
    public static List<Class<?>> getAllClasses(Condition<String> classNameCondition) {
        String[] classPathEntries;
        ArrayList classes = new ArrayList();
        for (String classPathEntry : classPathEntries = System.getProperty("java.class.path").split(";")) {
            File file = new File(classPathEntry);
            if (file.isDirectory()) {
                int l = file.getAbsolutePath().length() + 1;
                List<File> files = FileUtils.getAllFiles(file);
                for (File file2 : files) {
                    if (!file2.getName().endsWith(".class")) continue;
                    tryAddClass(classes, file2.getAbsolutePath().substring(l), classNameCondition);
                }
            }
            if (!file.isFile()) continue;
            try {
                ZipFile zip = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    String entry = ((Object)entries.nextElement()).toString();
                    if (!entry.endsWith(".class")) continue;
                    tryAddClass(classes, entry, classNameCondition);
                }
                zip.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }

    private static void tryAddClass(List<Class<?>> classes, String fileName, Condition<String> classNameCondition) {
        String className = fileName.substring(0, fileName.length() - 6);
        className = className.replace('\\', '.');
        className = className.replace('/', '.');
        if (classNameCondition == null || classNameCondition.isTrue(className)) {
            Class<?> clazz = null;
            try {
                clazz = Knot.getClass(className);
            } catch (Throwable e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            if (clazz != null) {
                classes.add(clazz);
            }
        }
    }
}
