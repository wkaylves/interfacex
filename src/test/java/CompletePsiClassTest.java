import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class CompletePsiClassTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getBasePath() {
        return "/src/main/java";
    }

    public void testPsiClassOperations() {
        // 1. 创建测试文件
        myFixture.configureByText("UserService.java", """
            package com.example.service;
            
            import java.util.List;
            
            public class UserService {
                private String name;
                
                public UserService(String name) {
                    this.name = name;
                }
                
                public String getName() {
                    return name;
                }
                
                public List<String> getUsers() {
                    return null;
                }
            }
            """);

        // 2. 获取 PsiClass
        PsiJavaFile javaFile = (PsiJavaFile) myFixture.getFile();
        PsiClass userServiceClass = javaFile.getClasses()[0];

        // 3. 验证基本属性
        assertEquals("UserService", userServiceClass.getName());
        assertEquals("com.example.service.UserService", userServiceClass.getQualifiedName());

        // 4. 验证字段
        PsiField[] fields = userServiceClass.getFields();
        assertEquals(1, fields.length);
        assertEquals("name", fields[0].getName());

        // 5. 验证方法
        PsiMethod[] methods = userServiceClass.getMethods();
        assertEquals(3, methods.length); // 包含默认构造函数

        PsiMethod getNameMethod = userServiceClass.findMethodsByName("getName", false)[0];
        assertNotNull(getNameMethod);
        assertEquals(PsiTypesUtil.getClassType(userServiceClass), getNameMethod.getReturnType());

        // 6. 验证导入
        PsiImportList importList = javaFile.getImportList();
        assertNotNull(importList);
    }

    public void testFindClassAfterIndexing() {
        // 创建多个类
        myFixture.configureByText("A.java", "package pkg; public class A {}");
        myFixture.configureByText("B.java", "package pkg; public class B {}");

        // 等待索引完成

        // 通过名称查找
        PsiClass classB = JavaPsiFacade.getInstance(getProject())
                .findClass("pkg.B", GlobalSearchScope.allScope(getProject()));

        assertNotNull(classB);
        assertEquals("B", classB.getName());
    }

}