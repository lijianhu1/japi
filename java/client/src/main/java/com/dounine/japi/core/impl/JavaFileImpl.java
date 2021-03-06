package com.dounine.japi.core.impl;

import com.alibaba.fastjson.JSON;
import com.dounine.japi.JapiClient;
import com.dounine.japi.common.JapiPattern;
import com.dounine.japi.core.IJavaFile;
import com.dounine.japi.exception.JapiException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by huanghuanlai on 2017/1/19.
 */
public class JavaFileImpl implements IJavaFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaFileImpl.class);

    private static final String CHECK_FILE_SUFFIX = ".java";
    private static final String PACKAGE_PREFIX = "import ";
    private static final String RELATIVE_PATH = "src/main/java/";

    private JavaFileImpl() {
    }

    private static final JavaFileImpl JAVA_FILE = new JavaFileImpl();

    public static final JavaFileImpl getInstance() {
        return JAVA_FILE;
    }

    @Override
    public File searchTxtJavaFileForProjectsPath(String javaTxt, String javaFilePath) {
        if (StringUtils.isBlank(javaTxt)) {
            throw new JapiException("javaTxt 关键字不能为空");
        }
        if (StringUtils.isBlank(javaFilePath)) {
            throw new JapiException("javaFilePath 主文件路径不能为空");
        }
//        if (null == JapiClient.getConfig().getIncludeProjectJavaPath() || (null != JapiClient.getConfig().getIncludeProjectJavaPath() && JapiClient.getConfig().getIncludeProjectJavaPath().size() == 0)) {
//            throw new JapiException("includePaths 至少包含一个主项目地扯");
//        }
        File javaFile = null;
        if (javaTxt.contains(".")) {//查找关键字包含点的
            File javaTxtFile = new File(getEndSplitPath(JapiClient.getConfig().getPrefixPath() + JapiClient.getConfig().getProjectJavaPath() + JapiClient.getConfig().getPostfixPath()) + javaTxt.replace(".", "/") + CHECK_FILE_SUFFIX);
            List<File> findChildFiles = new ArrayList<>();
            if (!javaTxtFile.exists()) {//主项目不存在,查找其它项目
                for (String childProjectPath : JapiClient.getConfig().getIncludeProjectJavaPath()) {
                    javaTxtFile = new File(getEndSplitPath(JapiClient.getConfig().getPrefixPath() + childProjectPath + JapiClient.getConfig().getPostfixPath()) + javaTxt.replace(".", "/") + CHECK_FILE_SUFFIX);
                    if (javaTxtFile.exists()) {
                        findChildFiles.add(javaTxtFile);
                    }
                }
            } else {
                findChildFiles.add(javaTxtFile);
            }
            if (findChildFiles.size() > 1) {
                findChildFiles = findChildFiles.stream().sorted((a, b) -> ((Integer) a.getAbsolutePath().length()).compareTo(b.getAbsolutePath().length())).collect(Collectors.toList());//优先取包层次少的文件
                javaFile = findChildFiles.get(0);
                LOGGER.warn("找到多个文件" + JSON.toJSONString(findChildFiles));
            } else if (findChildFiles.size() == 1) {
                javaFile = findChildFiles.get(0);
            }
        } else {//根据导入包的信息查找类的所在地
            List<String> javaFileLines = null;
            try {
                javaFileLines = FileUtils.readLines(new File(javaFilePath), Charset.forName("utf-8"));
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
            List<String> javaFileImportPackageLines = filterJavaFileImportPackageLines(javaFileLines).stream().filter(line -> line.startsWith("import ")).collect(Collectors.toList());

            Optional<String> keyEndOptional = javaFileImportPackageLines.stream().filter(line -> line.endsWith("." + javaTxt + ";")).findFirst();
            if (keyEndOptional.isPresent()) {//找到有.关键字结尾的包
                String packageStr = StringUtils.substring(keyEndOptional.get(), PACKAGE_PREFIX.length(), -1).replace(".", "/").trim();
                File javaTxtFile = new File(getEndSplitPath(JapiClient.getConfig().getPrefixPath() + JapiClient.getConfig().getProjectJavaPath() + JapiClient.getConfig().getPostfixPath()) + javaTxt.replace(".", "/") + CHECK_FILE_SUFFIX);
                if (!javaTxtFile.exists()) {//主项目不存在,查找其它项目
                    for (String childProjectPath : JapiClient.getConfig().getIncludeProjectJavaPath()) {
                        javaTxtFile = new File(getEndSplitPath(JapiClient.getConfig().getPrefixPath() + childProjectPath + JapiClient.getConfig().getPostfixPath()) + packageStr.replace(".", "/") + CHECK_FILE_SUFFIX);
                        if (javaTxtFile.exists()) {
                            javaFile = javaTxtFile;
                            break;
                        }
                    }
                } else {
                    javaFile = javaTxtFile;
                }
            } else {
                List<String> javaFileImportPackageHasAllLines = javaFileImportPackageLines.stream().filter(line -> line.endsWith(".*;")).collect(Collectors.toList());
                List<String> findHasPackageLines = new ArrayList<>();
                if (null != JapiClient.getConfig().getIncludePackages() && JapiClient.getConfig().getIncludePackages().length > 0) {//有使用确定包，可加快搜索速度
                    for (String includePackage : JapiClient.getConfig().getIncludePackages()) {
                        for (String allKey : javaFileImportPackageHasAllLines) {
                            if (allKey.contains(includePackage)) {
                                findHasPackageLines.add(StringUtils.substring(allKey.substring("import ".length()).trim(), 0, -3));
                            }
                        }
                    }
                } else {
                    for (String allKey : javaFileImportPackageHasAllLines) {
                        findHasPackageLines.add(StringUtils.substring(allKey.substring("import ".length()).trim(), 0, -3));
                    }
                }

                final IOFileFilter javaFileFilter = FileFilterUtils.asFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && pathname.getName().equals(javaTxt + CHECK_FILE_SUFFIX);
                    }
                });
                for (String packageStr : findHasPackageLines) {
                    String packagePath = getEndSplitPath(JapiClient.getConfig().getPrefixPath() + JapiClient.getConfig().getProjectJavaPath() + JapiClient.getConfig().getPostfixPath()) + packageStr.replace(".", "/");
                    File packageFile = new File(packagePath);
                    if (packageFile.exists()) {
                        Collection<File> files = FileUtils.listFiles(packageFile, javaFileFilter, TrueFileFilter.INSTANCE);
                        if (files.size() > 1) {
                            javaFile = files.stream().sorted((a, b) -> ((Integer) a.getAbsolutePath().length()).compareTo(b.getAbsolutePath().length())).collect(Collectors.toList()).get(0);
                            break;
                        } else if (files.size() == 1) {
                            javaFile = files.iterator().next();
                            break;
                        }
                    }

                    if (null == javaFile) {
                        for (String childProjectPath : JapiClient.getConfig().getIncludeProjectJavaPath()) {
                            packageFile = new File(getEndSplitPath(JapiClient.getConfig().getPrefixPath() + childProjectPath + JapiClient.getConfig().getPostfixPath()) + packageStr.replace(".", "/"));
                            Collection<File> files = FileUtils.listFiles(packageFile, javaFileFilter, TrueFileFilter.INSTANCE);
                            if (files.size() > 1) {
                                javaFile = files.stream().sorted((a, b) -> ((Integer) a.getAbsolutePath().length()).compareTo(b.getAbsolutePath().length())).collect(Collectors.toList()).get(0);
                                break;
                            } else if (files.size() == 1) {
                                javaFile = files.iterator().next();
                            }
                        }
                        if (null != javaFile) {
                            break;
                        }
                    }
                }
                if (null == javaFile) {
                    File myReletivePath = new File(new File(javaFilePath).getParentFile().getAbsoluteFile() + "/" + javaTxt + ".java");
                    if (myReletivePath.exists()) {
                        javaFile = myReletivePath;
                    }
                }
            }
        }

        return javaFile;
    }


    /**
     * 过滤java文件的导入包信息
     *
     * @param javaFileFullLines
     * @return 导入包信息
     */
    private List<String> filterJavaFileImportPackageLines(final List<String> javaFileFullLines) {
        if (null == javaFileFullLines && javaFileFullLines.size() == 0) {
            throw new JapiException("javaFileFullLines 不能为空");
        }
        final List<String> javaFileImportPackageLines = new ArrayList<>();
        String[] matchCharts = {"/**", "public ", "class ", "interface ", "@interface ", "enum ", "abstract ", "@interface "};
        for (String lineString : javaFileFullLines) {
            boolean match = false;//true 找到包结束,意味着导入包结束,严格要求,包中间不能包含注释
            for (String chart : matchCharts) {
                if (lineString.startsWith(chart)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                break;
            } else {
                javaFileImportPackageLines.add(lineString);
            }
        }
        return javaFileImportPackageLines;
    }

    /**
     * 过滤包信息
     *
     * @param packageLineInfo
     * @return 具体包，不包含import与分号
     */
    private static String filterPackageInfo(String packageLineInfo) {
        String[] importAndPack = packageLineInfo.split(" ");
        return StringUtils.substring(importAndPack[1], 0, -1);//去掉分号
    }

    private String getEndSplitPath(String path) {
        String splitChar = path.endsWith("/") ? "" : "/";
        return path + splitChar;
    }
}
