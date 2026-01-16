package com.example.CCLEvaluation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class PlanUiItem {
    public static final int TYPE_SECTION = 0;
    public static final int TYPE_KEY_VALUE = 1;
    public static final int TYPE_LIST_ITEM = 2;
    public static final int TYPE_ADD_BUTTON = 3;

    @IntDef({TYPE_SECTION, TYPE_KEY_VALUE, TYPE_LIST_ITEM, TYPE_ADD_BUTTON})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ItemType {}

    @ItemType
    public final int type;

    protected PlanUiItem(@ItemType int type) {
        this.type = type;
    }

    public static class SectionHeader extends PlanUiItem {
        public final String title;
        public final int level;

        public SectionHeader(String title, int level) {
            super(TYPE_SECTION);
            this.title = title;
            this.level = level;
        }
    }

    public static class KeyValue extends PlanUiItem {
        public final String keyPath;
        public final String label;
        public String value;
        public final int inputType;

        public KeyValue(String keyPath, String label, String value, int inputType) {
            super(TYPE_KEY_VALUE);
            this.keyPath = keyPath;
            this.label = label;
            this.value = value;
            this.inputType = inputType;
        }
    }

    public static class ListItem extends PlanUiItem {
        public final String listPath;
        public int index;
        public String value;

        public ListItem(String listPath, int index, String value) {
            super(TYPE_LIST_ITEM);
            this.listPath = listPath;
            this.index = index;
            this.value = value;
        }
    }

    public static class AddButton extends PlanUiItem {
        public final String listPath;
        public final String label;

        public AddButton(String listPath, String label) {
            super(TYPE_ADD_BUTTON);
            this.listPath = listPath;
            this.label = label;
        }
    }
}
