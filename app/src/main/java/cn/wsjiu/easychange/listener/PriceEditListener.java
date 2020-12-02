package cn.wsjiu.easychange.listener;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class PriceEditListener implements TextWatcher {
    private final Character DOT = '.';
    private boolean isInvilidate = false;
    private EditText editView;
    private String lastText;

    public PriceEditListener(EditText editView) {
        this.editView = editView;
        lastText = editView.getText().toString();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(isInvilidate) return;
        SpannableStringBuilder sb = (SpannableStringBuilder)s;
        int selectionIndex = editView.getSelectionStart();

        String text = s.toString();
        int dotIndex = text.lastIndexOf(DOT);
        if(dotIndex == -1) {
            text =lastText;
            dotIndex = text.lastIndexOf(DOT);
        }
        int len = text.length();
        StringBuilder newText = new StringBuilder("");

        int changeCount = 0;
        for(int i = 0; i < dotIndex; i++) {
            char c = text.charAt(i);
            if(c >= '0' && c <= '9') {
                newText.append(c);
            }else if (i < selectionIndex) changeCount++;
        }
        newText.append(DOT);
        int rightCount = 0;
        for(int i = dotIndex + 1; i < len; i++) {
            char c = text.charAt(i);
            if(c >= '0' && c <= '9') {
                newText.append(c);
                rightCount++;
            }else if (i < selectionIndex) changeCount++;
            if(rightCount == 2) {
                break;
            }
        }
        dotIndex = newText.lastIndexOf(DOT.toString());
        if(dotIndex == 0) {
            newText.insert(0, '0');
            changeCount++;
        } else {
            for(int i = 0; i < dotIndex - 1; i++) {
                if(newText.charAt(0) == '0') {
                    changeCount--;
                    newText.deleteCharAt(0);
                }
                else break;
            }
        }

        isInvilidate = true;
        editView.setText(newText.toString());
        int newSelectionIndex = selectionIndex - changeCount;
        if (newSelectionIndex >= 0 && newSelectionIndex <= newText.length()) {
            editView.setSelection(newSelectionIndex);
        }
        isInvilidate = false;
        lastText = newText.toString();
    }
}
