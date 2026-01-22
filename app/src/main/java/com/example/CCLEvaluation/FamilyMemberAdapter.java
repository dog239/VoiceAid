package com.example.CCLEvaluation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.MemberViewHolder> {

    public interface OnMemberDeleteListener {
        void onDelete(int position);
    }

    public static class FamilyMember {
        public String memberName = "";
        public String relation = "";
        public String memberPhone = "";
        public String occupation = "";
        public String education = "";

        public boolean isEmpty() {
            return memberName.trim().isEmpty()
                    && relation.trim().isEmpty()
                    && memberPhone.trim().isEmpty()
                    && occupation.trim().isEmpty()
                    && education.trim().isEmpty();
        }
    }

    private final List<FamilyMember> members;
    private final OnMemberDeleteListener deleteListener;
    private boolean readOnly = false;

    public FamilyMemberAdapter(List<FamilyMember> members, OnMemberDeleteListener deleteListener) {
        this.members = members != null ? members : new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        notifyDataSetChanged();
    }

    public List<FamilyMember> getMembers() {
        return members;
    }

    public void setMembers(List<FamilyMember> newMembers) {
        members.clear();
        if (newMembers != null) {
            members.addAll(newMembers);
        }
        notifyDataSetChanged();
    }

    public void addMember(FamilyMember member) {
        members.add(member);
        notifyItemInserted(members.size() - 1);
    }

    public void removeMember(int position) {
        if (position < 0 || position >= members.size()) {
            return;
        }
        members.remove(position);
        notifyItemRemoved(position);
    }

    public void clearMember(int position) {
        if (position < 0 || position >= members.size()) {
            return;
        }
        FamilyMember member = members.get(position);
        member.memberName = "";
        member.relation = "";
        member.memberPhone = "";
        member.occupation = "";
        member.education = "";
        notifyItemChanged(position);
    }

    public static List<FamilyMember> fromJsonArray(JSONArray array) {
        List<FamilyMember> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) {
                continue;
            }
            FamilyMember member = new FamilyMember();
            member.memberName = item.optString("member_name", "");
            member.relation = item.optString("relation", "");
            member.memberPhone = item.optString("member_phone", "");
            member.occupation = item.optString("occupation", "");
            member.education = item.optString("education", "");
            result.add(member);
        }
        return result;
    }

    public static JSONArray toJsonArray(List<FamilyMember> members) {
        JSONArray array = new JSONArray();
        if (members == null) {
            return array;
        }
        for (FamilyMember member : members) {
            if (member == null || member.isEmpty()) {
                continue;
            }
            JSONObject item = new JSONObject();
            try {
                item.put("member_name", member.memberName.trim());
                item.put("relation", member.relation.trim());
                item.put("member_phone", member.memberPhone.trim());
                item.put("occupation", member.occupation.trim());
                item.put("education", member.education.trim());
            } catch (Exception ignored) {
            }
            array.put(item);
        }
        return array;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_family_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private final EditText memberName;
        private final EditText relation;
        private final EditText memberPhone;
        private final EditText occupation;
        private final EditText education;
        private final ImageButton deleteButton;
        private SimpleTextWatcher nameWatcher;
        private SimpleTextWatcher relationWatcher;
        private SimpleTextWatcher phoneWatcher;
        private SimpleTextWatcher occupationWatcher;
        private SimpleTextWatcher educationWatcher;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.et_member_name);
            relation = itemView.findViewById(R.id.et_relation);
            memberPhone = itemView.findViewById(R.id.et_member_phone);
            occupation = itemView.findViewById(R.id.et_occupation);
            education = itemView.findViewById(R.id.et_education);
            deleteButton = itemView.findViewById(R.id.btn_delete_member);
        }

        void bind(FamilyMember member) {
            if (nameWatcher != null) {
                memberName.removeTextChangedListener(nameWatcher);
            }
            if (relationWatcher != null) {
                relation.removeTextChangedListener(relationWatcher);
            }
            if (phoneWatcher != null) {
                memberPhone.removeTextChangedListener(phoneWatcher);
            }
            if (occupationWatcher != null) {
                occupation.removeTextChangedListener(occupationWatcher);
            }
            if (educationWatcher != null) {
                education.removeTextChangedListener(educationWatcher);
            }

            memberName.setText(member.memberName);
            relation.setText(member.relation);
            memberPhone.setText(member.memberPhone);
            occupation.setText(member.occupation);
            education.setText(member.education);

            nameWatcher = new SimpleTextWatcher(text -> member.memberName = text);
            relationWatcher = new SimpleTextWatcher(text -> member.relation = text);
            phoneWatcher = new SimpleTextWatcher(text -> member.memberPhone = text);
            occupationWatcher = new SimpleTextWatcher(text -> member.occupation = text);
            educationWatcher = new SimpleTextWatcher(text -> member.education = text);

            memberName.addTextChangedListener(nameWatcher);
            relation.addTextChangedListener(relationWatcher);
            memberPhone.addTextChangedListener(phoneWatcher);
            occupation.addTextChangedListener(occupationWatcher);
            education.addTextChangedListener(educationWatcher);

            setEditable(memberName, !readOnly);
            setEditable(relation, !readOnly);
            setEditable(memberPhone, !readOnly);
            setEditable(occupation, !readOnly);
            setEditable(education, !readOnly);
            deleteButton.setVisibility(readOnly ? View.GONE : View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                if (!readOnly && deleteListener != null) {
                    int adapterPosition = getBindingAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        deleteListener.onDelete(adapterPosition);
                    }
                }
            });
        }

        private void setEditable(EditText editText, boolean enabled) {
            editText.setEnabled(enabled);
            editText.setFocusable(enabled);
            editText.setFocusableInTouchMode(enabled);
        }
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final ValueListener listener;

        SimpleTextWatcher(ValueListener listener) {
            this.listener = listener;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (listener != null) {
                listener.onValueChanged(s == null ? "" : s.toString());
            }
        }
    }

    private interface ValueListener {
        void onValueChanged(String value);
    }
}
