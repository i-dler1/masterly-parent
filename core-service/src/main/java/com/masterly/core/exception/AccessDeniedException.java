package com.masterly.core.exception;

import lombok.experimental.StandardException;

/**
 * Исключение, выбрасываемое при попытке доступа к чужим данным.
 * Используется для ограничения доступа мастера к клиентам/записям других мастеров.
 */
@StandardException
public class AccessDeniedException extends RuntimeException{
}
