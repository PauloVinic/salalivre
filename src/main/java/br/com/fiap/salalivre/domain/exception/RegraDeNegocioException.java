package br.com.fiap.salalivre.domain.exception;

public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }

    public RegraDeNegocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
