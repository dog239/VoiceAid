package com.example.CCLEvaluation;

import androidx.annotation.IntDef;

import java.util.ArrayList;
import java.util.List;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class PlanUiItem {
    public static final int TYPE_SECTION = 0;
    public static final int TYPE_KEY_VALUE = 1;
    public static final int TYPE_LIST_ITEM = 2;
    public static final int TYPE_ADD_BUTTON = 3;
    public static final int TYPE_CARD_HEADER = 4;
    public static final int TYPE_CARD_END = 5;
    public static final int TYPE_LIST_MIRROR = 6;
    public static final int TYPE_STAGE_HEADER = 7;
    public static final int TYPE_STAGE_END = 8;
    public static final int TYPE_MODULE_CARD = 9;
    public static final int TYPE_SECTION_DIVIDER = 10; // 新增：蓝色分区标题条
    public static final int TYPE_INFO_BOX = 11;        // 新增：灰色信息框

    @IntDef({TYPE_SECTION, TYPE_KEY_VALUE, TYPE_LIST_ITEM, TYPE_ADD_BUTTON,
            TYPE_CARD_HEADER, TYPE_CARD_END, TYPE_LIST_MIRROR, TYPE_STAGE_HEADER, TYPE_STAGE_END, 
            TYPE_MODULE_CARD, TYPE_SECTION_DIVIDER, TYPE_INFO_BOX})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ItemType {}

    @ItemType
    public final int type;

    protected PlanUiItem(@ItemType int type) {
        this.type = type;
    }

    public static class SectionDivider extends PlanUiItem {
        public final String title;

        public SectionDivider(String title) {
            super(TYPE_SECTION_DIVIDER);
            this.title = title;
        }
    }

    public static class InfoBox extends PlanUiItem {
        public final String title;
        public final List<PlanUiItem> children;

        public InfoBox(String title, List<PlanUiItem> children) {
            super(TYPE_INFO_BOX);
            this.title = title;
            this.children = children != null ? children : new ArrayList<>();
        }
    }

    public static class ModuleCard extends PlanUiItem {
        public final String title;
        public final String moduleKey;
        public final List<PlanUiItem> children;
        public boolean expanded;
        public boolean isEditing;

        public ModuleCard(String title, String moduleKey, List<PlanUiItem> children) {
            super(TYPE_MODULE_CARD);
            this.title = title;
            this.moduleKey = moduleKey;
            this.children = children != null ? children : new ArrayList<>();
            this.expanded = true;
            this.isEditing = false;
        }
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

    public static class CardHeader extends PlanUiItem {
        public final String title;
        public final String cardId;
        public final boolean collapsible;
        public boolean expanded;

        public CardHeader(String title, String cardId, boolean collapsible, boolean expanded) {
            super(TYPE_CARD_HEADER);
            this.title = title;
            this.cardId = cardId;
            this.collapsible = collapsible;
            this.expanded = expanded;
        }
    }

    public static class CardEnd extends PlanUiItem {
        public CardEnd() {
            super(TYPE_CARD_END);
        }
    }

    public static class ListMirror extends PlanUiItem {
        public final String listPath;
        public final List<String> fallbackValues;
        public final String emptyHint;

        public ListMirror(String listPath, List<String> fallbackValues, String emptyHint) {
            super(TYPE_LIST_MIRROR);
            this.listPath = listPath;
            this.fallbackValues = fallbackValues;
            this.emptyHint = emptyHint;
        }
    }

    public static class StageHeader extends PlanUiItem {
        public final String title;

        public StageHeader(String title) {
            super(TYPE_STAGE_HEADER);
            this.title = title;
        }
    }

    public static class StageEnd extends PlanUiItem {
        public StageEnd() {
            super(TYPE_STAGE_END);
        }
    }
}
