package com.github.sylphlike.gateway.common.exception;


import com.github.sylphlike.framework.norm.CharsetUtil;
import com.github.sylphlike.framework.norm.RCode;
import com.github.sylphlike.framework.norm.UniteException;

/**
 * <p>  time 12/11/2020 13:18  星期四 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 *
 * @author Gopal.pan
 * @version 1.0.0
 */
public class GatewayException extends UniteException {


    private static final long serialVersionUID = -6531836994139597998L;

    public GatewayException(RCode message) {
        super(message.getMessage());
        this.code    = message.getCode();
        this.message = message.getMessage();
    }


    public GatewayException(RCode message, String detailMessage) {
        super( message.getMessage() + CharsetUtil.STRING_ENGLISH_COMMA_SPACE + detailMessage);
        this.code    = message.getCode();
        this.message = message.getMessage() + CharsetUtil.STRING_ENGLISH_COMMA_SPACE + detailMessage;
    }
}
