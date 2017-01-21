package com.dounine.japi.core;

import java.util.List;

/**
 * Created by huanghuanlai on 2017/1/18.
 */
public interface IMethod {

    List<IActionMethodDoc> getDocs();

    List<String> getAnnotations();

    String getReturnType();

    List<String> getParameters();

}