// ExamplePlugin.java
package com.github.very0928.cppparserclion;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.cidr.lang.hierarchy.structureVIew.OCStructureViewComponent;
import com.jetbrains.cidr.lang.psi.OCCallExpression;
import com.jetbrains.cidr.lang.psi.OCFunctionDeclaration;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.VfsUtil;
import com.jetbrains.cidr.lang.psi.OCStruct;
import com.jetbrains.cidr.lang.symbols.OCVisibility;
import com.jetbrains.cidr.lang.symbols.objc.OCClassSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ExamplePlugin extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) {
            return;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) {
            return;
        }

        CaretModel caretModel = editor.getCaretModel();
        int caretOffset = caretModel.getOffset();
        PsiElement elementAtCaret = psiFile.findElementAt(caretOffset);

        if (elementAtCaret == null) {
            return;
        }

        OCFunctionDeclaration function = PsiTreeUtil.getParentOfType(elementAtCaret, OCFunctionDeclaration.class);
        if (function == null) {
            return;
        }

        analyzeFunction(function);
    }

    private void analyzeFunction(OCFunctionDeclaration function) {
        System.out.println("Analyzing function: " + function.getName());
        PsiElement elementTest = Objects.requireNonNull(function.getNameIdentifier()).getParent().getFirstChild();

        PsiElement nameIdentifier = Objects.requireNonNull(function.getNameIdentifier());
        PsiElement element = nameIdentifier.getPrevSibling().getPrevSibling();
        if (element != null) {
            PsiReference psiReference = element.getReference();
            if (psiReference != null && psiReference.resolve() instanceof OCStruct funcBelongedClass) {
                OCVisibility defaultVisibility = funcBelongedClass.getDefaultVisibility();

                Map<OCVisibility, List<OCFunctionDeclaration>> constructorsByVisibility =
                        getConstructorsByVisibility(funcBelongedClass);
                for (Map.Entry<OCVisibility, List<OCFunctionDeclaration>> entry : constructorsByVisibility.entrySet()) {
                    OCVisibility visibility = entry.getKey();
                    List<OCFunctionDeclaration> constructors = entry.getValue(); if (constructors.isEmpty()) continue;
                    System.out.println("Visibility: " + visibility);
                    for (OCFunctionDeclaration constructor : constructors) {
                        System.out.println("Constructor: " + constructor.getName());
                    }
                }
            }
        }

        Arrays.stream(PsiTreeUtil.getChildrenOfType(element.getReference().resolve(), OCFunctionDeclaration.class))
                .filter(ocFunctionDeclaration -> ocFunctionDeclaration.getName().equals(function.getName()))
                .collect(Collectors.toUnmodifiableList());
        OCCallExpression[] functionCalls = PsiTreeUtil.getChildrenOfType(function, OCCallExpression.class);
        if (functionCalls != null) {
            for (OCCallExpression call : functionCalls) {
                if (call.getReference() != null) {
                    PsiElement calledFunction = call.getReference().resolve();
                    if (calledFunction instanceof OCFunctionDeclaration) {
                        System.out.println("Function " + function.getName() +
                                " calls: " + ((OCFunctionDeclaration) calledFunction).getName());
                    }
                }
            }
        }
    }

    private Map<OCVisibility, List<OCFunctionDeclaration>> getConstructorsByVisibility(OCStruct funcBelongedClass) {
        Map<OCVisibility, List<OCFunctionDeclaration>> constructorsByVisibility = new EnumMap<>(OCVisibility.class);
        for (OCVisibility visibility : OCVisibility.values()) {
            constructorsByVisibility.put(visibility, new ArrayList<>());
        }

        List<OCFunctionDeclaration> constructors = funcBelongedClass.getConstructors();
        for (OCFunctionDeclaration constructor : constructors) {
            OCVisibility visibility = Objects.requireNonNull(constructor.getSymbol()).getVisibility();
            String visitModifier = Objects.requireNonNull(visibility).toString();
            constructorsByVisibility.get(visibility).add(constructor);
        }

        return constructorsByVisibility;
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(editor != null);
    }

    private void processProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects) {
            PsiManager psiManager = PsiManager.getInstance(project);
            VirtualFile projectBaseDir = project.getBaseDir();
            if (projectBaseDir != null) {
                VfsUtil.visitChildrenRecursively(projectBaseDir, new CppFileVisitor(psiManager));
            }
        }
    }

    private static class CppFileVisitor extends VirtualFileVisitor<Object> {
        private final PsiManager psiManager;

        public CppFileVisitor(PsiManager psiManager) {
            this.psiManager = psiManager;
        }

        @Override
        public boolean visitFile(@NotNull VirtualFile file) {
            if (isCppFile(file)) {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null) {
                    processPsiFile(psiFile);
                }
            }
            return true;
        }

        private boolean isCppFile(VirtualFile file) {
            return file.getFileType().getDefaultExtension().equals("cpp");
        }

        private void processPsiFile(PsiFile psiFile) {
            OCFunctionDeclaration[] functionDeclarations = PsiTreeUtil.getChildrenOfType(psiFile, OCFunctionDeclaration.class);
            if (functionDeclarations != null) {
                for (OCFunctionDeclaration function : functionDeclarations) {
                    if (isTargetFunction(function)) {
                        processFunction(function);
                    }
                }
            }
        }

        private boolean isTargetFunction(OCFunctionDeclaration function) {
            return "SetRingerModeCallback".equals(function.getName());
        }

        private void processFunction(OCFunctionDeclaration function) {
            OCCallExpression[] functionCalls = PsiTreeUtil.getChildrenOfType(function, OCCallExpression.class);
            if (functionCalls != null) {
                for (OCCallExpression call : functionCalls) {
                    PsiElement calledFunction = call.getOriginalElement();
                    if (calledFunction instanceof OCFunctionDeclaration) {
                        System.out.println("Function A calls: " + ((OCFunctionDeclaration) calledFunction).getName());
                    }
                }
            }
        }
    }
}

