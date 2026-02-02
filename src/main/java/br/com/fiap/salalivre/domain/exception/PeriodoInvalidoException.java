package br.com.fiap.salalivre.domain.exception;

public class PeriodoInvalidoException extends RegraDeNegocioException {
    public PeriodoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
