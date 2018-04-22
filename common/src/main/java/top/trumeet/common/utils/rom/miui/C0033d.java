package top.trumeet.common.utils.rom.miui;

import android.content.Context;
import android.os.Build.VERSION;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

class C0033d {
    private static Object m46a(ClassLoader classLoader) throws NoSuchFieldException {
        if (classLoader instanceof BaseDexClassLoader) {
            Field[] declaredFields = BaseDexClassLoader.class.getDeclaredFields();
            int length = declaredFields.length;
            int i = 0;
            while (i < length) {
                Field field = declaredFields[i];
                if ("dalvik.system.DexPathList".equals(field.getType().getName())) {
                    field.setAccessible(true);
                    try {
                        return field.get(classLoader);
                    } catch (IllegalArgumentException e) {
                    } catch (IllegalAccessException e2) {
                    }
                } else {
                    i++;
                }
            }
        }
        throw new NoSuchFieldException("dexPathList field not found.");
    }

    private static Field m47a(Object obj) throws NoSuchFieldException {
        for (Field field : obj.getClass().getDeclaredFields()) {
            Class type = field.getType();
            if (type.isArray() && type.getComponentType() == File.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new NoSuchFieldException("nativeLibraryDirectories field not found.");
    }

    private static Field m48a(Object obj, String str) throws NoSuchFieldException {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.getName().equals(str)) {
                Class type = field.getType();
                if (type.isArray() && "dalvik.system.DexPathList$Element".equals(type.getComponentType().getName())) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }
        throw new NoSuchFieldException(str + " field not found.");
    }

    private static void m49a(Object obj, Object obj2) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        C0033d.m51b(obj, obj2, "dexElements");
    }

    private static void m50a(Object obj, Object obj2, String str) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        if (VERSION.SDK_INT >= 23) {
            C0033d.m51b(obj, obj2, "nativeLibraryPathElements");
        } else {
            C0033d.m52b(obj, str);
        }
    }

    private static void m51b(Object obj, Object obj2, String str) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Object[] objArr = (Object[]) C0033d.m48a(obj2, str).get(obj2);
        Field a = C0033d.m48a(obj, str);
        Object[] objArr2 = (Object[]) a.get(obj);
        Object[] objArr3 = (Object[]) Array.newInstance(Class.forName("dalvik.system.DexPathList$Element"), objArr2.length + 1);
        objArr3[0] = objArr[0];
        System.arraycopy(objArr2, 0, objArr3, 1, objArr2.length);
        a.set(obj, objArr3);
    }

    private static void m52b(Object obj, String str) throws NoSuchFieldException, IllegalAccessException {
        Field a = C0033d.m47a(obj);
        File[] fileArr = (File[]) a.get(obj);
        File[] obj2 = new File[(fileArr.length + 1)];
        obj2[0] = new File(str);
        System.arraycopy(fileArr, 0, obj2, 1, fileArr.length);
        a.set(obj, obj2);
    }

    public static boolean load(String str, String str2, String str3, ClassLoader classLoader) {
        return C0033d.load(str, str2, str3, classLoader, null);
    }

    static boolean load(String str, String str2, String str3, ClassLoader classLoader, Context context) {
        if (str == null && (str3 == null || context == null)) {
            return false;
        }
        try {
            String str4;
            Object a = C0033d.m46a(classLoader);
            if (str != null) {
                str4 = str;
            } else if (VERSION.SDK_INT < 23) {
                C0033d.m52b(a, str3);
                return true;
            } else {
                str2 = null;
                str4 = context.getApplicationInfo().sourceDir;
            }
            Object a2 = C0033d.m46a(str2 == null ? new PathClassLoader(str4, str3, classLoader.getParent()) : new DexClassLoader(str4, str2, str3, classLoader.getParent()));
            if (str != null) {
                C0033d.m49a(a, a2);
            }
            if (str3 != null) {
                C0033d.m50a(a, a2, str3);
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        } catch (IllegalAccessException e2) {
            return false;
        } catch (ClassNotFoundException e3) {
            return false;
        } catch (NoSuchFieldException e4) {
            return false;
        }
    }
}
