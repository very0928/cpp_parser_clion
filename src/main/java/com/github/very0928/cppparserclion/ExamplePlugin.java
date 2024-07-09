// ExamplePlugin.java
package com.github.very0928.cppparserclion;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
//import com.jetbrains.cidr.lang.psi.OCFunctionDeclaration;
//import com.jetbrains.cidr.lang.psi.OCFunctionCallExpression;
//import com.jetbrains.cidr.lang.psi.OCLanguage;

public class ExamplePlugin implements ApplicationComponent {

    @Override
    public void initComponent() {
        // 获取当前打开的项目
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects) {
            PsiManager psiManager = PsiManager.getInstance(project);
            // 获取所有的C++文件
//            PsiFile[] psiFiles = psiManager.getProjectFileIndex().getAllFiles(OCLanguage.INSTANCE);
//            for (PsiFile psiFile : psiFiles) {
//                // 遍历每个文件，获取函数声明
//                OCFunctionDeclaration[] functionDeclarations = PsiTreeUtil.getChildrenOfType(psiFile, OCFunctionDeclaration.class);
//                if (functionDeclarations != null) {
//                    for (OCFunctionDeclaration function : functionDeclarations) {
//                        // 查找指定的函数
//                        if ("A".equals(function.getName())) {
//                            // 获取函数体中的所有函数调用
//                            OCFunctionCallExpression[] functionCalls = PsiTreeUtil.getChildrenOfType(function, OCFunctionCallExpression.class);
//                            if (functionCalls != null) {
//                                for (OCFunctionCallExpression call : functionCalls) {
//                                    // 打印调用的函数名称
//                                    PsiElement calledFunction = call.resolve();
//                                    if (calledFunction instanceof OCFunctionDeclaration) {
//                                        System.out.println("Function A calls: " + ((OCFunctionDeclaration) calledFunction).getName());
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }
    }

    @Override
    public void disposeComponent() {
        // 插件销毁时的清理工作
    }
}
