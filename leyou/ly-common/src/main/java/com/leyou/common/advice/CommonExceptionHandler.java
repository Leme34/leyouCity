package com.leyou.common.advice;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 全局异常处理器,返回json到前端
 */
@ControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handlerException(LyException e) {
        //取出自定义异常类中的ExceptionEnum对象
        ExceptionEnum em = e.getExceptionEnum();
        return ResponseEntity.status(em.getStatusCode())  //请求响应状态码
                .body(new ExceptionResult(em));  //请求响应体(vo对象)
    }

}
