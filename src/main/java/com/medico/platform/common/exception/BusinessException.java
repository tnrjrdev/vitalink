package com.medico.platform.common.exception;

/**
 * lancada quando uma regra de negocio e violada (ex.: agendamento no passado,
 * conflito de horario, transicao de status invalida). Resulta em HTTP 422.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
