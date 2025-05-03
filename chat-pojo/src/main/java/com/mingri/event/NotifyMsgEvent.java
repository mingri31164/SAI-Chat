package com.mingri.event;

import com.mingri.enumeration.NotifyTypeEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;


/***
 * @Description:
 * @Author: mingri31164
 * @Date: 2025/2/6 22:55
 **/
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class NotifyMsgEvent<T> extends ApplicationEvent {

    private static final long serialVersionUID = 3769329609100817957L;

    private NotifyTypeEnum notifyType;

    private T content;


    public NotifyMsgEvent(Object source, NotifyTypeEnum notifyType, T content) {
        super(source);
        this.notifyType = notifyType;
        this.content = content;
    }
}
