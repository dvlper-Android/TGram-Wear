package org.tgramwear;

import org.telegram.android.SendMessagesHelper;

/**
 * Created by dvlper.android on 15/05/2015.
 */
public class SendMessageTW {

    private boolean sended;

    public SendMessageTW(String message, long user_id) {
        if (processSendingText(message, user_id)) {
            sended = true;
        }
    }

    public boolean processSendingText(String text, long user_id) {
        text = getTrimmedString(text);
        if (text.length() != 0) {
            int count = (int) Math.ceil(text.length() / 4096.0f);
            for (int a = 0; a < count; a++) {
                String mess = text.substring(a * 4096, Math.min((a + 1) * 4096, text.length()));
                SendMessagesHelper.getInstance().sendMessage(mess, user_id);
            }
            return true;
        }
        return false;
    }

    private String getTrimmedString(String src) {
        String result = src.trim();
        if (result.length() == 0) {
            return result;
        }
        while (src.startsWith("\n")) {
            src = src.substring(1);
        }
        while (src.endsWith("\n")) {
            src = src.substring(0, src.length() - 1);
        }
        return src;
    }

    public void setSended(){
        if(sended)
            sended = false;
        else
            sended = true;
    }

    public boolean getSended(){
        return sended;
    }
}
