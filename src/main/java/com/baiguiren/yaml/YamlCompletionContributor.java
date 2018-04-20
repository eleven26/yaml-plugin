package com.baiguiren.yaml;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class YamlCompletionContributor extends CompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        HashMap<String, String> keywordMap = getKeywords();

        for (Object o : keywordMap.entrySet()) {
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
                    presentation.setItemText(getLookupString() + " " + comment);
                }

                public void handleInsert(InsertionContext context) {
                    context.getDocument().insertString(context.getTailOffset(), ": ");
                    context.getEditor().getCaretModel().moveToOffset(context.getTailOffset() + 2);
                }
            });
        }
    }

    private static HashMap<String, String> getKeywords() {
        HashMap<String, String> keywordMap = new HashMap<>();
        keywordMap.put("list_name", "列表名称(不带\"列表\")");
        keywordMap.put("form_title", "表单名称(不带 新增、删除)");
        keywordMap.put("search_fields", "搜索字段, 如\"name\"、\"[\"name\", \"phone\"]\"");
        keywordMap.put("search_placeholder", "列表搜索框placeholder");
        keywordMap.put("required_fields", "必填字段(用于表单验证)");

        return keywordMap;
    }
}
