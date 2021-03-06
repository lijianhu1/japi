package com.dounine.japi.core.valid;

import com.dounine.japi.common.JapiPattern;
import com.dounine.japi.core.IParameter;
import com.dounine.japi.core.impl.ParameterImpl;
import com.dounine.japi.exception.JapiException;
import com.dounine.japi.serial.request.IRequest;
import com.dounine.japi.serial.request.RequestImpl;
import com.dounine.japi.core.valid.jsr303.ValidValid;
import com.dounine.japi.core.valid.jsr303.ValidatedValid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by lake on 17-2-10.
 */
public class JSR303Valid implements IValid {

    private String javaFilePath;
    private static final Logger LOGGER = LoggerFactory.getLogger(JSR303Valid.class);

    @Override
    public List<IMVC> getTypes() {
        List<IMVC> imvcs = new ArrayList<>();
        imvcs.add(new ValidatedValid(javaFilePath));
        imvcs.add(new ValidValid(javaFilePath));
        return imvcs;
    }

    @Override
    public IParameter getParameter(String parameterStr,List<String> docsStrs) {
        if(parameterStr.contains("[")||parameterStr.contains("<")){
            throw new JapiException("暂时不支持数组或列表对象数据解析.");
        }
        Matcher typeAndNameMatcher = JapiPattern.TYPE_NAME_PATTERN.matcher(parameterStr);
        typeAndNameMatcher.find();
        String typeAndName = typeAndNameMatcher.group();
        String typeStr = typeAndName.substring(0, typeAndName.indexOf(" "));
        String nameStr = typeAndName.substring(typeStr.length() + 1).trim();
        ParameterImpl parameter = new ParameterImpl();
        parameter.setRequestFields(getRequestFields(StringUtils.substring(parameterStr, 0, -typeAndName.length()),typeStr,nameStr,docsStrs));
        return parameter;
    }

    private List<IRequest> getRequestFields(String parameterStrExcTypeAndName, String typeStr, String nameStr, List<String> docsStrs) {
        Matcher singleAnnoMatcher = JapiPattern.getPattern("@[a-zA-Z0-9_]*").matcher(parameterStrExcTypeAndName);
        List<String> annos = new ArrayList<>();
        int preIndex = -1, nextIndex = -1;
        while (singleAnnoMatcher.find()) {
            nextIndex = singleAnnoMatcher.start();
            if (-1 != preIndex) {
                annos.add(parameterStrExcTypeAndName.substring(preIndex, nextIndex).trim());
                preIndex = nextIndex;
            } else {
                preIndex = 0;
            }
        }
        if (nextIndex != -1) {
            annos.add(parameterStrExcTypeAndName.substring(nextIndex).trim());
        }
        List<IRequest> requestFields = new ArrayList<>();
        for (String annoStr : annos) {
            if(isValid(annoStr)){//全部使用默认值
                IMVC imvc = getValid(annoStr.substring(1));
                if(null!=imvc){
                    IRequest requestField = imvc.getRequestField(parameterStrExcTypeAndName,typeStr,nameStr,docsStrs,new File(javaFilePath));
                    if(null!=requestField){
                        requestFields.add(requestField);
                    }
                }
            }else{
                LOGGER.warn(annoStr+ " 不在MVCValid识别范围内.");
            }
        }
        return requestFields;
    }

    public String getJavaFilePath() {
        return javaFilePath;
    }

    public void setJavaFilePath(String javaFilePath) {
        this.javaFilePath = javaFilePath;
    }

}
