package br.com.fiap.salalivre.domain.exception;

public class ConflitoDeHorarioException extends RegraDeNegocioException {
    public ConflitoDeHorarioException(String mensagem) {
        super(mensagem);
    }
}
