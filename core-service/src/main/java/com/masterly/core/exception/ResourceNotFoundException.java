package com.masterly.core.exception;

import lombok.experimental.StandardException;
/**
 * Исключение, выбрасываемое при отсутствии запрашиваемого ресурса.
 */
@StandardException
public class ResourceNotFoundException extends RuntimeException{

}
