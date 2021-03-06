package com.dounine.japi.core.valid.jsr303.list;

import com.dounine.japi.common.JapiPattern;
import com.dounine.japi.core.IFieldDoc;
import com.dounine.japi.core.impl.TypeConvert;
import com.dounine.japi.core.valid.IMVC;
import com.dounine.japi.serial.request.RequestImpl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lake on 17-2-17.
 */
public class MinValid implements IMVC {

    private String javaFilePath;

    @Override
    public String getRequestParamName() {
        return "javax.validation.constraints.Min";
    }

    @Override
    public RequestImpl getRequestFieldForAnno(String annoStr, String typeStr, String nameStr, List<IFieldDoc> docs, List<String> interfaceGroups) {
        RequestImpl requestField = new RequestImpl();
        String newNameStr = nameStr;
        String defaultValue = "";
        StringBuffer constraint = new StringBuffer();
        String description = getDescription(docs);
        boolean required = isRequired(annoStr, interfaceGroups, javaFilePath);

        Pattern valuePattern = JapiPattern.getPattern("value\\s*=\\s*\\d+");
        Matcher valueMatcher = valuePattern.matcher(annoStr);

        if(valueMatcher.find()){
            String str = valueMatcher.group().split("=")[1].trim();
            constraint.append("最小值:");
            constraint.append(str);
        }else{
            Pattern defaultPattern = JapiPattern.getPattern("Min[(]\\d*[)]");
            Matcher defaultMatcher = defaultPattern.matcher(annoStr);
            if(defaultMatcher.find()){
                String maxStr = defaultMatcher.group();
                constraint.append("最小值:"+maxStr.substring(maxStr.indexOf("(")+1,maxStr.lastIndexOf(")")));
            }

        }

        requestField.setType(TypeConvert.getHtmlType(typeStr));
        requestField.setDescription(description);
        requestField.setConstraint(constraint.toString());
        requestField.setRequired(required);
        requestField.setDefaultValue(defaultValue);
        requestField.setName(newNameStr);
        return requestField;
    }

    public String getJavaFilePath() {
        return javaFilePath;
    }

    public void setJavaFilePath(String javaFilePath) {
        this.javaFilePath = javaFilePath;
    }

}
