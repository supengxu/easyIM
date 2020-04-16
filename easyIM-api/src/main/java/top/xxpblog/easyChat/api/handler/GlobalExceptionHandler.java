package top.xxpblog.easyChat.api.handler;

import top.xxpblog.easyChat.api.exception.JsonException;
import top.xxpblog.easyChat.common.enums.ResultEnum;
import top.xxpblog.easyChat.common.utils.ResultVOUtils;
import top.xxpblog.easyChat.common.vo.res.BaseResVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 错误回调
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // 拦截API异常
    @ExceptionHandler(value = JsonException.class)
    public BaseResVO handlerJsonException(JsonException e) {
        // 返回对应的错误信息
        return ResultVOUtils.error(e.getCode(), e.getMessage());
    }

    // 拦截API异常
    @ExceptionHandler(value = RuntimeException.class)
    public BaseResVO handlerRuntimeException(RuntimeException e) {
        log.error(e.getMessage());
        // 返回对应的错误信息
        return ResultVOUtils.error(ResultEnum.NOT_NETWORK);
    }

    // 拦截API异常
    @ExceptionHandler(value = Exception.class)
    public BaseResVO handlerRuntimeException(Exception e) {
        log.error(e.getMessage());
        return ResultVOUtils.error(ResultEnum.NOT_NETWORK, "Request resource not found.");
    }
    
}
