package com.baiguiren.yaml;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class YamlCompletionContributor extends CompletionContributor {

    // 固定的 yaml 关键字
    private static HashMap<String, String> keywordMap;

    // 保存每个 project 的 fields, 特定关键字
    private static HashMap<String, HashMap<String, String>> projectFields = new HashMap<>();

    public YamlCompletionContributor() {
        keywordMap = getKeywords();
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        HashMap<String, String> fieldsMap = getAllFields(parameters);
        HashMap<String, String> map = new HashMap<>();

        map.putAll(keywordMap);
        map.putAll(fieldsMap);

        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String keyword = (String) entry.getKey();
            String comment = (String) entry.getValue();

            result.addElement(new LookupElement() {
                @NotNull
                @Override
                public String getLookupString() {
                    return keyword;
                }

                public void renderElement(LookupElementPresentation presentation) {
                    if (!comment.equals("_field")) {
                        presentation.setItemText(getLookupString() + " " + comment);
                    } else {
                        presentation.setItemText(getLookupString());
                    }
                }

                public void handleInsert(InsertionContext context) {
                    if (!comment.equals("_field")) {
                        context.getDocument().insertString(context.getTailOffset(), ": ");
                        context.getEditor().getCaretModel().moveToOffset(context.getTailOffset() + 2);
                    }
                }
            });
        }
    }

    /**
     * 在 ApplicationComponent 里面实现脏检查, 不用每次都读取文件
     * 只有初始化或者文件发生变化的时候才去读取文件
     * @return HashMap<String, String>
     */
    private static HashMap<String, String> getAllFields(CompletionParameters parameters) {
        String basePath = parameters.getEditor().getProject().getBasePath();
        String projectName = parameters.getEditor().getProject().getName();

        if (projectFields.get(projectName) == null || YamlApplicationComponent.projectIsDirty(projectName)) {
            HashMap<String, String> map = new HashMap<>();

            File file = new File(basePath + "/code-gen/fields.txt");
            if (file.exists()) {
                try {
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        line = line.replace("\n", "");
                        map.put(line, "_field");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            projectFields.put(projectName, map);
            YamlApplicationComponent.dirtyProjects.remove(projectName);
        }

        return projectFields.get(projectName);
    }

    private static HashMap<String, String> getKeywords() {
        HashMap<String, String> keywordMap = new HashMap<>();
        keywordMap.put("list_name", "列表名称(不带\"列表\")");
        keywordMap.put("form_title", "表单名称(不带 新增、删除)");
        keywordMap.put("search_fields", "搜索字段(数组或字符串)");
        keywordMap.put("search_placeholder", "列表搜索框placeholder");
        keywordMap.put("required_fields", "必填字段(用于表单验证)");
        keywordMap.put("form_type", "表单类型(dialog或者空)");

        keywordMap.put("passwords", "密码字段");
        keywordMap.put("radios", "单选类型字段");
        keywordMap.put("checkboxes", "复选类型字段");
        keywordMap.put("images", "图片上传字段");
        keywordMap.put("textareas", "textarea 类型字段");

        keywordMap.put("start_index", "单选, 多选值开始的索引");
        keywordMap.put("values", "单选, 多选的 label");

        return keywordMap;
    }
}
